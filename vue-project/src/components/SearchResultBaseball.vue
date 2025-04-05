<template>
  <div class="table-container">
    <table v-if="matchResultList.length" class="table" ref="resultsTable">
      <thead>
        <tr>
          <th v-for="field in fields" :key="field.key" :class="['th', { 'th-active': sortKey === field.key }]">
            <button @click="sortBy(field.key)" :class="['sort-button', { active: sortKey === field.key }]" :data-sort-direction="sortKey === field.key ? (sortAsc ? '▲' : '▼') : '↕️'">
              {{ field.label }}
            </button>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in sortedMatchResultList" :key="index">
          <td v-for="field in fields" :key="field.key" :class="['td', getCellClass(field.key, item)]">
            <template v-if="field.key === 'pitcherNm'">
              <a :href="item.pitcherNpbUrl" target="_blank" rel="noopener noreferrer">{{ item.pitcherNm }}</a>
            </template>
            <template v-else-if="field.key === 'batterNm'">
              <a :href="item.batterNpbUrl" target="_blank" rel="noopener noreferrer">{{ item.batterNm }}</a>
            </template>
            <template v-else-if="['battingAverage', 'ops', 'onBasePercentage', 'sluggingPercentage'].includes(field.key)">
              {{ item[field.key].toFixed(3) }}
            </template>
            <template v-else>
              {{ item[field.key] }}
            </template>
          </td>
        </tr>
      </tbody>
    </table>
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
        { key: "pitcherNm", label: "投手" },
        { key: "pitcherTeamNm", label: "チーム" },
        { key: "batterNm", label: "打者" },
        { key: "batterTeamNm", label: "チーム" },
        { key: "battingAverage", label: "打率" },
        { key: "atBatNumber", label: "打席" },
        { key: "strokesNumber", label: "打数" },
        { key: "hitNumber", label: "安打" },
        { key: "singlesNumber", label: "単打" },
        { key: "doublesNumber", label: "二塁打" },
        { key: "triplesNumber", label: "三塁打" },
        { key: "homeRun", label: "本塁打" },
        { key: "baseHitsNumber", label: "塁打" },
        { key: "fourBallNumber", label: "四球" },
        { key: "hitBallNumber", label: "死球" },
        { key: "sacrificeFly", label: "犠飛" },
        { key: "strikeoutsNumber", label: "三振" },
        { key: "ops", label: "OPS" },
        { key: "onBasePercentage", label: "出塁率" },
        { key: "sluggingPercentage", label: "長打率" },
      ],
      sortKey: "",
      sortAsc: true,
    };
  },
  computed: {
    sortedMatchResultList() {
      if (!this.sortKey) return this.matchResultList;
      return [...this.matchResultList].sort((a, b) => {
        const aVal = a[this.sortKey];
        const bVal = b[this.sortKey];
        if (typeof aVal === "number" && typeof bVal === "number") {
          return this.sortAsc ? aVal - bVal : bVal - aVal;
        } else {
          return this.sortAsc ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
        }
      });
    },
  },
  watch: {
    matchResultList(newVal) {
      if (newVal && newVal.length > 0) {
        this.$nextTick(() => {
          this.scrollToTable();
        });
      }
    },
  },
  methods: {
    scrollToTable() {
      const tableElement = this.$refs.resultsTable;
      if (tableElement) {
        tableElement.scrollIntoView({ behavior: "smooth" });
      }
    },
    // チームIDに応じてクラス名を返す
    getTeamClass(teamId) {
      const teamClasses = {
        1: "team-yakult",
        2: "team-giants",
        3: "team-dena",
        4: "team-hanshin",
        5: "team-hiroshima",
        6: "team-chunichi",
        7: "team-softbank",
        8: "team-nipponham",
        9: "team-seibu",
        10: "team-orix",
        11: "team-lotte",
        12: "team-rakuten",
      };
      return teamClasses[teamId] || "default-team";
    },
    getCellClass(fieldKey, item) {
      if (fieldKey === "batterTeamNm") {
        return this.getTeamClass(item.batterTeamId);
      } else if (fieldKey === "pitcherTeamNm") {
        return this.getTeamClass(item.pitcherTeamId);
      }
      return "";
    },
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortAsc = !this.sortAsc;
      } else {
        this.sortKey = key;
        this.sortAsc = true;
      }
    },
  },
};
</script>

