package com.ryanair.challenge.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FlightDTO implements Serializable {

    @JsonProperty("carrierCode")
    private String carrierCode;
    @JsonProperty("number")
    private String number;
    @JsonProperty("departureTime")
    private String departureTime;
    @JsonProperty("arrivalTime")
    private String arrivalTime;
    private transient LocalDateTime departureLocalDateTime;
    private transient LocalDateTime arrivalLocalDateTime;
    private transient String departureCity;
    private transient String arrivalCity;
}
