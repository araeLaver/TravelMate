import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
  Alert,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

const { width } = Dimensions.get('window');

interface TravelMood {
  id: string;
  emoji: string;
  title: string;
  description: string;
  color: string[];
}

const travelMoods: TravelMood[] = [
  {
    id: 'adventure',
    emoji: '🏔️',
    title: '모험 탐험',
    description: '산, 등산, 트래킹을 좋아해요',
    color: ['#ff6b6b', '#feca57']
  },
  {
    id: 'food',
    emoji: '🍜',
    title: '맛집 탐방',
    description: '현지 음식과 맛집 찾기',
    color: ['#ff9ff3', '#f368e0']
  },
  {
    id: 'photo',
    emoji: '📸',
    title: '인생샷 찍기',
    description: '포토 스팟과 사진 찍기',
    color: ['#54a0ff', '#2e86de']
  },
  {
    id: 'culture',
    emoji: '🎨',
    title: '문화 체험',
    description: '박물관, 갤러리, 문화 탐방',
    color: ['#5f27cd', '#341f97']
  },
  {
    id: 'cafe',
    emoji: '☕',
    title: '카페 투어',
    description: '분위기 좋은 카페 탐방',
    color: ['#00d2d3', '#01a3a4']
  },
  {
    id: 'night',
    emoji: '🌃',
    title: '야경 감상',
    description: '밤 풍경과 야경 명소',
    color: ['#fd79a8', '#e84393']
  }
];

