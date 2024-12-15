// MatchResultSearchRequest.java
package com.example.baseball.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchResultSearchRequest {

    // pitcherTeamIdは任意（required=false）のため、バリデーションなし
    // ただし、与えられた場合に桁数制限をかけたい場合は@Size等使用可能
    // （nullの場合はチェックされない）
    @Pattern(regexp = "\\d+", message = "pitcherTeamIdは数値で入力してください", groups = OptionalCheck.class)
    @Size(max = 2, message = "pitcherTeamIdは2桁以下で入力してください", groups = OptionalCheck.class)
    private String pitcherTeamId;

    @Pattern(regexp = "\\d+", message = "batterTeamIdは数値で入力してください", groups = OptionalCheck.class)
    @Size(max = 2, message = "batterTeamIdは2桁以下で入力してください", groups = OptionalCheck.class)
    private String batterTeamId;

    // pitcherId 5桁以下の数値なら@Size(max=5)
    // required=falseのため、nullや空文字は許容。ただし値がある時だけチェックしたい場合はgroupsで制御
    @Pattern(regexp = "\\d+", message = "pitcherIdは数値で入力してください", groups = OptionalCheck.class)
    @Size(max = 5, message = "pitcherIdは5桁以下で入力してください", groups = OptionalCheck.class)
    private String pitcherId;

    @Pattern(regexp = "\\d+", message = "batterIdは数値で入力してください", groups = OptionalCheck.class)
    @Size(max = 5, message = "batterIdは5桁以下で入力してください", groups = OptionalCheck.class)
    private String batterId;

    // selectedYearも任意。指定された場合4桁以下
    @Size(max = 4, message = "selectedYearは4桁以下で入力してください", groups = OptionalCheck.class)
    private String selectedYear;

    // 任意項目のバリデーションを分けたい場合、groupsを使う
    // ここではOptionalCheck groupを定義し、値がnullや空でなければチェックするといった応用が可能
    public interface OptionalCheck {}
}