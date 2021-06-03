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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.ryanair.challenge.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookFlightServiceImplIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String BGY = "BGY";

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

    @Value("classpath:response/valid_BCN_CFU_routes.json")
    private Resource validBCNCFURoutes;

    @Value("classpath:response/valid_BCN_CFU_schedule.json")
    private Resource validBCNCFUSchedule;

    @Value("classpath:response/valid_BGY_CFU_schedule.json")
    private Resource validBGYCFUSchedule;

    @Value("classpath:response/valid_BCN_BGY_schedule.json")
    private Resource validBCNBGYSchedule;

    @Value("classpath:response/invalid_schedule.json")
    private Resource invalidSchedule;

    @Test
    void should_return_valid_response_when_there_are_routes_and_schedule() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes(BCN)).thenReturn(
            OBJECT_MAPPER.readValue(IOUtils.toString(validRoutes.getInputStream()), new TypeReference<List<RouteDTO>>() {
            }));
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
    void should_return_exception_when_there_are_not_schedule() throws IOException {

        //GIVEN
        when(ryanairRouteClient.getRoutes("BCN"))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(validRoutes.getInputStream()), new TypeReference<List<RouteDTO>>() {
            }));
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


    @Test
    void should_return_multiple_fligths_when_there_are_routes_and_schedule() throws IOException {

        //GIVEN
        mockReturnForMultipleResults();

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival(CFU).departure(BCN)
                    .departureDateTime(LocalDateTime.of(2021, 07, 27, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 07, 27, 23, 59)).build());

        //THEN
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
        assertThat(flights.get())
            .isNotNull()
            .hasSize(2);
    }


    @Test
    void should_return_fligth_when_filter_by_hour_there_are_routes_and_schedule() throws IOException {

        //GIVEN
        mockReturnForMultipleResults();

        //WHEN
        Either<BookFlightServiceException, List<Flight>> flights =
            bookFlightService.getFlights(
                BookFlightRequest.builder().arrival(CFU).departure(BCN)
                    .departureDateTime(LocalDateTime.of(2021, 07, 27, 00, 01))
                    .arrivalDateTime(LocalDateTime.of(2021, 07, 27, 20, 10)).build());

        //THEN
        assertThat(flights)
            .isNotNull()
            .isInstanceOf(Either.Right.class);
        assertThat(flights.get())
            .isNotNull()
            .hasSize(1)
            .asList()
            .first()
            .hasFieldOrPropertyWithValue("stops", 1)
            .extracting("legs")
            .asList()
            .hasSize(2);
    }

    private void mockReturnForMultipleResults() throws IOException {
        when(ryanairRouteClient.getRoutes(BCN)).thenReturn(
            OBJECT_MAPPER.readValue(IOUtils.toString(validBCNCFURoutes.getInputStream()), new TypeReference<List<RouteDTO>>() {
            }));
        when(ryanairScheduleClient.getSchedule(BCN, CFU, 2021, 07))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(validBCNCFUSchedule.getInputStream()), ScheduleDTO.class));
        when(ryanairScheduleClient.getSchedule(BCN, BGY, 2021, 07))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(validBCNBGYSchedule.getInputStream()), ScheduleDTO.class));
        when(ryanairScheduleClient.getSchedule(BGY, CFU, 2021, 07))
            .thenReturn(OBJECT_MAPPER.readValue(IOUtils.toString(validBGYCFUSchedule.getInputStream()), ScheduleDTO.class));
    }


}