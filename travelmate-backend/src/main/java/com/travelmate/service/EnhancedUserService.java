package com.travelmate.service;

import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.exception.UserException;
import com.travelmate.repository.UserRepositoryEnhanced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedUserService {
    
    private final UserRepositoryEnhanced userRepository;
    private final EnhancedJwtService jwtService;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final ActivityTrackingService activityTrackingService;
    private final LocationService locationService;
    private final TwoFactorAuthService twoFactorAuthService;
    
    /**
     * 사용자 등록
     */
    @Transactional
    public UserDto.RegisterResponse registerUser(UserDto.RegisterRequest request, HttpServletRequest httpRequest) {
        // 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException.EmailAlreadyExistsException();
        }
        
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new UserException.NicknameAlreadyExistsException();
        }
        
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("이미 등록된 전화번호입니다.");
        }
        
        // 비밀번호 정책 검증
        passwordService.validatePassword(request.getPassword());
        passwordService.validatePasswordSimilarity(request.getPassword(), 
            request.getEmail(), request.getNickname(), request.getFullName());
        
        // 사용자 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordService.encodePassword(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setFullName(request.getFullName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setTravelStyle(request.getTravelStyle());
        user.setInterests(request.getInterests());
        user.setLanguages(request.getLanguages());
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setLastActivityAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // 이메일 인증 발송
        try {
            emailService.sendEmailVerification(savedUser);
        } catch (Exception e) {
            log.error("이메일 인증 발송 실패: {}", savedUser.getEmail(), e);
            // 실패해도 회원가입은 완료
        }
        
        // 로그인 히스토리 기록
        activityTrackingService.recordLoginHistory(savedUser, httpRequest, 
            "SUCCESS", null);
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(
            savedUser,
            extractDeviceId(httpRequest),
            extractDeviceName(httpRequest),
            activityTrackingService.getClientIpAddress(httpRequest),
            httpRequest.getHeader("User-Agent")
        );
        
        log.info("새로운 사용자 등록 완료: {}", savedUser.getEmail());
        
        return UserDto.RegisterResponse.builder()
            .user(convertToDto(savedUser))
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
    
    /**
     * 로그인
     */
    @Transactional
    public UserDto.LoginResponse loginUser(UserDto.LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = activityTrackingService.getClientIpAddress(httpRequest);
        
        // Rate limiting 체크
        if (!activityTrackingService.checkAndRecordLoginAttempt(request.getEmail(), ipAddress, false)) {
            throw new UserException.TooManyLoginAttemptsException();
        }
        
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                activityTrackingService.recordLoginHistory(null, httpRequest, 
                    "FAILED", "User not found");
                return new UserException.UserNotFoundException();
            });
        
        // 계정 잠금 확인
        if (user.isAccountLocked()) {
            throw new UserException.AccountLockedException(
                (int) Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes());
        }
        
        // 비밀번호 검증
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            user.incrementLoginAttempts();
            userRepository.save(user);
            
            activityTrackingService.recordLoginHistory(user, httpRequest, 
                "FAILED", "Invalid password");
            throw new UserException.InvalidPasswordException();
        }
        
        // 이메일 인증 확인 (선택적)
        if (!user.getIsEmailVerified()) {
            log.warn("미인증 이메일로 로그인 시도: {}", user.getEmail());
            // throw new UserException.EmailNotVerifiedException(); // 필요시 주석 해제
        }
        
        // 2FA 검증
        if (request.getTwoFactorCode() != null) {
            if (!twoFactorAuthService.verifyTwoFactorCode(user, request.getTwoFactorCode())) {
                activityTrackingService.recordLoginHistory(user, httpRequest, 
                    "FAILED", "Invalid 2FA code");
                throw new UserException.InvalidVerificationCodeException();
            }
        } else if (twoFactorAuthService.isTwoFactorEnabled(user)) {
            // 2FA가 활성화되어 있으나 코드가 제공되지 않음
            return UserDto.LoginResponse.builder()
                .requiresTwoFactor(true)
                .message("2FA 인증이 필요합니다.")
                .build();
        }
        
        // 로그인 성공 처리
        user.resetLoginAttempts();
        user.setLastActivityAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 의심스러운 로그인 감지
        activityTrackingService.detectSuspiciousLogin(user, ipAddress, 
            httpRequest.getHeader("User-Agent"));
        
        // 성공적인 로그인 기록
        activityTrackingService.checkAndRecordLoginAttempt(request.getEmail(), ipAddress, true);
        activityTrackingService.recordLoginHistory(user, httpRequest, 
            "SUCCESS", null);
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(
            user,
            extractDeviceId(httpRequest),
            extractDeviceName(httpRequest),
            ipAddress,
            httpRequest.getHeader("User-Agent")
        );
        
        log.info("로그인 성공: {}", user.getEmail());
        
        return UserDto.LoginResponse.builder()
            .user(convertToDto(user))
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .requiresTwoFactor(false)
            .build();
    }
    
    /**
     * 사용자 프로필 조회 (캐시 적용)
     */
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserDto.Response getUserProfile(Long userId) {
        User user = userRepository.findWithDetailsById(userId)
            .orElseThrow(() -> new UserException.UserNotFoundException(userId));
        
        if (!user.getIsActive()) {
            throw new UserException.UserNotFoundException(userId);
        }
        
        return convertToDto(user);
    }
    
    /**
     * 사용자 프로필 업데이트 (캐시 갱신)
     */
    @CachePut(value = "users", key = "#userId")
    @Transactional
    public UserDto.Response updateUserProfile(Long userId, UserDto.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException.UserNotFoundException(userId));
        
        // 닉네임 중복 확인
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new UserException.NicknameAlreadyExistsException();
            }
            user.setNickname(request.getNickname());
        }
        
        // 다른 필드들 업데이트
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getTravelStyle() != null) user.setTravelStyle(request.getTravelStyle());
        if (request.getInterests() != null) user.setInterests(request.getInterests());
        if (request.getLanguages() != null) user.setLanguages(request.getLanguages());
        
        User savedUser = userRepository.save(user);
        
        log.info("사용자 프로필 업데이트: userId={}", userId);
        return convertToDto(savedUser);
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserDto.Response getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UserException.UserNotFoundException();
        }
        return getUserProfile(userId);
    }
    
    /**
     * 사용자 검색 (캐시 적용)
     */
    @Cacheable(value = "user_search", key = "#keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<UserDto.Response> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, pageable);
        return users.map(this::convertToDto);
    }
    
    /**
     * 필터로 사용자 검색
     */
    @Transactional(readOnly = true)
    public Page<UserDto.Response> findUsersByFilters(Integer ageMin, Integer ageMax, 
                                                    User.Gender gender, User.TravelStyle travelStyle, 
                                                    Pageable pageable) {
        Page<User> users = userRepository.findByFilters(ageMin, ageMax, gender, travelStyle, pageable);
        return users.map(this::convertToDto);
    }
    
    /**
     * 사용자 계정 비활성화 (캐시 삭제)
     */
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException.UserNotFoundException(userId));
        
        user.setIsActive(false);
        user.setDeletionRequestedAt(LocalDateTime.now().plusDays(30)); // 30일 후 완전 삭제
        
        // 모든 토큰 무효화
        jwtService.revokeAllUserTokens(user);
        
        userRepository.save(user);
        
        log.info("사용자 계정 비활성화: userId={}", userId);
    }
    
    /**
     * DTO 변환
     */
    private UserDto.Response convertToDto(User user) {
        return UserDto.Response.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .fullName(user.getFullName())
            .age(user.getAge())
            .gender(user.getGender())
            .profileImageUrl(user.getProfileImageUrl())
            .bio(user.getBio())
            .travelStyle(user.getTravelStyle())
            .interests(user.getInterests())
            .languages(user.getLanguages())
            .rating(user.getRating())
            .reviewCount(user.getReviewCount())
            .isEmailVerified(user.getIsEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .lastActivityAt(user.getLastActivityAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
    
    /**
     * 현재 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format: {}", authentication.getName());
            }
        }
        return null;
    }
    
    /**
     * HTTP 요청에서 기기 ID 추출
     */
    private String extractDeviceId(HttpServletRequest request) {
        // 클라이언트에서 전송한 기기 ID 또는 User-Agent 기반 생성
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId == null) {
            String userAgent = request.getHeader("User-Agent");
            deviceId = userAgent != null ? Integer.toString(userAgent.hashCode()) : "unknown";
        }
        return deviceId;
    }
    
    /**
     * HTTP 요청에서 기기 이름 추출
     */
    private String extractDeviceName(HttpServletRequest request) {
        String deviceName = request.getHeader("X-Device-Name");
        if (deviceName == null) {
            String userAgent = request.getHeader("User-Agent");
            deviceName = userAgent != null ? 
                activityTrackingService.extractDeviceType(userAgent) + " Device" : "Unknown Device";
        }
        return deviceName;
    }
}