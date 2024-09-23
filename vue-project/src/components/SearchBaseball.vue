<template>
  <div class="container">
    <h1>2024年度プロ野球｜対戦成績検索画面</h1>
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

    <!-- 年度選択 -->
    <!-- <b-row class="mt-3 justify-content-center">
      <b-col cols="12" md="6" class="mb-3">
        <label class="w-100 text-center">年度</label>
        <multiselect v-model="selectedYear" :options="yearOptions" placeholder="選択してください" :selectLabel="'選択'" :deselectLabel="'選択解除'">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
    </b-row> -->

    <!-- チーム選択 -->
    <b-row class="mt-3">
      <b-col cols="12" md="6" class="mb-3">
        <label>投手チーム</label>
        <multiselect v-model="selectPitcherTeamOptions" :options="pitcherTeamOptions" label="pitcherTeamNm" track-by="pitcherTeamId" placeholder="チーム名を入力して絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'選択解除'" @input="getPitcherList">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
      <b-col cols="12" md="6" class="mb-3">
        <label>野手チーム</label>
        <multiselect v-model="selectBatterTeamOptions" :options="batterTeamOptions" label="batterTeamNm" track-by="batterTeamId" placeholder="チーム名を入力して絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'選択解除'" @input="getBatterList">
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
      <b-col cols="12" md="6" class="mb-3">
        <label>投手名</label>
        <multiselect v-model="selectPitcherOptions" :options="pitcherOptions" label="displayPicherLabel" track-by="pitcherId" placeholder="背番号か選手名を入力で絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'選択解除'" :noDateLabel="defaultLabel.noDateLabel">
          <template slot="noResult">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
          <template slot="noOptions">
            <span>{{ defaultLabel.noDateLabel }}</span>
          </template>
        </multiselect>
      </b-col>
      <b-col cols="12" md="6" class="mb-3">
        <label>野手名</label>
        <multiselect v-model="selectBatterOptions" :options="batterOptions" label="displayBatterLabel" track-by="batterId" placeholder="背番号か選手名を入力で絞り込み可能" :selectLabel="'選択'" :selectedLabel="'選択中'" :deselectLabel="'選択解除'" :noDateLabel="defaultLabel.noDateLabel">
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
  },
  data() {
    return {
      selectPitcherOptions: null,
      selectBatterOptions: null,
      selectBatterTeamOptions: null,
      selectPitcherTeamOptions: null,
      localPitcherList: [...this.pitcherList],
      localBatterList: [...this.batterList],
      selectedYear: "2024",
      yearOptions: ["2024"],
      defaultLabel: {
        teamPlaceholder: "チーム名を入力して絞り込み可能",
        playerPlaceholder: "背番号か選手名を入力で絞り込み可能",
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
    },
    batterList(newVal) {
      this.localBatterList = [...newVal];
    },
  },
  computed: {
    batterTeamOptions() {
      return this.baseballTeamList.map((batterTeam) => ({
        batterTeamId: batterTeam.teamId,
        batterTeamNm: batterTeam.teamNm,
      }));
    },
    pitcherTeamOptions() {
      return this.baseballTeamList.map((pitcherTeam) => ({
        pitcherTeamId: pitcherTeam.teamId,
        pitcherTeamNm: pitcherTeam.teamNm,
      }));
    },
    batterOptions() {
      return this.localBatterList.map((batter) => ({
        batterId: batter.playerId,
        batterNm: batter.playerNm,
        displayBatterLabel: `${batter.uniformNo}: ${batter.playerNm}`,
      }));
    },
    pitcherOptions() {
      return this.localPitcherList.map((pitcher) => ({
        pitcherId: pitcher.playerId,
        pitcherNm: pitcher.playerNm,
        displayPicherLabel: `${pitcher.uniformNo}: ${pitcher.playerNm}`,
      }));
    },
    isSearchEnabled() {
      return this.selectPitcherOptions || this.selectBatterOptions;
    },
  },
  methods: {
    getPitcherList() {
      this.selectPitcherOptions = null;
      this.localPitcherList = [];
      if (this.selectPitcherTeamOptions !== null) {
        this.$emit("getPitcherList", this.selectPitcherTeamOptions.pitcherTeamId);
      }
    },
    getBatterList() {
      this.selectBatterOptions = null;
      this.localBatterList = [];
      if (this.selectBatterTeamOptions !== null) {
        this.$emit("getBatterList", this.selectBatterTeamOptions.batterTeamId);
      }
    },
    matchResultSearch() {
      const pitcherTeamId = this.selectPitcherTeamOptions ? this.selectPitcherTeamOptions.pitcherTeamId : null;
      const batterTeamId = this.selectBatterTeamOptions ? this.selectBatterTeamOptions.batterTeamId : null;
      const pitcherId = this.selectPitcherOptions ? this.selectPitcherOptions.pitcherId : null;
      const batterId = this.selectBatterOptions ? this.selectBatterOptions.batterId : null;

      this.$emit("matchResultSearch", pitcherTeamId, batterTeamId, pitcherId, batterId, this.selectedYear);
    },
  },
};
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style></style>
