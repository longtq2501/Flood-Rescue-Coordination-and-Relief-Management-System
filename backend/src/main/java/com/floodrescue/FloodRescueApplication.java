package com.floodrescue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FloodRescueApplication {
    public static void main(String[] args) {
        SpringApplication.run(FloodRescueApplication.class, args);
    }
}