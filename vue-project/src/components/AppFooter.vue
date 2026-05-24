<template>
  <div class="footer-container">
    <hr />
    <div class="footer">
      <div class="reference">
        <h2 class="footer-heading">参考文献</h2>
        <p>
          <a href="https://npb.jp/" target="_blank" rel="noopener noreferrer">日本野球機構（NPB）</a>
        </p>
        <p>
          <a href="https://baseball.yahoo.co.jp/" target="_blank" rel="noopener noreferrer">Yahoo!スポーツ・プロ野球</a>
        </p>
      </div>
      <div class="disclaimer">
        <h2 class="footer-heading disclaimer-heading">利用責任</h2>
        <p>本サイトの情報は、正確性や最新性の維持に努めていますが、利用者が本サイトの情報を使用して生じた一切の損害について、運営者は責任を負いかねます。利用者は自己責任で本サイトの情報を使用してください。</p>
      </div>
      <!-- X (旧Twitter) リンク -->
      <div class="x-section">
        <a href="https://x.com/JANYS992112" target="_blank" rel="noopener noreferrer" class="x-link" aria-label="X (Twitter)">
          <svg class="x-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"></path>
          </svg>
        </a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AppFooter",
};
</script>

<style scoped>
.x-section {
  text-align: center;
  margin-top: 1.5rem;
}

.x-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: #000;
  border-radius: 50%;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.x-link:hover {
  transform: scale(1.1);
  background-color: #1a1a1a;
}

.x-icon {
  width: 24px;
  height: 24px;
  fill: #fff;
}

@media (max-width: 575px) {
  .x-link {
    width: 42px;
    height: 42px;
  }
  .x-icon {
    width: 20px;
    height: 20px;
  }
}
</style>
