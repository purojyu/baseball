//package com.example.scraper;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//
//@SpringBootApplication
//@ComponentScan(basePackages = {"com.example.scraper", "com.example.baseball"})
//public class NPBWebScraperApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(NPBWebScraperApplication.class, args);
//    }
//    @Bean
//    CommandLineRunner run(NPBWebScraper npbWebScraper) {
//        return args -> {
//            npbWebScraper.scrapeData();
//        };
//    }
//}
