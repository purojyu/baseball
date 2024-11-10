package com.example.scraper;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.scraper", "com.example.baseball"})
public class NPBWebScraperApplication {

    public static void main(String[] args) {
//        SpringApplication.run(NPBWebScraperApplication.class, args);
    }
//  @Bean
//  CommandLineRunner run(UpdNPBPlayerScraper2024 updNPBPlayerScraper2024) {
//      return args -> {
//    	  updNPBPlayerScraper2024.scrapePlayer();
//      };
//  }
//  @Bean
//  CommandLineRunner run(TradeScraper tradeScraper) {
//      return args -> {
//    	  tradeScraper.updateBaseballHistory();
//      };
//  }
////    @Bean
//    CommandLineRunner run(NPBWebScraper npbWebScraper) {
//        return args -> {
//            npbWebScraper.scrapeData();
//        };
//    }
//  @Bean
//    CommandLineRunner run(FirstNPBPlayerScraper2024 firstNPBPlayerScraper2024) {
//        return args -> {
//        	firstNPBPlayerScraper2024.scrapePlayer();
//        };
//    }
//  @Bean
//  CommandLineRunner run(FirstNPBPlayerScraper runClass) {
//      return args -> {
//    	  runClass.scrapeAllTeams();
//      };
//  }
}
