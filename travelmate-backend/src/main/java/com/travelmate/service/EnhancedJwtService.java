package com.travelmate.service;

import com.travelmate.entity.RefreshToken;
import com.travelmate.entity.User;
import com.travelmate.exception.UserException;
import com.travelmate.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedJwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    private static final int MAX_DEVICES_PER_USER = 5;

    /**
     * Access Token 생성
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("role", user.getRole().name());
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * Refresh Token 생성 및 저장
     */
    @Transactional
    public String generateRefreshToken(User user, String deviceId, String deviceName, 
                                     String ipAddress, String userAgent) {
        // 기존 활성화된 토큰 수 확인
        long activeTokenCount = refreshTokenRepository.countByUserAndIsRevokedFalse(user);
        
        // 최대 기기 수 초과 시 가장 오래된 토큰 무효화
        if (activeTokenCount >= MAX_DEVICES_PER_USER) {
            // 가장 오래된 토큰 무효화
            var oldestTokens = refreshTokenRepository.findByUserAndIsRevokedFalse(user);
            if (!oldestTokens.isEmpty()) {
                oldestTokens.get(0).setIsRevoked(true);
                refreshTokenRepository.save(oldestTokens.get(0));
            }
        }
        
        // 새 Refresh Token 생성
        String token = generateSecureRandomToken();
        
        RefreshToken refreshToken = RefreshToken.builder()
            .token(token)
            .user(user)
            .deviceId(deviceId)
            .deviceName(deviceName)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
            .build();
        
        refreshTokenRepository.save(refreshToken);
        
        log.info("새로운 Refresh Token 생성: userId={}, deviceId={}", user.getId(), deviceId);
        return token;
    }
    
    /**
     * Refresh Token으로 새 Access Token 생성
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new UserException.InvalidTokenException());
        
        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UserException.TokenExpiredException();
        }
        
        // Token 사용 기록 업데이트
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        
        return generateAccessToken(refreshToken.getUser());
    }
    
    /**
     * JWT Token에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            throw new UserException.InvalidTokenException();
        }
    }
    
    /**
     * JWT Token에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Failed to extract email from token", e);
            throw new UserException.InvalidTokenException();
        }
    }
    
    /**
     * JWT Token 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.debug("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.debug("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.debug("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during token validation", ex);
        }
        return false;
    }
    
    /**
     * 모든 사용자 토큰 무효화 (로그아웃)
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("모든 토큰 무효화: userId={}", user.getId());
    }
    
    /**
     * 특정 기기 토큰 무효화
     */
    @Transactional
    public void revokeDeviceTokens(User user, String deviceId) {
        refreshTokenRepository.revokeAllByUserAndDevice(user, deviceId);
        log.info("기기별 토큰 무효화: userId={}, deviceId={}", user.getId(), deviceId);
    }
    
    /**
     * 만료된 토큰 정리 (스케줄러에서 호출)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("만료된 토큰 정리 완료");
    }
    
    /**
     * Claims 파싱
     */
    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    /**
     * 안전한 랜덤 토큰 생성
     */
    private String generateSecureRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}