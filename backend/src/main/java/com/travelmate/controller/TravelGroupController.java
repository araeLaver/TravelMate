package com.travelmate.controller;

import com.travelmate.dto.TravelGroupDto;
import com.travelmate.entity.TravelGroup;
import com.travelmate.service.TravelGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/travel-groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TravelGroupController {
    
    private final TravelGroupService travelGroupService;
    
    @PostMapping
    public ResponseEntity<TravelGroupDto.Response> createGroup(
            @Valid @RequestBody TravelGroupDto.CreateRequest request) {
        TravelGroupDto.Response response = travelGroupService.createGroup(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<TravelGroupDto.Response>> getGroups(
            @RequestParam(required = false) TravelGroup.Purpose purpose,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<TravelGroupDto.Response> groups = travelGroupService.getGroups(purpose, latitude, longitude, radiusKm);
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TravelGroupDto.DetailResponse> getGroup(@PathVariable Long id) {
        TravelGroupDto.DetailResponse group = travelGroupService.getGroupDetail(id);
        return ResponseEntity.ok(group);
    }
    
    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinGroup(
            @PathVariable Long id,
            @RequestParam Long userId) {
        travelGroupService.joinGroup(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long id,
            @RequestParam Long userId) {
        travelGroupService.leaveGroup(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateGroupStatus(
            @PathVariable Long id,
            @RequestParam TravelGroup.Status status) {
        travelGroupService.updateGroupStatus(id, status);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my-groups")
    public ResponseEntity<List<TravelGroupDto.Response>> getMyGroups(@RequestParam Long userId) {
        List<TravelGroupDto.Response> myGroups = travelGroupService.getMyGroups(userId);
        return ResponseEntity.ok(myGroups);
    }
}