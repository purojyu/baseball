package com.example.scraper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Lambda用スクレイピングハンドラ
 * EventBridge Scheduleから呼び出される
 */
@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan("com.example.baseball.entity")
@EnableJpaRepositories("com.example.baseball.repository")
public class LambdaScraperHandler implements RequestHandler<Map<String, Object>, String> {

    private static final Logger log = LoggerFactory.getLogger(LambdaScraperHandler.class);

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        log.info("Lambda scraper started. Event: {}", event);

        SpringApplication app = new SpringApplication(LambdaScraperHandler.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("prod");
        // application.propertiesのデフォルトdevを上書きしてprodのみ有効化
        System.setProperty("spring.profiles.active", "prod");

        try (ConfigurableApplicationContext ctx = app.run()) {
            NPBWebScraper npbScraper = ctx.getBean(NPBWebScraper.class);

            // NPBスクレイピング実行
            log.info("NPBスクレイピング開始");
            npbScraper.scrapeData();
            log.info("NPBスクレイピング完了");

            // Yahoo投球データスクレイピングは現在未使用
            // ※ scrapeRangeは7日分処理でスリープが長く、Lambda 15分制限を超えるため
            // LocalDate from = LocalDate.now().minusDays(7);
            // LocalDate to = LocalDate.now().minusDays(1);
            // yahooScraper.scrapeRange(from, to);

            return "Scraping completed successfully";
        } catch (Exception e) {
            log.error("Scraping failed", e);
            return "Scraping failed: " + e.getMessage();
        }
    }
}
