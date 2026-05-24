package com.example.scraper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * Yahoo スクレイプ対象試合一覧を返す Lambda Handler。
 *
 * Step Functions の最初のステップとして起動され、Yahoo schedule fetch を
 * 走らせて「指定日の終了試合 yahooGameId 一覧」を返す。
 *
 * Input  : { "date": "2026-05-22" }  date 省略時は JST yesterday
 * Output : { "games": [{ "yahooGameId": "..." }, ...] }
 *
 * Map state の ItemsPath を $.games に設定することで、各 Lambda invocation に
 * 1要素 = 1試合が割り振られる。
 */
@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan("com.example.baseball.entity")
@EnableJpaRepositories("com.example.baseball.repository")
public class YahooListGamesHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(YahooListGamesHandler.class);
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private static volatile ConfigurableApplicationContext CTX;

    private static synchronized ConfigurableApplicationContext context() {
        if (CTX == null) {
            SpringApplication app = new SpringApplication(YahooListGamesHandler.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            app.setAdditionalProfiles("prod");
            System.setProperty("spring.profiles.active", "prod");
            CTX = app.run();
        }
        return CTX;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context lambdaContext) {
        LocalDate date = resolveDate(event);
        log.info("YahooListGames start: date={}", date);

        try {
            YahooPitchScraper scraper = context().getBean(YahooPitchScraper.class);
            List<String> yahooGameIds = scraper.fetchYahooGameIdsForDate(date);

            List<Map<String, String>> games = yahooGameIds.stream()
                    .map(id -> Map.of("yahooGameId", id))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("date", date.toString());
            result.put("games", games);
            log.info("YahooListGames done: date={}, count={}", date, games.size());
            return result;
        } catch (Exception e) {
            log.error("YahooListGames failed: date={}", date, e);
            throw new RuntimeException("list-target-games failed: " + e.getMessage(), e);
        }
    }

    private LocalDate resolveDate(Map<String, Object> event) {
        if (event != null && event.get("date") instanceof String s && !s.isBlank()) {
            return LocalDate.parse(s);
        }
        return LocalDate.now(JST).minusDays(1);
    }
}
