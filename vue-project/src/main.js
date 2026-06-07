import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import axios from "axios";

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

const axiosInstance = axios.create({
  baseURL: "/baseball/api",
});

Vue.prototype.$axios = axiosInstance;

new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");
