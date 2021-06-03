package com.ryanair.challenge.infrastructure;

import com.ryanair.challenge.domain.BookFlightService;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.Leg;
import com.ryanair.challenge.infrastructure.client.dto.DayDTO;
import com.ryanair.challenge.infrastructure.client.dto.FlightDTO;
import com.ryanair.challenge.infrastructure.client.dto.RouteDTO;
import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;
import com.ryanair.challenge.infrastructure.client.mapper.FlightClientMapper;
import com.ryanair.challenge.infrastructure.client.ryanair.routes.RyanairRouteClient;
import com.ryanair.challenge.infrastructure.client.ryanair.schedule.RyanairScheduleClient;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class BookFlightServiceImpl implements BookFlightService {

    private static final String COLON = ":";
    private static final String RYANAIR = "RYANAIR";
    private static final String ROUTE_NOT_FOUND = "Route not found";
    private static final String NO_FLIGHTS_AVAILABLE = "No flights available from %s to %s on this date";

    private final RyanairRouteClient ryanairRouteClient;
    private final RyanairScheduleClient ryanairScheduleClient;

    @Override
    public Either<BookFlightServiceException, List<Flight>> getFlights(final BookFlightRequest request) {

        final List<RouteDTO> routes = getAvailableRoutesWithFilter(request);

        if (notRoutesAvailable(routes)) {
            return Either.left(new BookFlightServiceException(ROUTE_NOT_FOUND));
        } else if (onlyDirectRoutes(routes)) {
            return directFlights(request);
        } else return directFlightsAndFlightsWithStop(request, routes);

    }

    private Either<BookFlightServiceException, List<Flight>> directFlightsAndFlightsWithStop(final BookFlightRequest request,
                                                                                             final List<RouteDTO> routes) {

        List<Flight> flights = new ArrayList<>();
        final RouteDTO directRoutes = getDirectRoutes(routes);
        final List<RouteDTO> routesWithStop = getRoutesWithStop(routes);

        if (Objects.nonNull(directRoutes)) {
            Either<BookFlightServiceException, List<Flight>> directFlights = directFlights(request);
            flights.addAll(directFlights.get());
        }
        if (!routesWithStop.isEmpty()) {
            Either<BookFlightServiceException, List<Flight>> flightsWithStop = flightsWithStop(request, routesWithStop);
            flights.addAll(flightsWithStop.get());
        }
        if (flights.isEmpty()) {
            return Either.left(new BookFlightServiceException(NO_FLIGHTS_AVAILABLE
                .formatted(request.getDeparture(), request.getArrival())));
        } else return Either.right(flights);
    }

    private RouteDTO getDirectRoutes(final List<RouteDTO> routes) {
        return routes.stream().filter(x -> Objects.isNull(x.getConnectingAirport())).findFirst().orElse(null);
    }

    private Either<BookFlightServiceException, List<Flight>> flightsWithStop(final BookFlightRequest request,
                                                                             final List<RouteDTO> routes) {

        LocalDateTime date = request.getDepartureDateTime();
        List<Flight> list = new ArrayList<>();

        for (RouteDTO route : routes) {
            final var firstFlightToStop = getFlightsByDay(route.getAirportFrom(),
                route.getConnectingAirport(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            for (FlightDTO flightDTO : firstFlightToStop) {
                final var stopFlightToArrival = getFlightsByDay(route.getConnectingAirport(),
                    route.getAirportTo(), date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                for (FlightDTO arrival : stopFlightToArrival) {
                    if (stopHasTwoHours(flightDTO, arrival)) {
                        list.add(Flight.builder().stops(1).legs(buildLegsFlight(flightDTO, arrival)).build());
                    }
                }
            }
        }

        return Either.right(list);
    }

    private List<Leg> buildLegsFlight(final FlightDTO firstFlightToStop, final FlightDTO stopFlightToArrival) {
        return List.of(Leg.builder().departureAirport(firstFlightToStop.getDepartureCity())
                .arrivalAirport(firstFlightToStop.getArrivalCity())
                .departureDateTime(firstFlightToStop.getDepartureLocalDateTime().toString())
                .arrivalDateTime(firstFlightToStop.getArrivalLocalDateTime().toString())
                .build(),
            Leg.builder()
                .departureAirport(stopFlightToArrival.getDepartureCity())
                .arrivalAirport(stopFlightToArrival.getArrivalCity())
                .departureDateTime(stopFlightToArrival.getDepartureLocalDateTime().toString())
                .arrivalDateTime(stopFlightToArrival.getArrivalLocalDateTime().toString()).build());
    }

    private boolean stopHasTwoHours(final FlightDTO firstFlightToStop, final FlightDTO stopFlightToArrival) {
        return firstFlightToStop.getArrivalLocalDateTime()
            .plusHours(2).isBefore(stopFlightToArrival.getDepartureLocalDateTime());
    }

    private List<RouteDTO> getRoutesWithStop(final List<RouteDTO> routes) {
        return routes.stream()
            .filter(e -> Objects.nonNull(e.getConnectingAirport()))
            .collect(Collectors.toList());
    }

    private Either<BookFlightServiceException, List<Flight>> directFlights(final BookFlightRequest request) {

        final String departure = request.getDeparture();
        final String arrival = request.getArrival();

        final var flightsByDay = getFlightsByDay(departure, arrival, request.getDepartureDateTime().getYear(),
            request.getDepartureDateTime().getMonthValue(), request.getDepartureDateTime().getDayOfMonth());

        final var flightsWithFilter =
            Try.of(() -> getDirectValidFlights(request, flightsByDay))
                .toEither()
                .mapLeft(BookFlightServiceException::new);

        if (flightsWithFilter.isRight()) {
            return flightsWithFilter;
        } else return Either.left(new BookFlightServiceException(NO_FLIGHTS_AVAILABLE.formatted(departure, arrival)));

    }

    private List<Flight> getDirectValidFlights(final BookFlightRequest bookFlightRequest, final List<FlightDTO> flights) {
        return Optional.of(flights.stream()
            .filter(filterByTime(bookFlightRequest))
            .map(FlightClientMapper::toBookFlightRequest)
            .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    private void completeObjectWithDatesAndIataCode(List<FlightDTO> flights, final LocalDateTime day, String departure, String arrival) {
        if (!ObjectUtils.isEmpty(flights))
            flights.forEach(flight -> {
                flight.setDepartureLocalDateTime(LocalDateTime.parse(getDepartureTime(day, flight)));
                flight.setArrivalLocalDateTime(LocalDateTime.parse(getArrivalTime(day, flight)));
                flight.setDepartureCity(departure);
                flight.setArrivalCity(arrival);
            });
    }

    private List<FlightDTO> getFlightsByDay(String departure, String arrival, Integer year, Integer month, Integer day) {

        List<FlightDTO> flights = Optional.ofNullable(ryanairScheduleClient.getSchedule(departure, arrival, year, month))
            .map(ScheduleDTO::getDays)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(DayDTO::getDay, DayDTO::getFlights))
            .get(day);
        completeObjectWithDatesAndIataCode(flights, LocalDateTime.of(year, month, day, 0, 0), departure, arrival);
        return flights;
    }

    private List<RouteDTO> getAvailableRoutesWithFilter(BookFlightRequest request) {

        return Optional.of(ryanairRouteClient.getRoutes(request.getDeparture())
            .stream().filter(filterRyanairResults(request))
            .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    private Predicate<RouteDTO> filterRyanairResults(BookFlightRequest request) {
        return x -> RYANAIR.equals(x.getOperator()) && request.getArrival().equals(x.getAirportTo());
    }

    private Predicate<FlightDTO> filterByTime(BookFlightRequest bookFlightRequest) {
        return flight -> flight.getDepartureLocalDateTime().isAfter(bookFlightRequest.getDepartureDateTime())
            && flight.getArrivalLocalDateTime().isBefore(bookFlightRequest.getArrivalDateTime());
    }

    private int getHourFromString(String time) {
        return Integer.parseInt(time.split(COLON)[0]);
    }

    private int getMinuteFromString(String time) {
        return Integer.parseInt(time.split(COLON)[1]);
    }


    private boolean onlyDirectRoutes(List<RouteDTO> routes) {
        return routes.size() == 1 && routes.stream().anyMatch(x -> ObjectUtils.isEmpty(x.getConnectingAirport()));
    }

    private boolean notRoutesAvailable(List<RouteDTO> routes) {
        return routes.isEmpty();
    }

    private String getArrivalTime(LocalDateTime day, FlightDTO x) {
        return day.withHour(getHourFromString(x.getArrivalTime())).withMinute(getMinuteFromString(x.getArrivalTime())).toString();
    }

    private String getDepartureTime(LocalDateTime day, FlightDTO x) {
        return day.withHour(getHourFromString(x.getDepartureTime())).withMinute(getMinuteFromString(x.getDepartureTime())).toString();
    }
}