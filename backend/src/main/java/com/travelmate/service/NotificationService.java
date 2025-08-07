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
        
        // TODO: 푸시 알림 서비스 연동 (FCM 등)
        // sendPushNotification(userId, message);
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
    
    // 푸시 알림 전송 (FCM 연동 시 구현)
    private void sendPushNotification(Long userId, String message) {
        // TODO: Firebase Cloud Messaging 연동
        // FCM 토큰 조회
        // 푸시 메시지 생성 및 전송
    }
}