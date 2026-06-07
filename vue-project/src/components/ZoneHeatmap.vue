<template>
  <div class="zone-section">
    <h3 class="section-title">
      {{ title }}
      <span v-if="year" class="year-badge">{{ year }}年シーズン</span>
      <span class="section-sub">{{ subtitle }} · n={{ sampleSize }}打席</span>
    </h3>
    <div class="course-with-silhouettes">
      <img
        v-if="showSilhouettes && showLeftSilhouette"
        class="batter-silhouette left"
        :src="batterImageLeft"
        alt=""
        aria-hidden="true"
      />
      <div
        v-else-if="showSilhouettes"
        class="batter-silhouette left placeholder"
        aria-hidden="true"
      ></div>
      <div class="course-grid-wrapper">
        <div class="zone-axis-label zone-axis-top">↑ 高め</div>
        <div class="course-grid">
          <div
            v-for="zone in courseStats"
            :key="zone.zone"
            class="course-cell"
            :class="{ 'strike-zone': isStrikeZone(zone.zone), 'ball-zone': !isStrikeZone(zone.zone) }"
            :style="{
              backgroundColor: getCourseColor(zone.avg),
              boxShadow: getStrikeZoneShadow(zone.zone),
            }"
          >
            <span class="course-avg">{{ zone.avg !== null ? formatAvg(zone.avg) : '-' }}</span>
            <span v-if="zone.ab > 0" class="course-count">{{ zone.h }}/{{ zone.ab }}</span>
          </div>
        </div>
        <div class="zone-axis-label zone-axis-bottom">↓ 低め</div>
      </div>
      <img
        v-if="showSilhouettes && showRightSilhouette"
        class="batter-silhouette right"
        :src="batterImageRight"
        alt=""
        aria-hidden="true"
      />
      <div
        v-else-if="showSilhouettes"
        class="batter-silhouette right placeholder"
        aria-hidden="true"
      ></div>
    </div>
    <div class="zone-legend">
      <span class="legend-item">
        <span class="legend-strike-marker"></span>
        <span>太い枠の内側 = ストライクゾーン</span>
      </span>
      <span class="legend-item">
        <span class="legend-color legend-color-hot"></span>{{ hotLabel }}
        <span class="legend-color legend-color-mid"></span>平均
        <span class="legend-color legend-color-cold"></span>{{ coldLabel }}
      </span>
    </div>
  </div>
</template>

<script>
import batterImageLeftDefault from "@/assets/batter-silhouette-b.png";
import batterImageRightDefault from "@/assets/batter-silhouette-a.png";

