package com.travelmate.service;

import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.exception.BusinessException;
import com.travelmate.exception.ResourceNotFoundException;
import com.travelmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto.RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setFullName("테스트 사용자");
        testUser.setPassword("encodedPassword");
        testUser.setAge(25);
        testUser.setGender(User.Gender.MALE);
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);
        testUser.setCreatedAt(LocalDateTime.now());

        registerRequest = UserDto.RegisterRequest.builder()
                .email("newuser@example.com")
                .username("newuser")
                .fullName("새로운 사용자")
                .password("password123")
                .age(25)
                .gender(User.Gender.FEMALE)
                .build();
    }

    @Test
    @DisplayName("사용자 등록 - 성공")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto.UserResponse result = userService.registerUser(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
    }

    @Test
    @DisplayName("사용자 등록 - 이메일 중복")
    void registerUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.registerUser(registerRequest));
        
        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 - 사용자명 중복")
    void registerUser_DuplicateUsername() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.registerUser(registerRequest));
        
        assertEquals("이미 사용 중인 사용자명입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDto.UserResponse result = userService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("사용자 ID로 조회 - 존재하지 않는 사용자")
    void findById_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> userService.findById(999L));
        
        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("이메일로 로그인 - 성공")
    void login_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt.token.here";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser.getId().toString())).thenReturn(token);

        // When
        UserDto.LoginResponse result = userService.login(email, password);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getAccessToken());
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
        verify(jwtService, times(1)).generateToken(testUser.getId().toString());
    }

    @Test
    @DisplayName("이메일로 로그인 - 잘못된 이메일")
    void login_InvalidEmail() {
        // Given
        String email = "wrong@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.login(email, password));
        
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("이메일로 로그인 - 잘못된 비밀번호")
    void login_InvalidPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.login(email, password));
        
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testUser.getPassword());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 - 성공")
    void updateProfile_Success() {
        // Given
        UserDto.UpdateProfileRequest updateRequest = UserDto.UpdateProfileRequest.builder()
                .fullName("업데이트된 이름")
                .bio("새로운 소개")
                .age(26)
                .travelStyle(User.TravelStyle.ADVENTURE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto.UserResponse result = userService.updateProfile(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
        assertEquals("업데이트된 이름", testUser.getFullName());
        assertEquals("새로운 소개", testUser.getBio());
    }

    @Test
    @DisplayName("사용자 비활성화 - 성공")
    void deactivateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(1L);

        // Then
        assertFalse(testUser.getIsActive());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("사용자 위치 업데이트 - 성공")
    void updateLocation_Success() {
        // Given
        Double latitude = 37.5665;
        Double longitude = 126.9780;
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateLocation(1L, latitude, longitude);

        // Then
        assertEquals(latitude, testUser.getLocationLatitude());
        assertEquals(longitude, testUser.getLocationLongitude());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("사용자 위치 업데이트 - 잘못된 좌표")
    void updateLocation_InvalidCoordinates() {
        // Given
        Double invalidLatitude = 91.0; // 유효 범위: -90 ~ 90
        Double validLongitude = 126.9780;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> userService.updateLocation(1L, invalidLatitude, validLongitude));
        
        assertEquals("잘못된 위치 정보입니다.", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 - 성공")
    void updateFcmToken_Success() {
        // Given
        String fcmToken = "fcm.token.here";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateFcmToken(1L, fcmToken);

        // Then
        assertEquals(fcmToken, testUser.getFcmToken());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }
}