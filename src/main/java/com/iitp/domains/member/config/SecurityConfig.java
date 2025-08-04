package com.iitp.domains.member.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안함 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 폼 로그인 비활성화
                .formLogin(form -> form.disable())

                // HTTP Basic 비활성화
                .httpBasic(basic -> basic.disable())

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",        // 인증 관련 API
                                "/api/public/**",      // 공개 API
                                "/error"               // 에러 페이지
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );
    return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 서버 포트
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080"
        ));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 요청할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 클라이언트에서 쿠키나 인증 정보를 함께 보낼 수 있게 허용
        configuration.setAllowCredentials(true);
        // CORS 결과를 브라우저 캐시에 저장할 시간(초 단위) → 1시간
        configuration.setMaxAge(3600L);
        // CORS설정을 적용할 URL 경로
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
