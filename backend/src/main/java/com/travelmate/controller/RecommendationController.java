package com.travelmate.controller;

import com.travelmate.dto.UserDto;
import com.travelmate.service.LocationService;
import com.travelmate.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDto.Response>> getRecommendedUsers(
            @RequestParam Long userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        List<UserDto.Response> recommendations = locationService.getSmartRecommendations(userId, latitude, longitude);
        return ResponseEntity.ok(recommendations);
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