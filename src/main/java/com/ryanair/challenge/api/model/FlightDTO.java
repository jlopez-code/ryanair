package com.ryanair.challenge.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO implements Serializable {

    private int stops;
    private List<LegDTO> legs;
}
