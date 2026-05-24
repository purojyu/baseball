package com.example.baseball.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.scraper.NPBWebScraper;
import com.example.scraper.YahooPitchScraper;

@RestController
@RequestMapping("/batch")
public class ScrapeBatchController {

    private final NPBWebScraper npbWebScraper;
    private final YahooPitchScraper yahooPitchScraper;

    public ScrapeBatchController(NPBWebScraper npbWebScraper, YahooPitchScraper yahooPitchScraper) {
        this.npbWebScraper = npbWebScraper;
        this.yahooPitchScraper = yahooPitchScraper;
    }

    @GetMapping("/runYahooPitchScrape")
    public String runYahooPitchScrape(
            @RequestParam String from,
            @RequestParam String to) {
        try {
            yahooPitchScraper.scrapeRange(LocalDate.parse(from), LocalDate.parse(to));
            return "YahooPitchScrape done. from=" + from + ", to=" + to;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
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