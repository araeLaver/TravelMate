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

@Controller
@RequiredArgsConstructor
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
    public static class ChatRestController {
        
        private final ChatService chatService;
        
        @PostMapping("/rooms")
        public ResponseEntity<ChatDto.ChatRoomResponse> createChatRoom(
                @RequestBody ChatDto.CreateChatRoomRequest request) {
            ChatDto.ChatRoomResponse response = chatService.createChatRoom(request);
            return ResponseEntity.ok(response);
        }
        
        @GetMapping("/rooms")
        public ResponseEntity<List<ChatDto.ChatRoomResponse>> getChatRooms(
                @RequestParam Long userId) {
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
                @RequestParam Long userId) {
            chatService.markAsRead(roomId, userId);
            return ResponseEntity.ok().build();
        }
    }
}