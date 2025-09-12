package com.travelmate.dto;

import com.travelmate.entity.TravelGroup;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class TravelGroupDto {
    
    @Data
    public static class CreateRequest {
        @NotBlank
        private String title;
        
        private String description;
        
        @NotNull
        private TravelGroup.Purpose purpose;
        
        
        private Integer maxMembers = 4;
        
        @NotNull
        private Double meetingLatitude;
        
        @NotNull
        private Double meetingLongitude;
        
        private String meetingAddress;
        private LocalDateTime scheduledTime;
    }
    
    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private TravelGroup.Purpose purpose;
        private UserDto.Response creator;
        private Integer maxMembers;
        private Integer currentMemberCount;
        private Double meetingLatitude;
        private Double meetingLongitude;
        private String meetingAddress;
        private LocalDateTime scheduledTime;
        private TravelGroup.Status status;
        private LocalDateTime createdAt;
    }
    
    @Data
    public static class DetailResponse extends Response {
        private List<MemberDto> members;
        private Boolean isJoinedByCurrentUser;
        private Boolean canJoin;
    }
    
    @Data
    public static class MemberDto {
        private Long id;
        private UserDto.Response user;
        private String role;
        private String status;
        private LocalDateTime joinedAt;
    }
    
    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private Integer maxMembers;
        private Double meetingLatitude;
        private Double meetingLongitude;
        private String meetingAddress;
        private LocalDateTime scheduledTime;
    }
    
    @Data
    public static class InviteRequest {
        @NotNull
        private Long inviteeId;
        
        private String message;
    }
    
    @Data
    public static class MemberResponse {
        private Long id;
        private UserDto.Response user;
        private String role;
        private String status;
        private LocalDateTime joinedAt;
        private Boolean isCreator;
    }
}