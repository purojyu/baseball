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

    // 投球コース一括取り込み用（ローカル実行専用）
    // 自動 scrape は無効化。/batch/runYahooPitchScrape エンドポイント経由で手動 fire する。
    // @Bean
    // CommandLineRunner run(YahooPitchScraper yahooScraper) {
    //     return args -> {
    //         yahooScraper.scrapeRange(LocalDate.of(2026, 3, 27), LocalDate.now().minusDays(1));
    //     };
    // }
}