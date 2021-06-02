package com.ryanair.challenge.api;

import com.ryanair.challenge.api.rest.response.GenericResponse;
import com.ryanair.challenge.api.rest.response.GenericResponseError;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class GlobalDefaultExceptionHandler {

    private static final String EMPTY_STRING = "";

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public GenericResponse<Object> handle( BindException exception) {
        log.error(exception.getMessage(), exception);
        if (exception.getFieldError() != null) {
            return GenericResponse.builder()
                .error(
                    GenericResponseError.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("%s %s".formatted(getFieldError(exception), getDefaultMessage(exception)))
                        .build()).build();
        }else return null;
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public GenericResponse<Object> handle(Exception exception) {
        log.error(exception.getMessage(), exception);
        return GenericResponse.builder()
            .data(null)
            .error(
                GenericResponseError.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(exception.getLocalizedMessage())
                    .build()).build();
    }

    private String getDefaultMessage(BindException exception) {
        return Objects.nonNull(exception.getFieldError()) ? exception.getFieldError().getDefaultMessage() : EMPTY_STRING;
    }

    private String getFieldError(BindException exception) {
        return Objects.nonNull(exception.getFieldError()) ? exception.getFieldError().getField() : EMPTY_STRING;
    }

}