<style scoped>
.table-container {
  background-color: #e6f3ff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1rem;
  overflow-x: auto;
  width: 100%;
}

.table {
  width: 100%;
  border-collapse: collapse; /* separate から collapse に変更 */
  border-spacing: 0;
  font-size: 0.8rem;
}

.th,
.td {
  padding: 0.5rem;
  text-align: center;
  white-space: nowrap;
  border: 1px solid #a0c2e8; /* 網目状の枠線を追加 */
}

.th {
  background-color: #4e73df;
  color: white;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: sticky;
  top: 0;
  z-index: 10;
}

tbody tr:nth-of-type(even) {
  background-color: #f8faff;
}

tbody tr:hover {
  background-color: #d4e4ff;
}
/* チームカラーの定義 */

/* 1: 東京ヤクルトスワローズ */
.team-yakult {
  background-color: #0f1350;
  color: #aacd17;
}

/* 2: 読売ジャイアンツ */
.team-giants {
  background-color: #f97709;
  color: #000000;
}

/* 3: 横浜DeNAベイスターズ */
.team-dena {
  background-color: #0055a5;
  color: #ffffff;
}

/* 4: 阪神タイガース */
.team-hanshin {
  background-color: #000000;
  color: #ffe201;
}

/* 5: 広島東洋カープ */
.team-hiroshima {
  background-color: #ff2b06;
  color: #ffffff;
}

/* 6: 中日ドラゴンズ */
.team-chunichi {
  background-color: #002569;
  color: #ffffff;
}

/* 7: 福岡ソフトバンクホークス */
.team-softbank {
  background-color: #f5c700;
  color: #000000;
}

/* 8: 北海道日本ハムファイターズ */
.team-nipponham {
  background-color: #4c7b98;
  color: #ffffff;
}

/* 9: 埼玉西武ライオンズ */
.team-seibu {
  background-color: #1f366a;
  color: #ffffff;
}

/* 10: オリックス・バファローズ */
.team-orix {
  background-color: #000019;
  color: #e2d69e;
}

/* 11: 千葉ロッテマリーンズ */
.team-lotte {
  background-color: #221815;
  color: #ffffff;
}

/* 12: 東北楽天ゴールデンイーグルス */
.team-rakuten {
  background-color: #860010;
  color: #ffffff;
}

/* デフォルトのチームカラー */
.default-team {
  background-color: #cccccc;
  color: #000000;
}

.sort-button {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: inherit;
  font-weight: inherit;
}
.sort-button:focus {
  outline: none;
}

/* ヘッダーのスタイル改善 */
.th .sort-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  position: relative;
  padding-right: 15px; /* アイコン用のスペース確保 */
}

/* デフォルトのソートアイコン（未ソート状態） */
.sort-button::after {
  content: "↕️";
  font-size: 0.7rem;
  opacity: 0.5;
  position: absolute;
  right: 0;
}

/* アクティブなソートの状態 */
.sort-button.active::after {
  opacity: 1;
  content: attr(data-sort-direction);
}

/* ホバー効果 */
.sort-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
}

/* アクティブなヘッダーの強調 */
.th-active {
  background-color: #385bc0; /* 通常よりも少し暗い色 */
}

@media (max-width: 768px) {
  .table-container {
    border-radius: 0;
    padding: 0.5rem;
  }

  .table {
    font-size: 0.6rem;
  }

  .th,
  .td {
    padding: 0.3rem;
  }
}
</style>
