package com.example.scraper;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballTeamService;

// 未使用
@Component
public class OldNPBWebScraper {

	@Autowired
	private BaseballTeamService baseballTeamService;
	@Autowired
	private BaseballGameService baseballGameService;
	@Autowired
	private AtBatResultService atBatResultService;

	private static final String BASE_URL = "https://npb.jp/games/2024/schedule_%02d_detail.html";

	public void scrapeData() {
		// 年と月の範囲を設定
		int startYear = 2005;
		int endYear = 2015;
		int startMonth = 4;
		int endMonth = 11;

		// ベースURLの設定
		String baseUrl = "https://npb.jp/bis/";

		// 年ごとにループ
		for (int year = startYear; year <= endYear; year++) {
			// 月ごとにループ
			for (int month = startMonth; month <= endMonth; month++) {
				// 月を2桁の文字列に変換（例：4 -> "04"）
				String monthStr = String.format("%02d", month);

				// URLを構築
				String url = baseUrl + year + "/calendar/index_" + monthStr + ".html";
				System.out.println("アクセス中のURL: " + url);

				try {
					// URLに接続してレスポンスを取得
					Connection.Response response = Jsoup.connect(url)
							.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
									"AppleWebKit/537.36 (KHTML, like Gecko) " +
									"Chrome/85.0.4183.121 Safari/537.36")
							.timeout(10 * 1000) // タイムアウトを10秒に設定
							.ignoreHttpErrors(true) // HTTPエラーも無視してレスポンスを取得
							.execute();
					if (response.statusCode() == 200) {
						// ページが存在する場合、ドキュメントを解析
						Document doc = response.parse();
						// stvsteam クラスの div をすべて取得
						Elements stvsteamDivs = doc.select(".stvsteam");
						// 各 stvsteam div を処理
						for (Element stvsteam : stvsteamDivs) {
							Element tescheaten = stvsteam.selectFirst(".tescheaten");
							if (tescheaten != null && (tescheaten.text().contains("オールスター")||tescheaten.text().contains("日本シリーズ")||tescheaten.text().contains("CS"))) {
								continue; // 特別試合の場合はスキップ
							}
							Elements gameLinks = stvsteam.select("a");
							for (Element link : gameLinks) {
								String linkText = link.text();
								// 中止の試合結果をスキップ
								if (linkText.contains("*")) {
									continue;
								}
								String relativePath = link.attr("href");
								String gameUrl = baseUrl + year + relativePath; // ベースURLと相対パスを結合
								System.out.println("取得するリンク: " + gameUrl);

								// 各リンク先のページを取得
								try {
									Document gameDoc = Jsoup.connect(gameUrl)
											.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
													"AppleWebKit/537.36 (KHTML, like Gecko) " +
													"Chrome/85.0.4183.121 Safari/537.36")
											.timeout(10 * 1000) // タイムアウトを10秒に設定
											.get();

									// 必要な情報を取得（例としてタイトルを取得）
									String title = gameDoc.title();
									System.out.println("ページタイトル: " + title);

								} catch (IOException e) {
									System.out.println("ページの取得に失敗しました: " + gameUrl);
									e.printStackTrace();
									// ページ取得に失敗した場合はスキップ
									continue;
								}
							}
						}
					} else {
						// ページが存在しない場合はスキップ
						System.out.println("ページが存在しないか、取得に失敗しました。ステータスコード: " + response.statusCode());
						continue;
					}
				} catch (IOException e) {
					System.out.println("ページの取得に失敗しました: " + url);
					e.printStackTrace();
					// ページ取得に失敗した場合はスキップ
					continue;
				}

				// サーバーへの負荷を避けるために待機（例: 1秒）
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}