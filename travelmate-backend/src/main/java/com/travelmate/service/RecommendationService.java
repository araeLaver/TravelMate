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

    public List<Map<String, Object>> getTravelTips(String destination, String travelStyle) {
        // 간단한 여행 팁 제공 (실제로는 외부 API 연동 필요)
        List<Map<String, Object>> tips = new ArrayList<>();

        if (destination != null && !destination.isEmpty()) {
            tips.add(createTip("교통", destination + "에서는 현지 교통카드를 미리 구매하는 것이 좋습니다."));
            tips.add(createTip("날씨", destination + " 여행 시 날씨를 확인하고 적절한 옷을 준비하세요."));
            tips.add(createTip("음식", destination + "의 유명한 현지 음식을 꼭 체험해보세요."));
        }

        if (travelStyle != null) {
            switch (travelStyle.toUpperCase()) {
                case "BACKPACKER":
                    tips.add(createTip("숙소", "배낭여행객을 위한 저렴한 숙소 정보를 미리 조사하세요."));
                    tips.add(createTip("교통", "현지 대중교통 이용법을 숙지하세요."));
                    break;
                case "LUXURY":
                    tips.add(createTip("숙소", "고급 호텔과 미슐랭 레스토랑 예약을 미리 해두세요."));
                    tips.add(createTip("투어", "프라이빗 투어나 가이드 서비스를 고려해보세요."));
                    break;
                case "CULTURAL":
                    tips.add(createTip("문화", "현지 박물관과 문화유적지의 운영시간을 확인하세요."));
                    tips.add(createTip("에티켓", "현지 문화 예절과 매너를 미리 학습하세요."));
                    break;
                case "ADVENTURE":
                    tips.add(createTip("안전", "모험 활동을 위한 안전 장비를 준비하세요."));
                    tips.add(createTip("보험", "여행자 보험에 모험 활동이 포함되어 있는지 확인하세요."));
                    break;
            }
        }

        if (tips.isEmpty()) {
            tips.add(createTip("준비", "여행 계획을 세우고 필수 물품을 체크리스트로 만들어보세요."));
            tips.add(createTip("안전", "현지 긴급연락처와 대사관 정보를 미리 저장해두세요."));
        }

        return tips;
    }

    private Map<String, Object> createTip(String category, String content) {
        Map<String, Object> tip = new HashMap<>();
        tip.put("category", category);
        tip.put("content", content);
        tip.put("timestamp", new Date());
        return tip;
    }

    public List<Map<String, Object>> getNearbyAttractions(Double latitude, Double longitude, Double radius) {
        // 간단한 근처 관광지 정보 제공 (실제로는 외부 API 연동 필요)
        List<Map<String, Object>> attractions = new ArrayList<>();

        // 샘플 데이터
        attractions.add(createAttraction("남산타워", "서울의 랜드마크", latitude + 0.01, longitude + 0.01, 1.2));
        attractions.add(createAttraction("경복궁", "조선시대 왕궁", latitude - 0.02, longitude + 0.02, 2.5));
        attractions.add(createAttraction("명동", "쇼핑과 맛집의 거리", latitude + 0.015, longitude - 0.01, 1.8));

        // radius 기준으로 필터링
        return attractions.stream()
            .filter(attr -> (Double) attr.get("distance") <= radius)
            .collect(Collectors.toList());
    }

    private Map<String, Object> createAttraction(String name, String description, Double lat, Double lng, Double distance) {
        Map<String, Object> attraction = new HashMap<>();
        attraction.put("name", name);
        attraction.put("description", description);
        attraction.put("latitude", lat);
        attraction.put("longitude", lng);
        attraction.put("distance", distance);
        attraction.put("rating", 4.5);
        return attraction;
    }

    public void processFeedback(Map<String, Object> feedbackData) {
        // 피드백 처리 로직 (실제로는 DB 저장 필요)
        log.info("Processing feedback: {}", feedbackData);

        // 피드백 데이터 검증
        if (feedbackData == null || feedbackData.isEmpty()) {
            throw new IllegalArgumentException("피드백 데이터가 비어있습니다.");
        }

        // 필수 필드 확인
        if (!feedbackData.containsKey("userId") || !feedbackData.containsKey("rating")) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
        }

        // 피드백 저장 로직 (추후 구현)
        Long userId = Long.valueOf(feedbackData.get("userId").toString());
        Integer rating = Integer.valueOf(feedbackData.get("rating").toString());
        String comment = feedbackData.getOrDefault("comment", "").toString();

        log.info("Feedback received from user {}: rating={}, comment={}", userId, rating, comment);
    }
}