package com.ryanair.challenge.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryanair.challenge.api.rest.response.GenericResponse;
import com.ryanair.challenge.api.rest.response.GenericResponseError;
import com.ryanair.challenge.application.GetFlights;
import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.domain.model.BookFlightRequest;
import com.ryanair.challenge.util.AbstractRestTest;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import static com.ryanair.challenge.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class InterconnectionControllerIT extends AbstractRestTest {

    @MockBean
    GetFlights getFlights;

    @Test
    void should_return_not_found_if_the_route_is_not_present() throws Exception {

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class)))
            .thenReturn(Either.left(new BookFlightServiceException(ROUTE_NOT_FOUND)));

        // WHEN
        final ResultActions resultActions = this.mockMvc.perform(get(INTERCONNECTIONS_URI
            .formatted(BCN, BUD, T_06_19, T_23_59))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8)
            .content(getNotFoundErrorMessage(ROUTE_NOT_FOUND)))

            // THEN
            .andDo(print())
            .andExpect(status().isOk());

        validateExceptionContent(resultActions);
        assertThat(resultActions.andReturn().getResponse().getContentAsString())
            .contains(ROUTE_NOT_FOUND);
    }

    @Test
    void should_return_not_found_if_the_schedule_is_not_present() throws Exception {

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class)))
            .thenReturn(Either.left(new BookFlightServiceException(SCHEDULE_NOT_FOUND)));

        // WHEN
        final ResultActions resultActions = this.mockMvc.perform(get(INTERCONNECTIONS_URI
            .formatted(BCN, BUD, T_06_19, T_23_59))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8)
            .content(getNotFoundErrorMessage(SCHEDULE_NOT_FOUND)))

            // THEN
            .andDo(print())
            .andExpect(status().isOk());

        validateExceptionContent(resultActions);
        assertThat(resultActions.andReturn().getResponse().getContentAsString())
            .contains(SCHEDULE_NOT_FOUND);
    }

    @Test
    void should_return_valid_response_if_the_route_and_schedule_are_present() throws Exception {

        // GIVEN
        when(getFlights.apply(any(BookFlightRequest.class)))
            .thenReturn(Either.right(validResponse()));

        // WHEN
        final ResultActions resultActions = this.mockMvc.perform(get(INTERCONNECTIONS_URI
            .formatted(BCN, BUD, T_06_19, T_23_59))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8)
            .content(getValidJsonResponse()))

            // THEN
            .andDo(print())
            .andExpect(status().isOk());

        validateExceptionContent(resultActions);
        assertThat(resultActions.andReturn().getResponse().getContentAsString())
            .contains(BCN, BUD, T_10_00, T_12_30);
    }

    private String getNotFoundErrorMessage(String message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            GenericResponse.builder().error(GenericResponseError.builder()
                .message(message).code(HttpStatus.NOT_FOUND.value()).build()));
    }

    private String getValidJsonResponse() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            GenericResponse.builder().data(validResponse()).build());
    }

    private void validateExceptionContent(ResultActions perform) {
        assertThat(perform.andReturn()).isNotNull()
            .extracting("mockResponse").isNotNull()
            .extracting("content").isNotNull();
    }

}