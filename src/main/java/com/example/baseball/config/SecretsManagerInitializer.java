package com.example.baseball.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SecretsManagerInitializer implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String secretName = environment.getProperty("DATABASE_URL_SECRET_NAME");
        System.out.println("[SecretsManagerInitializer] DATABASE_URL_SECRET_NAME = " + secretName);
        if (secretName == null || secretName.isBlank()) {
            System.out.println("[SecretsManagerInitializer] SECRET_NAME is empty, skipping");
            return;
        }

        try (SecretsManagerClient client = SecretsManagerClient.create()) {
            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretName)
                            .build()
            );

            String rawUrl = response.secretString();
            Map<String, Object> props = convertToJdbcProperties(rawUrl);
            environment.getPropertySources().addFirst(
                    new MapPropertySource("secretsManager", props)
            );
            System.out.println("[SecretsManagerInitializer] DATABASE_URL injected successfully");
        } catch (Exception e) {
            System.err.println("[SecretsManagerInitializer] Failed to get secret: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve database URL from Secrets Manager", e);
        }
    }

    /**
     * Neon/libpq形式のURLをJDBC形式に変換する。
     * 入力例: postgresql://user:pass@host/db?sslmode=require
     * 出力: DATABASE_URL=jdbc:postgresql://host/db?sslmode=require, username, password
     */
    private Map<String, Object> convertToJdbcProperties(String rawUrl) {
        Map<String, Object> props = new HashMap<>();

        if (rawUrl.startsWith("jdbc:")) {
            props.put("DATABASE_URL", rawUrl);
            return props;
        }

        // postgresql://user:pass@host:port/db?params → URI解析用にschemeを変換
        URI uri = URI.create(rawUrl.replaceFirst("^postgresql://", "http://"));
        String userInfo = uri.getUserInfo();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(host);
        if (port > 0) {
            jdbcUrl.append(":").append(port);
        }
        jdbcUrl.append(path);
        if (query != null && !query.isEmpty()) {
            jdbcUrl.append("?").append(query);
        }

        props.put("DATABASE_URL", jdbcUrl.toString());

        if (userInfo != null && userInfo.contains(":")) {
            String[] parts = userInfo.split(":", 2);
            props.put("spring.datasource.username", parts[0]);
            props.put("spring.datasource.password", parts[1]);
        }

        return props;
    }
}
