package com.example.baseball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPlayerListRequest {

    @NotBlank(message = "teamIdは必須です")
    @Pattern(regexp = "\\d+", message = "teamIdは数値で入力してください")
    @Size(max = 2, message = "teamIdは2桁以下で入力してください")
    private String teamId;

    @NotBlank(message = "yearは必須です")
    @Size(max = 4, message = "yearは4桁以下で入力してください")
    private String year;
}