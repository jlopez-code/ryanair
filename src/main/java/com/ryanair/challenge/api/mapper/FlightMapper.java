package com.ryanair.challenge.api.mapper;

import com.ryanair.challenge.api.model.FlightDTO;
import com.ryanair.challenge.api.model.LegDTO;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.Leg;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;


@UtilityClass
public class FlightMapper {

    public FlightDTO toFlightDTO(Flight flight) {

        if (flight == null) {
            return null;
        }

        return FlightDTO.builder()
            .stops(flight.getLegs().size() - 1)
            .legs(flight.getLegs().stream()
                .map(FlightMapper::toLegDTO)
                .collect(Collectors.toList())
            )
            .build();
    }

    private LegDTO toLegDTO(Leg leg) {
        return LegDTO.builder()
            .arrivalAirport(leg.getArrivalAirport())
            .departureAirport(leg.getDepartureAirport())
            .arrivalDateTime(leg.getArrivalDateTime())
            .departureDateTime(leg.getDepartureDateTime())
            .build();
    }

}
