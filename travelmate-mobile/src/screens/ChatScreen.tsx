import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

interface ChatRoom {
  id: string;
  name: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
  isOnline: boolean;
  avatar: string;
  type: 'direct' | 'group';
  participants?: number;
}

interface Message {
  id: string;
  text: string;
  timestamp: string;
  isMe: boolean;
  senderName?: string;
}

const mockChatRooms: ChatRoom[] = [
  {
    id: '1',
    name: '김모험가',
    lastMessage: '내일 한라산 등반 어때요? 🏔️',
    lastMessageTime: '방금 전',
    unreadCount: 2,
    isOnline: true,
    avatar: '🏔️',
    type: 'direct',
  },
  {
    id: '2',
    name: '박미식가',
    lastMessage: '이 근처 맛집 추천 좀 해주세요!',
    lastMessageTime: '5분 전',
    unreadCount: 0,
    isOnline: true,
    avatar: '🍜',
    type: 'direct',
  },
  {
    id: '3',
    name: '서울 여행 그룹',
    lastMessage: '이포토그래퍼: 경복궁에서 만날까요?',
    lastMessageTime: '1시간 전',
    unreadCount: 5,
    isOnline: false,
    avatar: '🏰',
    type: 'group',
    participants: 8,
  },
  {
    id: '4',
    name: '정야경러',
    lastMessage: '남산타워 야경 정말 예뻤어요 ✨',
    lastMessageTime: '3시간 전',
    unreadCount: 0,
    isOnline: false,
    avatar: '🌃',
    type: 'direct',
  },
  {
    id: '5',
    name: '부산 맛집 탐방대',
    lastMessage: '최카페러버: 해운대 맛집 리스트 공유!',
    lastMessageTime: '어제',
    unreadCount: 12,
    isOnline: true,
    avatar: '🦐',
    type: 'group',
    participants: 15,
  },
];

const mockMessages: Message[] = [
  {
    id: '1',
    text: '안녕하세요! 저도 혼자 여행 중이에요 😊',
    timestamp: '오후 2:30',
    isMe: false,
    senderName: '김모험가',
  },
  {
    id: '2',
    text: '반가워요! 어디 가시는 중이신가요?',
    timestamp: '오후 2:32',
    isMe: true,
  },
  {
    id: '3',
    text: '제주도 왔어요! 내일 한라산 등반 계획 중인데, 함께 가실래요?',
    timestamp: '오후 2:35',
    isMe: false,
    senderName: '김모험가',
  },
  {
    id: '4',
    text: '우와! 좋아요! 등반 경험은 있으신가요?',
    timestamp: '오후 2:36',
    isMe: true,
  },
  {
    id: '5',
    text: '네, 등산을 자주 해서 익숙해요. 장비도 챙겨왔고요 🎒',
    timestamp: '오후 2:38',
    isMe: false,
    senderName: '김모험가',
  },
];

