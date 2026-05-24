<template>
  <div class="player-profile-page">
    <!-- ローディング -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner"></div>
        <p class="loading-text">読み込み中...</p>
      </div>
    </div>

    <!-- エラー -->
    <div v-else-if="errorMessage" class="error-card">
      <p>{{ errorMessage }}</p>
      <router-link to="/" class="back-link">← トップに戻る</router-link>
    </div>

    <!-- 本体 -->
    <div v-else-if="profile" class="profile-content">
      <!-- パンくず -->
      <nav class="breadcrumb" aria-label="パンくず">
        <router-link to="/">ホーム</router-link>
        <span class="breadcrumb-sep">›</span>
        <span>選手</span>
        <span class="breadcrumb-sep">›</span>
        <span class="breadcrumb-current">{{ playerInfo.playerNm }}</span>
      </nav>

      <!-- ヘッダー -->
      <header class="player-header">
        <div class="player-header-main">
          <h1 class="player-name">{{ playerInfo.playerNm }}</h1>
          <div class="player-meta">
            <span class="meta-team">{{ playerInfo.teamShortNm || playerInfo.teamNm || "—" }}</span>
            <span class="meta-divider">·</span>
            <span class="meta-role" :class="isPitcher ? 'meta-role-pitcher' : 'meta-role-batter'">
              {{ isPitcher ? "投手" : "野手" }}
            </span>
            <span v-if="handedLabel" class="meta-divider">·</span>
            <span v-if="handedLabel" class="meta-handed">{{ handedLabel }}</span>
            <span v-if="playerInfo.npbUrl" class="meta-divider">·</span>
            <a v-if="playerInfo.npbUrl" :href="playerInfo.npbUrl" target="_blank" rel="noopener noreferrer" class="meta-npb-link">
              NPB公式 ↗
            </a>
          </div>
        </div>
        <div class="player-header-cta">
          <span class="role-badge" :class="isPitcher ? 'badge-pitcher' : 'badge-batter'">
            {{ isPitcher ? "投手ページ（被打率視点）" : "打者ページ（打率視点）" }}
          </span>
        </div>
      </header>

      <!-- データなし -->
      <div v-if="!profile.hasData" class="no-data">
        <p>{{ year }}年シーズンの対戦データはまだありません。</p>
        <p class="no-data-sub">シーズン中は毎日10:30 JSTに最新データを取り込みます。</p>
      </div>

      <!-- データあり -->
      <div v-else class="profile-layout">
        <!-- 左サイドバー: SUMMARY -->
        <aside class="profile-sidebar-left">
          <div class="summary-section">
            <h3 class="section-title">SUMMARY <span class="section-sub">{{ year }}年</span></h3>
            <div class="summary-grid">
              <div class="summary-row">
                <span class="summary-label">打席 PA</span>
                <span class="summary-value">{{ summary.pa }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "対戦打数 AB" : "打数 AB" }}</span>
                <span class="summary-value">{{ summary.ab }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "被安打 H" : "安打 H" }}</span>
                <span class="summary-value">{{ summary.h }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "被本塁打" : "本塁打 HR" }}</span>
                <span class="summary-value">{{ summary.hr }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "奪三振 SO" : "三振 SO" }}</span>
                <span class="summary-value">{{ summary.so }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "与四球 BB" : "四球 BB" }}</span>
                <span class="summary-value">{{ summary.bb }}</span>
              </div>
              <div class="summary-row highlight">
                <span class="summary-label">{{ isPitcher ? "被打率 BA" : "打率 BA" }}</span>
                <span class="summary-value">{{ formatAvg(summary.ba) }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "被OBP" : "OBP" }}</span>
                <span class="summary-value">{{ formatAvg(summary.obp) }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label">{{ isPitcher ? "被SLG" : "SLG" }}</span>
                <span class="summary-value">{{ formatAvg(summary.slg) }}</span>
              </div>
              <div class="summary-row highlight">
                <span class="summary-label">{{ isPitcher ? "被OPS" : "OPS" }}</span>
                <span class="summary-value">{{ formatAvg(summary.ops) }}</span>
              </div>
            </div>
          </div>

          <!-- SPLITS -->
          <div class="splits-section">
            <h3 class="section-title">SPLITS（ホーム/ビジター）</h3>
            <div class="splits-grid">
              <div class="split-row">
                <span class="split-label">ホーム</span>
                <span class="split-value">{{ splits.home ? splits.home.summary : "-" }}</span>
              </div>
              <div class="split-row">
                <span class="split-label">ビジター</span>
                <span class="split-value">{{ splits.away ? splits.away.summary : "-" }}</span>
              </div>
            </div>
          </div>
        </aside>

        <!-- メイン -->
        <div class="profile-main">
          <!-- ゾーンヒートマップ -->
          <ZoneHeatmap
            :title="isPitcher ? 'ゾーン別被打率' : 'ゾーン別打率'"
            :subtitle="isPitcher ? '投手目線（赤=被打率高=ピンチゾーン）' : '打者目線（赤=打率高=得意ゾーン）'"
            :course-stats="courseStats"
            :sample-size="summary.ab || 0"
            :batter-handed="isPitcher ? null : playerInfo.handed"
            :show-silhouettes="!isPitcher"
            :hot-label="isPitcher ? 'ピンチ' : '得意'"
            :cold-label="isPitcher ? '抑え' : '苦手'"
            :year="year"
          />

          <!-- 球種別 -->
          <div v-if="pitchTypeStats.length" class="pitch-type-section">
            <h3 class="section-title">球種別 <span class="section-sub">{{ isPitcher ? "被打率" : "打率" }}</span></h3>
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
        </div>

        <!-- 右サイドバー: TOP/WORST -->
        <aside class="profile-sidebar-right">
          <div class="opponents-section">
            <h3 class="section-title">
              {{ isPitcher ? "抑えてる打者 TOP5" : "得意な投手 TOP5" }}
              <span class="section-sub">{{ isPitcher ? "被打率低い順" : "打率高い順" }}</span>
            </h3>
            <div v-if="topOpponents.length === 0" class="empty-note">PA≥3の対戦データなし</div>
            <table v-else class="opponents-table">
              <thead>
                <tr>
                  <th>選手</th>
                  <th>PA</th>
                  <th>BA</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="opp in topOpponents" :key="opp.opponentId">
                  <td class="opp-name">
                    <router-link :to="`/players/${opp.opponentId}`">{{ opp.opponentNm }}</router-link>
                    <small>{{ opp.opponentTeamShortNm }}</small>
                  </td>
                  <td class="opp-pa">{{ opp.pa }}</td>
                  <td class="opp-avg">{{ formatAvg(opp.avg) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="opponents-section">
            <h3 class="section-title">
              {{ isPitcher ? "苦手な打者 WORST5" : "苦手な投手 WORST5" }}
              <span class="section-sub">{{ isPitcher ? "被打率高い順" : "打率低い順" }}</span>
            </h3>
            <div v-if="worstOpponents.length === 0" class="empty-note">PA≥3の対戦データなし</div>
            <table v-else class="opponents-table">
              <thead>
                <tr>
                  <th>選手</th>
                  <th>PA</th>
                  <th>BA</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="opp in worstOpponents" :key="opp.opponentId">
                  <td class="opp-name">
                    <router-link :to="`/players/${opp.opponentId}`">{{ opp.opponentNm }}</router-link>
                    <small>{{ opp.opponentTeamShortNm }}</small>
                  </td>
                  <td class="opp-pa">{{ opp.pa }}</td>
                  <td class="opp-avg">{{ formatAvg(opp.avg) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import ZoneHeatmap from "../components/ZoneHeatmap.vue";

export default {
  name: "PlayerProfileView",
  components: {
    ZoneHeatmap,
  },
  props: {
    playerId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      profile: null,
      isLoading: false,
      errorMessage: "",
      year: new Date().getFullYear(),
    };
  },
  computed: {
    playerInfo() {
      return this.profile?.playerInfo || {};
    },
    isPitcher() {
      return this.profile?.isPitcher === true;
    },
    summary() {
      return this.profile?.summary || {};
    },
    courseStats() {
      return this.profile?.courseStats || [];
    },
    pitchTypeStats() {
      return this.profile?.pitchTypeStats || [];
    },
    splits() {
      return this.profile?.splits || {};
    },
    topOpponents() {
      return this.profile?.topOpponents || [];
    },
    worstOpponents() {
      return this.profile?.worstOpponents || [];
    },
    maxPitchTypeAb() {
      if (!this.pitchTypeStats.length) return 1;
      return Math.max(...this.pitchTypeStats.map((pt) => pt.ab));
    },
    handedLabel() {
      const h = this.playerInfo.handed;
      if (!h) return null;
      if (this.isPitcher) {
        // 投手の場合 thrower 表示が望ましい（投げる手）
        const t = this.playerInfo.thrower;
        if (t === "0") return "右投";
        if (t === "1") return "左投";
        if (t === "2") return "両投";
        return null;
      }
      if (h === "0") return "右打";
      if (h === "1") return "左打";
      if (h === "2") return "両打";
      return null;
    },
  },
  watch: {
    playerId: {
      immediate: true,
      handler() {
        this.fetchProfile();
      },
    },
  },
  methods: {
    async fetchProfile() {
      this.isLoading = true;
      this.errorMessage = "";
      this.profile = null;
      try {
        const response = await this.$axios.get(`/playerProfile/${this.playerId}`, {
          params: { year: this.year },
        });
        if (response.status === 200) {
          this.profile = response.data.data.playerProfile;
          this.updateMeta();
        }
      } catch (error) {
        if (error.response && error.response.status === 404) {
          this.errorMessage = "選手が見つかりませんでした。";
        } else {
          const msg = (error.response && error.response.data && error.response.data.message) || error.message;
          this.errorMessage = msg || "プロフィールの取得に失敗しました";
        }
      } finally {
        this.isLoading = false;
      }
    },
    updateMeta() {
      const info = this.playerInfo;
      const team = info.teamShortNm || info.teamNm || "";
      const roleLabel = this.isPitcher ? "被打率" : "打率";
      const baLabel = this.summary.ba != null ? this.formatAvg(this.summary.ba) : "—";

      // タイトル
      const title = `${info.playerNm}（${team}）${roleLabel}・ゾーン別成績 ${this.year}年 | Pitcher-vs-Batter`;
      document.title = title;

      // description
      const desc = `${team}・${info.playerNm}の${this.year}年シーズン${roleLabel}${baLabel}、対戦相手別・5×5ゾーン別の成績を可視化。投手vs打者の個人対戦データ。`;
      this.upsertMeta("description", desc);

      // canonical
      this.upsertCanonical(`https://baseball-pitcher-vs-batter.com/players/${this.playerId}`);

      // OGP
      this.upsertMeta("og:title", title, "property");
      this.upsertMeta("og:description", desc, "property");
      this.upsertMeta("og:url", `https://baseball-pitcher-vs-batter.com/players/${this.playerId}`, "property");
    },
    upsertMeta(name, content, attr = "name") {
      let el = document.querySelector(`meta[${attr}="${name}"]`);
      if (!el) {
        el = document.createElement("meta");
        el.setAttribute(attr, name);
        document.head.appendChild(el);
      }
      el.setAttribute("content", content);
    },
    upsertCanonical(url) {
      let el = document.querySelector('link[rel="canonical"]');
      if (!el) {
        el = document.createElement("link");
        el.setAttribute("rel", "canonical");
        document.head.appendChild(el);
      }
      el.setAttribute("href", url);
    },
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
  },
};
</script>

<style scoped>
.player-profile-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1a1a2e;
  text-align: left;
}

.loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}
.loading-text {
  margin: 0;
  font-size: 1rem;
  color: #0056b3;
  font-weight: bold;
}
.spinner {
  border: 8px solid #f3f3f3;
  border-top: 8px solid #3498db;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-card {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}
.back-link {
  display: inline-block;
  margin-top: 16px;
  color: #2563eb;
  text-decoration: none;
}
.back-link:hover {
  text-decoration: underline;
}

/* パンくず */
.breadcrumb {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 16px;
}
.breadcrumb a {
  color: #2563eb;
  text-decoration: none;
}
.breadcrumb a:hover {
  text-decoration: underline;
}
.breadcrumb-sep {
  margin: 0 6px;
  color: #d1d5db;
}
.breadcrumb-current {
  color: #1a1a2e;
  font-weight: 600;
}

/* ヘッダー */
.player-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 16px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e5e7eb;
  flex-wrap: wrap;
}
.player-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 6px 0;
  color: #111827;
}
.player-meta {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
}
.meta-team {
  font-weight: 600;
  color: #2563eb;
}
.meta-divider {
  margin: 0 8px;
  color: #d1d5db;
}
.meta-role {
  font-weight: 600;
}
.meta-role-pitcher {
  color: #2563eb;
}
.meta-role-batter {
  color: #f59e0b;
}
.meta-npb-link {
  color: #2563eb;
  text-decoration: none;
}
.meta-npb-link:hover {
  text-decoration: underline;
}
.role-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}
.badge-pitcher {
  background: #2563eb;
}
.badge-batter {
  background: #f59e0b;
}

