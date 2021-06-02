package com.ryanair.challenge.infrastructure.client.ryanair.routes;

import com.ryanair.challenge.infrastructure.client.dto.RouteDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(
    name = "ryanair-route",
    url = "${provider.ryanair.routes.url}"
)
public interface RyanairRouteClient {

    @RequestLine("GET routes/{airportFrom}")
    List<RouteDTO> getRoutes(@Param("airportFrom") String airportFrom);
}
