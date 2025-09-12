package com.travelmate.service;

import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;

    public List<UserDto.Response> getRecommendedUsers(Long userId) {
        // 현재 사용자 조회
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (currentUser.getCurrentLatitude() == null || currentUser.getCurrentLongitude() == null) {
            return Collections.emptyList();
        }
        
        // 모든 활성 사용자 중에서 현재 사용자 제외하고 추천
        List<User> allUsers = userRepository.findAll()
            .stream()
            .filter(user -> !user.getId().equals(userId))
            .filter(User::getIsActive)
            .limit(10) // 성능을 위해 제한
            .collect(Collectors.toList());
        
        return allUsers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<UserDto.Response> findNearbyTravelers(Long userId, Integer radiusKm) {
        // 기본 반경을 10km로 설정
        int radius = radiusKm != null ? radiusKm : 10;
        
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (currentUser.getCurrentLatitude() == null || currentUser.getCurrentLongitude() == null) {
            return Collections.emptyList();
        }
        
        // 간단한 구현으로 모든 사용자를 반환 (실제로는 지리적 쿼리 필요)
        List<User> nearbyUsers = userRepository.findAll()
            .stream()
            .filter(user -> !user.getId().equals(userId))
            .filter(User::getIsActive)
            .filter(User::getIsMatchingEnabled)
            .limit(radius) // 임시로 radius를 개수 제한으로 사용
            .collect(Collectors.toList());
        
        return nearbyUsers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
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
}