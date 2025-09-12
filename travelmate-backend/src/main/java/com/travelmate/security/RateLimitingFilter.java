package com.travelmate.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // 사용자별 제한: 분당 100개 요청
    private static final int USER_REQUESTS_PER_MINUTE = 100;
    // IP별 제한: 분당 200개 요청
    private static final int IP_REQUESTS_PER_MINUTE = 200;

    // 특별 제한이 필요한 엔드포인트들
    private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
        "/api/users/login", 5,      // 로그인: 분당 5회
        "/api/users/register", 3,   // 회원가입: 분당 3회
        "/api/users/shake", 20,     // 폰 흔들기: 분당 20회
        "/api/chat/rooms", 30,      // 채팅방 생성: 분당 30회
        "/api/travel-groups", 10    // 그룹 생성: 분당 10회
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();
        String userId = getUserIdFromToken(request);

        // 특별 제한 체크
        if (hasSpecialLimit(requestURI) && !checkSpecialRateLimit(requestURI, clientIP)) {
            handleRateLimitExceeded(response, "특정 API 호출 한도를 초과했습니다.");
            return;
        }

        // IP 기반 제한 체크
        if (!checkIPRateLimit(clientIP)) {
            handleRateLimitExceeded(response, "IP별 요청 한도를 초과했습니다.");
            return;
        }

        // 사용자 기반 제한 체크 (로그인된 사용자)
        if (userId != null && !checkUserRateLimit(userId)) {
            handleRateLimitExceeded(response, "사용자별 요청 한도를 초과했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasSpecialLimit(String requestURI) {
        return ENDPOINT_LIMITS.keySet().stream()
                .anyMatch(requestURI::startsWith);
    }

    private boolean checkSpecialRateLimit(String requestURI, String clientIP) {
        String endpoint = ENDPOINT_LIMITS.keySet().stream()
                .filter(requestURI::startsWith)
                .findFirst()
                .orElse(null);
        
        if (endpoint == null) return true;

        int limit = ENDPOINT_LIMITS.get(endpoint);
        String bucketKey = "special:" + endpoint + ":" + clientIP;
        
        Bucket bucket = ipBuckets.computeIfAbsent(bucketKey, k -> 
            createBucket(limit, Duration.ofMinutes(1))
        );
        
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Special rate limit exceeded for endpoint {} from IP {}", endpoint, clientIP);
        }
        
        return allowed;
    }

    private boolean checkIPRateLimit(String clientIP) {
        Bucket bucket = ipBuckets.computeIfAbsent(clientIP, k -> 
            createBucket(IP_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("IP rate limit exceeded for IP: {}", clientIP);
        }
        
        return allowed;
    }

    private boolean checkUserRateLimit(String userId) {
        Bucket bucket = userBuckets.computeIfAbsent(userId, k -> 
            createBucket(USER_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("User rate limit exceeded for user: {}", userId);
        }
        
        return allowed;
    }

    private Bucket createBucket(int capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, refillPeriod));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"RATE_LIMIT_EXCEEDED\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
            message,
            java.time.Instant.now().toString()
        );
        
        response.getWriter().write(jsonResponse);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                // JWT 토큰에서 사용자 ID 추출 로직
                // 실제 구현에서는 JwtService를 사용
                return extractUserIdFromJWT(token);
            } catch (Exception e) {
                log.debug("Failed to extract user ID from token", e);
            }
        }
        return null;
    }

    private String extractUserIdFromJWT(String token) {
        // JWT 토큰 파싱 로직
        // 실제 구현에서는 JwtService.getUserIdFromToken(token) 사용
        return null;
    }

    // 주기적으로 오래된 버킷들을 정리하는 메서드
    // @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void cleanupOldBuckets() {
        // 구현: 오래된 버킷들을 제거하여 메모리 사용량 관리
        log.debug("Cleaning up old rate limiting buckets");
    }
}