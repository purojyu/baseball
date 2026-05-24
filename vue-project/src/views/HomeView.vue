<template>
  <div>
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner"></div>
        <p class="loading-text">読み込み中...</p>
        <p v-if="showSlowLoadingMessage" class="loading-subtext">
          初回の読み込みには時間がかかることがあります<br>
          <small>（最大30秒程度）</small>
        </p>
      </div>
    </div>
    <main>
      <template v-if="!pitchDetail">
        <SearchBaseball
          :baseballTeamList="baseballTeamList"
          :pitcherList="pitcherList"
          :batterList="batterList"
          :years="years"
          @getPitcherList="getPitcherList"
          @getBatterList="getBatterList"
          @matchResultSearch="matchResultSearch"
        />
        <SearchResultBaseball
          :matchResultList="matchResultList"
          @showPitchDetail="showPitchDetail"
        />
        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          {{ errorMessage }}
        </div>
        <SeoContent />
      </template>
      <template v-else>
        <MatchupDetail :detail="pitchDetail" @close="pitchDetail = null" />
      </template>
    </main>
  </div>
</template>

<script>
import SearchBaseball from "../components/SearchBaseball.vue";
import SearchResultBaseball from "../components/SearchResultBaseball.vue";
import MatchupDetail from "../components/MatchupDetail.vue";
import SeoContent from "../components/SeoContent.vue";

export default {
  name: "HomeView",
  components: {
    SearchBaseball,
    SearchResultBaseball,
    MatchupDetail,
    SeoContent,
  },
  data() {
    return {
      baseballTeamList: [],
      pitcherList: [],
      batterList: [],
      matchResultList: [],
      errorMessage: "",
      years: [],
      isLoading: false,
      showSlowLoadingMessage: false,
      slowLoadingTimer: null,
      pitchDetail: null,
    };
  },
  mounted() {
    this.getInitData();
    // ホームに戻ったときに title を既定に戻す
    document.title = "プロ野球 個人対戦成績 検索｜投手VS打者 NPB対戦データ | Pitcher-vs-Batter";
  },
  watch: {
    isLoading(newVal) {
      if (newVal) {
        this.slowLoadingTimer = setTimeout(() => {
          this.showSlowLoadingMessage = true;
        }, 5000);
      } else {
        if (this.slowLoadingTimer) {
          clearTimeout(this.slowLoadingTimer);
          this.slowLoadingTimer = null;
        }
        this.showSlowLoadingMessage = false;
      }
    },
  },
  methods: {
    async getInitData() {
      this.isLoading = true;
      try {
        const response = await this.$axios.get("/getInitData");
        if (response.status === 200) {
          const responseData = response.data.data;
          this.baseballTeamList = responseData.baseballTeam || [];
          this.years = responseData.years || [];
          this.years.unshift("通算");
        }
      } catch (error) {
        console.error("getInitData error:", error);
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || "初期表示エラー";
        alert(msg);
        this.isLoading = false;
        return;
      } finally {
        this.isLoading = false;
      }
      this.getPitcherList(0, "通算");
      this.getBatterList(0, "通算");
    },
    async getPitcherList(teamId, year) {
      try {
        const response = await this.$axios.get("/getPitcherList", {
          params: { teamId: teamId, year: year },
        });
        if (response.status === 200) {
          const responseData = response.data.data;
          this.pitcherList = responseData.pitcherList || [];
        }
      } catch (error) {
        console.error("getPitcherList error:", error);
        this.pitcherList = [];
        if (error.response && error.response.status === 404) {
          return;
        }
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || "ピッチャーの取得に失敗しました";
        alert(msg);
      }
    },
    async getBatterList(teamId, year) {
      try {
        const response = await this.$axios.get("/getBatterList", {
          params: { teamId: teamId, year: year },
        });
        if (response.status === 200) {
          const responseData = response.data.data;
          this.batterList = responseData.batterList || [];
        }
      } catch (error) {
        console.error("getBatterList error:", error);
        this.batterList = [];
        if (error.response && error.response.status === 404) {
          return;
        }
        const msg = (error.response && error.response.data && error.response.data.message) || error.message || "バッターの取得に失敗しました";
        alert(msg);
      }
    },
    async matchResultSearch(pitcherTeamId, batterTeamId, pitcherId, batterId, selectedYear) {
      this.isLoading = true;
      try {
        const response = await this.$axios.get("/matchResultSearch", {
          params: {
            pitcherTeamId: pitcherTeamId,
            batterTeamId: batterTeamId,
            pitcherId: pitcherId,
            batterId: batterId,
            selectedYear: selectedYear,
          },
        });
        if (response.status === 200) {
          const responseData = response.data.data;
          this.matchResultList = responseData.matchResult || [];
          this.errorMessage = "";
        }
      } catch (error) {
        this.matchResultList = [];
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = "対戦結果の取得に失敗しました";
        }
      } finally {
        this.isLoading = false;
      }
    },
    async showPitchDetail(matchResult) {
      if (!matchResult.pitcherId || !matchResult.batterId) {
        this.errorMessage = "ゾーン別対戦成績の表示にはIDが必要です";
        return;
      }
      this.isLoading = true;
      try {
        const response = await this.$axios.get("/pitchDetail", {
          params: { pitcherId: matchResult.pitcherId, batterId: matchResult.batterId },
        });
        if (response.status === 200) {
          const responseData = response.data.data;
          this.pitchDetail = responseData.pitchDetail || null;
          this.errorMessage = "";
        }
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = "ゾーン別対戦成績の取得に失敗しました";
        }
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>
