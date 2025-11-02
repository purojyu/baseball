package com.example.scraper;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example")          // ★ここ
@EntityScan("com.example.baseball.entity")                       // ★Entity
@EnableJpaRepositories("com.example.baseball.repository")        // ★Repository
public class NPBWebScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(NPBWebScraperApplication.class, args);
    }

    @Bean
    CommandLineRunner run(YahooPitchScraper scraper) {
        return args -> scraper.scrapeRange(
                LocalDate.of(2025, 3, 28),
                LocalDate.now());
    }
}