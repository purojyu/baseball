package com.example.baseball.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.scraper.NPBWebScraper;

// Lambda移行後はEventBridge経由で実行するため、このエンドポイントは無効化
// 手動実行が必要な場合のみコメントアウトを外す（認証なしで公開されるので注意）
//@RestController
@RequestMapping("/batch")
public class ScrapeBatchController {

    private final NPBWebScraper npbWebScraper;

    public ScrapeBatchController(NPBWebScraper npbWebScraper) {
        this.npbWebScraper = npbWebScraper;
    }

    @GetMapping("/runScrape")
    public String runScrape() {
        try {
            npbWebScraper.scrapeData();
            return "ScrapeData done.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 指定年・月の一括取り込み
     * 例: /batch/runBatch?years=2026&months=03,04
     */
    @GetMapping("/runBatch")
    public String runBatch(
            @RequestParam(defaultValue = "2026") String years,
            @RequestParam(defaultValue = "03,04") String months) {
        try {
            List<String> yearList = Arrays.asList(years.split(","));
            List<String> monthList = Arrays.asList(months.split(","));
            npbWebScraper.scrapeBatch(yearList, monthList);
            return "ScrapeBatch done. years=" + yearList + ", months=" + monthList;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}