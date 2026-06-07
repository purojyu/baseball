import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import PlayerProfileView from "../views/PlayerProfileView.vue";
import MatchupDetailView from "../views/MatchupDetailView.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/players/:playerId",
    name: "player",
    component: PlayerProfileView,
    props: true,
  },
  {
    path: "/matchup/:pitcherId/:batterId",
    name: "matchup",
    component: MatchupDetailView,
    props: true,
  },
  // 不正なURLはトップへ
  {
    path: "*",
    redirect: "/",
  },
];

const router = new VueRouter({
  mode: "history",
  base: "/",
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 戻る/進むで保存位置を復元、それ以外は先頭へ
    if (savedPosition) return savedPosition;
    return { x: 0, y: 0 };
  },
});

export default router;
