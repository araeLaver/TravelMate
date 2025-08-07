package com.travelmate.service;

import com.travelmate.dto.ChatDto;
import com.travelmate.dto.UserDto;
import com.travelmate.entity.*;
import com.travelmate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    public ChatDto.ChatRoomResponse createChatRoom(ChatDto.CreateChatRoomRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(request.getRoomName());
        chatRoom.setRoomType(request.getRoomType());
        chatRoom.setIsActive(true);
        
        // 여행 그룹 채팅인 경우
        if (request.getTravelGroupId() != null) {
            TravelGroup travelGroup = travelGroupRepository.findById(request.getTravelGroupId())
                .orElseThrow(() -> new RuntimeException("여행 그룹을 찾을 수 없습니다."));
            chatRoom.setTravelGroup(travelGroup);
            chatRoom.setRoomName(travelGroup.getTitle() + " 채팅");
        }
        
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        
        // 참가자 추가
        addParticipantToRoom(savedRoom, creator);
        
        if (request.getParticipantIds() != null) {
            for (Long participantId : request.getParticipantIds()) {
                User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("참가자를 찾을 수 없습니다: " + participantId));
                addParticipantToRoom(savedRoom, participant);
            }
        }
        
        // 시스템 메시지 전송
        sendSystemMessage(savedRoom.getId(), String.format("%s님이 채팅방을 생성했습니다.", creator.getNickname()));
        
        log.info("새 채팅방 생성: {} (타입: {})", savedRoom.getId(), savedRoom.getRoomType());
        
        return convertChatRoomToDto(savedRoom);
    }
    
    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomResponse> getChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
        
        return rooms.stream()
            .map(room -> {
                ChatDto.ChatRoomResponse dto = convertChatRoomToDto(room);
                // 읽지 않은 메시지 수 계산
                dto.setUnreadCount(getUnreadMessageCount(room.getId(), userId));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ChatDto.MessageResponse> getChatMessages(Long roomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("sentAt").descending());
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndIsDeletedFalse(roomId, pageRequest);
        
        return messages.stream()
            .map(this::convertMessageToDto)
            .collect(Collectors.toList());
    }
    
    public void processMessage(ChatDto.MessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        
        User sender = userRepository.findById(request.getSenderId())
            .orElseThrow(() -> new RuntimeException("발신자를 찾을 수 없습니다."));
        
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setImageUrl(request.getImageUrl());
        message.setLocationLatitude(request.getLocationLatitude());
        message.setLocationLongitude(request.getLocationLongitude());
        message.setLocationName(request.getLocationName());
        message.setIsDeleted(false);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // 채팅방 최근 메시지 업데이트
        chatRoom.setLastMessage(request.getContent());
        chatRoom.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
        
        // 채팅방 참가자들에게 메시지 브로드캐스트
        ChatDto.MessageResponse messageDto = convertMessageToDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), messageDto);
        
        log.debug("메시지 전송: 방 {} - 발신자 {}", request.getChatRoomId(), sender.getNickname());
    }
    
    public void joinChatRoom(ChatDto.JoinRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 이미 참가자인지 확인
        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(request.getChatRoomId(), request.getUserId())) {
            addParticipantToRoom(chatRoom, user);
            sendSystemMessage(request.getChatRoomId(), String.format("%s님이 입장했습니다.", user.getNickname()));
        }
        
        // 참가자에게 입장 알림
        messagingTemplate.convertAndSendToUser(
            request.getUserId().toString(),
            "/queue/chat/joined",
            "채팅방에 참여했습니다."
        );
        
        log.info("채팅방 참가: 방 {} - 사용자 {}", request.getChatRoomId(), user.getNickname());
    }
    
    public void leaveChatRoom(ChatDto.LeaveRequest request) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(
            request.getChatRoomId(), request.getUserId())
            .orElseThrow(() -> new RuntimeException("채팅방 참가자를 찾을 수 없습니다."));
        
        participant.setIsActive(false);
        chatParticipantRepository.save(participant);
        
        User user = participant.getUser();
        sendSystemMessage(request.getChatRoomId(), String.format("%s님이 퇴장했습니다.", user.getNickname()));
        
        log.info("채팅방 퇴장: 방 {} - 사용자 {}", request.getChatRoomId(), user.getNickname());
    }
    
    public void markAsRead(Long roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
            .orElseThrow(() -> new RuntimeException("채팅방 참가자를 찾을 수 없습니다."));
        
        // 최신 메시지 ID 조회
        ChatMessage latestMessage = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(roomId);
        if (latestMessage != null) {
            participant.setLastReadMessageId(latestMessage.getId());
            participant.setLastReadAt(LocalDateTime.now());
            chatParticipantRepository.save(participant);
        }
        
        log.debug("메시지 읽음 처리: 방 {} - 사용자 {}", roomId, userId);
    }
    
    private void addParticipantToRoom(ChatRoom chatRoom, User user) {
        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(chatRoom.getId(), user.getId())) {
            ChatParticipant participant = new ChatParticipant();
            participant.setChatRoom(chatRoom);
            participant.setUser(user);
            participant.setIsActive(true);
            chatParticipantRepository.save(participant);
        }
    }
    
    private void sendSystemMessage(Long roomId, String content) {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setChatRoom(chatRoomRepository.findById(roomId).orElse(null));
        systemMessage.setContent(content);
        systemMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
        systemMessage.setIsDeleted(false);
        
        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);
        
        ChatDto.MessageResponse messageDto = convertMessageToDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, messageDto);
    }
    
    private Integer getUnreadMessageCount(Long roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
            .orElse(null);
        
        if (participant == null || participant.getLastReadMessageId() == null) {
            return chatMessageRepository.countByChatRoomIdAndIsDeletedFalse(roomId);
        }
        
        return chatMessageRepository.countByChatRoomIdAndIdGreaterThanAndIsDeletedFalse(
            roomId, participant.getLastReadMessageId());
    }
    
    private ChatDto.ChatRoomResponse convertChatRoomToDto(ChatRoom chatRoom) {
        ChatDto.ChatRoomResponse dto = new ChatDto.ChatRoomResponse();
        dto.setId(chatRoom.getId());
        dto.setRoomName(chatRoom.getRoomName());
        dto.setRoomType(chatRoom.getRoomType());
        dto.setTravelGroupId(chatRoom.getTravelGroup() != null ? chatRoom.getTravelGroup().getId() : null);
        dto.setLastMessage(chatRoom.getLastMessage());
        dto.setLastMessageAt(chatRoom.getLastMessageAt());
        dto.setCreatedAt(chatRoom.getCreatedAt());
        
        // 참가자 목록
        if (chatRoom.getParticipants() != null) {
            List<ChatDto.ParticipantDto> participants = chatRoom.getParticipants().stream()
                .map(this::convertParticipantToDto)
                .collect(Collectors.toList());
            dto.setParticipants(participants);
        }
        
        return dto;
    }
    
    private ChatDto.ParticipantDto convertParticipantToDto(ChatParticipant participant) {
        ChatDto.ParticipantDto dto = new ChatDto.ParticipantDto();
        dto.setUserId(participant.getUser().getId());
        dto.setNickname(participant.getUser().getNickname());
        dto.setProfileImageUrl(participant.getUser().getProfileImageUrl());
        dto.setLastReadAt(participant.getLastReadAt());
        dto.setIsActive(participant.getIsActive());
        return dto;
    }
    
    private ChatDto.MessageResponse convertMessageToDto(ChatMessage message) {
        ChatDto.MessageResponse dto = new ChatDto.MessageResponse();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        
        if (message.getSender() != null) {
            dto.setSender(convertUserToDto(message.getSender()));
        }
        
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setImageUrl(message.getImageUrl());
        dto.setLocationLatitude(message.getLocationLatitude());
        dto.setLocationLongitude(message.getLocationLongitude());
        dto.setLocationName(message.getLocationName());
        dto.setSentAt(message.getSentAt());
        dto.setIsDeleted(message.getIsDeleted());
        
        return dto;
    }
    
    private UserDto.Response convertUserToDto(User user) {
        UserDto.Response dto = new UserDto.Response();
        dto.setId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        return dto;
    }
}