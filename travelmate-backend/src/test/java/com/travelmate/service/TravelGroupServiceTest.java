package com.travelmate.service;

import com.travelmate.dto.TravelGroupDto;
import com.travelmate.entity.GroupMember;
import com.travelmate.entity.TravelGroup;
import com.travelmate.entity.User;
import com.travelmate.exception.BusinessException;
import com.travelmate.exception.ResourceNotFoundException;
import com.travelmate.repository.GroupMemberRepository;
import com.travelmate.repository.TravelGroupRepository;
import com.travelmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("여행 그룹 서비스 테스트")
class TravelGroupServiceTest {

    @Mock
    private TravelGroupRepository travelGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private TravelGroupService travelGroupService;

    private User testUser;
    private TravelGroup testGroup;
    private TravelGroupDto.CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setFullName("테스트 사용자");
        testUser.setIsActive(true);

        testGroup = new TravelGroup();
        testGroup.setId(1L);
        testGroup.setTitle("제주도 여행");
        testGroup.setDescription("3박 4일 제주도 여행");
        testGroup.setDestination("제주시");
        testGroup.setStartDate(LocalDate.now().plusDays(7));
        testGroup.setEndDate(LocalDate.now().plusDays(10));
        testGroup.setMaxMembers(5);
        testGroup.setCurrentMembers(1);
        testGroup.setCreator(testUser);
        testGroup.setIsActive(true);
        testGroup.setIsPublic(true);
        testGroup.setCreatedAt(LocalDateTime.now());

