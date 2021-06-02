package com.ryanair.challenge.util;

import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.Leg;
import com.ryanair.challenge.infrastructure.client.dto.DayDTO;
import com.ryanair.challenge.infrastructure.client.dto.FlightDTO;
import com.ryanair.challenge.infrastructure.client.dto.RouteDTO;
import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;

import java.util.List;


public class Constants {

    public static final String ROUTE_NOT_FOUND = "Route not found";
    public static final String SCHEDULE_NOT_FOUND = "No flights available from X to Y on this date";
    public static final String DETAIL_MESSAGE = "detailMessage";
    public static final String INTERCONNECTIONS_URI = "/interconnections?departure=%s&arrival=%s&departureDateTime=%s&arrivalDateTime=%s";
    public static final String BCN = "BCN";
    public static final String BUD = "BUD";
    public static final String DUB = "DUB";
    public static final String UTF_8 = "UTF-8";
    public static final String T_06_19 = "2021-06-16T06:19";
    public static final String T_23_59 = "2021-06-16T23:59";
    public static final String T_10_00 = "2021-06-16T10:00";
    public static final String T_12_30 = "2021-06-16T12:30";

    public static final List<Flight> validResponse() {
        return List.of(Flight.builder()
            .stops(0)
            .legs(List.of(Leg.builder().departureDateTime(T_10_00).arrivalDateTime(T_12_30)
                .arrivalAirport(BUD).departureAirport(BCN).build())).build());
    }

    public static final List<RouteDTO> getValidRoutesResponse() {
        return List.of(RouteDTO.builder().airportFrom("BCN").airportTo("MAD")
            .operator("RYANAIR").connectingAirport(null).build());
    }

    public static final ScheduleDTO getValidScheduleResponse() {
        return ScheduleDTO.builder()
            .month(6)
            .days(List.of(DayDTO.builder()
                .day(16)
                .flights(List.of(FlightDTO.builder().arrivalCity("MAD").departureCity("BCN")
                    .arrivalTime("15:00").departureTime("13:00").build())).build()))
            .build();
    }

    public static final ScheduleDTO getMultiplyScheduleResponse() {
        return ScheduleDTO.builder()
            .month(6)
            .days(List.of(DayDTO.builder()
                .day(16)
                .flights(List.of(
                    FlightDTO.builder().arrivalCity("MAD").departureCity("BCN")
                    .arrivalTime("15:00").departureTime("13:00").build(),
                    FlightDTO.builder().arrivalCity("MAD").departureCity("BCN").number("number")
                        .arrivalTime("19:00").carrierCode("carrierCode").departureTime("17:00").build())).build()))
            .build();
    }

    public static final String routeResponse = """
        [{"airportFrom":"BCN","airportTo":"ACE","connectingAirport":null,"newRoute":false,"seasonalRoute":false,
        "operator":"RYANAIR","carrierCode":"FR","group":"GENERIC","similarArrivalAirportCodes":[],"tags":[]},
        {"airportFrom":"BCN","airportTo":"AGP","connectingAirport":null,"newRoute":false,"seasonalRoute":false,
        "operator":"RYANAIR","carrierCode":"FR","group":"DOMESTIC",
        "similarArrivalAirportCodes":["ACE","IBZ","LPA","MAH","OPO","PMI","XRY"],"tags":[]}]
        """;

}
