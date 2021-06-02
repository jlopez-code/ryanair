package com.ryanair.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RyanairChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RyanairChallengeApplication.class, args);
    }

}
