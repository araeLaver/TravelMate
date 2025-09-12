import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

interface TravelGroup {
  id: string;
  name: string;
  description: string;
  destination: string;
  startDate: string;
  endDate: string;
  currentMembers: number;
  maxMembers: number;
  tags: string[];
  difficulty: 'easy' | 'medium' | 'hard';
  cost: string;
  organizer: string;
  image: string;
  isJoined: boolean;
}

const mockTravelGroups: TravelGroup[] = [
  {
    id: '1',
    name: '제주도 힐링 여행',
    description: '한라산 등반과 해변에서의 여유로운 시간을 함께해요!',
    destination: '제주도',
    startDate: '2024-03-15',
    endDate: '2024-03-18',
    currentMembers: 6,
    maxMembers: 8,
    tags: ['자연', '힐링', '등산', '해변'],
    difficulty: 'medium',
    cost: '30만원대',
    organizer: '김모험가',
    image: '🏔️',
    isJoined: true,
  },
  {
    id: '2',
    name: '서울 맛집 투어',
    description: '서울의 숨겨진 맛집들을 찾아 떠나는 미식 여행!',
    destination: '서울',
    startDate: '2024-03-08',
    endDate: '2024-03-10',
    currentMembers: 4,
    maxMembers: 6,
    tags: ['미식', '도시', '카페', '전통'],
    difficulty: 'easy',
    cost: '15만원대',
    organizer: '박미식가',
    image: '🍜',
    isJoined: false,
  },
  {
    id: '3',
    name: '부산 야경 포토 투어',
    description: '부산의 아름다운 야경을 담는 사진 촬영 여행',
    destination: '부산',
    startDate: '2024-03-22',
    endDate: '2024-03-24',
    currentMembers: 3,
    maxMembers: 5,
    tags: ['사진', '야경', '도시', '바다'],
    difficulty: 'medium',
    cost: '25만원대',
    organizer: '이포토그래퍼',
    image: '📸',
    isJoined: false,
  },
  {
    id: '4',
    name: '경주 역사 탐방',
    description: '천년 고도 경주의 역사와 문화를 체험하는 여행',
    destination: '경주',
    startDate: '2024-04-05',
    endDate: '2024-04-07',
    currentMembers: 5,
    maxMembers: 10,
    tags: ['역사', '문화', '유적', '전통'],
    difficulty: 'easy',
    cost: '20만원대',
    organizer: '최문화인',
    image: '🏛️',
    isJoined: false,
  },
  {
    id: '5',
    name: '강원도 스키 여행',
    description: '겨울 스포츠와 온천을 함께 즐기는 강원도 여행',
    destination: '평창/강릉',
    startDate: '2024-03-29',
    endDate: '2024-03-31',
    currentMembers: 2,
    maxMembers: 6,
    tags: ['스키', '온천', '겨울', '스포츠'],
    difficulty: 'hard',
    cost: '40만원대',
    organizer: '정스키어',
    image: '⛷️',
    isJoined: false,
  },
];

