package com.example.baseball.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.scraper.NPBWebScraper;

//動かす時だけコメントアウトを外す
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