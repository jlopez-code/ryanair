package com.ryanair.challenge.api.rest;

import com.ryanair.challenge.api.model.BookFlightRequestDTO;
import com.ryanair.challenge.api.model.FlightDTO;
import com.ryanair.challenge.api.rest.response.GenericResponse;
import com.ryanair.challenge.application.GetFlights;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.ryanair.challenge.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterconnectionControllerUnitTest {

    private InterconnectionController interconnectionController;
    private GetFlights getFlights;

    @BeforeEach
    void setUp() {
        getFlights = mock(GetFlights.class);
        interconnectionController = new InterconnectionController(getFlights);
    }

    @Test
    void should_return_not_found_if_the_route_is_not_present() {

        assertThat(interconnectionController).isNotNull();

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class)))
            .thenReturn(Either.left(new BookFlightServiceException(ROUTE_NOT_FOUND)));

        // WHEN
        GenericResponse<List<FlightDTO>> flights =
            interconnectionController.getFlights(BookFlightRequestDTO.builder().build());

        // THEN
        verify(getFlights).apply(any());

        assertThat(flights)
            .isInstanceOf(GenericResponse.class)
            .extracting("error")
            .hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
            .hasFieldOrPropertyWithValue("message", "Route not found");
    }

    @Test
    void should_return_not_found_if_the_schedule_is_not_present() {

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class)))
            .thenReturn(Either.left(new BookFlightServiceException(SCHEDULE_NOT_FOUND)));

        // WHEN
        GenericResponse<List<FlightDTO>> flights =
            interconnectionController.getFlights(BookFlightRequestDTO.builder().build());

        // THEN
        verify(getFlights).apply(any());

        assertThat(flights)
            .isInstanceOf(GenericResponse.class)
            .extracting("error")
            .hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
            .hasFieldOrPropertyWithValue("message", SCHEDULE_NOT_FOUND);
    }

    @Test
    void should_return_valid_response_if_the_route_and_schedule_are_present() {

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class))).thenReturn(Either.right(validResponse()));

        // WHEN
        GenericResponse<List<FlightDTO>> flights =
            interconnectionController.getFlights(BookFlightRequestDTO.builder().build());

        // THEN
        assertThat(flights)
            .isInstanceOf(GenericResponse.class)
            .extracting("data")
            .asList()
            .first()
            .hasFieldOrPropertyWithValue("stops", 0)
            .hasFieldOrProperty("legs");
    }

}