package com.ryanair.challenge.domain;

import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.Flight;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import io.vavr.control.Either;

import java.util.List;

public interface BookFlightService {

    Either<BookFlightServiceException, List<Flight>> getFlights(final BookFlightRequest city);
}
