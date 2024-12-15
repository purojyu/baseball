package com.example.baseball.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * APIレスポンスの標準的な形式を提供するためのデータ転送オブジェクト（DTO)
 */
@Getter
public class ResponseDto {
    
    /**
     * レスポンスメッセージを格納するフィールド。
     */
    private final String message;
    
    /**
     * レスポンスに含まれるデータを格納するマップ。
     */
    private final Map<String, Object> data;

    /**
     * プライベートコンストラクタ。ビルダーからのみインスタンス化可能。
     * @param builder ビルダーオブジェクト
     */
    private ResponseDto(Builder builder) {
        this.message = builder.message;
        this.data = builder.data;
    }

    /**
     * ビルダーのインスタンスを取得する静的メソッド。
     * @return Builderオブジェクト
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * ResponseDtoクラスのビルダークラス。
     * ビルダーを使用してResponseDtoオブジェクトを段階的に構築します。
     */
    public static class Builder {
        private String message;
        private Map<String, Object> data = new HashMap<>();

        /**
         * レスポンスメッセージを設定します。
         * @param message レスポンスメッセージ
         * @return Builderオブジェクト（メソッドチェーンのために自身を返す）
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * データマップにキーと値のペアを追加します。
         * 
         * @param key データのキー
         * @param value データの値
         * @return Builderオブジェクト（メソッドチェーンのために自身を返す）
         */
        public Builder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        /**
         * ビルダーで設定された値を使用して、ResponseDtoのインスタンスを生成します。
         * 
         * @return 新しいResponseDtoオブジェクト
         */
        public ResponseDto build() {
            return new ResponseDto(this);
        }
    }
}