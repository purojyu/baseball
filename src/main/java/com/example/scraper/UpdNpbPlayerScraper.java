package com.example.scraper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.service.BaseballPlayerService;

@Component
public class UpdNpbPlayerScraper {
	@Autowired
	private BaseballPlayerService baseballPlayerService;

	private static final String BASE_URL = "https://npb.jp/history/register/"; // ベースURL

	public void scrapePlayerLinks() throws IOException {
		Document doc = Jsoup.connect(BASE_URL).get();

		// 50音順のリンクを持つ要素を選択
		Elements links = doc.select("#pl_lk_unit ul li a");

		for (Element link : links) {
			String href = link.attr("href");
			String soundUrl = BASE_URL + href;
			Document soundDoc = Jsoup.connect(soundUrl).get();
			Elements soundlinks = soundDoc.select("div#st_list a.unit.player_unit_1");
			for (Element soundlink : soundlinks) {
				String soundHref = soundlink.attr("href");
				String playerUrl = "https://npb.jp" + soundHref;
				Document playerDoc = Jsoup.connect(playerUrl).get();
				// 打撃成績だけを取得(投手成績はない場合はあるが野手成績は必ず存在するため。また、投手登板があれば必ず野手成績が残るため)
				Elements rows = playerDoc.select("#stats_b .registerStats");
				if (rows.size() != 0) {
					Element laseRow = rows.get(rows.size() - 1);
					String lastYear = laseRow.select(".year").text();
					String convLastYear = lastYear.substring(0, 4);
					int lastYearNumber = Integer.parseInt(convLastYear);
					if (lastYearNumber >= 2016) {
						// 身長と体重を分割
						// 身長と体重の文字列から "cm" や "kg" を除いて int に変換
						Element playerNameElement = playerDoc.selectFirst("#pc_v_name li#pc_v_name");
						String playerName = playerNameElement.text();
						Element playerNameKanaElement = playerDoc.selectFirst("#pc_v_name li#pc_v_kana");
						String playerKana = playerNameKanaElement.text();
						Elements playerInfo = playerDoc.select("#pc_bio");
						String birthdDateStr = playerInfo.select("th:contains(生年月日) + td").text();
						LocalDate birthDate = convertToLocalDate(birthdDateStr);
						BaseballPlayer baseballPlayer = baseballPlayerService.findByPlayerNmAndBirthDate(playerName, birthDate);
						baseballPlayer.setPlayerNmKana(playerKana);
						baseballPlayer.setNpbUrl(playerUrl);
						baseballPlayerService.savePlayer(baseballPlayer);
						System.out.println(playerName);
					}
				}
			}
		}
		System.out.println("終わり");
	}

	// LocalDate型に変換するメソッド
	public LocalDate convertToLocalDate(String dateStr) {
		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy年M月d日"); // 日本語フォーマット
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // スラッシュ区切り

		try {
			// 最初に日本語フォーマットを試す
			return LocalDate.parse(dateStr, formatter1);
		} catch (DateTimeParseException e) {
			// 失敗したらスラッシュ区切りのフォーマットを試す
			try {
				return LocalDate.parse(dateStr, formatter2);
			} catch (DateTimeParseException ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}

	// 投打を変換('投げ　0:右投げ　1:左投げ　2:両投げ')
	public String convThrower(String throwingBatting) {
		String throwingCd = null;
		if (throwingBatting.contains("右投")) {
			throwingCd = "0";
		} else if (throwingBatting.contains("左投")) {
			throwingCd = "1";
		} else if (throwingBatting.contains("両投")) {
			throwingCd = "2";
		}
		return throwingCd;
	}

	// 投打を変換('打者　0:右打ち　1:左打ち　2:両打ち')
	public String convHanded(String throwingBatting) {
		String throwingCd = null;
		if (throwingBatting.contains("右打")) {
			throwingCd = "0";
		} else if (throwingBatting.contains("左打")) {
			throwingCd = "1";
		} else if (throwingBatting.contains("両打")) {
			throwingCd = "2";
		}
		return throwingCd;
	}

	/**
	 * DBの登録用にポジションをポジションCDに変更
	 */
	public String convPosition(String position) {
		switch (position) {
		case "投　手":
			return "1";
		case "捕　手":
			return "2";
		case "内野手":
			return "3";
		case "外野手":
			return "4";
		default:
			return null;
		}
	}

	/**
	 * DBの登録用にチームをチームIDに変更
	 */
	public int convTeam(String team) {
		if (team.contains("西武") || team.contains("西　武")) {
			return 9; // 埼玉西武ライオンズ
		} else if (team.contains("ソフトバンク")) {
			return 7; // 福岡ソフトバンクホークス
		} else if (team.contains("日本ハム")) {
			return 8; // 北海道日本ハムファイターズ
		} else if (team.contains("ロッテ")) {
			return 11; // 千葉ロッテマリーンズ
		} else if (team.contains("オリックス")) {
			return 10; // オリックス・バファローズ
		} else if (team.contains("楽天")) {
			return 12; // 東北楽天ゴールデンイーグルス
		} else if (team.contains("中　日")) {
			return 6; // 中日ドラゴンズ
		} else if (team.contains("ヤクルト")) {
			return 1; // 東京ヤクルトスワローズ
		} else if (team.contains("読　売")) {
			return 2; // 読売ジャイアンツ
		} else if (team.contains("阪　神")) {
			return 4; // 阪神タイガース
		} else if (team.contains("広島")) {
			return 5; // 広島東洋カープ
		} else if (team.contains("横浜") || team.contains("横　浜")) {
			return 3; // 横浜DeNAベイスターズ
		} else {
			return 0;
		}
	}

}
