package com.desarrollo.raffy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RaffyAplication {
    public static void main(String[] args) {
        SpringApplication.run(RaffyAplication.class, args);
    }
}
