package com.example.scraper;

import java.util.Arrays;
import java.util.List;

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

    // バッチ一括取り込み用（ローカル実行専用、Lambda起動時には実行しない）
    // 使用時はコメントアウトを外し、ローカルでNeon DB接続して実行する
    // @Bean
    // CommandLineRunner run(NPBWebScraper scraper) {
    //     return args -> {
    //         List<String> years = Arrays.asList("2026");
    //         List<String> months = Arrays.asList("03", "04", "05");
    //         scraper.scrapeBatch(years, months);
    //     };
    // }
}