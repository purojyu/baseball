const { defineConfig } = require("@vue/cli-service");
const path = require("path");

module.exports = defineConfig({
  transpileDependencies: true,

  // エイリアス設定
  chainWebpack: (config) => {
    config.resolve.alias.set("vue$", path.resolve(__dirname, "node_modules/vue/dist/vue.esm.js"));
  },

  // ビルド成果物の出力先（S3デプロイ用）
  outputDir: "dist",

  // プロダクションビルドの設定
  publicPath: "/",

  // 開発サーバー設定（開発環境のみ）
  devServer: {
    proxy: {
      "/baseball/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },

  // プロダクションソースマップの無効化
  productionSourceMap: false,
});
