package com.example.baseball.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
            npbWebScraper.scrapeData(); // 実際のスクレイプ処理を呼ぶ
            return "ScrapeData done.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}