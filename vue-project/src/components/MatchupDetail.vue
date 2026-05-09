<template>
  <div v-if="detail" class="matchup-detail">
    <!-- ヘッダー -->
    <div class="detail-header">
      <button class="back-button" @click="$emit('close')">← 対戦一覧に戻る</button>
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
              <a v-if="info.pitcherNpbUrl" :href="info.pitcherNpbUrl" target="_blank" class="player-name">{{ info.pitcherNm }}</a>
              <span v-else class="player-name">{{ info.pitcherNm }}</span>
              <span class="player-team">{{ info.pitcherTeamNm }}</span>
            </div>
          </div>
          <div class="player-card batter-card">
            <span class="player-position">B</span>
            <div class="player-info">
              <a v-if="info.batterNpbUrl" :href="info.batterNpbUrl" target="_blank" class="player-name">{{ info.batterNm }}</a>
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
        <!-- ゾーン別対戦成績 -->
        <div class="course-section">
          <h3 class="section-title">ゾーン別対戦成績 <span class="section-sub">打者目線 · 5×5コース別打率 (n={{ summary.ab }})</span></h3>
          <div class="course-with-silhouettes">
            <svg class="batter-silhouette left" viewBox="0 0 100 240" aria-hidden="true" focusable="false">
              <g fill="currentColor">
                <ellipse cx="52" cy="24" rx="13" ry="15"/>
                <path d="M44 26 Q40 30 44 34 Q54 34 60 30 L56 22 Z"/>
                <path d="M38 40 Q52 36 64 42 L62 80 Q60 100 58 118 L36 118 Q34 100 34 82 Z"/>
                <path d="M34 118 L62 118 L66 142 L28 142 Z"/>
                <path d="M30 142 Q24 175 20 210 L16 232 L32 232 L36 200 Q40 165 42 142 Z"/>
                <path d="M55 142 Q62 175 70 200 L78 232 L62 232 L56 210 Q50 175 47 142 Z"/>
                <path d="M40 48 Q26 56 20 72 Q18 82 24 82 Q34 76 42 64 Q44 56 42 50 Z"/>
                <path d="M60 48 Q72 58 76 74 Q76 84 70 82 Q60 76 54 64 Q52 56 56 50 Z"/>
                <rect x="60" y="-8" width="7" height="78" rx="3" transform="rotate(-22 63 31)"/>
              </g>
            </svg>
            <div class="course-grid">
              <div
                v-for="zone in courseStats"
                :key="zone.zone"
                class="course-cell"
                :style="{ backgroundColor: getCourseColor(zone.avg) }"
                :class="{ 'strike-zone': isStrikeZone(zone.zone) }"
              >
                <span class="course-avg">{{ zone.avg !== null ? formatAvg(zone.avg) : '-' }}</span>
                <span v-if="zone.ab > 0" class="course-count">{{ zone.h }}/{{ zone.ab }}</span>
              </div>
            </div>
            <svg class="batter-silhouette right" viewBox="0 0 100 240" aria-hidden="true" focusable="false">
              <g fill="currentColor">
                <ellipse cx="52" cy="24" rx="13" ry="15"/>
                <path d="M44 26 Q40 30 44 34 Q54 34 60 30 L56 22 Z"/>
                <path d="M38 40 Q52 36 64 42 L62 80 Q60 100 58 118 L36 118 Q34 100 34 82 Z"/>
                <path d="M34 118 L62 118 L66 142 L28 142 Z"/>
                <path d="M30 142 Q24 175 20 210 L16 232 L32 232 L36 200 Q40 165 42 142 Z"/>
                <path d="M55 142 Q62 175 70 200 L78 232 L62 232 L56 210 Q50 175 47 142 Z"/>
                <path d="M40 48 Q26 56 20 72 Q18 82 24 82 Q34 76 42 64 Q44 56 42 50 Z"/>
                <path d="M60 48 Q72 58 76 74 Q76 84 70 82 Q60 76 54 64 Q52 56 56 50 Z"/>
                <rect x="60" y="-8" width="7" height="78" rx="3" transform="rotate(-22 63 31)"/>
              </g>
            </svg>
          </div>
        </div>

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
export default {
  name: "MatchupDetail",
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
      const strikeZones = [7, 8, 9, 12, 13, 14, 17, 18, 19];
      return strikeZones.includes(zone);
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
  width: 64px;
  height: auto;
  color: #9ca3af;
  opacity: 0.55;
  pointer-events: none;
}

.batter-silhouette.right {
  transform: scaleX(-1);
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  width: 320px;
  flex-shrink: 0;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  overflow: hidden;
}

@media (max-width: 640px) {
  .batter-silhouette {
    width: 40px;
    opacity: 0.4;
  }
  .course-with-silhouettes {
    gap: 4px;
  }
  .course-grid {
    width: auto;
    flex: 1 1 auto;
    max-width: 320px;
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

.course-cell.strike-zone {
  border: 1px solid rgba(0, 0, 0, 0.08);
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
