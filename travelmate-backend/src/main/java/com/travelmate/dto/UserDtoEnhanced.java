package com.travelmate.dto;

import com.travelmate.entity.User;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.List;

public class UserDto {
    
    @Data
    @Builder
    public static class RegisterRequest {
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;
        
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 128, message = "비밀번호는 8-128자여야 합니다.")
        private String password;
        
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 50, message = "닉네임은 2-50자여야 합니다.")
        private String nickname;
        
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        private String fullName;
        
        @Min(value = 14, message = "나이는 14세 이상이어야 합니다.")
        @Max(value = 120, message = "나이는 120세 이하여야 합니다.")
        private Integer age;
        
        private User.Gender gender;
        
        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        private String phoneNumber;
        
        private User.TravelStyle travelStyle;
        
        private List<String> interests;
        
        private List<String> languages;
    }
    
    @Data
    @Builder
    public static class LoginRequest {
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;
        
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
        
        private String twoFactorCode;
    }
    
    @Data
    @Builder
    public static class LoginResponse {
        private Response user;
        private String accessToken;
        private String refreshToken;
        private boolean requiresTwoFactor;
        private String message;
    }
    
    @Data
    @Builder
    public static class RegisterResponse {
        private Response user;
        private String accessToken;
        private String refreshToken;
    }
    
    @Data
    @Builder
    public static class Response {
        private Long id;
        private String email;
        private String nickname;
        private String fullName;
        private Integer age;
        private User.Gender gender;
        private String profileImageUrl;
        private String bio;
        private User.TravelStyle travelStyle;
        private List<String> interests;
        private List<String> languages;
        private Double rating;
        private Integer reviewCount;
        private Boolean isEmailVerified;
        private Boolean phoneVerified;
        private LocalDateTime lastActivityAt;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    public static class UpdateProfileRequest {
        @Size(min = 2, max = 50, message = "닉네임은 2-50자여야 합니다.")
        private String nickname;
        
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        private String fullName;
        
        @Size(max = 500, message = "소개는 500자 이하여야 합니다.")
        private String bio;
        
        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
        private String profileImageUrl;
        
        @Min(value = 14, message = "나이는 14세 이상이어야 합니다.")
        @Max(value = 120, message = "나이는 120세 이하여야 합니다.")
        private Integer age;
        
        private User.Gender gender;
        private User.TravelStyle travelStyle;
        private List<String> interests;
        private List<String> languages;
    }
    
    // 기존 DTO들도 유지
    @Data
    public static class LocationUpdateRequest {
        @NotNull(message = "사용자 ID는 필수입니다.")
        private Long userId;
        
        @NotNull(message = "위도는 필수입니다.")
        private Double latitude;
        
        @NotNull(message = "경도는 필수입니다.")
        private Double longitude;
    }
    
    @Data
    public static class ShakeRequest {
        @NotNull(message = "사용자 ID는 필수입니다.")
        private Long userId;
        
        @NotNull(message = "위도는 필수입니다.")
        private Double latitude;
        
        @NotNull(message = "경도는 필수입니다.")
        private Double longitude;
        
        @NotNull(message = "X축 가속도는 필수입니다.")
        private Double accelerationX;
        
        @NotNull(message = "Y축 가속도는 필수입니다.")
        private Double accelerationY;
        
        @NotNull(message = "Z축 가속도는 필수입니다.")
        private Double accelerationZ;
    }
    
    @Data
    public static class ReportRequest {
        @NotNull(message = "신고 대상자 ID는 필수입니다.")
        private Long reportedUserId;
        
        @NotBlank(message = "신고 사유는 필수입니다.")
        private String reason;
        
        private String description;
    }
    
    @Data
    public static class ReviewResponse {
        private Long id;
        private Long reviewerId;
        private String reviewerNickname;
        private String reviewerProfileImageUrl;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }
    
    @Data
    public static class WriteReviewRequest {
        @NotNull(message = "리뷰 대상자 ID는 필수입니다.")
        private Long reviewedUserId;
        
        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Integer rating;
        
        @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
        private String comment;
    }
}