<template>
  <div class="table-responsive">
    <b-table v-if="matchResultList.length" striped hover :items="matchResultList" :fields="fields" class="mt-4" head-variant="dark">
      <!-- 各カラムの表示 -->
      <template #cell(battingAverage)="data">
        {{ data.item.battingAverage.toFixed(3) }}
      </template>
      <template #cell(atBatNumber)="data">
        {{ data.item.atBatNumber }}
      </template>
      <template #cell(strokesNumber)="data">
        {{ data.item.strokesNumber }}
      </template>
      <template #cell(hitNumber)="data">
        {{ data.item.hitNumber }}
      </template>
      <template #cell(singlesNumber)="data">
        {{ data.item.singlesNumber }}
      </template>
      <template #cell(doublesNumber)="data">
        {{ data.item.doublesNumber }}
      </template>
      <template #cell(triplesNumber)="data">
        {{ data.item.triplesNumber }}
      </template>
      <template #cell(homeRun)="data">
        {{ data.item.homeRun }}
      </template>
      <template #cell(baseHitsNumber)="data">
        {{ data.item.baseHitsNumber }}
      </template>
      <template #cell(fourBallNumber)="data">
        {{ data.item.fourBallNumber }}
      </template>
      <template #cell(hitBallNumber)="data">
        {{ data.item.hitBallNumber }}
      </template>
      <template #cell(strikeoutsNumber)="data">
        {{ data.item.strikeoutsNumber }}
      </template>
      <template #cell(ops)="data">
        {{ data.item.ops.toFixed(3) }}
      </template>
      <template #cell(onBasePercentage)="data">
        {{ data.item.onBasePercentage.toFixed(3) }}
      </template>
      <template #cell(sluggingPercentage)="data">
        {{ data.item.sluggingPercentage.toFixed(3) }}
      </template>
    </b-table>
  </div>
</template>

<script>
export default {
  name: "SearchResultBaseball",
  props: {
    matchResultList: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      fields: [
        { key: "battingAverage", label: "打率" },
        { key: "atBatNumber", label: "打席数" },
        { key: "strokesNumber", label: "打数" },
        { key: "hitNumber", label: "ヒット数" },
        { key: "singlesNumber", label: "単打数" },
        { key: "doublesNumber", label: "二塁打数" },
        { key: "triplesNumber", label: "三塁打数" },
        { key: "homeRun", label: "本塁打数" },
        { key: "baseHitsNumber", label: "塁打数" },
        { key: "fourBallNumber", label: "四球数" },
        { key: "hitBallNumber", label: "死球数" },
        { key: "strikeoutsNumber", label: "三振数" },
        { key: "ops", label: "OPS" },
        { key: "onBasePercentage", label: "出塁率" },
        { key: "sluggingPercentage", label: "長打率" },
      ],
    };
  },
};
</script>

<style scoped></style>
