package com.example.scraper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.entity.AtBatResult;
import com.example.baseball.entity.BaseballGame;
import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.VBaseballPlayerHistory;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VBaseballPlayerHistoryService;
import com.example.scraper.entity.BatterPitcherInfoList;
import com.example.scraper.entity.BatterResults;
import com.example.scraper.entity.ConvBatterResults;
import com.example.scraper.entity.ConvPitcherResults;
import com.example.scraper.entity.PitcherResults;

/**
 * NPBのサイトから試合結果、打席情報を取得
 */
@Component
public class NPBWebScraper {

	@Autowired
	private BaseballTeamService baseballTeamService;
	@Autowired
	private BaseballGameService baseballGameService;
	@Autowired
	private VBaseballPlayerHistoryService vBaseballPlayerHistoryService;
	@Autowired
	private AtBatResultService atBatResultService;

	private static final List<String> years = Arrays.asList(
			"2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024");

	private static final List<String> months = Arrays.asList(
			//			"03", "04", "05", "06", "07", "08", "09", "10", "11");
			"10");

	/**
	 * 打席結果を取得
	 * @throws IOException
	 * @throws ParseException
	 */
	public void scrapeData() throws IOException, ParseException {
		for (String year : years) {
			for (String month : months) {
				String url = "https://npb.jp/games/" + year + "/schedule_" + month + "_detail.html";
				List<String> gameLinks = getGameLinks(url, year);
				// 年と月でリンクが存在している場合
				if (gameLinks != null) {
					// 当日の試合を除外
					filterOutSpecificDates(gameLinks);
					for (String gameLink : gameLinks) {
						// 試合結果を取得
						BaseballGame baseballGame = getGameInfo(gameLink);
						if (baseballGame != null) {
							// 打席結果を取得
							BatterPitcherInfoList batterPitcherInfo = parseGameDetails(gameLink);
							if (batterPitcherInfo != null) {
								convertPlayer(batterPitcherInfo, baseballGame);
							}
						}
					}
				}
			}
		}
		System.out.println("終わり");
	}

	/**
	 * 取得対象の試合のリンクを取得
	 * @param month
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	private List<String> getGameLinks(String url, String year) throws IOException, ParseException {
		Document doc = Jsoup.connect(url).get();
		List<String> gameLinks = new ArrayList<>();
		Elements gamelinks = doc.select("a[href]");
		Pattern pattern = Pattern.compile("/scores/" + year + "/(\\d{4})/");
		for (Element gamelink : gamelinks) {
			Matcher matcherGameLink = pattern.matcher(gamelink.attr("href"));
			if (matcherGameLink.find()) {
				// ゲームの日付を取得
				String gameDateStr = matcherGameLink.group(1);
				String fullLinkDate = year + gameDateStr;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				Date gameDate = dateFormat.parse(fullLinkDate);
				// 既存のゲームの日付を取得
				List<BaseballGame> baseballGame = baseballGameService.findByGameDate(gameDate);
				// 取得していないゲーム日付の場合
				if (baseballGame.size() == 0) {
					Element parent = gamelink.parent();
					// 中止の試合を取得
					Elements cancelDivs = parent.select("div.cancel");
					// 中止の試合を除外
					if (cancelDivs.isEmpty()) {
						gameLinks.add("https://npb.jp" + gamelink.attr("href") + "box.html");
					}
				}
			}
		}
		return gameLinks;
	}

	/**
	 * 当日の試合を除外
	 * @param gameLinks
	 */
	private void filterOutSpecificDates(List<String> gameLinks) {
		LocalDate today = LocalDate.now();
		DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMdd");

		String currentYear = today.format(yearFormatter);
		String todayStr = today.format(dateFormatter);

		gameLinks.removeIf(link -> {
			// リンクから年と日付を取得
			Pattern pattern = Pattern.compile("/scores/(\\d{4})/(\\d{4})/.*");
			Matcher matcher = pattern.matcher(link);
			if (matcher.find()) {
				String linkYear = matcher.group(1);// yyyy形式
				String linkDate = matcher.group(2).substring(0, 4); // MMdd形式

				// 年と日付が一致する場合にスキップ
				return linkYear.equals(currentYear) && linkDate.equals(todayStr);
			}
			return false;
		});
	}

