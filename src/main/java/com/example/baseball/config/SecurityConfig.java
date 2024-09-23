package com.example.baseball.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// HTTPSリダイレクトを強制(ローカルではコメントアウト)
				.requiresChannel(channel -> channel.anyRequest().requiresSecure())
				// CSRF保護を無効化（必要に応じて調整）
				.csrf(csrf -> csrf.disable())
				// CORS設定を適用()(ローカルではコメントアウト)
				.cors(Customizer.withDefaults())
				// 認証なしで全てのリクエストを許可
				.authorizeHttpRequests(auth -> auth
						.anyRequest().permitAll());

		return http.build();
	}

	// CORS設定
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 許可するオリジン（末尾にスラッシュは不要）
		configuration.setAllowedOrigins(Arrays.asList("https://baseball-pitcher-vs-batter.com",
				"https://www.baseball-pitcher-vs-batter.com"));
		// 許可するHTTPメソッド
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		// 許可するヘッダー
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization",
				"X-Requested-With"));
		// クレデンシャルの許可
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// 全てのパスにCORS設定を適用
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	// ForwardedHeaderFilterを登録して、リバースプロキシ経由のリクエストを正しく認識
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}
}
