const { defineConfig } = require("@vue/cli-service");
const path = require("path");

module.exports = defineConfig({
  transpileDependencies: true,

  // エイリアス設定
  chainWebpack: (config) => {
    config.resolve.alias.set("vue$", path.resolve(__dirname, "node_modules/vue/dist/vue.esm.js"));
  },

  // ビルド成果物の出力先をSpring Bootのstaticフォルダに設定
  outputDir: path.resolve(__dirname, "../src/main/resources/static"),

  // プロダクションビルドの設定
  publicPath: process.env.NODE_ENV === "production" ? "/" : "/",

  // 開発サーバー設定（開発環境のみ）
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        pathRewrite: { "^/api": "" },
      },
    },
  },

  // プロダクションソースマップの無効化
  productionSourceMap: false,
});
