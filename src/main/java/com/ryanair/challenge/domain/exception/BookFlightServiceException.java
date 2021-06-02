package com.ryanair.challenge.domain.exception;


public class BookFlightServiceException extends RuntimeException {

    public BookFlightServiceException(String message) {
        super(message);
    }

    public BookFlightServiceException(Throwable throwable) {
        super(throwable);
    }
}