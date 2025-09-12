package com.travelmate.controller;

import com.travelmate.dto.UserDto;
import com.travelmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.Response response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@Valid @RequestBody UserDto.LoginRequest request) {
        UserDto.LoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserDto.Response> getProfile(@PathVariable Long id) {
        UserDto.Response response = userService.getUserProfile(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody UserDto.LocationUpdateRequest request) {
        userService.updateUserLocation(request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<UserDto.Response>> getNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        List<UserDto.Response> nearbyUsers = userService.getNearbyUsers(latitude, longitude, radiusKm);
        return ResponseEntity.ok(nearbyUsers);
    }
    
    @PostMapping("/shake")
    public ResponseEntity<List<UserDto.Response>> findUsersOnShake(
            @Valid @RequestBody UserDto.ShakeRequest request) {
        List<UserDto.Response> users = userService.findUsersOnShake(request);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserDto.Response> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserDto.Response response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserDto.Response response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String fcmToken = request.get("fcmToken");
        userService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/report")
    public ResponseEntity<Void> reportUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.ReportRequest request) {
        Long reporterId = Long.parseLong(userDetails.getUsername());
        userService.reportUser(reporterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/reviews/{userId}")
    public ResponseEntity<List<UserDto.ReviewResponse>> getUserReviews(@PathVariable Long userId) {
        List<UserDto.ReviewResponse> reviews = userService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping("/reviews")
    public ResponseEntity<UserDto.ReviewResponse> writeReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.WriteReviewRequest request) {
        Long reviewerId = Long.parseLong(userDetails.getUsername());
        UserDto.ReviewResponse response = userService.writeReview(reviewerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}