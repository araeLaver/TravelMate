package com.travelmate.controller;

import com.travelmate.dto.TravelGroupDto;
import com.travelmate.entity.TravelGroup;
import com.travelmate.service.TravelGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/travel-groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TravelGroupController {
    
    private final TravelGroupService travelGroupService;
    
    @PostMapping
    public ResponseEntity<TravelGroupDto.Response> createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TravelGroupDto.CreateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TravelGroupDto.Response response = travelGroupService.createGroup(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        travelGroupService.joinGroup(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        travelGroupService.leaveGroup(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TravelGroupDto.Response> updateGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TravelGroupDto.UpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TravelGroupDto.Response response = travelGroupService.updateGroup(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateGroupStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam TravelGroup.Status status) {
        Long userId = Long.parseLong(userDetails.getUsername());
        travelGroupService.updateGroupStatus(id, userId, status);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my-groups")
    public ResponseEntity<List<TravelGroupDto.Response>> getMyGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<TravelGroupDto.Response> myGroups = travelGroupService.getMyGroups(userId);
        return ResponseEntity.ok(myGroups);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        travelGroupService.deleteGroup(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> inviteToGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TravelGroupDto.InviteRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        travelGroupService.inviteToGroup(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}/members")
    public ResponseEntity<List<TravelGroupDto.MemberResponse>> getGroupMembers(@PathVariable Long id) {
        List<TravelGroupDto.MemberResponse> members = travelGroupService.getGroupMembers(id);
        return ResponseEntity.ok(members);
    }
}