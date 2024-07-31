const { defineConfig } = require("@vue/cli-service");
const path = require("path");

module.exports = defineConfig({
  transpileDependencies: true,

  // エイリアス設定
  chainWebpack: (config) => {
    config.resolve.alias.set("vue$", path.resolve(__dirname, "node_modules/vue/dist/vue.esm.js"));
  },

  // プロキシ設定
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
    },
  },
});
  