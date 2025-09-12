package com.travelmate.config;

import com.travelmate.security.JwtAuthenticationEntryPoint;
import com.travelmate.security.JwtAuthenticationFilter;
import com.travelmate.security.RateLimitingFilter;
import com.travelmate.security.RequestLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfigEnhanced {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final RequestLoggingFilter requestLoggingFilter;

    // 공개 접근 허용 엔드포인트
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/users/login",
        "/api/users/register",
        "/api/health",
        "/actuator/health",
        "/h2-console/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/error"
    };

    // 관리자 전용 엔드포인트
    private static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/**",
        "/actuator/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화 (JWT 사용)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 비활성화
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 요청 인증 설정
            .authorizeHttpRequests(auth -> auth
                // 공개 엔드포인트
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // 관리자 전용 엔드포인트
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                
                // WebSocket 연결
                .requestMatchers("/ws/**").authenticated()
                
                // 파일 업로드
                .requestMatchers("/api/files/**").authenticated()
                
                // 사용자 API (자신의 정보만 접근 가능)
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers("/api/users/{id}/**").access(
                    "@securityService.hasUserAccess(authentication, #id)"
                )
                
                // 그룹 API
                .requestMatchers("/api/travel-groups").authenticated()
                .requestMatchers("/api/travel-groups/{id}").authenticated()
                .requestMatchers("/api/travel-groups/{id}/join").authenticated()
                .requestMatchers("/api/travel-groups/{id}/leave").authenticated()
                
                // 채팅 API
                .requestMatchers("/api/chat/**").authenticated()
                
                // 추천 API
                .requestMatchers("/api/recommendations/**").authenticated()
                
                // 기타 모든 요청
                .anyRequest().authenticated()
            )
            
            // 예외 처리
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 보안 헤더 설정
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .includeSubdomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .and()
                .httpPublicKeyPinning(hpkp -> hpkp
                    .addSha256Pins(
                        "9SLklscvzMYj8f+52lp5ze/hY0CFHyLSPQzSpYYIBm8=",
                        "YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="
                    )
                    .maxAgeInSeconds(5184000)
                )
            );

        // 커스텀 필터 추가
        http.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 도메인들 (운영 환경에서는 구체적으로 지정)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",  // React 개발 서버
            "http://localhost:8081",  // React Native Metro
            "https://travelmate.app", // 운영 도메인
            "https://admin.travelmate.app" // 관리자 도메인
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 높은 강도 설정
    }
}