const GroupsScreen: React.FC = () => {
  const [groups, setGroups] = useState<TravelGroup[]>(mockTravelGroups);
  const [filter, setFilter] = useState<'all' | 'joined' | 'available'>('all');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  const allTags = Array.from(new Set(groups.flatMap(g => g.tags)));

  const joinGroup = (groupId: string) => {
    Alert.alert(
      '그룹 가입',
      '이 여행 그룹에 가입하시겠습니까?',
      [
        { text: '취소', style: 'cancel' },
        {
          text: '가입하기',
          onPress: () => {
            setGroups(prev =>
              prev.map(group =>
                group.id === groupId
                  ? {
                      ...group,
                      isJoined: true,
                      currentMembers: group.currentMembers + 1,
                    }
                  : group
              )
            );
            Alert.alert('가입 완료!', '여행 그룹에 성공적으로 가입되었습니다. 채팅방에서 다른 멤버들과 소통해보세요!');
          },
        },
      ]
    );
  };

  const leaveGroup = (groupId: string) => {
    Alert.alert(
      '그룹 탈퇴',
      '정말로 이 여행 그룹에서 탈퇴하시겠습니까?',
      [
        { text: '취소', style: 'cancel' },
        {
          text: '탈퇴하기',
          style: 'destructive',
          onPress: () => {
            setGroups(prev =>
              prev.map(group =>
                group.id === groupId
                  ? {
                      ...group,
                      isJoined: false,
                      currentMembers: group.currentMembers - 1,
                    }
                  : group
              )
            );
            Alert.alert('탈퇴 완료', '여행 그룹에서 탈퇴되었습니다.');
          },
        },
      ]
    );
  };

  const toggleTagFilter = (tag: string) => {
    setSelectedTags(prev =>
      prev.includes(tag)
        ? prev.filter(t => t !== tag)
        : [...prev, tag]
    );
  };

  const filteredGroups = groups.filter(group => {
    // 필터 조건
    if (filter === 'joined' && !group.isJoined) return false;
    if (filter === 'available' && (group.isJoined || group.currentMembers >= group.maxMembers)) return false;
    
    // 태그 필터
    if (selectedTags.length > 0 && !selectedTags.some(tag => group.tags.includes(tag))) return false;
    
    return true;
  });

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return '#2ecc71';
      case 'medium': return '#f39c12';
      case 'hard': return '#e74c3c';
      default: return '#95a5a6';
    }
  };

  const getDifficultyText = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return '쉬움';
      case 'medium': return '보통';
      case 'hard': return '어려움';
      default: return '';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  };

  return (
    <LinearGradient colors={['#667eea', '#764ba2']} style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* 헤더 */}
        <View style={styles.header}>
          <Text style={styles.title}>🗺️ 여행 그룹</Text>
          <Text style={styles.subtitle}>함께할 여행 동반자들을 찾아보세요</Text>
        </View>

        {/* 필터 버튼 */}
        <View style={styles.filterContainer}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {[
              { key: 'all', label: '전체', count: groups.length },
              { key: 'joined', label: '참여 중', count: groups.filter(g => g.isJoined).length },
              { key: 'available', label: '참여 가능', count: groups.filter(g => !g.isJoined && g.currentMembers < g.maxMembers).length },
            ].map(filterOption => (
              <TouchableOpacity
                key={filterOption.key}
                onPress={() => setFilter(filterOption.key as any)}
                style={[
                  styles.filterButton,
                  filter === filterOption.key && styles.filterButtonActive,
                ]}
              >
                <Text
                  style={[
                    styles.filterButtonText,
                    filter === filterOption.key && styles.filterButtonTextActive,
                  ]}
                >
                  {filterOption.label} ({filterOption.count})
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* 태그 필터 */}
        <View style={styles.tagsContainer}>
          <Text style={styles.tagsTitle}>관심 태그</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {allTags.map(tag => (
              <TouchableOpacity
                key={tag}
                onPress={() => toggleTagFilter(tag)}
                style={[
                  styles.tagButton,
                  selectedTags.includes(tag) && styles.tagButtonActive,
                ]}
              >
                <Text
                  style={[
                    styles.tagButtonText,
                    selectedTags.includes(tag) && styles.tagButtonTextActive,
                  ]}
                >
                  {tag}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* 그룹 리스트 */}
        <View style={styles.groupsContainer}>
          {filteredGroups.map(group => (
            <View key={group.id} style={styles.groupCard}>
              <LinearGradient
                colors={['rgba(255, 255, 255, 0.95)', 'rgba(255, 255, 255, 0.85)']}
                style={styles.groupCardGradient}
              >
                {/* 그룹 헤더 */}
                <View style={styles.groupHeader}>
                  <View style={styles.groupImageContainer}>
                    <Text style={styles.groupImage}>{group.image}</Text>
                    {group.isJoined && (
                      <View style={styles.joinedBadge}>
                        <Icon name="check" size={12} color="#ffffff" />
                      </View>
                    )}
                  </View>
                  
                  <View style={styles.groupInfo}>
                    <Text style={styles.groupName}>{group.name}</Text>
                    <Text style={styles.groupDestination}>📍 {group.destination}</Text>
                    <Text style={styles.groupDate}>
                      🗓️ {formatDate(group.startDate)} - {formatDate(group.endDate)}
                    </Text>
                  </View>
                  
                  <View style={styles.groupMeta}>
                    <View style={[styles.difficultyBadge, { backgroundColor: getDifficultyColor(group.difficulty) }]}>
                      <Text style={styles.difficultyText}>{getDifficultyText(group.difficulty)}</Text>
                    </View>
                    <Text style={styles.groupCost}>{group.cost}</Text>
                  </View>
                </View>

                {/* 그룹 설명 */}
                <Text style={styles.groupDescription}>{group.description}</Text>

                {/* 태그 */}
                <View style={styles.groupTags}>
                  {group.tags.map(tag => (
                    <View key={tag} style={styles.groupTag}>
                      <Text style={styles.groupTagText}>#{tag}</Text>
                    </View>
                  ))}
                </View>

                {/* 그룹 하단 정보 */}
                <View style={styles.groupFooter}>
                  <View style={styles.groupStats}>
                    <View style={styles.groupStat}>
                      <Icon name="person" size={16} color="#667eea" />
                      <Text style={styles.groupStatText}>
                        {group.currentMembers}/{group.maxMembers}명
                      </Text>
                    </View>
                    <View style={styles.groupStat}>
                      <Icon name="account-circle" size={16} color="#667eea" />
                      <Text style={styles.groupStatText}>{group.organizer}</Text>
                    </View>
                  </View>

                  <TouchableOpacity
                    onPress={() => group.isJoined ? leaveGroup(group.id) : joinGroup(group.id)}
                    style={[
                      styles.groupAction,
                      group.isJoined ? styles.leaveButton : styles.joinButton,
                      group.currentMembers >= group.maxMembers && !group.isJoined && styles.disabledButton,
                    ]}
                    disabled={group.currentMembers >= group.maxMembers && !group.isJoined}
                  >
                    <LinearGradient
                      colors={
                        group.isJoined
                          ? ['#e74c3c', '#c0392b']
                          : group.currentMembers >= group.maxMembers
                          ? ['#95a5a6', '#7f8c8d']
                          : ['#667eea', '#764ba2']
                      }
                      style={styles.groupActionGradient}
                    >
                      <Icon
                        name={
                          group.isJoined
                            ? 'exit-to-app'
                            : group.currentMembers >= group.maxMembers
                            ? 'block'
                            : 'group-add'
                        }
                        size={16}
                        color="#ffffff"
                      />
                      <Text style={styles.groupActionText}>
                        {group.isJoined
                          ? '탈퇴'
                          : group.currentMembers >= group.maxMembers
                          ? '마감'
                          : '가입'}
                      </Text>
                    </LinearGradient>
                  </TouchableOpacity>
                </View>
              </LinearGradient>
            </View>
          ))}
        </View>

        {/* 새 그룹 만들기 버튼 */}
        <TouchableOpacity style={styles.createGroupButton}>
          <LinearGradient
            colors={['rgba(255, 255, 255, 0.9)', 'rgba(255, 255, 255, 0.7)']}
            style={styles.createGroupButtonGradient}
          >
            <Icon name="add-circle" size={24} color="#667eea" />
            <Text style={styles.createGroupButtonText}>새 여행 그룹 만들기</Text>
          </LinearGradient>
        </TouchableOpacity>

        {/* 하단 여백 */}
        <View style={styles.bottomPadding} />
      </ScrollView>
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
    paddingBottom: 30,
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
  filterContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  filterButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    marginRight: 10,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
  },
  filterButtonActive: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
  },
  filterButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
  },
  filterButtonTextActive: {
    color: '#667eea',
  },
  tagsContainer: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  tagsTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 10,
  },
  tagButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
    marginRight: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
  },
  tagButtonActive: {
    backgroundColor: '#feca57',
  },
  tagButtonText: {
    color: '#ffffff',
    fontSize: 12,
    fontWeight: '500',
  },
  tagButtonTextActive: {
    color: '#ffffff',
    fontWeight: '600',
  },
  groupsContainer: {
    paddingHorizontal: 20,
  },
  groupCard: {
    marginBottom: 20,
    borderRadius: 15,
    overflow: 'hidden',
    elevation: 4,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  groupCardGradient: {
    padding: 20,
  },
  groupHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 15,
  },
  groupImageContainer: {
    position: 'relative',
    marginRight: 15,
  },
  groupImage: {
    fontSize: 30,
    width: 50,
    height: 50,
    textAlign: 'center',
    textAlignVertical: 'center',
    backgroundColor: '#f8fafc',
    borderRadius: 25,
    overflow: 'hidden',
  },
  joinedBadge: {
    position: 'absolute',
    top: -5,
    right: -5,
    width: 20,
    height: 20,
    backgroundColor: '#2ecc71',
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#ffffff',
  },
  groupInfo: {
    flex: 1,
  },
  groupName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2d3748',
    marginBottom: 4,
  },
  groupDestination: {
    fontSize: 14,
    color: '#667eea',
    marginBottom: 2,
  },
  groupDate: {
    fontSize: 14,
    color: '#4a5568',
  },
  groupMeta: {
    alignItems: 'flex-end',
  },
  difficultyBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
    marginBottom: 4,
  },
  difficultyText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#ffffff',
  },
  groupCost: {
    fontSize: 14,
    fontWeight: '600',
    color: '#e74c3c',
  },
  groupDescription: {
    fontSize: 14,
    color: '#4a5568',
    lineHeight: 18,
    marginBottom: 15,
  },
  groupTags: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 15,
  },
  groupTag: {
    backgroundColor: 'rgba(102, 126, 234, 0.1)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
    marginRight: 6,
    marginBottom: 4,
  },
  groupTagText: {
    fontSize: 12,
    color: '#667eea',
    fontWeight: '500',
  },
  groupFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  groupStats: {
    flex: 1,
  },
  groupStat: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  groupStatText: {
    fontSize: 13,
    color: '#4a5568',
    marginLeft: 6,
  },
  groupAction: {
    borderRadius: 20,
    overflow: 'hidden',
  },
  groupActionGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  groupActionText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
    marginLeft: 6,
  },
  joinButton: {},
  leaveButton: {},
  disabledButton: {
    opacity: 0.6,
  },
  createGroupButton: {
    marginHorizontal: 20,
    marginTop: 10,
    borderRadius: 15,
    overflow: 'hidden',
  },
  createGroupButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 15,
  },
  createGroupButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#667eea',
    marginLeft: 8,
  },
  bottomPadding: {
    height: 30,
  },
});

export default GroupsScreen;