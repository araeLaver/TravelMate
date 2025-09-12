package com.travelmate.service;

import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.exception.UserException;
import com.travelmate.repository.UserRepositoryEnhanced;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName(\"향상된 사용자 서비스 테스트\")
class EnhancedUserServiceTest {
    
    @Mock
    private UserRepositoryEnhanced userRepository;
    
    @Mock
    private EnhancedJwtService jwtService;
    
    @Mock
    private PasswordService passwordService;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private ActivityTrackingService activityTrackingService;
    
    @InjectMocks
    private EnhancedUserService userService;
    
    private User testUser;
    private UserDto.RegisterRequest registerRequest;
    private MockHttpServletRequest mockRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(\"test@example.com\");
        testUser.setNickname(\"testuser\");
        testUser.setFullName(\"Test User\");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(true);
        
        registerRequest = UserDto.RegisterRequest.builder()
            .email(\"test@example.com\")
            .password(\"Password123!\")
            .nickname(\"testuser\")
            .fullName(\"Test User\")
            .build();
            
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr(\"127.0.0.1\");
        mockRequest.addHeader(\"User-Agent\", \"Test Agent\");
    }
    
    @Test
    @DisplayName(\"사용자 등록 성공\")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordService.encodePassword(anyString())).thenReturn(\"encoded_password\");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(\"access_token\");
        when(jwtService.generateRefreshToken(any(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(\"refresh_token\");
        when(activityTrackingService.getClientIpAddress(any())).thenReturn(\"127.0.0.1\");
        
        // When
        UserDto.RegisterResponse response = userService.registerUser(registerRequest, mockRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(\"test@example.com\", response.getUser().getEmail());
        
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmailVerification(any(User.class));
        verify(passwordService).validatePassword(anyString());
    }
    
    @Test
    @DisplayName(\"사용자 등록 실패 - 이메일 중복\")
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(UserException.EmailAlreadyExistsException.class, () -> {
            userService.registerUser(registerRequest, mockRequest);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName(\"사용자 프로필 조회 성공\")
    void getUserProfile_Success() {
        // Given
        when(userRepository.findWithDetailsById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        UserDto.Response response = userService.getUserProfile(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getNickname(), response.getNickname());
    }
    
    @Test
    @DisplayName(\"사용자 프로필 조회 실패 - 사용자 없음\")
    void getUserProfile_UserNotFound() {
        // Given
        when(userRepository.findWithDetailsById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserException.UserNotFoundException.class, () -> {
            userService.getUserProfile(1L);
        });
    }
    
    @Test
    @DisplayName(\"사용자 프로필 업데이트 성공\")
    void updateUserProfile_Success() {
        // Given
        UserDto.UpdateProfileRequest updateRequest = UserDto.UpdateProfileRequest.builder()
            .nickname(\"updateduser\")
            .fullName(\"Updated User\")
            .bio(\"Updated bio\")
            .build();
            
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByNickname(\"updateduser\")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserDto.Response response = userService.updateUserProfile(1L, updateRequest);
        
        // Then
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName(\"사용자 계정 비활성화 성공\")
    void deactivateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        userService.deactivateUser(1L);
        
        // Then
        verify(userRepository).save(any(User.class));
        verify(jwtService).revokeAllUserTokens(any(User.class));
    }
}