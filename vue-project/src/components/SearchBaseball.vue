<template>
  <div class="container">
    <h1>プロ野球｜対戦成績検索画面</h1>
    <b-row class="mt-4 align-items-center">
      <b-col cols="4">
        <label class="header_font text-right pitcher-label">投手</label>
      </b-col>
      <b-col cols="4" class="text-center">
        <span class="vs-label">VS</span>
      </b-col>
      <b-col cols="4">
        <label class="header_font text-left batter-label">野手</label>
      </b-col>
    </b-row>
    <hr />
    <!-- 注意書き -->
    <div class="notice mt-3">
      <p>検索仕様</p>
      <div class="text-left small-text">
        <ul>
          <li>投手名か野手名のどちらか一方は必ず選択して検索ボタンを押下してください。</li>
          <li>投手名か野手名のとちらか一方の検索の場合、選択した選手と選択したチームの全ての対戦結果を表示します。</li>
          <li>通算の検索は、2016年以降の結果が表示されます。</li>
          <li>プルダウンの選択は、文字入力で絞り込み可能です。</li>
          <li>移籍歴のある選手で、特定のチームを選択した場合、選手が選択されたチームに所属していた期間の結果が表示されます。</li>
          <li>移籍歴のある選手で、全てのチームを選択した場合、選手が複数の球団に所属していた全期間の結果が表示されます。</li>
        </ul>
      </div>
    </div>

    <!-- 年度選択 -->
    <b-row class="mt-3 justify-content-center">
      <b-col cols="6" xs="6" class="mb-3">
        <label class="w-100 text-center">年度</label>
        <multiselect
          v-model="selectedYear"
          :options="years"
          placeholder="選択してください"
          :selectLabel="'選択'"
          :deselectLabel="''"
          :allowEmpty="false"
          required
          @input="
            () => {
              this.getPitcherList();
              this.getBatterList();
            }
          "
        >
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
    </b-row>

    <!-- チーム選択 -->
    <b-row class="mt-3">
      <b-col cols="6" xs="6" class="mb-3">
        <label>投手チーム</label>
        <multiselect v-model="selectPitcherTeamOptions" :options="pitcherTeamOptions" label="pitcherTeamNm" track-by="pitcherTeamId" placeholder="チーム名を入力して絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="''" :allowEmpty="false" required @input="this.getPitcherList">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
      <b-col cols="6" xs="6" class="mb-3">
        <label>野手チーム</label>
        <multiselect v-model="selectBatterTeamOptions" :options="batterTeamOptions" label="batterTeamNm" track-by="batterTeamId" placeholder="チーム名を入力して絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="''" :allowEmpty="false" required @input="this.getBatterList">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
    </b-row>

    <!-- 選手選択 -->
    <b-row class="mt-3">
      <b-col cols="6" xs="6" class="mb-3">
        <label>投手名</label>
        <multiselect v-model="selectPitcherOptions" :options="filteredPitcherList" label="playerNm" track-by="playerId" placeholder="選手名を入力で絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'解除'" @search-change="updatePitcherSearch" :filterable="false" :internal-search="false">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
      <b-col cols="6" xs="6" class="mb-3">
        <label>野手名</label>
        <multiselect v-model="selectBatterOptions" :options="filteredBatterList" label="playerNm" track-by="playerId" placeholder="選手名を入力で絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'解除'" @search-change="updateBatterSearch" :filterable="false" :internal-search="false">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
    </b-row>

    <!-- 検索ボタン -->
    <div class="text-center">
      <b-button variant="primary" :disabled="!isSearchEnabled" class="mt-4 mb-4 search-button" @click="matchResultSearch">検索</b-button>
    </div>
    <hr />
  </div>
</template>

<script>
import Multiselect from "vue-multiselect";

