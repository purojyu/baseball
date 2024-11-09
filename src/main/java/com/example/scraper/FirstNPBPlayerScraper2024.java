package com.example.scraper;

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
import com.example.baseball.entity.BaseballPlayerHistory;
import com.example.baseball.service.BaseballPlayerHistoryService;
import com.example.baseball.service.BaseballPlayerService;
import com.example.baseball.service.BaseballTeamService;

@Component
public class FirstNPBPlayerScraper2024 {

	@Autowired
	private BaseballPlayerService baseballPlayerService;

	@Autowired
	private BaseballTeamService baseballTeamService;

	@Autowired
	private BaseballPlayerHistoryService baseballPlayerHistoryService;

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
				Long teamId = null;
				for (Element row : rows) {
					Element tenametitleTd = teamDoc.selectFirst("td.tenametitle");
					if (tenametitleTd != null) {
						// h1タグを探して、そのテキストを取得する
						Element h1Tag = tenametitleTd.selectFirst("h1");
						if (h1Tag != null) {
							String teamName = h1Tag.text();
							teamId = baseballTeamService.findByTeamNm(teamName).getTeamId();
						}
					}
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
						String playerName = getPlayerName(playerLink);
						String playerNo = columns.get(0).text();
						String birthDate = columns.get(2).text();
						String height = columns.size() > 3 ? columns.get(3).text() : "NULL";
						String weight = columns.size() > 4 ? columns.get(4).text() : "NULL";
						String thrower = columns.size() > 5 ? columns.get(5).text() : "NULL";
						String handed = columns.size() > 6 ? columns.get(6).text() : "NULL";

						String throwerCd = "0".equals(thrower) ? "0" : "1";
						String handedCd = "右".equals(handed) ? "0" : ("左".equals(handed) ? "1" : "2");

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
						LocalDate date = LocalDate.parse(birthDate, formatter);
						BaseballPlayer existBaseballPlayer = baseballPlayerService
								.findByPlayerNmAndBirthDate(playerName, date);
						// 選手が登録されていなければ
						if (existBaseballPlayer == null) {
							BaseballPlayer baseballPlayer = new BaseballPlayer();
							baseballPlayer.setPlayerNm(playerName);
							baseballPlayer.setPosition(currentPosition);
							baseballPlayer.setBirthDate(date);
							baseballPlayer.setHeight(height.isEmpty() ? 1 : Long.valueOf(height));
							baseballPlayer.setWeight(weight.isEmpty() ? 1 : Long.valueOf(weight));
							baseballPlayer.setThrower(throwerCd);
							baseballPlayer.setHanded(handedCd);

							BaseballPlayer savePlayer = baseballPlayerService.savePlayer(baseballPlayer);

							BaseballPlayerHistory BaseballPlayerHistory = new BaseballPlayerHistory();
							BaseballPlayerHistory.setTeamId(teamId);
							BaseballPlayerHistory.setPlayerId(savePlayer.getPlayerId());
							BaseballPlayerHistory.setUniformNo(playerNo);
							LocalDate targetDate = LocalDate.of(2024, 01, 01);
							BaseballPlayerHistory.setStartDate(targetDate);
							BaseballPlayerHistory.setEndDate(null);
							baseballPlayerHistoryService.saveBaseballPlayerHistory(BaseballPlayerHistory);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 選手の詳細ページにアクセスして選手名を取得
	 * @param playerLink 選手の詳細ページのURL部分
	 * @return 選手名
	 */
	private String getPlayerName(String playerLink) {
		String fullUrl = "https://npb.jp" + playerLink;
		String playerName = null;
		try {
			Document playerDoc = Jsoup.connect(fullUrl).get();
			Element playerNameElement = playerDoc.selectFirst("#pc_v_name li#pc_v_name");
			if (playerNameElement != null) {
				playerName = playerNameElement.text();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return playerName;
	}
}