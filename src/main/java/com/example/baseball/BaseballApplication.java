package com.example.baseball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@EnableScheduling herokuのapiで定期実行するのでコメントアウト
@ComponentScan(basePackages = {"com.example.scraper", "com.example.baseball"})
//@ComponentScan(basePackages = {"com.example.baseball"})
public class BaseballApplication {
    public static void main(String[] args) {
        SpringApplication.run(BaseballApplication.class, args);
    }
}