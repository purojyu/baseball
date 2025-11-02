package com.example.scraper.bk;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.entity.BaseballPlayerHistory;
import com.example.baseball.service.BaseballPlayerHistoryService;
import com.example.baseball.service.BaseballPlayerService;

/**
 * 同年に複数球団に所属している選手の選手historyを更新
 */
@Component
public class TradeScraper {
	@Autowired
	private BaseballPlayerService baseballPlayerService;
	@Autowired
	private BaseballPlayerHistoryService baseballPlayerHistoryService;

	private static final List<String> years = Arrays.asList(
			"2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024");

	// 共通の DateTimeFormatter を定義
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

	private static final String BASE_URL = "https://npb.jp/announcement/"; // ベースURL

	public void updateBaseballHistory() throws IOException {
		for (String year : years) {
			System.out.println(year);
			Document doc = Jsoup.connect(BASE_URL + year + "/pn_traded.html").get();
			Elements changePlayers = doc.select("div.table_normal_noborder");
			// トレードの日付が早い順にループ
			for (int i = changePlayers.size() - 1; i >= 0; i--) {
				Elements trs = changePlayers.get(i).select("table > tbody > tr");
				for (Element tr : trs) {
					String date = tr.selectFirst("th.trdate").text().trim();
					String playerUrl = tr.selectFirst("td.trname").select("a").attr("href");
					String playerName = getPlayerName(playerUrl, year);
					if(StringUtils.isEmpty(playerName)) {
						continue;
					}
					String fromTeam = tr.select("td[class*='trteam']").get(0).text().trim();
					String toTeam = tr.select("td[class*='trteam']").get(1).text().trim();
					System.out.print(playerName);
					changePlayer(playerName, (long) convTeam(fromTeam), (long) convTeam(toTeam), date, year);
				}
			}
		}
	}
	public String getPlayerName(String url, String argYear) {
	    String playerFullName = null;
	    try {
	        // URLが "https://npb.jp" で始まらない場合、付与する
	    	if (!url.startsWith("http://") && !url.startsWith("https://")) {
	            url = "https://npb.jp" + url;
	        }
	        // URLが存在するかを確認し、404エラーが発生した場合はスキップ
	        Document playerDoc = Jsoup.connect(url).get();

	        // 重複年のチェック
	        Elements rows = playerDoc.select("#stats_b .registerStats");
	        if (rows.size() != 0) {
	            List<String> yearList = new ArrayList<>();
	            Set<String> duplicateYears = new HashSet<>();
	            
	            for (Element row : rows) {
	                String year = row.select(".year").text().trim(); // 年度
	                if (yearList.contains(year)) {
	                    duplicateYears.add(year);
	                }
	                yearList.add(year);
	            }

	            // 重複年の処理：重複年がなければスルー
	            if (duplicateYears.isEmpty() || !duplicateYears.contains(argYear)) {
	                return playerFullName;
	            }
	        }
	        
	     // 「公式戦出場機会なし」のチェック
	        Element dnpElement = playerDoc.selectFirst("p#dnp");
	        if (dnpElement != null) {
	            String dnpText = dnpElement.text().trim();
	            if ("（公式戦出場機会なし）".equals(dnpText)) {
	                return playerFullName; // スキップ
	            }
	        }

	        // プレイヤー名の取得
	        Element playerNameElement = playerDoc.selectFirst("#pc_v_name li#pc_v_name");
	        if (playerNameElement != null) {
	            playerFullName = playerNameElement.text().trim();
	        }
	    } catch (HttpStatusException e) {
	        System.out.println("URLが404エラー: " + url);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return playerFullName;
	}


	// トレード、現役ドラフト用
	public void changePlayer(String playerName, Long fromTeamId, Long toTeamId, String date, String year) {
		BaseballPlayer baseballPlayer = baseballPlayerService.findByplayerNm(playerName);
		BaseballPlayerHistory baseballPlayerHistoryByFromTeam = baseballPlayerHistoryService
				.findByPlayerIdAndTeamIdAndEndDateYear(
						baseballPlayer.getPlayerId(), fromTeamId, year);
		baseballPlayerHistoryByFromTeam.setEndDate(parseDate(date).minusDays(1)); 
		baseballPlayerHistoryService.saveBaseballPlayerHistory(baseballPlayerHistoryByFromTeam);

		BaseballPlayerHistory baseballPlayerHistoryByToTeam = baseballPlayerHistoryService
				.findByPlayerIdAndTeamIdAndStartDateYear(
						baseballPlayer.getPlayerId(), toTeamId, year);
		baseballPlayerHistoryByToTeam.setStartDate(parseDate(date));
		baseballPlayerHistoryService.saveBaseballPlayerHistory(baseballPlayerHistoryByToTeam);
	}

	/**
	 * 日付文字列を LocalDate に変換する共通メソッド
	 */
	private LocalDate parseDate(String dateStr) {
		return LocalDate.parse(dateStr, DATE_FORMATTER);
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
		} else if (team.contains("中日")) {
			return 6; // 中日ドラゴンズ
		} else if (team.contains("ヤクルト")) {
			return 1; // 東京ヤクルトスワローズ
		} else if (team.contains("読売")) {
			return 2; // 読売ジャイアンツ
		} else if (team.contains("阪神")) {
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