	/**
	 * 打者と投手の結果を取得
	 * @param gameUrl
	 * @return
	 * @throws IOException
	 */
	private BatterPitcherInfoList parseGameDetails(String gameUrl)
			throws IOException {
		Document doc = Jsoup.connect(gameUrl).get();
		Elements teamNames = doc.select("h4");
		BatterPitcherInfoList returnList = new BatterPitcherInfoList();
		// チーム数が2チームない場合
		if (teamNames.size() < 2) {
			return null;
		}
		String topTeam = teamNames.get(0).text();
		String bottomTeam = teamNames.get(1).text();

		Element topBattingTable = !doc.select("div#table_top_b").isEmpty() ? doc.select("div#table_top_b").get(0)
				: (!doc.select("div.table_batter").isEmpty() ? doc.select("div.table_batter").get(0) : null);
		Element topPitcherTable = !doc.select("div#table_top_p").isEmpty() ? doc.select("div#table_top_p").get(0)
				: (!doc.select("div.table_pitcher").isEmpty() ? doc.select("div.table_pitcher").get(0) : null);
		Element bottomBattingTable = !doc.select("div#table_bottom_b").isEmpty()
				? doc.select("div#table_bottom_b").get(0)
				: (!doc.select("div.table_batter").isEmpty() ? doc.select("div.table_batter").get(1) : null);
		Element bottomPitcherTable = !doc.select("div#table_bottom_p").isEmpty()
				? doc.select("div#table_bottom_p").get(0)
				: (!doc.select("div.table_pitcher").isEmpty() ? doc.select("div.table_pitcher").get(1) : null);

		// 投手と打者の結果がない場合
		if (topBattingTable == null || topPitcherTable == null || bottomBattingTable == null
				|| bottomPitcherTable == null) {
			return null;
		}
		// 上のチームのバッター情報を取得
		List<BatterResults> topBatterResultList = getBatterResults(topBattingTable, topTeam);
		// 上のチームのピッチャー情報を取得
		List<PitcherResults> topPitcherResultList = getPitcherResults(topPitcherTable, topTeam);
		// 下のチームのバッター情報を取得
		List<BatterResults> bottomBatterResultList = getBatterResults(bottomBattingTable, bottomTeam);
		// 下のチームのピッチャー情報を取得
		List<PitcherResults> bottomPitcherResultList = getPitcherResults(bottomPitcherTable, bottomTeam);
		returnList.setTopBatterResults(topBatterResultList);
		returnList.setTopPitcherResults(topPitcherResultList);
		returnList.setBottomBatterResults(bottomBatterResultList);
		returnList.setBottomPitcherResults(bottomPitcherResultList);
		return returnList;
	}

	/**
	 * 試合情報を取得
	 * @param gameUrl
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private BaseballGame getGameInfo(String gameUrl)
			throws IOException, ParseException {
		Document doc = Jsoup.connect(gameUrl).get();

		Element gameTitDiv = doc.selectFirst(".game_tit");
		Element h3 = gameTitDiv.selectFirst("h3");
		if (h3 != null) {
			String h3Text = h3.text();
			// 特別試合の場合はスキップ
			if (h3Text.contains("オールスターゲーム") || h3Text.contains("CS") || h3Text.contains("日本シリーズ")) {
				return null;
			}
		}
		// 場所の取得
		Element placeElement = doc.select("span.place").first();
		String place = placeElement.text();

		Element timeElement = doc.select("time").first();
		String time = timeElement.text();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日（E）");
		Date date = dateFormat.parse(time);
		// 日付をyyyy/MM/dd形式の文字列に変換
		SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String formattedDateStr = outputDateFormat.format(date);
		Date formattedDate = outputDateFormat.parse(formattedDateStr);

		// トップチームとスコアの取得
		Element topTeamElement = doc.select("tr.top th span.hide_sp").first() != null
				? doc.select("tr.top th span.hide_sp").first()
				: doc.select("tr.top span[class^=flag_]").first();
		String topTeam = topTeamElement.text();
		BaseballTeam topBaseballTeam = baseballTeamService.findByTeamNm(topTeam);

		Element topTeamScoreElement = doc.select("tr.top td.total-1").first();
		String topTeamScore = topTeamScoreElement.text();

		// ボトムチームとスコアの取得
		Element bottomTeamElement = doc.select("tr.bottom th span.hide_sp").first() != null
				? doc.select("tr.bottom th span.hide_sp").first()
				: doc.select("tr.bottom span[class^=flag_]").first();
		String bottomTeam = bottomTeamElement.text();
		BaseballTeam bottomBaseballTeam = baseballTeamService.findByTeamNm(bottomTeam);

		Element bottomTeamScoreElement = doc.select("tr.bottom td.total-1").first();
		String bottomTeamScore = bottomTeamScoreElement.text();

		BaseballGame baseballGame = new BaseballGame();
		baseballGame.setStadium(place);
		baseballGame.setHomeTeamId(bottomBaseballTeam.getTeamId());
		baseballGame.setAwayTeamId(topBaseballTeam.getTeamId());
		baseballGame.setHomeTeamScore(Long.valueOf(bottomTeamScore));
		baseballGame.setAwayTeamScore(Long.valueOf(topTeamScore));
		baseballGame.setGameDate(formattedDate);
		return baseballGame;
	}

	/**
	 * 投手情報を取得
	 * @param pitcherTables
	 * @param team
	 * @return
	 */
	private List<PitcherResults> getPitcherResults(Element pitcherTables, String team) {
		List<PitcherResults> pitcherResultsList = new ArrayList<>();
		Elements pitcherRows = pitcherTables.select("tr");
		for (Element pitcherRow : pitcherRows) {
			Elements playerCell = pitcherRow.select("td.player");
			Elements cells = pitcherRow.select("td");
			if (!playerCell.isEmpty() && cells.size() > 3) {
				PitcherResults pitcherResults = new PitcherResults();
				String playerUrl = cells.get(1).select("a").attr("href");
				String fullPlayerUrl = "https://npb.jp" + playerUrl;
				String pitcherName = getPlayerName(fullPlayerUrl);
				String pitchingResults = cells.get(3).text().trim();
				pitcherResults.setPitcher(pitcherName);
				pitcherResults.setTeam(team);
				pitcherResults.setMatchNumber(pitchingResults);
				pitcherResultsList.add(pitcherResults);
			}
		}
		return pitcherResultsList;
	}