        createRequest = TravelGroupDto.CreateRequest.builder()
                .title("부산 여행")
                .description("2박 3일 부산 여행")
                .destination("부산광역시")
                .startDate(LocalDate.now().plusDays(14))
                .endDate(LocalDate.now().plusDays(16))
                .maxMembers(4)
                .travelStyle(User.TravelStyle.CULTURE)
                .budgetRange("50만원-100만원")
                .requirements("금연자만")
                .build();
    }

    @Test
    @DisplayName("여행 그룹 생성 - 성공")
    void createGroup_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(travelGroupRepository.save(any(TravelGroup.class))).thenReturn(testGroup);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(new GroupMember());

        // When
        TravelGroupDto.GroupResponse result = travelGroupService.createGroup(1L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals(testGroup.getId(), result.getId());
        assertEquals(testGroup.getTitle(), result.getTitle());
        verify(travelGroupRepository, times(1)).save(any(TravelGroup.class));
        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("여행 그룹 생성 - 존재하지 않는 사용자")
    void createGroup_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> travelGroupService.createGroup(999L, createRequest));
        
        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"));
        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    @DisplayName("여행 그룹 생성 - 과거 날짜")
    void createGroup_PastDate() {
        // Given
        createRequest.setStartDate(LocalDate.now().minusDays(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.createGroup(1L, createRequest));
        
        assertEquals("과거 날짜는 선택할 수 없습니다.", exception.getMessage());
        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    @DisplayName("여행 그룹 생성 - 잘못된 날짜 범위")
    void createGroup_InvalidDateRange() {
        // Given
        createRequest.setEndDate(LocalDate.now().plusDays(10));
        createRequest.setStartDate(LocalDate.now().plusDays(15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.createGroup(1L, createRequest));
        
        assertEquals("잘못된 날짜 범위입니다.", exception.getMessage());
        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    @DisplayName("여행 그룹 조회 - 성공")
    void getGroup_Success() {
        // Given
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // When
        TravelGroupDto.GroupResponse result = travelGroupService.getGroup(1L);

        // Then
        assertNotNull(result);
        assertEquals(testGroup.getId(), result.getId());
        assertEquals(testGroup.getTitle(), result.getTitle());
        verify(travelGroupRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("여행 그룹 조회 - 존재하지 않는 그룹")
    void getGroup_GroupNotFound() {
        // Given
        when(travelGroupRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> travelGroupService.getGroup(999L));
        
        assertTrue(exception.getMessage().contains("여행 그룹을 찾을 수 없습니다"));
        verify(travelGroupRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("여행 그룹 목록 조회 - 성공")
    void getGroups_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TravelGroup> groupPage = new PageImpl<>(Collections.singletonList(testGroup));
        
        when(travelGroupRepository.findByIsActiveTrueAndIsPublicTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(groupPage);

        // When
        Page<TravelGroupDto.GroupResponse> result = travelGroupService.getGroups(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testGroup.getId(), result.getContent().get(0).getId());
        verify(travelGroupRepository, times(1))
                .findByIsActiveTrueAndIsPublicTrueOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("여행 그룹 가입 - 성공")
    void joinGroup_Success() {
        // Given
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("newuser@example.com");
        newUser.setIsActive(true);

        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 2L)).thenReturn(false);
        when(travelGroupRepository.save(any(TravelGroup.class))).thenReturn(testGroup);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(new GroupMember());

        // When
        travelGroupService.joinGroup(1L, 2L);

        // Then
        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
        verify(travelGroupRepository, times(1)).save(testGroup);
    }

    @Test
    @DisplayName("여행 그룹 가입 - 이미 멤버")
    void joinGroup_AlreadyMember() {
        // Given
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 2L)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.joinGroup(1L, 2L));
        
        assertEquals("이미 그룹의 멤버입니다.", exception.getMessage());
        verify(groupMemberRepository, never()).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("여행 그룹 가입 - 그룹 인원 초과")
    void joinGroup_GroupFull() {
        // Given
        testGroup.setCurrentMembers(testGroup.getMaxMembers());
        
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, 2L)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.joinGroup(1L, 2L));
        
        assertEquals("그룹이 가득 찼습니다.", exception.getMessage());
        verify(groupMemberRepository, never()).save(any(GroupMember.class));
    }

    @Test
    @DisplayName("여행 그룹 탈퇴 - 성공")
    void leaveGroup_Success() {
        // Given
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(testGroup);
        groupMember.setUser(testUser);
        groupMember.setRole(GroupMember.Role.MEMBER);
        
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(groupMember));
        when(travelGroupRepository.save(any(TravelGroup.class))).thenReturn(testGroup);

        // When
        travelGroupService.leaveGroup(1L, 1L);

        // Then
        verify(groupMemberRepository, times(1)).delete(groupMember);
        verify(travelGroupRepository, times(1)).save(testGroup);
    }

    @Test
    @DisplayName("여행 그룹 탈퇴 - 그룹 생성자")
    void leaveGroup_GroupCreator() {
        // Given
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(testGroup);
        groupMember.setUser(testUser);
        groupMember.setRole(GroupMember.Role.CREATOR);
        
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(groupMember));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.leaveGroup(1L, 1L));
        
        assertEquals("그룹 생성자는 탈퇴할 수 없습니다. 그룹을 삭제해주세요.", exception.getMessage());
        verify(groupMemberRepository, never()).delete(any(GroupMember.class));
    }

    @Test
    @DisplayName("여행 그룹 업데이트 - 성공")
    void updateGroup_Success() {
        // Given
        TravelGroupDto.UpdateRequest updateRequest = TravelGroupDto.UpdateRequest.builder()
                .title("업데이트된 제주도 여행")
                .description("업데이트된 설명")
                .maxMembers(8)
                .requirements("업데이트된 요구사항")
                .build();

        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(travelGroupRepository.save(any(TravelGroup.class))).thenReturn(testGroup);

        // When
        TravelGroupDto.GroupResponse result = travelGroupService.updateGroup(1L, 1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(travelGroupRepository, times(1)).save(testGroup);
        assertEquals("업데이트된 제주도 여행", testGroup.getTitle());
        assertEquals("업데이트된 설명", testGroup.getDescription());
    }

    @Test
    @DisplayName("여행 그룹 업데이트 - 권한 없음")
    void updateGroup_NoPermission() {
        // Given
        TravelGroupDto.UpdateRequest updateRequest = TravelGroupDto.UpdateRequest.builder()
                .title("업데이트 시도")
                .build();

        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.updateGroup(1L, 999L, updateRequest));
        
        assertEquals("그룹에 대한 권한이 없습니다.", exception.getMessage());
        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }

    @Test
    @DisplayName("여행 그룹 삭제 - 성공")
    void deleteGroup_Success() {
        // Given
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(travelGroupRepository.save(any(TravelGroup.class))).thenReturn(testGroup);

        // When
        travelGroupService.deleteGroup(1L, 1L);

        // Then
        assertFalse(testGroup.getIsActive());
        verify(travelGroupRepository, times(1)).save(testGroup);
    }

    @Test
    @DisplayName("여행 그룹 삭제 - 권한 없음")
    void deleteGroup_NoPermission() {
        // Given
        when(travelGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> travelGroupService.deleteGroup(1L, 999L));
        
        assertEquals("그룹에 대한 권한이 없습니다.", exception.getMessage());
        verify(travelGroupRepository, never()).save(any(TravelGroup.class));
    }
}