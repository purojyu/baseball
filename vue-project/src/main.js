import Vue from "vue";
import App from "./Baseball.vue";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import axios from "axios";

// BootstrapVueを使用
Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

// Axios のインスタンスを作成
const axiosInstance = axios.create({
  baseURL: process.env.VUE_APP_BASE_URL || "http://localhost:8080/baseball/api", // Herokuの環境変数を優先
});

// Axios インスタンスを Vue のプロトタイプに設定
Vue.prototype.$axios = axiosInstance;

new Vue({
  render: (h) => h(App),
}).$mount("#app");
