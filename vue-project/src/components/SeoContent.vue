<template>
  <section class="seo-content">
    <hr />
    <section class="usage-guide mb-4">
      <h2>使い方</h2>
      <ol class="seo-list">
        <li><strong>年度を選択</strong>：通算（2016年以降）または特定年度を選びます。</li>
        <li><strong>投手 or 打者を選択</strong>：チームと選手名を絞り込みます。片方だけの指定でもOK。</li>
        <li><strong>検索ボタンを押下</strong>：投手と打者の対戦成績（打率・本塁打・打席数など）が表示されます。</li>
      </ol>
    </section>

    <section class="features mb-4">
      <h2>このサイトでわかること</h2>
      <ul class="seo-list">
        <li>投手の<strong>得意な打者</strong>・<strong>苦手な打者</strong>の傾向</li>
        <li>打者の<strong>得意な投手</strong>・<strong>苦手な投手</strong>の傾向</li>
        <li>セ・リーグ／パ・リーグ全12球団の対戦データ</li>
        <li>2016年以降の通算成績、または年度別の成績</li>
        <li>打率・本塁打・打席数・出塁率・長打率・OPSなどの主要指標</li>
      </ul>
    </section>

    <section class="teams mb-4">
      <h2>対応している球団（NPB全12球団）</h2>
      <h3>セントラル・リーグ</h3>
      <p class="team-list">読売ジャイアンツ（巨人）／阪神タイガース／横浜DeNAベイスターズ／広島東洋カープ／東京ヤクルトスワローズ／中日ドラゴンズ</p>
      <h3>パシフィック・リーグ</h3>
      <p class="team-list">福岡ソフトバンクホークス／北海道日本ハムファイターズ／千葉ロッテマリーンズ／東北楽天ゴールデンイーグルス／オリックス・バファローズ／埼玉西武ライオンズ</p>
    </section>

    <section class="faq mb-4">
      <h2>よくある質問</h2>
      <dl class="faq-list">
        <dt>Q. プロ野球の個人対戦成績はどこで見られますか？</dt>
        <dd>A. 本サイトでNPB（プロ野球）の投手と打者の個人対戦成績を無料で検索できます。打率・本塁打・打席数などのデータを、選手別・年度別に表示します。</dd>

        <dt>Q. 対応している年度・球団はどこまでですか？</dt>
        <dd>A. セ・リーグ／パ・リーグの全12球団に対応しており、2016年以降の対戦データを年度別または通算成績として検索できます。</dd>

        <dt>Q. 投手だけ・打者だけでも検索できますか？</dt>
        <dd>A. はい。投手だけ指定すれば、その投手の全対戦打者との成績一覧を、打者だけ指定すればその打者の全対戦投手との成績一覧を表示できます。</dd>

        <dt>Q. データの更新頻度はどれくらいですか？</dt>
        <dd>A. 毎日自動でNPB公式の試合結果を取得し、対戦成績データを更新しています。</dd>
      </dl>
    </section>
  </section>
</template>

<script>
export default {
  name: "SeoContent",
};
</script>

<style scoped>
.seo-content {
  margin-top: 2rem;
  font-size: 0.9rem;
  color: #333;
  line-height: 1.7;
}

.seo-content h2 {
  font-size: 1.2rem;
  font-weight: bold;
  color: #0056b3;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.4rem;
  border-bottom: 2px solid #0056b3;
}

.seo-content h3 {
  font-size: 1rem;
  font-weight: bold;
  color: #0056b3;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.seo-list {
  padding-left: 1.4em;
  margin-bottom: 0;
}

.seo-list li {
  margin-bottom: 0.4rem;
}

.team-list {
  margin-bottom: 0.5rem;
}

.faq-list dt {
  font-weight: bold;
  margin-top: 0.8rem;
  color: #0056b3;
}

.faq-list dd {
  margin-left: 1em;
  margin-bottom: 0.4rem;
}

@media (max-width: 575px) {
  .seo-content {
    font-size: 0.85rem;
  }
  .seo-content h2 {
    font-size: 1.1rem;
  }
}
</style>
