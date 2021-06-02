package com.ryanair.challenge.application;

import com.ryanair.challenge.domain.BookFlightService;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.domain.model.Flight;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ryanair.challenge.util.Constants.ROUTE_NOT_FOUND;
import static com.ryanair.challenge.util.Constants.validResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetFlightsTest {

    private GetFlights getFlights;
    private BookFlightService bookFlightService;

    @BeforeEach
    void setUp() {
        bookFlightService = mock(BookFlightService.class);
        getFlights = new GetFlights(bookFlightService);
    }

    @Test
    void should_return_exception_when_provide_invalid_request() {

        //GIVEN
        when(bookFlightService.getFlights(any()))
            .thenReturn(Either.left(new BookFlightServiceException(ROUTE_NOT_FOUND)));

        //WHEN
        Either<BookFlightServiceException, List<Flight>> result = getFlights.apply(BookFlightRequest.builder().build());

        //THEN
        verify(bookFlightService).getFlights(any());
        assertThat(result)
            .isNotNull()
            .isInstanceOf(Either.Left.class);
    }

    @Test
    void should_return_valid_response_when_provide_valid_request() {

        //GIVEN
        when(bookFlightService.getFlights(any()))
            .thenReturn(Either.right(validResponse()));

        //WHEN
        Either<BookFlightServiceException, List<Flight>> result = getFlights.apply(BookFlightRequest.builder().build());

        //THEN
        verify(bookFlightService).getFlights(any());
        assertThat(result)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
    }
}