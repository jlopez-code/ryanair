package com.ryanair.challenge.api.mapper;

import com.ryanair.challenge.api.model.BookFlightRequestDTO;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestMapper {

    public BookFlightRequest toBookFlightRequest(BookFlightRequestDTO request){

        if(request == null){
            return null;
        }

        return BookFlightRequest.builder()
                .departure(request.getDeparture())
                .arrival(request.getArrival())
                .departureDateTime(request.getDepartureDateTime())
                .arrivalDateTime(request.getArrivalDateTime()).build();
    }

}
