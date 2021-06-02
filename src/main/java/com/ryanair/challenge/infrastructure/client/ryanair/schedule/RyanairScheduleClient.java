package com.ryanair.challenge.infrastructure.client.ryanair.schedule;

import com.ryanair.challenge.infrastructure.client.dto.ScheduleDTO;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
    name = "ryanair-schedule",
    url = "${provider.ryanair.schedule.url}"
)
public interface RyanairScheduleClient {

    @RequestLine("GET schedules/{from}/{to}/years/{year}/months/{month}")
    ScheduleDTO getSchedule(@Param("from") String from, @Param("to") String to,
                            @Param("year") Integer year, @Param("month") Integer month);
}
