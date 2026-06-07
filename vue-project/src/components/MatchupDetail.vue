<template>
  <div v-if="detail" class="matchup-detail">
    <!-- ヘッダー -->
    <div class="detail-header">
      <button class="back-button" @click="$emit('close')">← 検索に戻る</button>
      <h2 class="detail-title">{{ info.pitcherNm }} vs {{ info.batterNm }} 対戦成績</h2>
    </div>

    <div class="detail-layout">
      <!-- 左サイドバー: HEAD-TO-HEAD -->
      <aside class="detail-sidebar-left">
        <div class="head-to-head">
          <h3 class="section-title">HEAD-TO-HEAD</h3>
          <div class="player-card pitcher-card">
            <span class="player-position">P</span>
            <div class="player-info">
              <router-link
                v-if="info.pitcherId"
                :to="`/players/${info.pitcherId}`"
                class="player-name"
              >{{ info.pitcherNm }}</router-link>
              <span v-else class="player-name">{{ info.pitcherNm }}</span>
              <span class="player-team">{{ info.pitcherTeamNm }}</span>
            </div>
          </div>
          <div class="player-card batter-card">
            <span class="player-position">B</span>
            <div class="player-info">
              <router-link
                v-if="info.batterId"
                :to="`/players/${info.batterId}`"
                class="player-name"
              >{{ info.batterNm }}</router-link>
              <span v-else class="player-name">{{ info.batterNm }}</span>
              <span class="player-team">{{ info.batterTeamNm }}</span>
            </div>
          </div>
        </div>

        <!-- SUMMARY -->
        <div class="summary-section">
          <h3 class="section-title">SUMMARY</h3>
          <div class="summary-grid">
            <div class="summary-row">
              <span class="summary-label">打席 PA</span>
              <span class="summary-value">{{ summary.pa }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">打数 AB</span>
              <span class="summary-value">{{ summary.ab }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">安打 H</span>
              <span class="summary-value">{{ summary.h }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">二塁打 2B</span>
              <span class="summary-value">{{ summary.doubles }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">本塁打 HR</span>
              <span class="summary-value">{{ summary.hr }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">三振 SO</span>
              <span class="summary-value">{{ summary.so }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">四球 BB</span>
              <span class="summary-value">{{ summary.bb }}</span>
            </div>
            <div class="summary-row highlight">
              <span class="summary-label">打率 BA</span>
              <span class="summary-value">{{ formatAvg(summary.ba) }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">出塁率 OBP</span>
              <span class="summary-value">{{ formatAvg(summary.obp) }}</span>
            </div>
            <div class="summary-row">
              <span class="summary-label">長打率 SLG</span>
              <span class="summary-value">{{ formatAvg(summary.slg) }}</span>
            </div>
            <div class="summary-row highlight">
              <span class="summary-label">OPS</span>
              <span class="summary-value">{{ formatAvg(summary.ops) }}</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- メインコンテンツ -->
      <div class="detail-main">
        <!-- ゾーン別対戦成績（共通コンポーネント） -->
        <ZoneHeatmap
          title="ゾーン別対戦成績"
          subtitle="打者目線"
          :course-stats="courseStats"
          :sample-size="summary.ab"
          :batter-handed="info.batterHanded"
        />

        <!-- 球種別 -->
        <div class="pitch-type-section">
          <h3 class="section-title">球種別</h3>
          <div class="pitch-type-list">
            <div v-for="pt in pitchTypeStats" :key="pt.pitchType" class="pitch-type-row">
              <span class="pt-name">{{ pt.pitchType }}</span>
              <div class="pt-bar-container">
                <div class="pt-bar" :style="{ width: getPitchTypeBarWidth(pt.ab) + '%' }"></div>
              </div>
              <span class="pt-count">{{ pt.ab }}</span>
              <span class="pt-avg">{{ formatAvg(pt.avg) }}</span>
            </div>
          </div>
        </div>

        <!-- 打席ログ -->
        <div class="at-bat-log-section">
          <h3 class="section-title">打席ログ <span class="section-sub">n={{ summary.pa }} · 直近{{ Math.min(atBatLog.length, 20) }}件</span></h3>
          <table class="log-table">
            <thead>
              <tr>
                <th>日付</th>
                <th>結果</th>
                <th>内容</th>
                <th>配球</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(log, idx) in displayedLog" :key="idx">
                <td class="log-date">{{ log.date }}</td>
                <td class="log-result">
                  <span :class="getResultClass(log.result)">{{ getResultAbbr(log.result) }}</span>
                </td>
                <td class="log-detail">{{ log.result }}</td>
                <td class="log-pitch">
                  <span v-if="log.lastPitchType">{{ log.lastPitchType }} {{ log.lastPitchSpeed }}km</span>
                  <span v-else>-</span>
                </td>
              </tr>
            </tbody>
          </table>
          <button v-if="atBatLog.length > 20 && !showAllLog" class="show-more-btn" @click="showAllLog = true">
            すべて表示 ({{ atBatLog.length }}件)
          </button>
        </div>
      </div>

      <!-- 右サイドバー -->
      <aside class="detail-sidebar-right">
        <!-- 年度別 -->
        <div class="yearly-section">
          <h3 class="section-title">年度別</h3>
          <table class="yearly-table">
            <thead>
              <tr>
                <th></th>
                <th>PA</th>
                <th>AB</th>
                <th>H</th>
                <th>HR</th>
                <th>SO</th>
                <th>BB</th>
                <th>BA</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="y in yearlyStats" :key="y.year">
                <td class="year-label">{{ y.year }}</td>
                <td>{{ y.pa }}</td>
                <td>{{ y.ab }}</td>
                <td>{{ y.h }}</td>
                <td>{{ y.hr }}</td>
                <td>{{ y.so }}</td>
                <td>{{ y.bb }}</td>
                <td class="year-ba">{{ formatAvg(y.ba) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- SPLITS -->
        <div class="splits-section">
          <h3 class="section-title">SPLITS</h3>
          <div class="splits-grid">
            <div class="split-row">
              <span class="split-label">ホーム</span>
              <span class="split-value">{{ splits.home ? splits.home.summary : '-' }}</span>
            </div>
            <div class="split-row">
              <span class="split-label">ビジター</span>
              <span class="split-value">{{ splits.away ? splits.away.summary : '-' }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script>
import ZoneHeatmap from "./ZoneHeatmap.vue";

export default {
  name: "MatchupDetail",
  components: {
    ZoneHeatmap,
  },
  props: {
    detail: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      showAllLog: false,
    };
  },
  computed: {
    info() {
      return this.detail?.playerInfo || {};
    },
    summary() {
      return this.detail?.summary || {};
    },
    courseStats() {
      return this.detail?.courseStats || [];
    },
    pitchTypeStats() {
      return this.detail?.pitchTypeStats || [];
    },
    atBatLog() {
      return this.detail?.atBatLog || [];
    },
    yearlyStats() {
      return this.detail?.yearlyStats || [];
    },
    splits() {
      return this.detail?.splits || {};
    },
    displayedLog() {
      return this.showAllLog ? this.atBatLog : this.atBatLog.slice(0, 20);
    },
    maxPitchTypeAb() {
      if (!this.pitchTypeStats.length) return 1;
      return Math.max(...this.pitchTypeStats.map((pt) => pt.ab));
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
    getPitchTypeBarWidth(ab) {
      return (ab / this.maxPitchTypeAb) * 100;
    },
    getResultClass(result) {
      if (!result) return "";
      if (result.includes("本")) return "result-hr";
      if (result.includes("安") || result.includes("２") || result.includes("３")) return "result-hit";
      if (result.includes("四球") || result.includes("死球")) return "result-bb";
      if (result.includes("三振")) return "result-so";
      return "result-out";
    },
    getResultAbbr(result) {
      if (!result) return "-";
      if (result.includes("本")) return "HR";
      if (result.includes("３")) return "3B";
      if (result.includes("２")) return "2B";
      if (result.includes("安")) return "H";
      if (result.includes("四球")) return "BB";
      if (result.includes("敬遠")) return "IBB";
      if (result.includes("死球")) return "HBP";
      if (result.includes("三振")) return "SO";
      if (result.includes("犠飛")) return "SF";
      if (result.includes("犠打")) return "SH";
      if (result.includes("併殺")) return "DP";
      return "OUT";
    },
  },
};
</script>

<style scoped>
.matchup-detail {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1a1a2e;
  text-align: left;
}

/* ヘッダー */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.back-button {
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 14px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: all 0.2s;
}

.back-button:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

/* レイアウト */
.detail-layout {
  display: grid;
  grid-template-columns: 240px 1fr 260px;
  gap: 20px;
}

/* セクションタイトル */
.section-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #6b7280;
  margin: 0 0 12px 0;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}

.section-sub {
  font-weight: 400;
  font-size: 11px;
  color: #9ca3af;
  text-transform: none;
  letter-spacing: normal;
}

/* HEAD-TO-HEAD */
.head-to-head {
  margin-bottom: 20px;
}

.player-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #f9fafb;
}

.player-position {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  color: white;
}

.pitcher-card .player-position {
  background: #2563eb;
}

.batter-card .player-position {
  background: #f59e0b;
}

.player-info {
  display: flex;
  flex-direction: column;
}

.player-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  text-decoration: none;
}

.player-name:hover {
  text-decoration: underline;
}

.player-team {
  font-size: 11px;
  color: #6b7280;
}

/* SUMMARY */
.summary-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 8px;
  font-size: 13px;
  border-radius: 4px;
}

.summary-row.highlight {
  background: #f3f4f6;
  font-weight: 600;
}

.summary-label {
  color: #6b7280;
}

.summary-value {
  font-weight: 600;
  color: #111827;
}

/* ゾーン別対戦成績 */
.course-section {
  margin-bottom: 24px;
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
    width: 70px;
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
}

.course-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
  min-height: 52px;
  transition: background-color 0.2s;
}

.course-cell.ball-zone {
  /* ボールゾーンはわずかに彩度を落として、ストライクとの差を出す */
  filter: saturate(0.75);
}

/* 年度バッジ (例: 2026年シーズン) */
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

/* グリッドを囲う wrapper (上下に高め/低めラベルを置くため) */
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

/* 凡例 */
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

/* 球種別 */
.pitch-type-section {
  margin-bottom: 24px;
}

.pitch-type-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pitch-type-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pt-name {
  width: 100px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  flex-shrink: 0;
}

.pt-bar-container {
  flex: 1;
  height: 20px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.pt-bar {
  height: 100%;
  background: linear-gradient(90deg, #dc2626, #ef4444);
  border-radius: 4px;
  min-width: 2px;
  transition: width 0.3s ease;
}

.pt-count {
  width: 30px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.pt-avg {
  width: 45px;
  text-align: right;
  font-size: 13px;
  color: #6b7280;
}

/* 打席ログ */
.at-bat-log-section {
  margin-bottom: 24px;
}

.log-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.log-table thead th {
  text-align: left;
  padding: 8px 10px;
  background: #f9fafb;
  color: #6b7280;
  font-weight: 600;
  font-size: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.log-table tbody tr {
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.15s;
}

.log-table tbody tr:hover {
  background: #f9fafb;
}

.log-table td {
  padding: 8px 10px;
}

.log-date {
  color: #6b7280;
  white-space: nowrap;
}

.log-result span {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
}

.result-hr {
  background: #fef2f2;
  color: #dc2626;
}

.result-hit {
  background: #fef3c7;
  color: #d97706;
}

.result-bb {
  background: #eff6ff;
  color: #2563eb;
}

.result-so {
  background: #f3f4f6;
  color: #6b7280;
}

.result-out {
  background: #f9fafb;
  color: #9ca3af;
}

.log-pitch {
  color: #6b7280;
  font-size: 12px;
}

.show-more-btn {
  display: block;
  width: 100%;
  padding: 8px;
  margin-top: 8px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #374151;
  transition: background 0.2s;
}

.show-more-btn:hover {
  background: #e5e7eb;
}

/* 年度別 */
.yearly-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.yearly-table thead th {
  padding: 6px 6px;
  text-align: center;
  color: #6b7280;
  font-weight: 600;
  border-bottom: 2px solid #e5e7eb;
}

.yearly-table tbody td {
  padding: 6px 6px;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
}

.year-label {
  font-weight: 600;
  color: #374151;
}

.year-ba {
  font-weight: 700;
  color: #111827;
}

/* SPLITS */
.splits-section {
  margin-top: 20px;
}

.splits-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.split-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  font-size: 13px;
  background: #f9fafb;
  border-radius: 4px;
}

.split-label {
  color: #6b7280;
  font-weight: 500;
}

.split-value {
  font-weight: 600;
  color: #111827;
  font-size: 12px;
}

/* レスポンシブ */
@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-sidebar-left {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .course-grid {
    max-width: 280px;
  }
}

@media (max-width: 640px) {
  .matchup-detail {
    padding: 8px;
  }

  .detail-sidebar-left {
    grid-template-columns: 1fr;
  }

  .course-grid {
    max-width: 100%;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
