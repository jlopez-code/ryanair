package com.ryanair.challenge.configuration;

import feign.Contract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public Contract feignContract() {
        return new Contract.Default();
    }

}
