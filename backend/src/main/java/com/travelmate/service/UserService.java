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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
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
        // SecurityContext에서 현재 사용자 ID 추출
        // 실제 구현시 Spring Security Context 사용
        return 1L; // 임시 값
    }
}