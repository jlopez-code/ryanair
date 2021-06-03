package com.ryanair.challenge.infrastructure;

import com.ryanair.challenge.domain.BookFlightService;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;
import com.ryanair.challenge.infrastructure.client.ryanair.routes.RyanairRouteClient;
import com.ryanair.challenge.infrastructure.client.ryanair.schedule.RyanairScheduleClient;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.ryanair.challenge.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookFlightServiceImplTest {

    private RyanairRouteClient ryanairRouteClient;
    private RyanairScheduleClient ryanairScheduleClient;
    private BookFlightService bookFlightService;

    @BeforeEach
    void setUp() {
        ryanairRouteClient = mock(RyanairRouteClient.class);
        ryanairScheduleClient = mock(RyanairScheduleClient.class);
        bookFlightService = new BookFlightServiceImpl(ryanairRouteClient, ryanairScheduleClient);
    }

    @Test
    void should_return_exception_when_there_are_not_routes() {

        //GIVEN
        when(ryanairRouteClient.getRoutes(any())).thenReturn(Collections.emptyList());

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(BookFlightRequest.builder().build());

        //THEN
        verify(ryanairRouteClient).getRoutes(any());
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Left.class);
        assertThat(flights.getLeft())
            .isNotNull()
            .hasFieldOrPropertyWithValue(DETAIL_MESSAGE, ROUTE_NOT_FOUND);
    }

    @Test
    void should_return_exception_when_there_are_not_schedules() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes(any())).thenReturn(getValidRoutesResponse());
        when(ryanairScheduleClient.getSchedule(any(), any(), any(), any()))
            .thenReturn(ScheduleDTO.builder().month(0).days(Collections.emptyList()).build());

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival("MAD").departure("BCN")
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 23, 59)).build());

        //THEN
        verify(ryanairRouteClient).getRoutes(any());
        verify(ryanairScheduleClient).getSchedule(any(), any(), any(), any());
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Left.class);
        assertThat(flights.getLeft())
            .isNotNull()
            .hasFieldOrPropertyWithValue(DETAIL_MESSAGE, "No flights available from BCN to MAD on this date");
    }

    @Test
    void should_return_valid_response_when_there_are_route_and_schedules() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes(any())).thenReturn(getValidRoutesResponse());
        when(ryanairScheduleClient.getSchedule(any(), any(), any(), any())).thenReturn(getValidScheduleResponse());

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival("MAD").departure("BCN")
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 23, 59)).build());

        //THEN
        verify(ryanairRouteClient).getRoutes(any());
        verify(ryanairScheduleClient).getSchedule(any(), any(), any(), any());
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
        assertThat(flights.get())
            .isNotNull()
            .hasSizeLessThanOrEqualTo(1)
            .first()
            .hasNoNullFieldsOrProperties()
            .extracting("legs")
            .asList()
            .hasSizeLessThanOrEqualTo(1)
            .first()
            .hasNoNullFieldsOrProperties();
    }


    @Test
    void should_return_valid_response_with_more_than_one_flight_when_there_are_route_and_schedules() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes(any())).thenReturn(getValidRoutesResponse());
        when(ryanairScheduleClient.getSchedule(any(), any(), any(), any())).thenReturn(getMultiplyScheduleResponse());

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival("MAD").departure("BCN")
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 23, 59)).build());

        //THEN
        verify(ryanairRouteClient).getRoutes(any());
        verify(ryanairScheduleClient).getSchedule(any(), any(), any(), any());
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
        assertThat(flights.get())
            .isNotNull()
            .hasSizeLessThanOrEqualTo(2)
            .first()
            .hasNoNullFieldsOrProperties()
            .extracting("legs")
            .asList()
            .hasSizeLessThanOrEqualTo(2)
            .first()
            .hasNoNullFieldsOrProperties();
    }


}