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

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class GlobalDefaultExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public GenericResponse<Object> handle(BindException exception) {
        log.error(exception.getMessage(), exception);
        return GenericResponse.builder()
            .error(
                GenericResponseError.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("%s %s".formatted(exception.getFieldError().getField(),
                        exception.getFieldError().getDefaultMessage()))
                    .build()).build();
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

}
