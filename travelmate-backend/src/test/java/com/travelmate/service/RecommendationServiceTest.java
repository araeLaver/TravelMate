package com.travelmate.service;

import com.travelmate.dto.RecommendationDto;
import com.travelmate.entity.TravelGroup;
import com.travelmate.entity.User;
import com.travelmate.entity.UserGroupMembership;
import com.travelmate.repository.TravelGroupRepository;
import com.travelmate.repository.UserGroupMembershipRepository;
import com.travelmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RecommendationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TravelGroupRepository travelGroupRepository;

    @Mock
    private UserGroupMembershipRepository membershipRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private TravelGroup testGroup;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스터")
                .age(30)
                .travelStyle("CULTURAL")
                .interests("FOOD,HISTORY,PHOTOGRAPHY")
                .languages("KOREAN,ENGLISH")
                .isActive(true)
                .build();

        // 테스트 그룹 생성
        testGroup = TravelGroup.builder()
                .id(1L)
                .name("제주도 문화 탐방")
                .description("제주도의 역사와 문화를 탐방하는 여행")
                .destination("제주도")
                .travelStyle("CULTURAL")
                .currentMembers(3)
                .maxMembers(8)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .creator(testUser)
                .build();
    }

    @Test
    void 그룹_추천_테스트() {
        // Given
        List<TravelGroup> groups = new ArrayList<>();
        groups.add(testGroup);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(travelGroupRepository.findAll()).thenReturn(groups);
        when(membershipRepository.findByUserId(1L)).thenReturn(new ArrayList<>());
        when(membershipRepository.findByTravelGroupId(1L)).thenReturn(new ArrayList<>());

        // When
        List<RecommendationDto.GroupRecommendation> recommendations =
                recommendationService.recommendGroups(1L, 10);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());

        RecommendationDto.GroupRecommendation recommendation = recommendations.get(0);
        assertEquals("제주도 문화 탐방", recommendation.getGroupName());
        assertNotNull(recommendation.getRecommendationScore());
        assertTrue(recommendation.getRecommendationScore() > 0);
        assertNotNull(recommendation.getReasons());
        assertFalse(recommendation.getReasons().isEmpty());

        // Verify
        verify(userRepository, times(1)).findById(1L);
        verify(travelGroupRepository, times(1)).findAll();
    }

    @Test
    void 동행자_추천_테스트() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .nickname("동행자")
                .age(28)
                .travelStyle("CULTURAL")
                .interests("FOOD,HISTORY")
                .languages("KOREAN")
                .isActive(true)
                .build();

        List<User> users = new ArrayList<>();
        users.add(testUser);
        users.add(otherUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<RecommendationDto.UserRecommendation> recommendations =
                recommendationService.recommendTravelMates(1L, 10);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());

        RecommendationDto.UserRecommendation recommendation = recommendations.get(0);
        assertEquals("동행자", recommendation.getNickname());
        assertTrue(recommendation.getRecommendationScore() >= 30); // 최소 30점
        assertNotNull(recommendation.getCommonInterests());

        // Verify
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void 사용자_없음_예외_테스트() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            recommendationService.recommendGroups(999L, 10);
        });

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void 이미_가입한_그룹_제외_테스트() {
        // Given
        List<TravelGroup> groups = new ArrayList<>();
        groups.add(testGroup);

        UserGroupMembership membership = new UserGroupMembership();
        membership.setUser(testUser);
        membership.setTravelGroup(testGroup);
        List<UserGroupMembership> memberships = List.of(membership);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(travelGroupRepository.findAll()).thenReturn(groups);
        when(membershipRepository.findByUserId(1L)).thenReturn(memberships);

        // When
        List<RecommendationDto.GroupRecommendation> recommendations =
                recommendationService.recommendGroups(1L, 10);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty()); // 이미 가입한 그룹은 제외

        verify(membershipRepository, times(1)).findByUserId(1L);
    }

    @Test
    void 만원_그룹_제외_테스트() {
        // Given
        testGroup.setCurrentMembers(8);
        testGroup.setMaxMembers(8);

        List<TravelGroup> groups = List.of(testGroup);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(travelGroupRepository.findAll()).thenReturn(groups);
        when(membershipRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        // When
        List<RecommendationDto.GroupRecommendation> recommendations =
                recommendationService.recommendGroups(1L, 10);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty()); // 만원인 그룹은 제외
    }
}
