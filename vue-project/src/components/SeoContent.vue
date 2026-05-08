<template>
  <section class="seo-content">
    <hr />
    <!-- 使い方は初心者向け、デフォルト開く -->
    <details class="seo-details" open>
      <summary><h2>使い方</h2></summary>
      <ol class="seo-list">
        <li><strong>年度を選択</strong>：通算（2016年以降）または特定年度を選びます。</li>
        <li><strong>投手 or 打者を選択</strong>：チームと選手名を絞り込みます。片方だけの指定でもOK。</li>
        <li><strong>検索ボタンを押下</strong>：投手と打者の対戦成績（打率・本塁打・打席数など）が表示されます。</li>
      </ol>
    </details>

    <details class="seo-details">
      <summary><h2>このサイトでわかること</h2></summary>
      <ul class="seo-list">
        <li>投手の<strong>得意な打者</strong>・<strong>苦手な打者</strong>の傾向</li>
        <li>打者の<strong>得意な投手</strong>・<strong>苦手な投手</strong>の傾向</li>
        <li>セ・リーグ／パ・リーグ全12球団の対戦データ</li>
        <li>2016年以降の通算成績、または年度別の成績</li>
        <li>打率・本塁打・打席数・出塁率・長打率・OPSなどの主要指標</li>
      </ul>
    </details>

    <details class="seo-details">
      <summary><h2>よくある質問</h2></summary>
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
    </details>
  </section>
</template>

<script>
export default {
  name: "SeoContent",
};
</script>

<style scoped>
.seo-content {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: #333;
  line-height: 1.7;
  text-align: center;
}

.seo-details {
  display: block;
  width: 100%;
  margin: 0 auto 0.5rem;
  border: 1px solid #d6e4f0;
  border-radius: 6px;
  background-color: #f8fbfd;
  transition: background-color 0.2s ease;
  text-align: center;
}

.seo-details[open] {
  background-color: #fff;
}

.seo-details summary {
  cursor: pointer;
  padding: 0.6rem 1rem;
  list-style: none;
  user-select: none;
  position: relative;
  text-align: center;
}

.seo-details summary::-webkit-details-marker {
  display: none;
}

.seo-details summary::after {
  content: "▼";
  font-size: 0.7rem;
  color: #0056b3;
  transition: transform 0.2s ease;
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
}

.seo-details[open] summary::after {
  transform: translateY(-50%) rotate(180deg);
}

.seo-details summary h2 {
  display: inline-block;
  margin: 0;
  font-size: 1rem;
  font-weight: bold;
  color: #0056b3;
  border: none;
  padding: 0;
}

.seo-details > *:not(summary) {
  padding: 0 1rem 0.8rem 1rem;
}

.seo-details h3 {
  font-size: 0.95rem;
  font-weight: bold;
  color: #0056b3;
  margin-top: 0.6rem;
  margin-bottom: 0.3rem;
}

/* リスト・FAQ：ブロックは中央配置、行頭は揃える（inline-block + text-align:left） */
.seo-list,
.faq-list {
  display: inline-block;
  text-align: left;
  list-style: none;
  padding-left: 0;
  margin-bottom: 0;
}

.seo-list li {
  margin-bottom: 0.3rem;
}

.team-list {
  margin-bottom: 0.5rem;
}

.faq-list dt {
  font-weight: bold;
  margin-top: 0.6rem;
  color: #0056b3;
}

.faq-list dt:first-of-type {
  margin-top: 0;
}

.faq-list dd {
  margin-left: 0;
  margin-bottom: 0.3rem;
}

@media (max-width: 575px) {
  .seo-content {
    font-size: 0.85rem;
  }
  .seo-details summary h2 {
    font-size: 0.95rem;
  }
}
</style>
