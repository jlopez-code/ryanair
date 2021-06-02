package com.ryanair.challenge.infrastructure.client.ryanair.routes;

import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.infrastructure.client.dto.RouteDTO;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class RyanairRouteClientCallback implements RyanairRouteClient {

    private final Exception cause;

    @Override
    public List<RouteDTO> getRoutes(final String airportFrom) {
        throw new BookFlightServiceException(cause);
    }

}
