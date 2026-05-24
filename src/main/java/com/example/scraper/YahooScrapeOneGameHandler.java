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
 * 1試合分の Yahoo pitch_result を取り込む Lambda Handler。
 *
 * Step Functions の Map state から並列起動される。
 * 各 invocation は 1試合分の入力を受け取り、scrapeGameByYahooGameId で
 * Yahoo の一球速報を取得して pitch_result を INSERT する。
 *
 * Input : { "yahooGameId": "2021038650" }
 * Output: "done"
 */
@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan("com.example.baseball.entity")
@EnableJpaRepositories("com.example.baseball.repository")
public class YahooScrapeOneGameHandler implements RequestHandler<Map<String, Object>, String> {

    private static final Logger log = LoggerFactory.getLogger(YahooScrapeOneGameHandler.class);

    private static volatile ConfigurableApplicationContext CTX;

    private static synchronized ConfigurableApplicationContext context() {
        if (CTX == null) {
            SpringApplication app = new SpringApplication(YahooScrapeOneGameHandler.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            app.setAdditionalProfiles("prod");
            System.setProperty("spring.profiles.active", "prod");
            CTX = app.run();
        }
        return CTX;
    }

    @Override
    public String handleRequest(Map<String, Object> event, Context lambdaContext) {
        Object idObj = event == null ? null : event.get("yahooGameId");
        if (!(idObj instanceof String yahooGameId) || yahooGameId.isBlank()) {
            throw new IllegalArgumentException("yahooGameId は必須です: event=" + event);
        }
        log.info("YahooScrapeOneGame start: yahooGameId={}", yahooGameId);
        try {
            YahooPitchScraper scraper = context().getBean(YahooPitchScraper.class);
            scraper.scrapeGameByYahooGameId(yahooGameId);
            log.info("YahooScrapeOneGame done: yahooGameId={}", yahooGameId);
            return "done:" + yahooGameId;
        } catch (Exception e) {
            log.error("YahooScrapeOneGame failed: yahooGameId={}", yahooGameId, e);
            // Step Functions の Retry 機構に拾わせる
            throw new RuntimeException("scrape-one-game failed for " + yahooGameId + ": " + e.getMessage(), e);
        }
    }
}
