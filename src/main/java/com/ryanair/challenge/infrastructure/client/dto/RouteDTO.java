package com.ryanair.challenge.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RouteDTO implements Serializable {

    @JsonProperty("airportFrom")
    private String airportFrom;
    @JsonProperty("airportTo")
    private String airportTo;
    @JsonProperty("connectingAirport")
    private String connectingAirport;
    @JsonProperty("newRoute")
    private Boolean newRoute;
    @JsonProperty("seasonalRoute")
    private Boolean seasonalRoute;
    @JsonProperty("operator")
    private String operator;
    @JsonProperty("carrierCode")
    private String carrierCode;
    @JsonProperty("group")
    private String group;
    @JsonProperty("similarArrivalAirportCodes")
    private List<String> similarArrivalAirportCodes;
    @JsonProperty("tags")
    private List<String> tags;
}
