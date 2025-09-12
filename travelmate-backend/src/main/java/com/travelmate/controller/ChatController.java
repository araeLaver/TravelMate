package com.travelmate.controller;

import com.travelmate.dto.ChatDto;
import com.travelmate.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatDto.MessageRequest message, 
                           SimpMessageHeaderAccessor headerAccessor) {
        chatService.processMessage(message);
    }
    
    @MessageMapping("/chat.join")
    public void joinChatRoom(@Payload ChatDto.JoinRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        chatService.joinChatRoom(request);
        headerAccessor.getSessionAttributes().put("userId", request.getUserId());
        headerAccessor.getSessionAttributes().put("chatRoomId", request.getChatRoomId());
    }
    
    @MessageMapping("/chat.leave")
    public void leaveChatRoom(@Payload ChatDto.LeaveRequest request) {
        chatService.leaveChatRoom(request);
    }
    
    @RestController
    @RequestMapping("/api/chat")
    @RequiredArgsConstructor
    @CrossOrigin(origins = "*")
    public static class ChatRestController {
        
        private final ChatService chatService;
        
        @PostMapping("/rooms")
        public ResponseEntity<ChatDto.ChatRoomResponse> createChatRoom(
                @AuthenticationPrincipal UserDetails userDetails,
                @RequestBody ChatDto.CreateChatRoomRequest request) {
            Long userId = Long.parseLong(userDetails.getUsername());
            ChatDto.ChatRoomResponse response = chatService.createChatRoom(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        
        @GetMapping("/rooms")
        public ResponseEntity<List<ChatDto.ChatRoomResponse>> getChatRooms(
                @AuthenticationPrincipal UserDetails userDetails) {
            Long userId = Long.parseLong(userDetails.getUsername());
            List<ChatDto.ChatRoomResponse> rooms = chatService.getChatRooms(userId);
            return ResponseEntity.ok(rooms);
        }
        
        @GetMapping("/rooms/{roomId}/messages")
        public ResponseEntity<List<ChatDto.MessageResponse>> getChatMessages(
                @PathVariable Long roomId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "50") int size) {
            List<ChatDto.MessageResponse> messages = chatService.getChatMessages(roomId, page, size);
            return ResponseEntity.ok(messages);
        }
        
        @PostMapping("/rooms/{roomId}/read")
        public ResponseEntity<Void> markAsRead(
                @PathVariable Long roomId,
                @AuthenticationPrincipal UserDetails userDetails) {
            Long userId = Long.parseLong(userDetails.getUsername());
            chatService.markAsRead(roomId, userId);
            return ResponseEntity.ok().build();
        }
        
        @DeleteMapping("/rooms/{roomId}")
        public ResponseEntity<Void> leaveChatRoom(
                @PathVariable Long roomId,
                @AuthenticationPrincipal UserDetails userDetails) {
            Long userId = Long.parseLong(userDetails.getUsername());
            ChatDto.LeaveRequest request = new ChatDto.LeaveRequest();
            request.setChatRoomId(roomId);
            request.setUserId(userId);
            chatService.leaveChatRoom(request);
            return ResponseEntity.noContent().build();
        }
        
        @GetMapping("/rooms/{roomId}")
        public ResponseEntity<ChatDto.ChatRoomDetailResponse> getChatRoomDetail(
                @PathVariable Long roomId,
                @AuthenticationPrincipal UserDetails userDetails) {
            Long userId = Long.parseLong(userDetails.getUsername());
            ChatDto.ChatRoomDetailResponse response = chatService.getChatRoomDetail(roomId, userId);
            return ResponseEntity.ok(response);
        }
        
        @PostMapping("/rooms/{roomId}/typing")
        public ResponseEntity<Void> updateTypingStatus(
                @PathVariable Long roomId,
                @AuthenticationPrincipal UserDetails userDetails,
                @RequestParam boolean isTyping) {
            Long userId = Long.parseLong(userDetails.getUsername());
            chatService.updateTypingStatus(roomId, userId, isTyping);
            return ResponseEntity.ok().build();
        }
    }
}