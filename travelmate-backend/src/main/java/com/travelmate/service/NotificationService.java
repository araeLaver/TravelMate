package com.travelmate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendNotification(Long userId, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("type", "SYSTEM");
        
        // WebSocket으로 실시간 알림 전송
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/topic/notifications",
            notification
        );
        
        log.debug("알림 전송: User {} - {}", userId, message);
        
        sendPushNotification(userId, message);
    }
    
    public void sendGroupNotification(Long groupId, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("type", "GROUP");
        notification.put("groupId", groupId);
        
        // 그룹 채널로 브로드캐스트
        messagingTemplate.convertAndSend(
            "/topic/group/" + groupId,
            notification
        );
        
        log.debug("그룹 알림 전송: Group {} - {}", groupId, message);
    }
    
    public void sendLocationShareNotification(Long userId, Double latitude, Double longitude, String locationName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "LOCATION_SHARE");
        notification.put("latitude", latitude);
        notification.put("longitude", longitude);
        notification.put("locationName", locationName);
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/topic/location",
            notification
        );
        
        log.debug("위치 공유 알림: User {} - ({}, {})", userId, latitude, longitude);
    }
    
    public void sendMatchingNotification(Long userId, Long matchedUserId, String matchedUserNickname) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MATCHING");
        notification.put("matchedUserId", matchedUserId);
        notification.put("matchedUserNickname", matchedUserNickname);
        notification.put("message", String.format("%s님과 매칭되었습니다!", matchedUserNickname));
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/topic/matching",
            notification
        );
        
        log.info("매칭 알림: User {} matched with User {}", userId, matchedUserId);
    }
    
    // 푸시 알림 전송 (FCM 연동)
    private void sendPushNotification(Long userId, String message) {
        try {
            // FCM 토큰은 사용자 설정에서 관리
            // 실제 운영 시 FCM SDK 연동 필요
            log.info("푸시 알림 대기: User {} - {}", userId, message);
            
            // FCM 메시지 포맷 준비
            Map<String, String> data = new HashMap<>();
            data.put("title", "TravelMate");
            data.put("body", message);
            data.put("userId", userId.toString());
            
            // FCM 전송 로직은 별도 구현 필요
            // fcmService.sendToUser(userId, data);
            
        } catch (Exception e) {
            log.error("푸시 알림 전송 실패: User {}", userId, e);
        }
    }
    
    public void sendJoinRequestNotification(Long groupId, Long requesterId, String requesterName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "JOIN_REQUEST");
        notification.put("groupId", groupId);
        notification.put("requesterId", requesterId);
        notification.put("requesterName", requesterName);
        notification.put("message", String.format("%s님이 그룹 가입을 요청했습니다", requesterName));
        notification.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend(
            "/topic/group/" + groupId + "/admin",
            notification
        );
        
        log.info("가입 요청 알림: Group {} - Requester {}", groupId, requesterId);
    }
}