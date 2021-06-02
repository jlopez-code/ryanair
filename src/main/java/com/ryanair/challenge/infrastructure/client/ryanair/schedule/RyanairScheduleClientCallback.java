package com.ryanair.challenge.infrastructure.client.ryanair.schedule;

import com.ryanair.challenge.domain.exception.BookFlightServiceException;
import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RyanairScheduleClientCallback implements RyanairScheduleClient {

    private final Exception cause;

    @Override
    public ScheduleDTO getSchedule(final String from, final String to, final Integer year, final Integer month) {
        log.error(cause.getLocalizedMessage());
        throw new BookFlightServiceException(cause);
    }

}
