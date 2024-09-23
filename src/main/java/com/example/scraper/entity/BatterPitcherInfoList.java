package com.example.scraper.entity;

import java.util.List;

import lombok.Data;

@Data
public class BatterPitcherInfoList {
    private List<BatterResults> topBatterResults;
    private List<PitcherResults> topPitcherResults;
    private List<BatterResults> bottomBatterResults;
    private List<PitcherResults> bottomPitcherResults;
}
