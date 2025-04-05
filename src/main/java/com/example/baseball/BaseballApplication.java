package com.example.baseball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.example.scraper", "com.example.baseball"})
public class BaseballApplication {
    public static void main(String[] args) {
        SpringApplication.run(BaseballApplication.class, args);
    }
}