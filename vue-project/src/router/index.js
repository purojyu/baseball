import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

// ルートコンポーネントは遅延ロード（初期バンドル軽量化）
const HomeView = () => import(/* webpackChunkName: "home" */ "../views/HomeView.vue");
const PlayerProfileView = () =>
  import(/* webpackChunkName: "player" */ "../views/PlayerProfileView.vue");

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