export default {
  name: "SearchBaseball", // この行を追加
  components: { Multiselect },
  props: {
    baseballTeamList: Array,
    batterList: Array,
    pitcherList: Array,
    matchResultList: Array,
    years: Array,
  },
  data() {
    return {
      selectPitcherOptions: null,
      selectBatterOptions: null,
      selectBatterTeamOptions: {
        batterTeamId: 0,
        batterTeamNm: "全てのチーム",
      },
      selectPitcherTeamOptions: {
        pitcherTeamId: 0,
        pitcherTeamNm: "全てのチーム",
      },
      localPitcherList: [...this.pitcherList],
      localBatterList: [...this.batterList],
      selectedYear: "通算",
      searchQueryPitcher: "",
      searchQueryBatter: "",
      defaultLabel: {
        teamPlaceholder: "チーム名を入力して絞り込み可能",
        playerPlaceholder: "選手名を入力で絞り込み可能",
        selectLabel: "選択",
        selectedLabel: "選択中",
        deselectLabel: "選択解除",
        noDateLabel: "対象データがありません",
      },
    };
  },
  watch: {
    pitcherList(newVal) {
      this.localPitcherList = [...newVal];
      const currentSelection = this.selectPitcherOptions;
      if (currentSelection && !this.localPitcherList.some((player) => player.playerId === currentSelection.playerId)) {
        this.selectPitcherOptions = null;
      }
    },
    batterList(newVal) {
      this.localBatterList = [...newVal];
      const currentSelection = this.selectBatterOptions;
      if (currentSelection && !this.localBatterList.some((player) => player.playerId === currentSelection.playerId)) {
        this.selectBatterOptions = null;
      }
    },
  },
  computed: {
    batterTeamOptions() {
      return [
        {
          batterTeamId: 0,
          batterTeamNm: "全てのチーム",
        },
        ...this.baseballTeamList.map((batterTeam) => ({
          batterTeamId: batterTeam.teamId,
          batterTeamNm: batterTeam.teamNm,
        })),
      ];
    },
    pitcherTeamOptions() {
      return [
        {
          pitcherTeamId: 0,
          pitcherTeamNm: "全てのチーム",
        },
        ...this.baseballTeamList.map((pitcherTeam) => ({
          pitcherTeamId: pitcherTeam.teamId,
          pitcherTeamNm: pitcherTeam.teamNm,
        })),
      ];
    },
    isSearchEnabled() {
      return (this.selectPitcherOptions || this.selectBatterOptions) && this.selectedYear;
    },
    filteredPitcherList() {
      if (!this.searchQueryPitcher) {
        return this.localPitcherList;
      }
      // 漢字名とカナ名で検索できるようにする。検索クエリから半角・全角スペースを削除。
      const normalizedQuery = this.searchQueryPitcher.replace(/[\s\u3000]/g, "").toLowerCase();
      return this.localPitcherList.filter((player) => {
        const playerNm = player.playerNm ? player.playerNm.toLowerCase().replace(/[\s\u3000]/g, "") : "";
        const playerNmKana = player.playerNmKana ? player.playerNmKana.toLowerCase().replace(/[\s\u3000]/g, "") : "";
        return playerNm.includes(normalizedQuery) || playerNmKana.includes(normalizedQuery);
      });
    },
    filteredBatterList() {
      if (!this.searchQueryBatter) {
        return this.localBatterList;
      }
      // 漢字名とカナ名で検索できるようにする。検索クエリから半角・全角スペースを削除。
      const normalizedQuery = this.searchQueryBatter.replace(/[\s\u3000]/g, "").toLowerCase();
      return this.localBatterList.filter((player) => {
        const playerNm = player.playerNm ? player.playerNm.toLowerCase().replace(/[\s\u3000]/g, "") : "";
        const playerNmKana = player.playerNmKana ? player.playerNmKana.toLowerCase().replace(/[\s\u3000]/g, "") : "";
        return playerNm.includes(normalizedQuery) || playerNmKana.includes(normalizedQuery);
      });
    },
  },
  mounted() {
    this.getPitcherList();
    this.getBatterList();
  },
  methods: {
    getPitcherList() {
      this.localPitcherList = [];
      if (this.selectPitcherTeamOptions !== null && this.selectedYear !== null) {
        this.$emit("getPitcherList", this.selectPitcherTeamOptions.pitcherTeamId, this.selectedYear);
      }
    },
    getBatterList() {
      this.localBatterList = [];
      if (this.selectPitcherTeamOptions !== null && this.selectedYear !== null) {
        this.$emit("getBatterList", this.selectBatterTeamOptions.batterTeamId, this.selectedYear);
      }
    },
    matchResultSearch() {
      const pitcherTeamId = this.selectPitcherTeamOptions ? this.selectPitcherTeamOptions.pitcherTeamId : null;
      const batterTeamId = this.selectBatterTeamOptions ? this.selectBatterTeamOptions.batterTeamId : null;
      const pitcherId = this.selectPitcherOptions ? this.selectPitcherOptions.playerId : null;
      const batterId = this.selectBatterOptions ? this.selectBatterOptions.playerId : null;

      this.$emit("matchResultSearch", pitcherTeamId, batterTeamId, pitcherId, batterId, this.selectedYear);
    },
    updatePitcherSearch(query) {
      this.searchQueryPitcher = query;
    },
    updateBatterSearch(query) {
      this.searchQueryBatter = query;
    },
  },
};
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style></style>
