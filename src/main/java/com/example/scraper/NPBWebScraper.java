	package com.example.scraper;
	
	import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.baseball.entity.AtBatResult;
import com.example.baseball.entity.BaseballGame;
import com.example.baseball.entity.BaseballTeam;
import com.example.baseball.entity.VBaseballPlayerHistoryRegular;
import com.example.baseball.service.AtBatResultService;
import com.example.baseball.service.BaseballGameService;
import com.example.baseball.service.BaseballTeamService;
import com.example.baseball.service.VBaseballPlayerHistoryRegularService;
import com.example.scraper.entity.BatterPitcherInfoList;
import com.example.scraper.entity.BatterResults;
import com.example.scraper.entity.ConvBatterResults;
import com.example.scraper.entity.ConvPitcherResults;
import com.example.scraper.entity.PitcherResults;
	
	@Component
	public class NPBWebScraper {
	
		@Autowired
		private BaseballTeamService baseballTeamService;
		@Autowired
		private BaseballGameService baseballGameService;
		@Autowired
		private VBaseballPlayerHistoryRegularService vBaseballPlayerHistoryRegularService;
		@Autowired
		private AtBatResultService atBatResultService;
	
		private static final String BASE_URL = "https://npb.jp/games/2024/schedule_%02d_detail.html";
	
		public void scrapeData() throws IOException, ParseException {
			for (int month = 9; month <= 10; month++) {
				List<String> gameLinks = getGameLinks(month);
				filterOutSpecificDates(gameLinks); // 当日の試合をフィルタリング
				for (String gameLink : gameLinks) {
					// 打席結果を取得
					BatterPitcherInfoList batterPitcherInfo = parseGameDetails(gameLink);
					// 試合結果を取得
					BaseballGame baseballGame = getGameInfo(gameLink);
					convertPlayer(batterPitcherInfo, baseballGame);
				}
			}
			System.out.println("終わり");
		}
	
		/**
		 * 取得対象の試合のリンクを取得
		 * @param month
		 * @return
		 * @throws IOException
		 */
		private List<String> getGameLinks(int month) throws IOException {
			List<BaseballGame>  baseballGameList = baseballGameService.findAll();
			// 既存のゲームの日付をセットに変換
		    Set<String> existingGameDates = baseballGameList.stream()
		        .map(game -> new SimpleDateFormat("MMdd").format(game.getGameDate())) // MMdd形式に変換
		        .collect(Collectors.toSet());

		    String url = String.format(BASE_URL, month);
		    Document doc = Jsoup.connect(url).get();
		    List<String> gameLinks = new ArrayList<>();
		    Elements links = doc.select("a[href]");
		    Pattern pattern = Pattern.compile("/scores/2024/(\\d{4})/");

		    for (Element link : links) {
		        Matcher matcher = pattern.matcher(link.attr("href"));
		        if (matcher.find()) {
		            // マッチした場合のみ、グループ1を取得
		            String linkDate = matcher.group(1);

		            // 既存のゲームの日付に含まれていない場合のみリンクを追加
		            if (!existingGameDates.contains(linkDate)) {
		                // 親要素のチェック
		                Element parent = link.parent();
		                Elements cancelDivs = parent.select("div.cancel");
		                if (cancelDivs.isEmpty()) {
		                    gameLinks.add("https://npb.jp" + link.attr("href") + "box.html");
		                }
		            }
		        }
		    }
		    return gameLinks;
		}
	
		private void filterOutSpecificDates(List<String> gameLinks) {
			LocalDate today = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd");
			String todayStr = today.format(formatter);
	
			// 7/23 と 7/24 を除外
			String excludeDate1 = "0723";
			String excludeDate2 = "0724";
	
			gameLinks.removeIf(
					link -> link.contains(todayStr) || link.contains(excludeDate1) || link.contains(excludeDate2));
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
				return returnList;
			}
			String topTeam = teamNames.get(0).text();
			String bottomTeam = teamNames.get(1).text();
	
			Elements topBattingTables = doc.select("div#table_top_b");
			Elements topPitcherTables = doc.select("div#table_top_p");
			Elements bottomBattingTables = doc.select("div#table_bottom_b");
			Elements bottomPitcherTables = doc.select("div#table_bottom_p");
			// 投手と打者の結果がない場合
			if (topBattingTables.size() < 1 || topPitcherTables.size() < 1 || bottomBattingTables.size() < 1
					|| bottomPitcherTables.size() < 1) {
				return returnList;
			}
			// 上のチームのバッター情報を取得
			List<BatterResults> topBatterResultList = getBatterResults(topBattingTables, topTeam);
			// 上のチームのピッチャー情報を取得
			List<PitcherResults> topPitcherResultList = getPitcherResults(topPitcherTables, topTeam);
			// 下のチームのバッター情報を取得
			List<BatterResults> bottomBatterResultList = getBatterResults(bottomBattingTables, bottomTeam);
			// 下のチームのピッチャー情報を取得
			List<PitcherResults> bottomPitcherResultList = getPitcherResults(bottomPitcherTables, bottomTeam);
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
			Element topTeamElement = doc.select("tr.top th span.hide_sp").first();
			String topTeam = topTeamElement.text();
			BaseballTeam topBaseballTeam = baseballTeamService.findByTeamNm(topTeam);
	
			Element topTeamScoreElement = doc.select("tr.top td.total-1").first();
			String topTeamScore = topTeamScoreElement.text();
	
			// ボトムチームとスコアの取得
			Element bottomTeamElement = doc.select("tr.bottom th span.hide_sp").first();
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
		private List<PitcherResults> getPitcherResults(Elements pitcherTables, String team) {
			List<PitcherResults> pitcherResultsList = new ArrayList<>();
			Elements pitcherRows = pitcherTables.get(0).select("tr");
			for (Element pitcherRow : pitcherRows) {
				Elements playerCell = pitcherRow.select("td.player");
				Elements cells = pitcherRow.select("td");
				if (!playerCell.isEmpty() && cells.size() > 3) {
					PitcherResults pitcherResults = new PitcherResults();
					String pitcherName = playerCell.get(0).text().trim();
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
		private List<BatterResults> getBatterResults(Elements battingTables, String team) {
			List<BatterResults> batterResultsList = new ArrayList<>();
			int rowSize = battingTables.get(0).select("tr .inn").size();
			Elements battingTable = battingTables.get(0).select("table tbody");
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
					String playerName = cells.get(2).text().trim();
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
					String playerName = cells.get(2).text().trim();
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
					String playerName = cells.get(2).text().trim();
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
					VBaseballPlayerHistoryRegular baseballPlayer = vBaseballPlayerHistoryRegularService.findByTeamNmAndTeamId(
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
					VBaseballPlayerHistoryRegular baseballPlayer = vBaseballPlayerHistoryRegularService.findByTeamNmAndTeamId(
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
					VBaseballPlayerHistoryRegular baseballPlayer = vBaseballPlayerHistoryRegularService.findByTeamNmAndTeamId(
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
					VBaseballPlayerHistoryRegular baseballPlayer = vBaseballPlayerHistoryRegularService.findByTeamNmAndTeamId(
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
			// 上のピッチャーと下のバッターの対戦結果を取得
			for (ConvPitcherResults pitcherResult : topConvPitcherResultList) {
				List<AtBatResult> atBatResultList = new ArrayList<>();
				// ピッチャーの対戦した打者分ループ
				for (int i = 0; pitcherResult.getMatchNumber() > i; i++) {
					AtBatResult atBatResult = new AtBatResult();
					atBatResult.setGameId(savedGame.getGameId());
					atBatResult.setBatterId(bottomConvBatterResultList.get(j).getBatterId());
					atBatResult.setPitcherId(pitcherResult.getPitcherId());
					atBatResult.setResult(bottomConvBatterResultList.get(j).getResult());
					atBatResultList.add(atBatResult);
					j++;
				}
				atBatResultService.saveAtBatResult(atBatResultList);
			}
			int s = 0;
			// 下のピッチャーと上のバッターの対戦結果を取得
			for (ConvPitcherResults pitcherResult : bottomConvPitcherResultList) {
				List<AtBatResult> atBatResultList = new ArrayList<>();
				for (int i = 0; pitcherResult.getMatchNumber() > i; i++) {
					AtBatResult atBatResult = new AtBatResult();
					atBatResult.setGameId(savedGame.getGameId());
					atBatResult.setBatterId(topConvBatterResultList.get(s).getBatterId());
					atBatResult.setPitcherId(pitcherResult.getPitcherId());
					atBatResult.setResult(topConvBatterResultList.get(s).getResult());
					atBatResultList.add(atBatResult);
					s++;
				}
				atBatResultService.saveAtBatResult(atBatResultList);
			}
			System.out.print(baseballGame.getGameId() + ",");
		}
	
	}
