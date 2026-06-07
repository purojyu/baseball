<template>
  <div class="matchup-detail-page">
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner"></div>
        <p class="loading-text">読み込み中...</p>
      </div>
    </div>

    <div v-else-if="errorMessage" class="error-card">
      <p>{{ errorMessage }}</p>
      <router-link to="/" class="back-link">← 検索トップに戻る</router-link>
    </div>

    <MatchupDetail v-else-if="detail" :detail="detail" @close="goBack" />
  </div>
</template>

<script>
import MatchupDetail from "../components/MatchupDetail.vue";

export default {
  name: "MatchupDetailView",
  components: { MatchupDetail },
  props: {
    pitcherId: {
      type: [String, Number],
      required: true,
    },
    batterId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      detail: null,
      isLoading: false,
      errorMessage: "",
    };
  },
  computed: {
    // 投手ID・打者IDの組をまとめて監視するためのキー（1回のフェッチで済ませる）
    matchupKey() {
      return `${this.pitcherId}/${this.batterId}`;
    },
  },
  watch: {
    matchupKey: {
      immediate: true,
      handler() {
        this.fetchDetail();
      },
    },
  },
  methods: {
    async fetchDetail() {
      this.isLoading = true;
      this.errorMessage = "";
      this.detail = null;
      try {
        const response = await this.$axios.get("/pitchDetail", {
          params: { pitcherId: this.pitcherId, batterId: this.batterId },
        });
        if (response.status === 200) {
          this.detail = response.data.data.pitchDetail || null;
          if (!this.detail) {
            this.errorMessage = "この対戦の成績データが見つかりませんでした。";
          } else {
            this.updateMeta();
          }
        }
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = "対戦成績の取得に失敗しました。";
        }
      } finally {
        this.isLoading = false;
      }
    },
    goBack() {
      this.$router.push("/");
    },
    updateMeta() {
      const info = this.detail.playerInfo || {};
      const title = `${info.pitcherNm}（投） vs ${info.batterNm}（打）対戦成績・ゾーン別 | Pitcher-vs-Batter`;
      document.title = title;

      const desc = `${info.pitcherNm}と${info.batterNm}の対戦成績。打席結果・コース別(5×5ゾーン)・球種別の投手vs打者個人対戦データを可視化。`;
      this.upsertMeta("description", desc);
      this.upsertCanonical(`https://baseball-pitcher-vs-batter.com/matchup/${this.pitcherId}/${this.batterId}`);
      this.upsertMeta("og:title", title, "property");
      this.upsertMeta("og:description", desc, "property");
      this.upsertMeta("og:url", `https://baseball-pitcher-vs-batter.com/matchup/${this.pitcherId}/${this.batterId}`, "property");
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
  },
};
</script>

<style scoped>
.matchup-detail-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px;
  text-align: left;
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
</style>
