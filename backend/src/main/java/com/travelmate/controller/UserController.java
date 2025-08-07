package com.travelmate.controller;

import com.travelmate.dto.UserDto;
import com.travelmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
}