	/**
	 * 打者情報を取得
	 * @param battingTables
	 * @param team
	 * @return
	 */
	private List<BatterResults> getBatterResults(Element battingTables, String team) {
		List<BatterResults> batterResultsList = new ArrayList<>();
		// 打席結果の列数を取得
		Element thead = battingTables.selectFirst("thead");
		Elements thElements = thead.select("th");
		int rowSize = 0;
		// "1" 以降の列をカウント
		if (thElements.size() > 0) {
			rowSize = thElements.size() - 8;
		}
		Elements battingTable = battingTables.select("table tbody");
		// 打者結果を取得する行数を取得する
		int columnSize = battingTable.select("tr").size();
		// 一つ前の列で取得した行数を保持する
		int previousRow = -1;
		// 列数分ループ処理をする
		for (int i = 8; rowSize + 8 > i; i++) {
			// 一番上の結果から下まで取得する(一番最初または前回の取得が一番下で終わったとき)
			if (previousRow == -1) {
				previousRow = sortBattingResults(battingTable, previousRow + 1, columnSize - 1, i, team,
						batterResultsList);
				// 前回終了した場所から+１した行から一番下まで取得し、その後一番上から前回取得した行まで取得する
			} else {
				previousRow = sortMiddleBattingResults(battingTable, previousRow, columnSize - 1, i, team,
						batterResultsList);
			}
		}
		return batterResultsList;
	}

	/**
	 * 打者結果を打順の順番で取得(上から下まで)
	 * 関係のない結果を削除(-のデータは無視する)
	 * 一番最後に取得した行をreturnする。もし、これが一番下の行なら0をreturnする
	 * @param battingTable
	 * @param startRow
	 * @param endRow
	 * @param columnSize
	 * @param i
	 * @param team
	 * @param batterResultsList
	 */
	private int sortBattingResults(Elements battingTable, int startRow, int endRow, int i,
			String team, List<BatterResults> batterResultsList) {
		int previousRow = 0;
		for (int j = startRow; j <= endRow; j++) {
			Elements columnBatting = battingTable.get(0).select("tr");
			Elements cells = columnBatting.get(j).select("td");
			if (cells.size() != 0) {
				String playerUrl = cells.get(2).select("a").attr("href");
				String fullPlayerUrl = "https://npb.jp" + playerUrl;
				String playerName = getPlayerName(fullPlayerUrl);
				String batterResult = cells.get(i).text().trim();
				if (!batterResult.equals("-")) {
					BatterResults batterResults = new BatterResults();
					batterResults.setBatter(playerName);
					batterResults.setTeam(team);
					batterResults.setResult(batterResult);
					batterResultsList.add(batterResults);
					previousRow = j;
				}
			}
		}
		if (previousRow == endRow) {
			previousRow = -1;
		}
		return previousRow;
	}

