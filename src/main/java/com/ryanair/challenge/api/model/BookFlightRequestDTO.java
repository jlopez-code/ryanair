package com.ryanair.challenge.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFlightRequestDTO {

    @NotEmpty
    @Length(max = 3, min = 3, message = "The IATA code should have 3 characters")
    @JsonProperty("departure")
    private String departure;
    @NotEmpty
    @Length(max = 3, min = 3, message = "The IATA code should have 3 characters")
    @JsonProperty("arrival")
    private String arrival;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("departureDateTime")
    private LocalDateTime departureDateTime;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("arrivalDateTime")
    private LocalDateTime arrivalDateTime;
}
