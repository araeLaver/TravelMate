package com.travelmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.exception.BusinessException;
import com.travelmate.exception.ResourceNotFoundException;
import com.travelmate.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("사용자 컨트롤러 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto.RegisterRequest registerRequest;
    private UserDto.UserResponse userResponse;
    private UserDto.LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        registerRequest = UserDto.RegisterRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .fullName("테스트 사용자")
                .password("password123")
                .age(25)
                .gender(User.Gender.MALE)
                .build();

        userResponse = UserDto.UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .fullName("테스트 사용자")
                .age(25)
                .gender(User.Gender.MALE)
                .isActive(true)
                .rating(0.0)
                .reviewCount(0)
                .build();

        loginResponse = UserDto.LoginResponse.builder()
                .accessToken("jwt.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userResponse)
                .build();
    }

    @Test
    @DisplayName("사용자 등록 - 성공")
    void registerUser_Success() throws Exception {
        // Given
        when(userService.registerUser(any(UserDto.RegisterRequest.class)))
                .thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.username").value(userResponse.getUsername()));
    }

    @Test
    @DisplayName("사용자 등록 - 유효성 검증 실패")
    void registerUser_ValidationFailed() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email"); // 잘못된 이메일 형식

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 데이터가 유효하지 않습니다."))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("사용자 등록 - 이메일 중복")
    void registerUser_DuplicateEmail() throws Exception {
        // Given
        when(userService.registerUser(any(UserDto.RegisterRequest.class)))
                .thenThrow(BusinessException.conflict("이미 사용 중인 이메일입니다."));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_Success() throws Exception {
        // Given
        UserDto.LoginRequest loginRequest = UserDto.LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userService.login(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(loginResponse.getAccessToken()))
                .andExpect(jsonPath("$.user.id").value(userResponse.getId()));
    }

    @Test
    @DisplayName("로그인 - 잘못된 자격증명")
    void login_InvalidCredentials() throws Exception {
        // Given
        UserDto.LoginRequest loginRequest = UserDto.LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(userService.login(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenThrow(BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 프로필 조회 - 성공")
    void getUserProfile_Success() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 프로필 조회 - 존재하지 않는 사용자")
    void getUserProfile_UserNotFound() throws Exception {
        // Given
        when(userService.findById(999L))
                .thenThrow(ResourceNotFoundException.forUser(999L));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다. ID: 999"));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 프로필 업데이트 - 성공")
    void updateUserProfile_Success() throws Exception {
        // Given
        UserDto.UpdateProfileRequest updateRequest = UserDto.UpdateProfileRequest.builder()
                .fullName("업데이트된 이름")
                .bio("새로운 소개")
                .age(26)
                .travelStyle(User.TravelStyle.ADVENTURE)
                .build();

        UserDto.UserResponse updatedResponse = UserDto.UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .fullName("업데이트된 이름")
                .bio("새로운 소개")
                .age(26)
                .travelStyle(User.TravelStyle.ADVENTURE)
                .isActive(true)
                .rating(0.0)
                .reviewCount(0)
                .build();

        when(userService.updateProfile(eq(1L), any(UserDto.UpdateProfileRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("업데이트된 이름"))
                .andExpect(jsonPath("$.bio").value("새로운 소개"))
                .andExpect(jsonPath("$.age").value(26));
    }

    @Test
    @WithMockUser(username = "2")
    @DisplayName("사용자 프로필 업데이트 - 권한 없음")
    void updateUserProfile_AccessDenied() throws Exception {
        // Given
        UserDto.UpdateProfileRequest updateRequest = UserDto.UpdateProfileRequest.builder()
                .fullName("업데이트 시도")
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 위치 업데이트 - 성공")
    void updateUserLocation_Success() throws Exception {
        // Given
        UserDto.LocationUpdateRequest locationRequest = UserDto.LocationUpdateRequest.builder()
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        // When & Then
        mockMvc.perform(patch("/api/users/1/location")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("위치가 업데이트되었습니다."));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 위치 업데이트 - 잘못된 좌표")
    void updateUserLocation_InvalidCoordinates() throws Exception {
        // Given
        UserDto.LocationUpdateRequest locationRequest = UserDto.LocationUpdateRequest.builder()
                .latitude(91.0) // 유효 범위 초과
                .longitude(126.9780)
                .build();

        // When & Then
        mockMvc.perform(patch("/api/users/1/location")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.latitude").exists());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("FCM 토큰 업데이트 - 성공")
    void updateFcmToken_Success() throws Exception {
        // Given
        UserDto.FcmTokenRequest fcmRequest = UserDto.FcmTokenRequest.builder()
                .fcmToken("new.fcm.token.here")
                .build();

        // When & Then
        mockMvc.perform(patch("/api/users/1/fcm-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fcmRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FCM 토큰이 업데이트되었습니다."));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("사용자 비활성화 - 성공")
    void deactivateUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 계정이 비활성화되었습니다."));
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 보호된 엔드포인트 접근")
    void accessProtectedEndpoint_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}