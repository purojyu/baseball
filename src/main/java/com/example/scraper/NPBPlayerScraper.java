package com.example.scraper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

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

@Component
public class NPBPlayerScraper {
	@Autowired
	private BaseballPlayerService baseballPlayerService;
	@Autowired
	private BaseballPlayerHistoryService baseballPlayerHistoryService;
	
	// 共通の DateTimeFormatter を定義
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");


	// 各球団のURLのローマ字部分をリストで管理
	private static final List<String> teams = Arrays.asList(
			"l", "h", "f", "m", "bs", "e", // パリーグ
			"d", "s", "g", "t", "c", "yb" // セリーグ
	);

	private static final List<String> years = Arrays.asList(
			"2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016",
			"2017",
			"2018", "2019", "2020", "2021", "2022", "2023", "2024");

    public void getNpbPlayer() {
        for (String year : years) {
            // シーズン途中新規加入
            String newPlayerUrl = "https://npb.jp/announcement/" + year + "/pn_registered.html";
            try {
                Document doc = Jsoup.connect(newPlayerUrl).get();
                Element threeColumnWrap = doc.selectFirst("div.table_normal");
                Elements trs = threeColumnWrap.select("tr");
                for (Element tr : trs) {
                    String date = tr.selectFirst("th.pndate").text().trim();
                    String team = tr.selectFirst("td.pnteam").text().trim();
                    String name = tr.selectFirst("td.pnname").text().trim().replace("　", " ");
                    String position = tr.selectFirst("td.pnpos").text().trim();
                    int teamId = convTeam(team);
                    System.out.print(name);
                    BaseballPlayer baseballPlayer = baseballPlayerService.findByplayerNm(name);
                    if (baseballPlayer == null) {
                        String positionCd = convPosition(position);
                        // 選手を追加
                        savePlayerAndHistory(name, positionCd, date, teamId);
                    } else {
                        // メジャー帰り等の選手の場合
                        savePlayerHistory(name, date, teamId);
                    }
                }
                System.out.println("新規加入");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // トレード
            String changePlayerUrl = "https://npb.jp/announcement/" + year + "/pn_traded.html";
            try {
                Document doc = Jsoup.connect(changePlayerUrl).get();
                Elements changePlayers = doc.select("div.table_normal_noborder");
                // トレードの日付が早い順にループ
                for (int i = changePlayers.size() - 1; i >= 0; i--) {
                    Elements trs = changePlayers.get(i).select("table > tbody > tr");
                    for (Element tr : trs) {
                        String date = tr.selectFirst("th.trdate").text().trim();
                        String playerName = tr.selectFirst("td.trname").text().trim().replace("　", " ");
                        String fromTeam = tr.select("td[class*='trteam']").get(0).text().trim();
                        String toTeam = tr.select("td[class*='trteam']").get(1).text().trim();
                        // 「〃」がある場合は一つ上の行を見る
                        if (StringUtils.equals(date, "〃") || StringUtils.equals(fromTeam, "〃")
                                || StringUtils.equals(toTeam, "〃")) {
                            for (int j = i - 1; j >= 0; j--) {
                                Elements nextTrs = changePlayers.get(j).select("table > tbody > tr");
                                for (Element nextTr : nextTrs) {
                                    date = nextTr.selectFirst("th.trdate").text().trim();
                                    fromTeam = nextTr.select("td[class*='trteam']").get(0).text().trim();
                                    toTeam = nextTr.select("td[class*='trteam']").get(1).text().trim();
                                }
                            }
                            // 「〃」でない値が見つかった場合はループを抜ける
                            if (!StringUtils.equals(date, "〃") && !StringUtils.equals(fromTeam, "〃")
                                    && !StringUtils.equals(toTeam, "〃")) {
                                break;
                            }
                        }
                        System.out.print(playerName);
                        changePlayer(playerName, (long) convTeam(fromTeam), (long) convTeam(toTeam), date);
                    }
                }
                System.out.print("トレード");
            } catch (IOException e) {
                e.printStackTrace();
            }

			// 現役ドラフト
			try {
				String activeDraftUrl = "https://c.npb.jp/geneki_draft/" + year + "/";
				URL url = new URL(activeDraftUrl);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setRequestMethod("HEAD");
				int responseCode = huc.getResponseCode();
				// 現役ドラフトのリンクが存在する場合(最近開始した制度のため)
				if (responseCode == HttpURLConnection.HTTP_OK) {
					Document doc = Jsoup.connect(activeDraftUrl).get();
					Elements trs = doc.select("div.table_normal.sp_table tbody tr");
					for (Element tr : trs) {
						// 指名球団を取得
						String fromTeam = tr.select("th.team").text().trim();
						// 選手名を取得
						String playerName = tr.select("td.name a").text().trim().replace("　", " ");
						;
						// 現所属球団を取得
						String toTeam = tr.select("td.team").text().trim();
						System.out.print(playerName);
						changePlayer(playerName, (long) convTeam(fromTeam), (long) convTeam(toTeam), year + "/12/1");
					}
					System.out.print("現役ドラフト");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// FA宣言選手
			String faPlayerUrl = "https://npb.jp/announcement/" + year + "/fa_filed2.html";
			try {
				Document doc = Jsoup.connect(faPlayerUrl).get();
				Elements trs = doc.select("div.table_normal_noborder.scroll_wrapper tbody tr");
				for (Element tr : trs) {
					String playerName = tr.select("td.pnname a").text().trim().replace("　", " ");
					// クラス `pnteam` の要素を取得（移籍元と移籍先）
					Elements pnteamElements = tr.select("td.pnteam");
					// 移籍元
					int fromTeamId = convTeam(pnteamElements.first().text().trim());
					int toTeamId = 0;
					// MLB移籍以外の場合
					if (pnteamElements.size() > 1) {
						// 移籍先取得
						toTeamId = convTeam(pnteamElements.last().text().trim());
						String date = tr.select("td.pndate").text().trim();
						// 別のチームに移籍した場合
						if (fromTeamId != toTeamId) {
							System.out.print(playerName);
							changePlayer(playerName, (long) fromTeamId, (long) toTeamId, date);
						}
					}
					// MLB移籍の場合
					else {
						System.out.print(playerName);
						endHistoryPlayer(playerName, (long) fromTeamId, year + "/1/1");
					}
				}
				System.out.print("FA宣言");
			} catch (IOException e) {
				e.printStackTrace();
			}

//			// 自由契約
//			try {
//				String freePlayerUrl = "https://npb.jp/announcement/" + year + "/pn_released.html";
//				Document doc = Jsoup.connect(freePlayerUrl).get();
//				Elements trs = doc.select("div.table_normal.scroll_wrapper tbody tr");
//				for (Element tr : trs) {
//					// pnteamが空白でない行のみ処理
//					String team = tr.select("td.pnteam").text().trim();
//					if (!team.isEmpty()) {
//						// pndate, pnteam, pnnameの情報を取得
//						String date = tr.select("th.pndate").text().trim();
//						String playerName = tr.select("td.pnname").text().trim().replace("　", " ");
//						 int index = playerName.indexOf("※");
//					        if (index != -1) {
//					        	playerName = playerName.substring(0, index);
//					        }
//						System.out.print(playerName);
//						endHistoryPlayer(playerName, (long) convTeam(team), date);
//					}
//				}
//				System.out.print("自由契約");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//			// 引退
//			try {
//				String endPlayerUrl = "https://npb.jp/announcement/" + year + "pn_retired.html";
//				Document doc = Jsoup.connect(endPlayerUrl).get();
//	            Elements trs = doc.select("div.table_normal.scroll_wrapper tbody tr");
//	            for (Element tr : trs) {
//	                String team = tr.select("td.pnteam").text().trim();
//	                    String date = tr.select("th.pndate").text().trim();
//	                    String playerName = tr.select("td.pnname a").text().trim().replace("　", " ");
//	                    System.out.print(playerName);
//	                    endHistoryPlayer(playerName, (long)convTeam(team),date);
//	                }
//	            System.out.print("引退");
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
			 // 契約保留名簿(ドラフト等の新加入選手を登録する)
            for (String team : teams) {
                String nextYearPlayerUrl = "https://npb.jp/announcement/" + year + "/reserved_" + team + ".html";
                try {
                    Document doc = Jsoup.connect(nextYearPlayerUrl).get();
                    Element threeColumnWrap = doc.selectFirst("div.three_column_wrap");
                    if (threeColumnWrap == null) {
                        System.out.println("three_column_wrap が見つかりませんでした。");
                    }
                    Elements rows = threeColumnWrap.select("tr");
                    for (Element row : rows) {
                        Element positionElement = row.selectFirst("th.pos");
                        Element nameElement = row.selectFirst("td");
                        if (positionElement != null && nameElement != null) {
                            String position = positionElement.text().trim();
                            String name = nameElement.text().trim().replace("　", " ");
                            int teamId = getTeamIdFromTeamCode(team);
                            String nextYear = String.valueOf(Integer.parseInt(year) + 1);
                            BaseballPlayer baseballPlayer = baseballPlayerService.findByplayerNm(name);
                            // 新加入(ドラフト等)の場合
                            if (baseballPlayer == null) {
                                // ポジションをポジションCDに変更
                                String positionCd = convPosition(position);
                                System.out.print(name);
                                savePlayerAndHistory(name, positionCd, nextYear + "/01/01", teamId);
                            } else {
                                BaseballPlayerHistory baseballPlayerHistory = baseballPlayerHistoryService.findByPlayerIdAndteamId(
                                        baseballPlayer.getPlayerId(), (long) teamId);
                                // チームと選手で該当する履歴がない場合
                                if (baseballPlayerHistory == null) {
                                    BaseballPlayerHistory updBaseballPlayerHistory = baseballPlayerHistoryService.findByPlayerId(
                                            baseballPlayer.getPlayerId());
                                    updBaseballPlayerHistory.setEndDate(parseDate(year + "/01/01"));
                                    baseballPlayerHistoryService.saveBaseballPlayerHistory(updBaseballPlayerHistory);
                                    BaseballPlayerHistory insBaseballPlayerHistory = new BaseballPlayerHistory();
                                    insBaseballPlayerHistory.setPlayerId(baseballPlayer.getPlayerId());
                                    insBaseballPlayerHistory.setTeamId((long) teamId);
                                    insBaseballPlayerHistory.setStartDate(parseDate(nextYear + "/01/01"));
                                    baseballPlayerHistoryService.saveBaseballPlayerHistory(insBaseballPlayerHistory);
                                }else {
                                	
                                }
                            }
                        }
                    }
                    System.out.print("契約保留" + team);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.print(year);
        }
        System.out.print("終わり");
    }

    /**
     * 日付文字列を LocalDate に変換する共通メソッド
     */
    private LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * チームコードからチームIDを取得
     */
    private int getTeamIdFromTeamCode(String teamCode) {
        switch (teamCode) {
            case "l":
                return 9;  // 埼玉西武ライオンズ
            case "h":
                return 7;  // 福岡ソフトバンクホークス
            case "f":
                return 8;  // 北海道日本ハムファイターズ
            case "m":
                return 11; // 千葉ロッテマリーンズ
            case "bs":
                return 10; // オリックス・バファローズ
            case "e":
                return 12; // 東北楽天ゴールデンイーグルス
            case "d":
                return 6;  // 中日ドラゴンズ
            case "s":
                return 1;  // 東京ヤクルトスワローズ
            case "g":
                return 2;  // 読売ジャイアンツ
            case "t":
                return 4;  // 阪神タイガース
            case "c":
                return 5;  // 広島東洋カープ
            case "yb":
                return 3;  // 横浜DeNAベイスターズ
            default:
                return 0;
        }
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
        if (team.contains("西武")) {
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
        } else if (team.contains("横浜")) {
            return 3; // 横浜DeNAベイスターズ
        } else {
            return 0;
        }
    }

    /**
     * BASEBALL_PLAYERとBASEBALL_PLAYER_HISTORYに登録
     */
    public void savePlayerAndHistory(String name, String positionCd, String date, int teamId) {
        BaseballPlayer basebaPlayer = new BaseballPlayer();
        basebaPlayer.setPlayerNm(name);
        basebaPlayer.setPosition(positionCd);
        BaseballPlayer saveBasebaPlayer = baseballPlayerService.savePlayer(basebaPlayer);
        BaseballPlayerHistory baseballPlayerHistory = new BaseballPlayerHistory();

        baseballPlayerHistory.setPlayerId(saveBasebaPlayer.getPlayerId());
        baseballPlayerHistory.setTeamId((long) teamId);
        baseballPlayerHistory.setStartDate(parseDate(date)); // 共通メソッドを使用
        baseballPlayerHistoryService.saveBaseballPlayerHistory(baseballPlayerHistory);
    }

    /**
     * BASEBALL_PLAYER_HISTORYに登録
     */
    public void savePlayerHistory(String name, String date, int teamId) {
        BaseballPlayer basebaPlayer = baseballPlayerService.findByplayerNm(name);
        BaseballPlayerHistory baseballPlayerHistory = new BaseballPlayerHistory();

        baseballPlayerHistory.setPlayerId(basebaPlayer.getPlayerId());
        baseballPlayerHistory.setTeamId((long) teamId);
        baseballPlayerHistory.setStartDate(parseDate(date)); // 共通メソッドを使用
        baseballPlayerHistoryService.saveBaseballPlayerHistory(baseballPlayerHistory);
    }

    // トレード、現役ドラフト用
    public void changePlayer(String playerName, Long fromTeamId, Long toTeamId, String date) {
        BaseballPlayer baseballPlayer = baseballPlayerService.findByplayerNm(playerName);

        BaseballPlayerHistory updBaseballPlayerHistory = baseballPlayerHistoryService.findByPlayerIdAndteamId(
                baseballPlayer.getPlayerId(), fromTeamId);
        updBaseballPlayerHistory.setEndDate(parseDate(date)); // 共通メソッドを使用
        baseballPlayerHistoryService.saveBaseballPlayerHistory(updBaseballPlayerHistory);

        BaseballPlayerHistory insBaseballPlayerHistory = new BaseballPlayerHistory();
        insBaseballPlayerHistory.setPlayerId(baseballPlayer.getPlayerId());
        insBaseballPlayerHistory.setStartDate(parseDate(date)); // 共通メソッドを使用
        insBaseballPlayerHistory.setTeamId(toTeamId);
        baseballPlayerHistoryService.saveBaseballPlayerHistory(insBaseballPlayerHistory);
    }

    // 自由契約、引退、メジャー移籍
    public void endHistoryPlayer(String playerName, Long teamId, String date) {
        BaseballPlayer baseballPlayer = baseballPlayerService.findByplayerNm(playerName);
        BaseballPlayerHistory updBaseballPlayerHistory = baseballPlayerHistoryService.findByPlayerIdAndteamId(
                baseballPlayer.getPlayerId(), teamId);

        updBaseballPlayerHistory.setEndDate(parseDate(date)); // 共通メソッドを使用
        baseballPlayerHistoryService.saveBaseballPlayerHistory(updBaseballPlayerHistory);
    }
}