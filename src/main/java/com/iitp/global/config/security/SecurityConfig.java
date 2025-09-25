package com.iitp.global.config.security;

import com.iitp.global.jwt.JwtAuthenticationEntryPoint;
import com.iitp.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource, JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 세션 사용 안함 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 폼 로그인 비활성화
                .formLogin(form -> form.disable())

                // HTTP Basic 비활성화
                .httpBasic(basic -> basic.disable())
                // H2 연결을 위한
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                // 인증 실패 시 처리 EntryPoint 설정
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                                // === 완전 공개 (인증 불필요) ===
                                .requestMatchers(
                                        "/health",                      // 헬스체크
                                        "/error",                       // 에러 페이지
                                        "/",                           // 루트 페이지
                                        "/login.html"                  // 로그인 테스트 페이지
                                ).permitAll()

                                // === Swagger UI ===
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/swagger-ui/index.html"
                                ).permitAll()

                                // === H2 콘솔 ===
                                .requestMatchers("/h2-console/**").permitAll()

                                // === 인증 관련 API (로그인/회원가입) ===
                                .requestMatchers(
                                        "/api/auth/signup/**",          // 회원가입
                                        "/api/auth/login/**",           // 로그인
                                        "/api/auth/refresh"             // 토큰 갱신
                                ).permitAll()

                                // === 회원가입 유효성 검사 API ===
                                .requestMatchers(
                                "/api/members/check/**"         // 닉네임 중복 체크 등
                                 ).permitAll()

                                 // === 인증 필요하지만 모든 사용자 접근 가능 ===
                                 .requestMatchers(
                                         "/api/auth/logout"              // 로그아웃
                                 ).authenticated()

                                 // === 일반 사용자 전용 API ===
                                 .requestMatchers(
                                         "/api/members/profile",         // 프로필 조회
                                         "/api/members/nickname",        // 닉네임 수정
                                         "/api/members/delete",          // 회원 탈퇴
                                         "/api/members/location",        // 위치 관리
                                         "/api/members/me/**",           // 내 정보 관련
                                         "/api/members/cartCount",       // 장바구니 개수
                                         "/api/cart/**",                 // 장바구니 관리
                                         "/api/orders/**",               // 주문 관리
                                         "/api/reviews/**",              // 리뷰 관리
                                         "/api/favorites/**",            // 찜 관리
                                         "/api/payments/**"              // 결제 관리
                                 ).hasRole("USER")

                                 // === 매장 사업자 전용 API ===
                                 .requestMatchers(
                                         "/api/stores/manage/**",        // 매장 관리
                                         "/api/menus/manage/**",         // 메뉴 관리
                                         "/api/business/**"              // 사업자 관련
                                 ).hasRole("STORE")

                                 // === 공통 조회 API (모든 인증된 사용자) ===
                                 .requestMatchers(
                                         "/api/stores/search/**",        // 매장 검색
                                         "/api/stores/*/menus",          // 메뉴 조회
                                         "/api/map/**",                  // 지도 관련
                                         "/api/locations/**"             // 위치 관련
                                 ).authenticated()

                                 // === 이미지 업로드 (인증된 사용자) ===
                                 .requestMatchers("/api/s3/**").authenticated()

                                 // 나머지는 모두 인증 필요
                                 .anyRequest().authenticated()
                )
                // jwt 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 임시로 모든 도메인 허용 추후에 특정 도메인만 허용
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
