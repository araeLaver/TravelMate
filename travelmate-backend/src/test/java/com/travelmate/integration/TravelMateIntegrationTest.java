package com.travelmate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmate.dto.TravelGroupDto;
import com.travelmate.dto.UserDto;
import com.travelmate.entity.User;
import com.travelmate.repository.TravelGroupRepository;
import com.travelmate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TravelMate 통합 테스트")
class TravelMateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelGroupRepository travelGroupRepository;

    private UserDto.RegisterRequest registerRequest;
    private TravelGroupDto.CreateRequest groupCreateRequest;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        travelGroupRepository.deleteAll();
        userRepository.deleteAll();

        registerRequest = UserDto.RegisterRequest.builder()
                .email("integration@test.com")
                .username("integrationtest")
                .fullName("통합 테스트 사용자")
                .password("password123")
                .age(25)
                .gender(User.Gender.MALE)
                .build();

        groupCreateRequest = TravelGroupDto.CreateRequest.builder()
                .title("통합 테스트 여행")
                .description("통합 테스트용 여행 그룹")
                .destination("서울특별시")
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(9))
                .maxMembers(4)
                .travelStyle(User.TravelStyle.CULTURE)
                .budgetRange("50만원-100만원")
                .requirements("테스트 요구사항")
                .build();
    }

    @Test
    @DisplayName("사용자 전체 플로우 테스트: 등록 → 로그인 → 프로필 조회")
    void userFullFlow_RegisterLoginProfile() throws Exception {
        // 1. 사용자 등록
        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andExpect(jsonPath("$.username").value(registerRequest.getUsername()));

        // 2. 로그인
        UserDto.LoginRequest loginRequest = UserDto.LoginRequest.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .build();

        String loginResponse = mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(registerRequest.getEmail()))
                .andReturn().getResponse().getContentAsString();

        // JWT 토큰 추출 (실제로는 헤더에서 사용)
        String accessToken = objectMapper.readTree(loginResponse)
                .get("accessToken").asText();

        // 3. 프로필 조회 (토큰 사용 시뮬레이션)
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()));
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("여행 그룹 전체 플로우 테스트: 생성 → 조회 → 가입 → 탈퇴")
    void travelGroupFullFlow() throws Exception {
        // 테스트 사용자 생성
        User testUser = new User();
        testUser.setEmail("group@test.com");
        testUser.setUsername("grouptester");
        testUser.setFullName("그룹 테스터");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(true);
        User savedUser = userRepository.save(testUser);

        // 1. 여행 그룹 생성
        String createResponse = mockMvc.perform(post("/api/travel-groups")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(groupCreateRequest.getTitle()))
                .andExpect(jsonPath("$.destination").value(groupCreateRequest.getDestination()))
                .andExpect(jsonPath("$.currentMembers").value(1))
                .andReturn().getResponse().getContentAsString();

        Long groupId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. 여행 그룹 조회
        mockMvc.perform(get("/api/travel-groups/" + groupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.title").value(groupCreateRequest.getTitle()));

        // 3. 여행 그룹 목록 조회
        mockMvc.perform(get("/api/travel-groups")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(groupId));

        // 4. 다른 사용자 생성 후 그룹 가입
        User anotherUser = new User();
        anotherUser.setEmail("another@test.com");
        anotherUser.setUsername("anothertester");
        anotherUser.setFullName("또 다른 테스터");
        anotherUser.setPassword("encodedPassword");
        anotherUser.setIsActive(true);
        anotherUser.setIsEmailVerified(true);
        User savedAnotherUser = userRepository.save(anotherUser);

        mockMvc.perform(post("/api/travel-groups/" + groupId + "/join")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // 5. 그룹 멤버 수 확인
        mockMvc.perform(get("/api/travel-groups/" + groupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentMembers").value(2));

        // 6. 그룹 탈퇴
        mockMvc.perform(post("/api/travel-groups/" + groupId + "/leave")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // 7. 그룹 멤버 수 재확인
        mockMvc.perform(get("/api/travel-groups/" + groupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentMembers").value(1));
    }

    @Test
    @DisplayName("에러 처리 테스트: 존재하지 않는 리소스 접근")
    void errorHandling_ResourceNotFound() throws Exception {
        // 존재하지 않는 사용자 조회
        mockMvc.perform(get("/api/users/999999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다. ID: 999999"));

        // 존재하지 않는 여행 그룹 조회
        mockMvc.perform(get("/api/travel-groups/999999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("여행 그룹을 찾을 수 없습니다. ID: 999999"));
    }

    @Test
    @DisplayName("유효성 검증 테스트: 잘못된 입력 데이터")
    void validation_InvalidInputData() throws Exception {
        // 잘못된 이메일 형식으로 사용자 등록
        UserDto.RegisterRequest invalidRequest = UserDto.RegisterRequest.builder()
                .email("invalid-email")
                .username("test")
                .fullName("테스트")
                .password("123") // 너무 짧은 비밀번호
                .age(-1) // 음수 나이
                .build();

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("보안 테스트: 권한 없는 리소스 접근")
    void security_UnauthorizedAccess() throws Exception {
        // 다른 사용자의 정보에 접근 시도
        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isForbidden());

        // 다른 사용자의 정보 수정 시도
        UserDto.UpdateProfileRequest updateRequest = UserDto.UpdateProfileRequest.builder()
                .fullName("해킹 시도")
                .build();

        mockMvc.perform(put("/api/users/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API 응답 성능 테스트: 여행 그룹 목록 조회")
    void performance_GroupListQuery() throws Exception {
        // 테스트 데이터 생성 (여러 그룹 생성)
        User testUser = new User();
        testUser.setEmail("perf@test.com");
        testUser.setUsername("perftester");
        testUser.setFullName("성능 테스터");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // API 호출
        mockMvc.perform(get("/api/travel-groups")
                .param("page", "0")
                .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 응답 시간 체크 (1초 이내)
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        System.out.println("Response time: " + responseTime + "ms");
        assert responseTime < 1000 : "API 응답 시간이 1초를 초과했습니다: " + responseTime + "ms";
    }
}