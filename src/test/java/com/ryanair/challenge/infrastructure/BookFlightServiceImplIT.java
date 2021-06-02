package com.ryanair.challenge.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.infrastructure.client.dto.RouteDTO;
import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;
import com.ryanair.challenge.infrastructure.client.ryanair.routes.RyanairRouteClient;
import com.ryanair.challenge.infrastructure.client.ryanair.schedule.RyanairScheduleClient;
import io.vavr.control.Either;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.ryanair.challenge.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookFlightServiceImplIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BookFlightServiceImpl bookFlightService;

    @MockBean
    private RyanairRouteClient ryanairRouteClient;
    @MockBean
    private RyanairScheduleClient ryanairScheduleClient;

    @Value("classpath:response/valid_routes.json")
    private Resource validRoutes;

    @Value("classpath:response/valid_schedule.json")
    private Resource validSchedule;

    @Value("classpath:response/invalid_schedule.json")
    private Resource invalidSchedule;

    @Test
    @DirtiesContext
    void should_return_valid_response_when_there_are_routes_and_schedule() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes(BCN)).thenReturn(
            OBJECT_MAPPER.readValue(IOUtils.toString(validRoutes.getInputStream()), new TypeReference<List<RouteDTO>>() {}));
        when(ryanairScheduleClient.getSchedule(any(), any(), any(), any())).thenReturn(
            OBJECT_MAPPER.readValue(IOUtils.toString(validSchedule.getInputStream()), ScheduleDTO.class));

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival(DUB).departure(BCN)
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 23, 59)).build());

        //THEN
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
        assertThat(flights.get())
            .isNotNull()
            .first()
            .hasNoNullFieldsOrProperties();
    }

    @Test
    @DirtiesContext
    void should_return_exception_when_there_are_not_routes() {

        //GIVEN
        when(ryanairRouteClient.getRoutes(any())).thenReturn(Collections.emptyList());

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival(BUD).departure(BCN)
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 23, 59)).build());

        //THEN
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Left.class);
        assertThat(flights.getLeft())
            .isNotNull()
            .hasNoNullFieldsOrProperties()
            .hasFieldOrPropertyWithValue(DETAIL_MESSAGE, ROUTE_NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void should_return_exception_when_there_are_not_schedule() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes("BCN"))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(validRoutes.getInputStream()), new TypeReference<List<RouteDTO>>() {}));
        when(ryanairScheduleClient.getSchedule(any(), any(), any(), any()))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(invalidSchedule.getInputStream()), ScheduleDTO.class));

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival(DUB).departure(BCN)
                    .departureDateTime(LocalDateTime.of(2021, 06, 16, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 06, 16, 00, 10)).build());

        //THEN
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Left.class);
        assertThat(flights.getLeft())
            .isNotNull()
            .hasNoNullFieldsOrProperties()
            .extracting(DETAIL_MESSAGE)
            .asString()
            .contains("No flights available from");
    }

}