	public String getPlayerName(String url) {
		String playerFullName = null;
		try {
			// プレイヤーの詳細ページにアクセス
			Document playerDoc = Jsoup.connect(url).get();
			// liタグのid="pc_v_name"を取得
			Element playerNameElement = playerDoc.selectFirst("#pc_v_name li#pc_v_name");
			playerFullName = playerNameElement.text().trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return playerFullName;
	}

	/**
	 * 打者結果を打順の順番で取得(前回終了した場所から+１した行から一番下まで取得し、その後一番上から前回取得した行まで取得する)
	 * 関係のない結果を削除(-のデータは無視する)
	 * 一番最後に取得した行をreturnする。もし、これが一番下の行なら0をreturnする
	 * @param battingTable
	 * @param startRow
	 * @param endRow
	 * @param columnSize
	 * @param i
	 * @param team
	 * @param batterResultsList
	 */
	private int sortMiddleBattingResults(Elements battingTable, int startRow, int endRow, int i,
			String team, List<BatterResults> batterResultsList) {
		int previousRow = 0;
		//前回終了した場所から+１した行から一番下まで取得
		for (int j = startRow + 1; j <= endRow; j++) {
			Elements columnBatting = battingTable.get(0).select("tr");
			Elements cells = columnBatting.get(j).select("td");
			if (cells.size() != 0) {
				String playerUrl = cells.get(2).select("a").attr("href");
				String fullPlayerUrl = "https://npb.jp" + playerUrl;
				String playerName = getPlayerName(fullPlayerUrl);
				String batterResult = cells.get(i).text().trim();
				if (!batterResult.equals("-")) {
					BatterResults batterResults = new BatterResults();
					batterResults.setBatter(playerName);
					batterResults.setTeam(team);
					batterResults.setResult(batterResult);
					batterResultsList.add(batterResults);
					previousRow = j;
				}
			}
		}
		//一番上から前回取得した行まで取得
		for (int j = 0; j <= startRow; j++) {
			Elements columnBatting = battingTable.get(0).select("tr");
			Elements cells = columnBatting.get(j).select("td");
			if (cells.size() != 0) {
				String playerUrl = cells.get(2).select("a").attr("href");
				String fullPlayerUrl = "https://npb.jp" + playerUrl;
				String playerName = getPlayerName(fullPlayerUrl);
				String batterResult = cells.get(i).text().trim();
				if (!batterResult.equals("-")) {
					BatterResults batterResults = new BatterResults();
					batterResults.setBatter(playerName);
					batterResults.setTeam(team);
					batterResults.setResult(batterResult);
					batterResultsList.add(batterResults);
					previousRow = j;
				}
			}
		}
		if (previousRow == endRow) {
			previousRow = -1;
		}
		return previousRow;
	}

	private void convertPlayer(BatterPitcherInfoList playerList, BaseballGame baseballGame) {
		List<ConvBatterResults> topConvBatterResultList = new ArrayList<>();
		List<ConvBatterResults> bottomConvBatterResultList = new ArrayList<>();
		List<ConvPitcherResults> topConvPitcherResultList = new ArrayList<>();
		List<ConvPitcherResults> bottomConvPitcherResultList = new ArrayList<>();
		// 上のバッター
		for (BatterResults batterInfo : playerList.getTopBatterResults()) {
			try {
				ConvBatterResults convBatterResults = new ConvBatterResults();
				BaseballTeam baseballTeam = baseballTeamService.findByTeamNm(batterInfo.getTeam());
				VBaseballPlayerHistory baseballPlayer = vBaseballPlayerHistoryService
						.findByPlayerNmAndTeamId(
								baseballTeam.getTeamId(),
								batterInfo.getBatter(), baseballGame.getGameDate());
				convBatterResults.setTeamId(baseballTeam.getTeamId());
				convBatterResults.setBatterId(baseballPlayer.getPlayerId());
				convBatterResults.setResult(batterInfo.getResult());
				topConvBatterResultList.add(convBatterResults);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(baseballGame);
				System.out.println(batterInfo);
			}
		}
		// 下のバッター
		for (BatterResults batterInfo : playerList.getBottomBatterResults()) {
			try {
				ConvBatterResults convBatterResults = new ConvBatterResults();
				BaseballTeam baseballTeam = baseballTeamService.findByTeamNm(batterInfo.getTeam());
				VBaseballPlayerHistory baseballPlayer = vBaseballPlayerHistoryService
						.findByPlayerNmAndTeamId(
								baseballTeam.getTeamId(),
								batterInfo.getBatter(), baseballGame.getGameDate());
				convBatterResults.setTeamId(baseballTeam.getTeamId());
				convBatterResults.setBatterId(baseballPlayer.getPlayerId());
				convBatterResults.setResult(batterInfo.getResult());
				bottomConvBatterResultList.add(convBatterResults);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(baseballGame);
				System.out.println(batterInfo);
			}
		}
		// 上のピッチャー
		for (PitcherResults pitcherInfo : playerList.getTopPitcherResults()) {
			try {
				ConvPitcherResults convPitcherResults = new ConvPitcherResults();
				BaseballTeam baseballTeam = baseballTeamService.findByTeamNm(pitcherInfo.getTeam());
				VBaseballPlayerHistory baseballPlayer = vBaseballPlayerHistoryService
						.findByPlayerNmAndTeamId(
								baseballTeam.getTeamId(),
								pitcherInfo.getPitcher(), baseballGame.getGameDate());
				convPitcherResults.setTeamId(baseballTeam.getTeamId());
				convPitcherResults.setPitcherId(baseballPlayer.getPlayerId());
				convPitcherResults.setMatchNumber(Long.valueOf(pitcherInfo.getMatchNumber()));
				topConvPitcherResultList.add(convPitcherResults);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(baseballGame);
				System.out.println(pitcherInfo);
			}
		}
		// 下のピッチャー
		for (PitcherResults pitcherInfo : playerList.getBottomPitcherResults()) {
			try {
				ConvPitcherResults convPitcherResults = new ConvPitcherResults();
				BaseballTeam baseballTeam = baseballTeamService.findByTeamNm(pitcherInfo.getTeam());
				VBaseballPlayerHistory baseballPlayer = vBaseballPlayerHistoryService
						.findByPlayerNmAndTeamId(
								baseballTeam.getTeamId(),
								pitcherInfo.getPitcher(), baseballGame.getGameDate());
				convPitcherResults.setTeamId(baseballTeam.getTeamId());
				convPitcherResults.setPitcherId(baseballPlayer.getPlayerId());
				convPitcherResults.setMatchNumber(Long.valueOf(pitcherInfo.getMatchNumber()));
				bottomConvPitcherResultList.add(convPitcherResults);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(baseballGame);
				System.out.println(pitcherInfo);
			}
		}
		BaseballGame savedGame = baseballGameService.saveBaseballGame(baseballGame);
		int j = 0;
		List<AtBatResult> atBatResultList = new ArrayList<>();
		// 上のピッチャーと下のバッターの対戦結果を取得
		for (ConvPitcherResults pitcherResult : topConvPitcherResultList) {
			// ピッチャーの対戦した打者分ループ
			for (int i = 0; pitcherResult.getMatchNumber() > i; i++) {
				if (j >= bottomConvBatterResultList.size()) {
					System.out.print(bottomConvBatterResultList);
				}
				AtBatResult atBatResult = new AtBatResult();
				atBatResult.setGameId(savedGame.getGameId());
				atBatResult.setBatterId(bottomConvBatterResultList.get(j).getBatterId());
				atBatResult.setPitcherId(pitcherResult.getPitcherId());
				atBatResult.setResult(bottomConvBatterResultList.get(j).getResult());
				atBatResultList.add(atBatResult);
				j++;
			}
		}
		int s = 0;
		// 下のピッチャーと上のバッターの対戦結果を取得
		for (ConvPitcherResults pitcherResult : bottomConvPitcherResultList) {
			for (int i = 0; pitcherResult.getMatchNumber() > i; i++) {
				if (s >= topConvBatterResultList.size()) {
					System.out.print(topConvBatterResultList);
				}
				AtBatResult atBatResult = new AtBatResult();
				atBatResult.setGameId(savedGame.getGameId());
				atBatResult.setBatterId(topConvBatterResultList.get(s).getBatterId());
				atBatResult.setPitcherId(pitcherResult.getPitcherId());
				atBatResult.setResult(topConvBatterResultList.get(s).getResult());
				atBatResultList.add(atBatResult);
				s++;
			}
		}
		atBatResultService.saveAtBatResult(atBatResultList);
		System.out.print(baseballGame.getGameId() + ",");
	}
}
