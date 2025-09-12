package com.travelmate.controller;

import com.travelmate.dto.UserDto;
import com.travelmate.service.LocationService;
import com.travelmate.service.RecommendationService;
import com.travelmate.service.AdvancedRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    private final LocationService locationService;
    private final AdvancedRecommendationService advancedRecommendationService;
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDto.Response>> getRecommendedUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserDto.Response> recommendations = locationService.getSmartRecommendations(userId, latitude, longitude);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/personalized")
    public ResponseEntity<List<UserDto.Response>> getPersonalizedRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserDto.Response> recommendations = advancedRecommendationService
            .getPersonalizedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/activity")
    public ResponseEntity<List<UserDto.Response>> getActivityBasedRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String activity) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserDto.Response> recommendations = advancedRecommendationService
            .getActivityBasedRecommendations(userId, activity);
        return ResponseEntity.ok(recommendations);
    }
    
    @PostMapping("/intelligent-matching")
    public ResponseEntity<Void> performIntelligentMatching(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        advancedRecommendationService.performIntelligentMatching(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/travel-tips")
    public ResponseEntity<List<Map<String, Object>>> getTravelTips(
            @RequestParam String location,
            @RequestParam(required = false) String category) {
        List<Map<String, Object>> tips = recommendationService.getTravelTips(location, category);
        return ResponseEntity.ok(tips);
    }
    
    @GetMapping("/nearby-attractions")
    public ResponseEntity<List<Map<String, Object>>> getNearbyAttractions(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        List<Map<String, Object>> attractions = recommendationService.getNearbyAttractions(latitude, longitude, radiusKm);
        return ResponseEntity.ok(attractions);
    }
    
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(
            @RequestBody Map<String, Object> feedback) {
        recommendationService.processFeedback(feedback);
        return ResponseEntity.ok().build();
    }
}