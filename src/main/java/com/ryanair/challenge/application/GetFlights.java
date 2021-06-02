package com.ryanair.challenge.application;

import com.ryanair.challenge.domain.BookFlightService;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class GetFlights implements Function<BookFlightRequest, Either<BookFlightServiceException, List<Flight>>> {

    private final BookFlightService bookFlightService;

    @Override
    public Either<BookFlightServiceException, List<Flight>> apply(BookFlightRequest bookFlightRequest) {
        return bookFlightService.getFlights(bookFlightRequest);
    }
}
