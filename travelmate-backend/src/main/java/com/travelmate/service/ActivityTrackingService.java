package com.travelmate.service;

import com.travelmate.entity.LoginHistory;
import com.travelmate.entity.User;
import com.travelmate.repository.UserRepositoryEnhanced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityTrackingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepositoryEnhanced userRepository;
    private final EmailService emailService;
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_ATTEMPT_WINDOW_MINUTES = 30;
    private static final String REDIS_LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String REDIS_USER_ACTIVITY_PREFIX = "user_activity:";
    
    /**
     * 사용자 활동 업데이트
     */
    @Transactional
    public void updateUserActivity(User user) {
        user.setLastActivityAt(LocalDateTime.now());
        
        // Redis에 활동 시간 캐싱
        String key = REDIS_USER_ACTIVITY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString(), Duration.ofHours(1));
        
        log.debug("사용자 활동 업데이트: userId={}", user.getId());
    }
    
    /**
     * 로그인 시도 기록 및 제한 체크
     */
    public boolean checkAndRecordLoginAttempt(String email, String ipAddress, boolean success) {
        String key = REDIS_LOGIN_ATTEMPTS_PREFIX + email + ":" + ipAddress;
        
        if (success) {
            // 성공 시 시도 횟수 초기화
            redisTemplate.delete(key);
            return true;
        } else {
            // 실패 시 시도 횟수 증가
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts == 1) {
                redisTemplate.expire(key, LOGIN_ATTEMPT_WINDOW_MINUTES, TimeUnit.MINUTES);
            }
            
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                log.warn("로그인 시도 한계 초과: email={}, ip={}, attempts={}", email, ipAddress, attempts);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 로그인 히스토리 기록
     */
    @Transactional
    public LoginHistory recordLoginHistory(User user, HttpServletRequest request, 
                                         LoginHistory.LoginStatus status, String failureReason) {
        LoginHistory loginHistory = LoginHistory.builder()
            .user(user)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .deviceType(extractDeviceType(request.getHeader("User-Agent")))
            .osName(extractOsName(request.getHeader("User-Agent")))
            .browserName(extractBrowserName(request.getHeader("User-Agent")))
            .status(status)
            .failureReason(failureReason)
            .build();
        
        // 위치 정보가 있다면 추가
        String latitude = request.getHeader("X-Latitude");
        String longitude = request.getHeader("X-Longitude");
        if (latitude != null && longitude != null) {
            try {
                loginHistory.setLatitude(Double.parseDouble(latitude));
                loginHistory.setLongitude(Double.parseDouble(longitude));
            } catch (NumberFormatException e) {
                log.debug("잘못된 위치 정보 헤더: lat={}, lng={}", latitude, longitude);
            }
        }
        
        log.info("로그인 히스토리 기록: userId={}, status={}, ip={}", 
                user.getId(), status, loginHistory.getIpAddress());
        
        return loginHistory;
    }
    
    /**
     * 의심스러운 로그인 감지
     */
    public boolean detectSuspiciousLogin(User user, String currentIp, String userAgent) {
        // 새로운 IP에서의 로그인 체크
        String recentIpsKey = "recent_ips:" + user.getId();
        Boolean isKnownIp = redisTemplate.opsForSet().isMember(recentIpsKey, currentIp);
        
        if (Boolean.FALSE.equals(isKnownIp)) {
            // 새로운 IP 기록
            redisTemplate.opsForSet().add(recentIpsKey, currentIp);
            redisTemplate.expire(recentIpsKey, Duration.ofDays(30));
            
            // 의심스러운 로그인 알림
            emailService.sendSuspiciousLoginAlert(user, currentIp, "Unknown Location");
            
            log.warn("새로운 IP에서 로그인: userId={}, ip={}", user.getId(), currentIp);
            return true;
        }
        
        return false;
    }
    
    /**
     * 계정 잠금 해제 (스케줄러에서 호출)
     */
    @Transactional
    public void unlockExpiredAccounts() {
        var usersToUnlock = userRepository.findUsersToUnlock(LocalDateTime.now());
        
        for (User user : usersToUnlock) {
            user.resetLoginAttempts();
            log.info("계정 잠금 해제: userId={}", user.getId());
        }
        
        if (!usersToUnlock.isEmpty()) {
            userRepository.saveAll(usersToUnlock);
        }
    }
    
    /**
     * 비활성 사용자 정리
     */
    @Transactional
    public void cleanupInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        var inactiveUsers = userRepository.findInactiveUsers(cutoff);
        
        for (User user : inactiveUsers) {
            if (user.getDeletionRequestedAt() == null) {
                user.setDeletionRequestedAt(LocalDateTime.now().plusDays(7));
                log.info("비활성 사용자 삭제 예약: userId={}", user.getId());
            }
        }
        
        if (!inactiveUsers.isEmpty()) {
            userRepository.saveAll(inactiveUsers);
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For", 
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP를 포함할 수 있음
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * User-Agent에서 디바이스 타입 추출
     */
    private String extractDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "Mobile";
        } else if (userAgent.contains("tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
    
    /**
     * User-Agent에서 OS 이름 추출
     */
    private String extractOsName(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("windows")) return "Windows";
        if (userAgent.contains("mac os")) return "macOS";
        if (userAgent.contains("linux")) return "Linux";
        if (userAgent.contains("android")) return "Android";
        if (userAgent.contains("ios") || userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        }
        
        return "Unknown";
    }
    
    /**
     * User-Agent에서 브라우저 이름 추출
     */
    private String extractBrowserName(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("chrome") && !userAgent.contains("chromium")) return "Chrome";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("safari") && !userAgent.contains("chrome")) return "Safari";
        if (userAgent.contains("edge")) return "Edge";
        if (userAgent.contains("opera")) return "Opera";
        
        return "Unknown";
    }
}