package com.ryanair.challenge.infrastructure;

import com.ryanair.challenge.domain.BookFlightService;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.domain.model.Flight;
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
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BookFlightServiceImpl implements BookFlightService {

    private static final String COLON = ":";
    private static final String RYANAIR = "RYANAIR";
    private static final String ROUTE_NOT_FOUND = "Route not found";
    private static final String NO_FLIGHTS_AVAILABLE = "No flights available from %s to %s on this date";

    private RyanairRouteClient ryanairRouteClient;
    private RyanairScheduleClient ryanairScheduleClient;

    @Override
    public Either<BookFlightServiceException, List<Flight>> getFlights(final BookFlightRequest request) {

        final List<RouteDTO> routes = validateIfRouteNotExist(request);

        if (notRoutesAvailable(routes)) {
            return Either.left(new BookFlightServiceException(ROUTE_NOT_FOUND));
        }

        return validAndReturnAvailableFlights(request);
    }

    private boolean notRoutesAvailable(List<RouteDTO> routes) {
        return routes.isEmpty();
    }

    private Either<BookFlightServiceException, List<Flight>> validAndReturnAvailableFlights(BookFlightRequest bookFlightRequest) {

        var flights = getFlightsByDay(bookFlightRequest);

        if (ObjectUtils.isEmpty(flights)) {
            return Either.left(new BookFlightServiceException(NO_FLIGHTS_AVAILABLE
                .formatted(bookFlightRequest.getDeparture(), bookFlightRequest.getArrival())));
        }

        completeObjectWithDatesAndIataCode(flights, bookFlightRequest);

        return Try.of(() -> getFlights(bookFlightRequest, flights))
            .toEither()
            .mapLeft(BookFlightServiceException::new);
    }

    private List<Flight> getFlights(BookFlightRequest bookFlightRequest, List<FlightDTO> flights) {
        return flights.stream()
            .filter(filterByTime(bookFlightRequest))
            .map(FlightClientMapper::toBookFlightRequest)
            .collect(Collectors.toList());
    }

    private void completeObjectWithDatesAndIataCode(List<FlightDTO> flights,
                                                    final BookFlightRequest bookFlightRequest) {
        flights.forEach(
            flight -> {
                final LocalDateTime day = bookFlightRequest.getDepartureDateTime();
                flight.setDepartureLocalDateTime(LocalDateTime.parse(getDepartureTime(day, flight)));
                flight.setArrivalLocalDateTime(LocalDateTime.parse(getArrivalTime(day, flight)));
                flight.setDepartureCity(bookFlightRequest.getDeparture());
                flight.setArrivalCity(bookFlightRequest.getArrival());
            }
        );
    }

    private String getArrivalTime(LocalDateTime day, FlightDTO x) {
        return day.withHour(getHourFromString(x.getArrivalTime()))
            .withMinute(getMinuteFromString(x.getArrivalTime())).toString();
    }

    private String getDepartureTime(LocalDateTime day, FlightDTO x) {
        return day.withHour(getHourFromString(x.getDepartureTime()))
            .withMinute(getMinuteFromString(x.getDepartureTime())).toString();
    }

    private List<FlightDTO> getFlightsByDay(BookFlightRequest bookFlightRequest) {
        return Optional.ofNullable(
            ryanairScheduleClient.getSchedule(bookFlightRequest.getDeparture(), bookFlightRequest.getArrival(),
                bookFlightRequest.getDepartureDateTime().getYear(),
                bookFlightRequest.getArrivalDateTime().getMonthValue()))
            .map(ScheduleDTO::getDays)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(DayDTO::getDay, DayDTO::getFlights))
            .get(bookFlightRequest.getDepartureDateTime().getDayOfMonth());
    }

    private List<RouteDTO> validateIfRouteNotExist(BookFlightRequest request) {

        return Optional.of(ryanairRouteClient.getRoutes(request.getDeparture())
            .stream().filter(filterRyanairResults(request))
            .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    private Predicate<RouteDTO> filterRyanairResults(BookFlightRequest request) {
        return x -> Objects.isNull(x.getConnectingAirport())
            && RYANAIR.equals(x.getOperator()) && request.getArrival().equals(x.getAirportTo());
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

}