const ChatScreen: React.FC = () => {
  const [chatRooms, setChatRooms] = useState<ChatRoom[]>(mockChatRooms);
  const [selectedChat, setSelectedChat] = useState<ChatRoom | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [searchText, setSearchText] = useState('');

  useEffect(() => {
    if (selectedChat) {
      setMessages(mockMessages);
    }
  }, [selectedChat]);

  const openChat = (chatRoom: ChatRoom) => {
    setSelectedChat(chatRoom);
    // 읽음 처리
    setChatRooms(prevRooms =>
      prevRooms.map(room =>
        room.id === chatRoom.id
          ? { ...room, unreadCount: 0 }
          : room
      )
    );
  };

  const sendMessage = () => {
    if (newMessage.trim() && selectedChat) {
      const message: Message = {
        id: Date.now().toString(),
        text: newMessage.trim(),
        timestamp: new Date().toLocaleTimeString('ko-KR', { 
          hour: '2-digit', 
          minute: '2-digit' 
        }),
        isMe: true,
      };
      
      setMessages(prev => [...prev, message]);
      setNewMessage('');
      
      // 자동 응답 시뮬레이션 (실제 앱에서는 WebSocket 사용)
      setTimeout(() => {
        const autoReply: Message = {
          id: (Date.now() + 1).toString(),
          text: ['좋아요!', '그렇네요!', '알겠습니다 👍', '언제 만날까요?'][Math.floor(Math.random() * 4)],
          timestamp: new Date().toLocaleTimeString('ko-KR', { 
            hour: '2-digit', 
            minute: '2-digit' 
          }),
          isMe: false,
          senderName: selectedChat.name,
        };
        setMessages(prev => [...prev, autoReply]);
      }, 1000);
    }
  };

  const filteredChatRooms = chatRooms.filter(room =>
    room.name.toLowerCase().includes(searchText.toLowerCase()) ||
    room.lastMessage.toLowerCase().includes(searchText.toLowerCase())
  );

  const renderChatRoom = ({ item }: { item: ChatRoom }) => (
    <TouchableOpacity
      style={styles.chatRoomItem}
      onPress={() => openChat(item)}
    >
      <View style={styles.avatarContainer}>
        <Text style={styles.avatar}>{item.avatar}</Text>
        {item.isOnline && <View style={styles.onlineIndicator} />}
      </View>
      
      <View style={styles.chatRoomContent}>
        <View style={styles.chatRoomHeader}>
          <Text style={styles.chatRoomName}>
            {item.name}
            {item.type === 'group' && (
              <Text style={styles.participantCount}> ({item.participants}명)</Text>
            )}
          </Text>
          <Text style={styles.lastMessageTime}>{item.lastMessageTime}</Text>
        </View>
        
        <View style={styles.chatRoomFooter}>
          <Text 
            style={[styles.lastMessage, { flex: 1 }]} 
            numberOfLines={1}
          >
            {item.lastMessage}
          </Text>
          {item.unreadCount > 0 && (
            <View style={styles.unreadBadge}>
              <Text style={styles.unreadCount}>
                {item.unreadCount > 99 ? '99+' : item.unreadCount}
              </Text>
            </View>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );

  const renderMessage = ({ item }: { item: Message }) => (
    <View style={[
      styles.messageContainer,
      item.isMe ? styles.myMessageContainer : styles.otherMessageContainer
    ]}>
      {!item.isMe && selectedChat?.type === 'group' && (
        <Text style={styles.senderName}>{item.senderName}</Text>
      )}
      <View style={[
        styles.messageBubble,
        item.isMe ? styles.myMessageBubble : styles.otherMessageBubble
      ]}>
        <Text style={[
          styles.messageText,
          item.isMe ? styles.myMessageText : styles.otherMessageText
        ]}>
          {item.text}
        </Text>
      </View>
      <Text style={styles.messageTime}>{item.timestamp}</Text>
    </View>
  );

  if (selectedChat) {
    return (
      <View style={styles.container}>
        {/* 채팅 헤더 */}
        <LinearGradient colors={['#667eea', '#764ba2']} style={styles.chatHeader}>
          <TouchableOpacity 
            onPress={() => setSelectedChat(null)}
            style={styles.backButton}
          >
            <Icon name="arrow-back" size={24} color="#ffffff" />
          </TouchableOpacity>
          
          <View style={styles.chatHeaderContent}>
            <Text style={styles.chatHeaderAvatar}>{selectedChat.avatar}</Text>
            <View>
              <Text style={styles.chatHeaderName}>
                {selectedChat.name}
                {selectedChat.type === 'group' && (
                  <Text style={styles.chatHeaderParticipants}>
                    {' '}({selectedChat.participants}명)
                  </Text>
                )}
              </Text>
              <Text style={styles.chatHeaderStatus}>
                {selectedChat.isOnline ? '온라인' : '오프라인'}
              </Text>
            </View>
          </View>
          
          <TouchableOpacity style={styles.chatHeaderAction}>
            <Icon name="more-vert" size={24} color="#ffffff" />
          </TouchableOpacity>
        </LinearGradient>
        
        {/* 메시지 목록 */}
        <FlatList
          data={messages}
          keyExtractor={(item) => item.id}
          renderItem={renderMessage}
          style={styles.messagesList}
          showsVerticalScrollIndicator={false}
        />
        
        {/* 메시지 입력 */}
        <View style={styles.messageInputContainer}>
          <TextInput
            style={styles.messageInput}
            value={newMessage}
            onChangeText={setNewMessage}
            placeholder="메시지를 입력하세요..."
            placeholderTextColor="#999"
            multiline
          />
          <TouchableOpacity 
            onPress={sendMessage}
            style={[
              styles.sendButton,
              { opacity: newMessage.trim() ? 1 : 0.5 }
            ]}
            disabled={!newMessage.trim()}
          >
            <LinearGradient colors={['#667eea', '#764ba2']} style={styles.sendButtonGradient}>
              <Icon name="send" size={20} color="#ffffff" />
            </LinearGradient>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <LinearGradient colors={['#667eea', '#764ba2']} style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Text style={styles.title}>💬 채팅</Text>
        <Text style={styles.subtitle}>여행메이트와 실시간 대화하세요</Text>
      </View>
      
      {/* 검색 */}
      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <Icon name="search" size={20} color="#999" />
          <TextInput
            style={styles.searchInput}
            value={searchText}
            onChangeText={setSearchText}
            placeholder="채팅방 검색..."
            placeholderTextColor="#999"
          />
          {searchText ? (
            <TouchableOpacity onPress={() => setSearchText('')}>
              <Icon name="clear" size={20} color="#999" />
            </TouchableOpacity>
          ) : null}
        </View>
      </View>
      
      {/* 채팅방 목록 */}
      <View style={styles.chatRoomsContainer}>
        <FlatList
          data={filteredChatRooms}
          keyExtractor={(item) => item.id}
          renderItem={renderChatRoom}
          style={styles.chatRoomsList}
          showsVerticalScrollIndicator={false}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
        />
      </View>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    alignItems: 'center',
    paddingTop: 20,
    paddingBottom: 20,
    paddingHorizontal: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    textAlign: 'center',
  },
  searchContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  searchInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 25,
    paddingHorizontal: 15,
    paddingVertical: 12,
  },
  searchInput: {
    flex: 1,
    marginLeft: 10,
    fontSize: 16,
    color: '#2d3748',
  },
  chatRoomsContainer: {
    flex: 1,
    backgroundColor: '#ffffff',
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    paddingTop: 20,
  },
  chatRoomsList: {
    flex: 1,
    paddingHorizontal: 20,
  },
  chatRoomItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 15,
  },
  avatarContainer: {
    position: 'relative',
    marginRight: 15,
  },
  avatar: {
    fontSize: 24,
    width: 50,
    height: 50,
    textAlign: 'center',
    textAlignVertical: 'center',
    backgroundColor: '#f8fafc',
    borderRadius: 25,
    overflow: 'hidden',
  },
  onlineIndicator: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    width: 12,
    height: 12,
    backgroundColor: '#2ecc71',
    borderRadius: 6,
    borderWidth: 2,
    borderColor: '#ffffff',
  },
  chatRoomContent: {
    flex: 1,
  },
  chatRoomHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  chatRoomName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2d3748',
  },
  participantCount: {
    fontSize: 14,
    fontWeight: '400',
    color: '#667eea',
  },
  lastMessageTime: {
    fontSize: 12,
    color: '#999',
  },
  chatRoomFooter: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  lastMessage: {
    fontSize: 14,
    color: '#666',
  },
  unreadBadge: {
    backgroundColor: '#ff6b6b',
    borderRadius: 10,
    minWidth: 20,
    height: 20,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 6,
  },
  unreadCount: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  separator: {
    height: 1,
    backgroundColor: '#e2e8f0',
    marginLeft: 65,
  },
  // 채팅 화면 스타일
  chatHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
  },
  backButton: {
    marginRight: 15,
  },
  chatHeaderContent: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  chatHeaderAvatar: {
    fontSize: 20,
    marginRight: 12,
  },
  chatHeaderName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  chatHeaderParticipants: {
    fontSize: 14,
    fontWeight: '400',
  },
  chatHeaderStatus: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.8)',
    marginTop: 2,
  },
  chatHeaderAction: {
    marginLeft: 15,
  },
  messagesList: {
    flex: 1,
    backgroundColor: '#f8fafc',
    paddingHorizontal: 20,
    paddingTop: 20,
  },
  messageContainer: {
    marginBottom: 15,
  },
  myMessageContainer: {
    alignItems: 'flex-end',
  },
  otherMessageContainer: {
    alignItems: 'flex-start',
  },
  senderName: {
    fontSize: 12,
    color: '#667eea',
    fontWeight: '600',
    marginBottom: 4,
    marginLeft: 12,
  },
  messageBubble: {
    maxWidth: '80%',
    paddingHorizontal: 15,
    paddingVertical: 10,
    borderRadius: 20,
  },
  myMessageBubble: {
    backgroundColor: '#667eea',
    borderBottomRightRadius: 5,
  },
  otherMessageBubble: {
    backgroundColor: '#ffffff',
    borderBottomLeftRadius: 5,
  },
  messageText: {
    fontSize: 16,
    lineHeight: 20,
  },
  myMessageText: {
    color: '#ffffff',
  },
  otherMessageText: {
    color: '#2d3748',
  },
  messageTime: {
    fontSize: 11,
    color: '#999',
    marginTop: 4,
    marginHorizontal: 12,
  },
  messageInputContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: '#ffffff',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  messageInput: {
    flex: 1,
    maxHeight: 100,
    minHeight: 40,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    borderRadius: 20,
    paddingHorizontal: 15,
    paddingVertical: 10,
    fontSize: 16,
    color: '#2d3748',
    marginRight: 10,
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    overflow: 'hidden',
  },
  sendButtonGradient: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default ChatScreen;