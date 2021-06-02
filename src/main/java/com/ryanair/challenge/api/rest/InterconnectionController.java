package com.ryanair.challenge.api.rest;

import com.ryanair.challenge.api.mapper.FlightMapper;
import com.ryanair.challenge.api.mapper.RequestMapper;
import com.ryanair.challenge.api.model.BookFlightRequestDTO;
import com.ryanair.challenge.api.model.FlightDTO;
import com.ryanair.challenge.api.rest.response.GenericResponse;
import com.ryanair.challenge.api.rest.response.GenericResponseError;
import com.ryanair.challenge.application.GetFlights;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.Flight;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/ryanair")
@RequiredArgsConstructor
public class InterconnectionController {

    private final GetFlights getFlights;

    @GetMapping("/flights")
    public GenericResponse<List<FlightDTO>> getFlights(@Valid final BookFlightRequestDTO bookFlightRequestDTO) {
        log.debug("Getting flights by : departure : %s, departureDateTime : %s, arrival : %s, arrivalDateTime : %s"
            .formatted(bookFlightRequestDTO.getDeparture(), bookFlightRequestDTO.getDepartureDateTime(),
                bookFlightRequestDTO.getArrival(), bookFlightRequestDTO.getArrivalDateTime()));

        final var flightsData = getFlights.apply(RequestMapper.toBookFlightRequest(bookFlightRequestDTO));

        return flightsData.isRight() ?
            GenericResponse.<List<FlightDTO>>builder().data(buildDataList(flightsData)).build() :
            GenericResponse.<List<FlightDTO>>builder().error(buildErrorDTOObject(flightsData)).build();
    }

    private GenericResponseError buildErrorDTOObject(final Either<BookFlightServiceException, List<Flight>> dataList) {
        return GenericResponseError.builder()
            .code(HttpStatus.NOT_FOUND.value())
            .message(dataList.getLeft().getLocalizedMessage())
            .build();
    }

    private List<FlightDTO> buildDataList(final Either<BookFlightServiceException, List<Flight>> dataList) {
        return dataList.get().stream()
            .map(FlightMapper::toFlightDTO)
            .collect(Collectors.toList());
    }

}