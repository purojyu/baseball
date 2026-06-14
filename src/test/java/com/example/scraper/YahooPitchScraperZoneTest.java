package com.example.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Yahoo一球速報のボール座標 → 5×5ゾーン(1-25) 変換 {@link YahooPitchScraper#coordToZone(double, double)} の検証。
 *
 * グリッドは CHART 160×200px を 5×5 に分割（CELL_W=32, CELL_H=40）、BALL_RADIUS=13。
 * ゾーン番号は左上=1 …右上=5 / 左下=21 …右下=25（行優先, row*5+col+1）。Yahoo座標系のまま反転しない。
 * 取込本番 {@link YahooPitchScraper#buildCourseMap} と同一ロジックであることを担保する。
 */
class YahooPitchScraperZoneTest {

    private final YahooPitchScraper scraper = new YahooPitchScraper();

    @Test
    @DisplayName("中央のボールはゾーン13")
    void center() {
        // 中心(cx,cy)=(80,100) → col=2,row=2 → 13
        assertEquals(13, scraper.coordToZone(87, 67));
    }

    @Test
    @DisplayName("四隅: 左上=1 / 右上=5 / 左下=21 / 右下=25")
    void corners() {
        assertEquals(1, scraper.coordToZone(0, 0));     // row0,col0
        assertEquals(5, scraper.coordToZone(0, 127));   // cx=140 → col4
        assertEquals(21, scraper.coordToZone(167, 0));  // cy=180 → row4
        assertEquals(25, scraper.coordToZone(167, 127));// row4,col4
    }

    @Test
    @DisplayName("各列・各行の境界が期待どおりのセルに入る")
    void cellBoundaries() {
        // 1行目(row0, cy<40 → top<27): 列ごとに 1..5
        assertEquals(1, scraper.coordToZone(0, 0));    // cx=13 col0
        assertEquals(2, scraper.coordToZone(0, 32));   // cx=45 col1
        assertEquals(3, scraper.coordToZone(0, 64));   // cx=77 col2
        assertEquals(4, scraper.coordToZone(0, 96));   // cx=109 col3
        assertEquals(5, scraper.coordToZone(0, 128));  // cx=141 col4
    }

    @Test
    @DisplayName("範囲外の大きい座標はグリッド端(25)にクランプ")
    void clampsOutOfRange() {
        assertEquals(25, scraper.coordToZone(1000, 1000));
    }

    @Test
    @DisplayName("マイナス座標は最上段・最左列(ゾーン1)に寄せる")
    void negativeCoords() {
        assertEquals(1, scraper.coordToZone(-50, -50));
        assertEquals(3, scraper.coordToZone(-50, 64));   // top<0→row0, cx=77→col2 → 3
        assertEquals(21, scraper.coordToZone(160, -50)); // left<0→col0, cy=173→row4 → 21
    }
}
