package com.travelmate.controller;

import com.travelmate.dto.UserDto;
import com.travelmate.service.EnhancedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class EnhancedUserController {
    
    private final EnhancedUserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserDto.RegisterResponse> register(
            @Valid @RequestBody UserDto.RegisterRequest request,
            HttpServletRequest httpRequest) {
        UserDto.RegisterResponse response = userService.registerUser(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(
            @Valid @RequestBody UserDto.LoginRequest request,
            HttpServletRequest httpRequest) {
        UserDto.LoginResponse response = userService.loginUser(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getCurrentUser() {
        UserDto.Response response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUser(@PathVariable Long id) {
        UserDto.Response response = userService.getUserProfile(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/me")
    public ResponseEntity<UserDto.Response> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserDto.Response response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<UserDto.Response>> searchUsers(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<UserDto.Response> response = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}