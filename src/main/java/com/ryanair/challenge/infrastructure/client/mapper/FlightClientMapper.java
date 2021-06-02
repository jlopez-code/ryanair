package com.ryanair.challenge.infrastructure.client.mapper;

import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.Leg;
import com.ryanair.challenge.infrastructure.client.dto.FlightDTO;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class FlightClientMapper {

    public Flight toBookFlightRequest(FlightDTO flightDTO) {

        if (flightDTO == null) {
            return null;
        }

        return Flight.builder()
            .legs(List.of(
                Leg.builder()
                    .departureAirport(flightDTO.getDepartureCity())
                    .arrivalAirport(flightDTO.getArrivalCity())
                    .arrivalDateTime(flightDTO.getArrivalLocalDateTime().toString())
                    .departureDateTime(flightDTO.getDepartureLocalDateTime().toString())
                    .build()
            )).build();
    }

}
