package com.travelmate.dto;

import com.travelmate.entity.User;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UserDto {
    
    @Data
    public static class RegisterRequest {
        @Email
        @NotBlank
        private String email;
        
        @NotBlank
        private String password;
        
        @NotBlank
        private String nickname;
        
        private String phoneNumber;
        private User.TravelStyle travelStyle;
    }
    
    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        
        @NotBlank
        private String password;
    }
    
    @Data
    public static class LoginResponse {
        private String token;
        private Response user;
    }
    
    @Data
    public static class Response {
        private Long id;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private String bio;
        private Double currentLatitude;
        private Double currentLongitude;
        private User.TravelStyle travelStyle;
        private LocalDateTime createdAt;
    }
    
    @Data
    public static class LocationUpdateRequest {
        @NotNull
        private Long userId;
        
        @NotNull
        private Double latitude;
        
        @NotNull
        private Double longitude;
    }
    
    @Data
    public static class ShakeRequest {
        @NotNull
        private Long userId;
        
        @NotNull
        private Double latitude;
        
        @NotNull
        private Double longitude;
        
        @NotNull
        private Double accelerationX;
        
        @NotNull
        private Double accelerationY;
        
        @NotNull
        private Double accelerationZ;
    }
    
    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String bio;
        private String profileImageUrl;
        private String phoneNumber;
        private User.TravelStyle travelStyle;
    }
    
    @Data
    public static class ReportRequest {
        @NotNull
        private Long reportedUserId;
        
        @NotBlank
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
        @NotNull
        private Long reviewedUserId;
        
        @NotNull
        private Integer rating;
        
        private String comment;
    }
}