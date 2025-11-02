package com.example.scraper.bk;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.entity.BaseballPlayer;
import com.example.baseball.service.BaseballPlayerService;

@Component
public class UpdNPBPlayerScraper2024 {

	@Autowired
	private BaseballPlayerService baseballPlayerService;
	public void scrapePlayer() {
		String url = "https://npb.jp/bis/teams/";

		try {
			Document doc = Jsoup.connect(url).get();
			Elements teamLinks = doc.select("a[href*='rst']");
			for (Element link : teamLinks) {
				// 2024年のチームごとの選手情報URL
				String teamUrl = url + link.attr("href");
				scrapePlayerInfo(teamUrl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("終わり");
	}

	private void scrapePlayerInfo(String teamUrl) {
		try {
			Document teamDoc = Jsoup.connect(teamUrl).get();
			// 支配下選手のテーブルを特定
			Element rosterDiv = teamDoc.selectFirst("div.rosterdivlisttbl");
			if (rosterDiv != null) {
				Elements rows = rosterDiv.select("tr");
				String currentPosition = "";
				for (Element row : rows) {
					if (row.hasClass("rosterMainHead")) {
						Element positionElement = row.select("th.rosterPos a").first();
						if (positionElement == null) {
							positionElement = row.select("th.rosterPos").first();
						}
						if (positionElement != null) {
							String textPosition = positionElement.text();
							switch (textPosition) {
							case "監督":
								currentPosition = "0";
								break;
							case "投手":
								currentPosition = "1";
								break;
							case "捕手":
								currentPosition = "2";
								break;
							case "内野手":
								currentPosition = "3";
								break;
							case "外野手":
								currentPosition = "4";
								break;
							}
						}
					} else if (!currentPosition.equals("0")
							&& (row.hasClass("rosterPlayer") || row.hasClass("rosterRetire"))) {
						Elements columns = row.select("td");
						String playerLink = columns.get(1).select("a").attr("href");
						String fullUrl = "https://npb.jp" + playerLink;
						String playerName = null;
						String playerNameKana = null;
						try {
							Document playerDoc = Jsoup.connect(fullUrl).get();
							Element playerNameElement = playerDoc.selectFirst("#pc_v_name li#pc_v_name");
							Element playerNameKanaElement = playerDoc.selectFirst("#pc_v_name li#pc_v_kana");
								playerName = playerNameElement.text();
							playerNameKana = playerNameKanaElement.text();
						} catch (IOException e) {
							e.printStackTrace();
						}
						String birthDate = columns.get(2).text();

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
						LocalDate date = LocalDate.parse(birthDate, formatter);
						BaseballPlayer existBaseballPlayer = baseballPlayerService
								.findByPlayerNmAndBirthDate(playerName, date);
						// 選手が登録されていなければ
						existBaseballPlayer.setPlayerNmKana(playerNameKana);
						existBaseballPlayer.setNpbUrl(fullUrl);
						baseballPlayerService.savePlayer(existBaseballPlayer);
						System.out.print(playerName);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}