export default {
  name: "ZoneHeatmap",
  props: {
    // 25要素のゾーン別集計データ。各要素は { zone, ab, h, avg }
    courseStats: {
      type: Array,
      required: true,
    },
    // セクションタイトル（例: 「ゾーン別対戦成績」「ゾーン別被打率」）
    title: {
      type: String,
      default: "ゾーン別成績",
    },
    // 視点ラベル（例: 「打者目線」「投手目線」）
    subtitle: {
      type: String,
      default: "打者目線",
    },
    // 年度（バッジ表示）。未指定なら現在年
    year: {
      type: [Number, String],
      default: () => new Date().getFullYear(),
    },
    // n=表示用サンプル数
    sampleSize: {
      type: Number,
      default: 0,
    },
    // 打席（"0"=右打, "1"=左打, "2"=両打）。シルエット制御用
    batterHanded: {
      type: String,
      default: null,
    },
    // シルエット画像を表示するか（投手ページ等ではfalseにする）
    showSilhouettes: {
      type: Boolean,
      default: true,
    },
    // 凡例ラベル: 打者なら「得意/苦手」、投手なら「ピンチ/抑え」
    hotLabel: {
      type: String,
      default: "得意",
    },
    coldLabel: {
      type: String,
      default: "苦手",
    },
    // シルエット画像差し替え
    batterImageLeft: {
      type: String,
      default: () => batterImageLeftDefault,
    },
    batterImageRight: {
      type: String,
      default: () => batterImageRightDefault,
    },
  },
  computed: {
    showLeftSilhouette() {
      return this.batterHanded === "1" || this.batterHanded === "2";
    },
    showRightSilhouette() {
      return this.batterHanded === "0" || this.batterHanded === "2";
    },
  },
  methods: {
    formatAvg(val) {
      if (val === null || val === undefined) return "-";
      const num = Number(val);
      if (isNaN(num)) return "-";
      if (num === 0) return ".000";
      if (num >= 1) return num.toFixed(3);
      return "." + num.toFixed(3).split(".")[1];
    },
    getCourseColor(avg) {
      if (avg === null || avg === undefined) return "#f5f5f5";
      const val = Number(avg);
      if (val >= 0.4) return "rgba(220, 38, 38, 0.5)";
      if (val >= 0.3) return "rgba(220, 38, 38, 0.3)";
      if (val >= 0.25) return "rgba(220, 38, 38, 0.15)";
      if (val >= 0.15) return "rgba(59, 130, 246, 0.1)";
      if (val >= 0.05) return "rgba(59, 130, 246, 0.25)";
      return "rgba(59, 130, 246, 0.4)";
    },
    isStrikeZone(zone) {
      return [7, 8, 9, 12, 13, 14, 17, 18, 19].includes(zone);
    },
    // 5x5 のうち中央 3x3 (ストライクゾーン) の外周だけ太枠で囲う
    getStrikeZoneShadow(zone) {
      const color = "#1a1a2e";
      const w = 3;
      const shadows = [];
      if ([7, 8, 9].includes(zone)) shadows.push(`inset 0 ${w}px 0 ${color}`);
      if ([17, 18, 19].includes(zone)) shadows.push(`inset 0 -${w}px 0 ${color}`);
      if ([7, 12, 17].includes(zone)) shadows.push(`inset ${w}px 0 0 ${color}`);
      if ([9, 14, 19].includes(zone)) shadows.push(`inset -${w}px 0 0 ${color}`);
      return shadows.length ? shadows.join(", ") : undefined;
    },
  },
};
</script>

<style scoped>
.zone-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #6b7280;
  margin: 0 0 12px 0;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.section-sub {
  font-weight: 400;
  font-size: 11px;
  color: #9ca3af;
  text-transform: none;
  letter-spacing: normal;
}

.year-badge {
  display: inline-block;
  padding: 3px 10px;
  margin: 0 6px 0 4px;
  background: #2563eb;
  color: #ffffff;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-transform: none;
  vertical-align: middle;
}

.course-with-silhouettes {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.batter-silhouette {
  flex: 0 0 auto;
  width: 100px;
  height: auto;
  opacity: 0.85;
  mix-blend-mode: multiply;
  pointer-events: none;
  user-select: none;
}

.batter-silhouette.placeholder {
  visibility: hidden;
}

.course-grid-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.zone-axis-label {
  font-size: 11px;
  font-weight: 700;
  color: #6b7280;
  letter-spacing: 0.05em;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  width: 280px;
  flex-shrink: 0;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  overflow: hidden;
}

@media (max-width: 640px) {
  .batter-silhouette {
    width: 56px;
    opacity: 0.85;
  }
  .course-with-silhouettes {
    gap: 4px;
  }
  .course-grid {
    width: auto;
    flex: 1 1 auto;
    max-width: 280px;
  }
  .course-cell {
    padding: 2px 1px;
  }
  .course-avg {
    font-size: 12px;
  }
  .course-count {
    font-size: 9px;
    margin-top: 1px;
  }
}

.course-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 2px;
  aspect-ratio: 1 / 1;
  transition: background-color 0.2s;
}

.course-cell.ball-zone {
  filter: saturate(0.75);
}

.course-avg {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.course-count {
  font-size: 10px;
  color: #6b7280;
  margin-top: 2px;
}

.zone-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
  margin-top: 10px;
  padding: 8px 12px;
  font-size: 12px;
  color: #4b5563;
  background: #f9fafb;
  border-radius: 6px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-strike-marker {
  display: inline-block;
  width: 18px;
  height: 14px;
  border: 3px solid #1a1a2e;
  border-radius: 2px;
  background: #ffffff;
}

.legend-color {
  display: inline-block;
  width: 18px;
  height: 14px;
  border-radius: 2px;
  border: 1px solid #d1d5db;
}

.legend-color-hot {
  background: rgba(220, 38, 38, 0.4);
}
.legend-color-mid {
  background: rgba(220, 38, 38, 0.15);
}
.legend-color-cold {
  background: rgba(59, 130, 246, 0.4);
}
</style>