/* データなし */
.no-data {
  text-align: center;
  padding: 60px 20px;
  background: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
}
.no-data-sub {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

/* レイアウト */
.profile-layout {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 20px;
}

/* セクション共通 */
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

/* 対戦相手TOP/WORST */
.opponents-section {
  margin-bottom: 20px;
}
.opponents-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.opponents-table thead th {
  padding: 6px;
  text-align: left;
  color: #6b7280;
  font-weight: 600;
  border-bottom: 2px solid #e5e7eb;
}
.opponents-table thead th:not(:first-child) {
  text-align: center;
}
.opponents-table tbody td {
  padding: 6px;
  border-bottom: 1px solid #f3f4f6;
}
.opp-name a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}
.opp-name a:hover {
  text-decoration: underline;
}
.opp-name small {
  display: block;
  color: #9ca3af;
  font-size: 10px;
  margin-top: 2px;
}
.opp-pa {
  text-align: center;
  color: #6b7280;
}
.opp-avg {
  text-align: center;
  font-weight: 700;
  color: #111827;
}
.empty-note {
  font-size: 12px;
  color: #9ca3af;
  padding: 12px;
  text-align: center;
  background: #f9fafb;
  border-radius: 4px;
}

/* レスポンシブ */
@media (max-width: 1024px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
  .profile-sidebar-left,
  .profile-sidebar-right {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
}

@media (max-width: 640px) {
  .player-profile-page {
    padding: 8px;
  }
  .player-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .profile-sidebar-left,
  .profile-sidebar-right {
    grid-template-columns: 1fr;
  }
}
</style>