const DiscoverScreen: React.FC = () => {
  const navigation = useNavigation();
  const [selectedMood, setSelectedMood] = useState<string>('adventure');
  const [nearbyCount, setNearbyCount] = useState(12);
  const [activeUsers, setActiveUsers] = useState(8);

  useEffect(() => {
    // 실시간 사용자 수 업데이트 시뮬레이션
    const interval = setInterval(() => {
      setNearbyCount(prev => prev + Math.floor(Math.random() * 3) - 1);
      setActiveUsers(prev => Math.max(1, prev + Math.floor(Math.random() * 2) - 1));
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleShakeNavigation = () => {
    navigation.navigate('Shake' as never);
  };

  const handleMoodSelect = (moodId: string) => {
    setSelectedMood(moodId);
    Alert.alert('여행 무드 변경', `"${travelMoods.find(m => m.id === moodId)?.title}" 무드로 설정되었습니다!`);
  };

  return (
    <LinearGradient colors={['#667eea', '#764ba2']} style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* 헤더 */}
        <View style={styles.header}>
          <Text style={styles.title}>🌍 TravelMate</Text>
          <Text style={styles.subtitle}>완벽한 여행 동반자를 찾아보세요</Text>
        </View>

        {/* 실시간 통계 */}
        <View style={styles.statsContainer}>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{nearbyCount}</Text>
            <Text style={styles.statLabel}>주변 여행자</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{activeUsers}</Text>
            <Text style={styles.statLabel}>현재 활동 중</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>24</Text>
            <Text style={styles.statLabel}>새로운 매칭</Text>
          </View>
        </View>

        {/* 폰 흔들기 메인 버튼 */}
        <TouchableOpacity onPress={handleShakeNavigation} style={styles.shakeMainButton}>
          <LinearGradient
            colors={['rgba(255, 255, 255, 0.9)', 'rgba(255, 255, 255, 0.7)']}
            style={styles.shakeButtonGradient}
          >
            <View style={styles.shakeButtonContent}>
              <Icon name=\"vibration\" size={60} color=\"#667eea\" />
              <Text style={styles.shakeMainText}>📱 폰 흔들어서 발견하기</Text>
              <Text style={styles.shakeSubText}>
                실제 가속도계를 사용한 진짜 흔들기!
              </Text>
              <View style={styles.shakeIndicator}>
                <Text style={styles.shakeIndicatorText}>👈 탭해서 시작</Text>
              </View>
            </View>
          </LinearGradient>
        </TouchableOpacity>

        {/* 여행 무드 선택 */}
        <View style={styles.moodSection}>
          <Text style={styles.sectionTitle}>🎯 나의 여행 무드</Text>
          <Text style={styles.sectionSubtitle}>
            현재 기분에 맞는 여행 스타일을 선택해보세요
          </Text>
          
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={styles.moodScrollView}
          >
            {travelMoods.map((mood) => (
              <TouchableOpacity
                key={mood.id}
                onPress={() => handleMoodSelect(mood.id)}
                style={[
                  styles.moodCard,
                  selectedMood === mood.id && styles.moodCardSelected
                ]}
              >
                <LinearGradient
                  colors={mood.color}
                  style={[
                    styles.moodCardGradient,
                    selectedMood === mood.id && styles.moodCardGradientSelected
                  ]}
                >
                  <Text style={styles.moodEmoji}>{mood.emoji}</Text>
                  <Text style={styles.moodTitle}>{mood.title}</Text>
                  <Text style={styles.moodDescription}>{mood.description}</Text>
                  
                  {selectedMood === mood.id && (
                    <View style={styles.selectedIndicator}>
                      <Icon name=\"check-circle\" size={20} color=\"#ffffff\" />
                    </View>
                  )}
                </LinearGradient>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* 빠른 액션 버튼들 */}
        <View style={styles.quickActions}>
          <Text style={styles.sectionTitle}>⚡ 빠른 액션</Text>
          
          <View style={styles.actionGrid}>
            <TouchableOpacity style={styles.actionButton}>
              <LinearGradient colors={['#ff6b6b', '#feca57']} style={styles.actionButtonGradient}>
                <Icon name=\"location-on\" size={30} color=\"#ffffff\" />
                <Text style={styles.actionButtonText}>근처 탐색</Text>
              </LinearGradient>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.actionButton}>
              <LinearGradient colors={['#54a0ff', '#2e86de']} style={styles.actionButtonGradient}>
                <Icon name=\"shuffle\" size={30} color=\"#ffffff\" />
                <Text style={styles.actionButtonText}>랜덤 매칭</Text>
              </LinearGradient>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.actionButton}>
              <LinearGradient colors={['#5f27cd', '#341f97']} style={styles.actionButtonGradient}>
                <Icon name=\"event\" size={30} color=\"#ffffff\" />
                <Text style={styles.actionButtonText}>이벤트 참여</Text>
              </LinearGradient>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.actionButton}>
              <LinearGradient colors={['#00d2d3', '#01a3a4']} style={styles.actionButtonGradient}>
                <Icon name=\"star\" size={30} color=\"#ffffff\" />
                <Text style={styles.actionButtonText}>추천 메이트</Text>
              </LinearGradient>
            </TouchableOpacity>
          </View>
        </View>

        {/* 팁 섹션 */}
        <View style={styles.tipSection}>
          <View style={styles.tipCard}>
            <Icon name=\"lightbulb-outline\" size={24} color=\"#667eea\" />
            <View style={styles.tipContent}>
              <Text style={styles.tipTitle}>💡 사용 팁</Text>
              <Text style={styles.tipText}>
                폰을 세게 흔들수록 더 넓은 범위에서 메이트를 찾을 수 있어요!{'\n'}
                흔들기 강도에 따라 검색 반경이 1km~10km까지 조절됩니다.
              </Text>
            </View>
          </View>
        </View>
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
    fontSize: 32,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    textAlign: 'center',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    marginHorizontal: 20,
    borderRadius: 15,
    paddingVertical: 20,
    marginBottom: 30,
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  statLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.8)',
    marginTop: 4,
  },
  statDivider: {
    width: 1,
    height: 30,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
  },
  shakeMainButton: {
    marginHorizontal: 20,
    marginBottom: 30,
    borderRadius: 20,
    overflow: 'hidden',
    elevation: 8,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  shakeButtonGradient: {
    padding: 25,
  },
  shakeButtonContent: {
    alignItems: 'center',
  },
  shakeMainText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#667eea',
    marginTop: 15,
    textAlign: 'center',
  },
  shakeSubText: {
    fontSize: 14,
    color: '#4a5568',
    marginTop: 8,
    textAlign: 'center',
  },
  shakeIndicator: {
    backgroundColor: '#667eea',
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 20,
    marginTop: 15,
  },
  shakeIndicatorText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
  },
  moodSection: {
    marginBottom: 30,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginLeft: 20,
    marginBottom: 8,
  },
  sectionSubtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.8)',
    marginLeft: 20,
    marginBottom: 20,
  },
  moodScrollView: {
    paddingLeft: 20,
  },
  moodCard: {
    marginRight: 15,
    borderRadius: 15,
    overflow: 'hidden',
    elevation: 4,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  moodCardSelected: {
    transform: [{ scale: 1.05 }],
  },
  moodCardGradient: {
    width: 140,
    height: 120,
    padding: 15,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  moodCardGradientSelected: {
    borderWidth: 2,
    borderColor: '#ffffff',
  },
  moodEmoji: {
    fontSize: 28,
    marginBottom: 8,
  },
  moodTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 4,
  },
  moodDescription: {
    fontSize: 10,
    color: 'rgba(255, 255, 255, 0.9)',
    textAlign: 'center',
    lineHeight: 12,
  },
  selectedIndicator: {
    position: 'absolute',
    top: 8,
    right: 8,
  },
  quickActions: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  actionGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  actionButton: {
    width: (width - 50) / 2,
    marginBottom: 15,
    borderRadius: 12,
    overflow: 'hidden',
    elevation: 4,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  actionButtonGradient: {
    padding: 20,
    alignItems: 'center',
  },
  actionButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 8,
  },
  tipSection: {
    paddingHorizontal: 20,
    paddingBottom: 30,
  },
  tipCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 15,
    padding: 20,
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  tipContent: {
    flex: 1,
    marginLeft: 15,
  },
  tipTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2d3748',
    marginBottom: 8,
  },
  tipText: {
    fontSize: 14,
    color: '#4a5568',
    lineHeight: 20,
  },
});

export default DiscoverScreen;