package com.travelmate.service;

import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.travelmate.entity.UserReview;
import com.travelmate.repository.UserReviewRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserReviewRepository userReviewRepository;
    
    public UserDto.Response registerUser(UserDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setTravelStyle(request.getTravelStyle());
        user.setIsActive(true);
        user.setIsLocationEnabled(false);
        user.setIsMatchingEnabled(false);
        
        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 등록: {}", savedUser.getEmail());
        
        return convertToDto(savedUser);
    }
    
    public UserDto.LoginResponse loginUser(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        UserDto.LoginResponse response = new UserDto.LoginResponse();
        response.setToken(token);
        response.setUser(convertToDto(user));
        
        log.info("사용자 로그인: {}", user.getEmail());
        return response;
    }
    
    @Transactional(readOnly = true)
    public UserDto.Response getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return convertToDto(user);
    }
    
    public void updateUserLocation(UserDto.LocationUpdateRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setCurrentLatitude(request.getLatitude());
        user.setCurrentLongitude(request.getLongitude());
        user.setIsLocationEnabled(true);
        
        userRepository.save(user);
        log.debug("사용자 위치 업데이트: {} - ({}, {})", 
            user.getId(), request.getLatitude(), request.getLongitude());
    }
    
    @Transactional(readOnly = true)
    public List<UserDto.Response> getNearbyUsers(Double latitude, Double longitude, Double radiusKm) {
        Long currentUserId = getCurrentUserId(); // JWT에서 추출
        
        List<User> nearbyUsers = userRepository.findNearbyUsers(
            currentUserId, latitude, longitude, radiusKm);
        
        return nearbyUsers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDto.Response> findUsersOnShake(UserDto.ShakeRequest request) {
        // 가속도계 값으로 흔들기 강도 계산
        double shakeIntensity = Math.sqrt(
            Math.pow(request.getAccelerationX(), 2) +
            Math.pow(request.getAccelerationY(), 2) +
            Math.pow(request.getAccelerationZ(), 2)
        );
        
        // 흔들기 강도가 임계값 이상일 때만 검색
        if (shakeIntensity < 15.0) {
            return List.of();
        }
        
        // 흔들기 강도에 따라 검색 반경 조정 (1km ~ 5km)
        double searchRadius = Math.min(5.0, Math.max(1.0, shakeIntensity / 10));
        
        List<User> users = userRepository.findUsersForShake(
            request.getLatitude(), request.getLongitude(), searchRadius);
        
        log.info("폰 흔들기로 {} 반경 {}km 내 {}명의 사용자 발견", 
            request.getUserId(), searchRadius, users.size());
        
        return users.stream()
            .limit(10) // 최대 10명까지만 반환
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private UserDto.Response convertToDto(User user) {
        UserDto.Response dto = new UserDto.Response();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setBio(user.getBio());
        dto.setCurrentLatitude(user.getCurrentLatitude());
        dto.setCurrentLongitude(user.getCurrentLongitude());
        dto.setTravelStyle(user.getTravelStyle());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format: {}", authentication.getName());
            }
        }
        return null;
    }
    
    public UserDto.Response updateUserProfile(Long userId, UserDto.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("이미 존재하는 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        if (request.getTravelStyle() != null) {
            user.setTravelStyle(request.getTravelStyle());
        }
        
        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 업데이트: {}", userId);
        
        return convertToDto(savedUser);
    }
    
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setFcmToken(fcmToken);
        userRepository.save(user);
        log.info("FCM 토큰 업데이트: User {}", userId);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setIsActive(false);
        userRepository.save(user);
        log.info("사용자 계정 비활성화: {}", userId);
    }
    
    public void reportUser(Long reporterId, UserDto.ReportRequest request) {
        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new RuntimeException("신고자를 찾을 수 없습니다."));
        
        User reported = userRepository.findById(request.getReportedUserId())
            .orElseThrow(() -> new RuntimeException("신고 대상자를 찾을 수 없습니다."));
        
        // 신고 로그 생성 (실제로는 별도 Report Entity 필요)
        log.warn("사용자 신고: {} -> {} (사유: {})", reporterId, request.getReportedUserId(), request.getReason());
        
        // TODO: 관리자 알림 및 신고 처리 로직 구현
    }
    
    @Transactional(readOnly = true)
    public List<UserDto.ReviewResponse> getUserReviews(Long userId) {
        List<UserReview> reviews = userReviewRepository.findByReviewedUserId(userId);
        
        return reviews.stream()
            .map(this::convertToReviewDto)
            .collect(Collectors.toList());
    }
    
    public UserDto.ReviewResponse writeReview(Long reviewerId, UserDto.WriteReviewRequest request) {
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new RuntimeException("리뷰어를 찾을 수 없습니다."));
        
        User reviewed = userRepository.findById(request.getReviewedUserId())
            .orElseThrow(() -> new RuntimeException("리뷰 대상자를 찾을 수 없습니다."));
        
        // 중복 리뷰 체크
        if (userReviewRepository.existsByReviewerIdAndReviewedUserId(reviewerId, request.getReviewedUserId())) {
            throw new RuntimeException("이미 해당 사용자에 대한 리뷰를 작성했습니다.");
        }
        
        UserReview review = new UserReview();
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewed);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        UserReview savedReview = userReviewRepository.save(review);
        log.info("사용자 리뷰 작성: {} -> {} (평점: {})", reviewerId, request.getReviewedUserId(), request.getRating());
        
        return convertToReviewDto(savedReview);
    }
    
    private UserDto.ReviewResponse convertToReviewDto(UserReview review) {
        UserDto.ReviewResponse dto = new UserDto.ReviewResponse();
        dto.setId(review.getId());
        dto.setReviewerId(review.getReviewer().getId());
        dto.setReviewerNickname(review.getReviewer().getNickname());
        dto.setReviewerProfileImageUrl(review.getReviewer().getProfileImageUrl());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}