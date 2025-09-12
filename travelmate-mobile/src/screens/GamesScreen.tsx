import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
  Alert,
  Animated,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

const { width } = Dimensions.get('window');

interface Game {
  id: string;
  title: string;
  description: string;
  icon: string;
  color: string[];
  points: number;
  difficulty: 'easy' | 'medium' | 'hard';
  participants: number;
}

interface Challenge {
  id: string;
  title: string;
  description: string;
  reward: number;
  progress: number;
  total: number;
  timeLeft: string;
}

const games: Game[] = [
  {
    id: 'landmark',
    title: '근처 랜드마크 맞추기',
    description: '주변 1km 내 숨겨진 명소를 찾아보세요!',
    icon: 'location-on',
    color: ['#ff6b6b', '#feca57'],
    points: 150,
    difficulty: 'medium',
    participants: 24
  },
  {
    id: 'photo',
    title: '포토 챌린지',
    description: '오늘의 미션: 현지 음식 사진 찍기',
    icon: 'photo-camera',
    color: ['#54a0ff', '#2e86de'],
    points: 200,
    difficulty: 'easy',
    participants: 38
  },
  {
    id: 'roulette',
    title: '메이트 매칭 룰렛',
    description: '랜덤으로 완벽한 여행 동반자를 찾아보세요',
    icon: 'casino',
    color: ['#5f27cd', '#341f97'],
    points: 100,
    difficulty: 'easy',
    participants: 56
  },
  {
    id: 'quiz',
    title: '여행지 퀴즈',
    description: '현재 지역의 역사와 문화를 알아보세요',
    icon: 'quiz',
    color: ['#00d2d3', '#01a3a4'],
    points: 250,
    difficulty: 'hard',
    participants: 15
  }
];

const dailyChallenges: Challenge[] = [
  {
    id: 'steps',
    title: '일일 걸음 수 달성',
    description: '오늘 10,000보 걸어보세요',
    reward: 50,
    progress: 7823,
    total: 10000,
    timeLeft: '8시간 남음'
  },
  {
    id: 'photos',
    title: '여행 사진 공유',
    description: '3장의 여행 사진을 SNS에 공유하세요',
    reward: 75,
    progress: 1,
    total: 3,
    timeLeft: '12시간 남음'
  },
  {
    id: 'chat',
    title: '새로운 메이트와 대화',
    description: '새로운 여행메이트 5명과 채팅해보세요',
    reward: 100,
    progress: 3,
    total: 5,
    timeLeft: '24시간 남음'
  }
];

const GamesScreen: React.FC = () => {
  const [userPoints, setUserPoints] = useState(1250);
  const [userLevel, setUserLevel] = useState(8);
  const [rouletteRotation] = useState(new Animated.Value(0));

  const spinRoulette = () => {
    Animated.timing(rouletteRotation, {
      toValue: 1,
      duration: 3000,
      useNativeDriver: true,
    }).start(() => {
      rouletteRotation.setValue(0);
      const randomMate = ['김모험가', '박미식가', '이포토그래퍼', '최문화인', '정야경러'][
        Math.floor(Math.random() * 5)
      ];
      Alert.alert('🎯 매칭 완료!', `"${randomMate}"님과 매칭되었습니다!\n지금 바로 채팅을 시작해보세요.`);
      setUserPoints(prev => prev + 100);
    });
  };

  const playGame = (game: Game) => {
    switch (game.id) {
      case 'landmark':
        Alert.alert(
          '🏛️ 랜드마크 게임',
          '주변 명소를 찾는 게임을 시작합니다!\n힌트: 근처에 있는 유명한 건물을 찾아보세요.',
          [
            { text: '취소', style: 'cancel' },
            { text: '시작하기', onPress: () => {
              setUserPoints(prev => prev + game.points);
              Alert.alert('성공!', `${game.points}포인트를 획득했습니다!`);
            }}
          ]
        );
        break;
      case 'photo':
        Alert.alert(
          '📸 포토 챌린지',
          '현지 음식 사진을 찍어 업로드하세요!\n가장 맛있어 보이는 사진에 추가 보너스!',
          [
            { text: '취소', style: 'cancel' },
            { text: '카메라 열기', onPress: () => {
              setUserPoints(prev => prev + game.points);
              Alert.alert('업로드 완료!', `${game.points}포인트를 획득했습니다!`);
            }}
          ]
        );
        break;
      case 'roulette':
        spinRoulette();
        break;
      case 'quiz':
        Alert.alert(
          '🧠 여행지 퀴즈',
          '질문: 이 지역의 대표적인 전통 음식은?\n\n1. 김치찌개\n2. 불고기\n3. 비빔밥\n4. 떡볶이',
          [
            { text: '1번', onPress: () => Alert.alert('틀렸습니다!', '다음 기회에 도전하세요.') },
            { text: '2번', onPress: () => {
              setUserPoints(prev => prev + game.points);
              Alert.alert('정답!', `${game.points}포인트를 획득했습니다!`);
            }},
            { text: '3번', onPress: () => Alert.alert('틀렸습니다!', '다음 기회에 도전하세요.') },
            { text: '4번', onPress: () => Alert.alert('틀렸습니다!', '다음 기회에 도전하세요.') }
          ]
        );
        break;
    }
  };

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

  const rouletteRotate = rouletteRotation.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '1800deg'], // 5바퀴
  });

  return (
    <LinearGradient colors={['#ff6b6b', '#feca57']} style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* 헤더 & 사용자 정보 */}
        <View style={styles.header}>
          <Text style={styles.title}>🎮 여행 게임 센터</Text>
          <Text style={styles.subtitle}>게임을 플레이하고 포인트를 모아보세요!</Text>
          
          <View style={styles.userInfo}>
            <View style={styles.userStats}>
              <View style={styles.statItem}>
                <Text style={styles.statNumber}>{userPoints.toLocaleString()}</Text>
                <Text style={styles.statLabel}>포인트</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={styles.statNumber}>Lv.{userLevel}</Text>
                <Text style={styles.statLabel}>레벨</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={styles.statNumber}>#47</Text>
                <Text style={styles.statLabel}>순위</Text>
              </View>
            </View>
          </View>
        </View>

        {/* 게임 목록 */}
        <View style={styles.gamesSection}>
          <Text style={styles.sectionTitle}>🎯 인기 게임</Text>
          
          {games.map((game) => (
            <TouchableOpacity
              key={game.id}
              onPress={() => playGame(game)}
              style={styles.gameCard}
            >
              <LinearGradient
                colors={['rgba(255, 255, 255, 0.95)', 'rgba(255, 255, 255, 0.85)']}
                style={styles.gameCardGradient}
              >
                <View style={styles.gameCardContent}>
                  <LinearGradient colors={game.color} style={styles.gameIcon}>
                    {game.id === 'roulette' ? (
                      <Animated.View style={{ transform: [{ rotate: rouletteRotate }] }}>
                        <Icon name={game.icon} size={30} color="#ffffff" />
                      </Animated.View>
                    ) : (
                      <Icon name={game.icon} size={30} color="#ffffff" />
                    )}
                  </LinearGradient>
                  
                  <View style={styles.gameInfo}>
                    <Text style={styles.gameTitle}>{game.title}</Text>
                    <Text style={styles.gameDescription}>{game.description}</Text>
                    
                    <View style={styles.gameMetadata}>
                      <View style={styles.gamePoints}>
                        <Icon name="stars" size={16} color="#feca57" />
                        <Text style={styles.gamePointsText}>+{game.points}P</Text>
                      </View>
                      
                      <View style={[styles.gameDifficulty, { backgroundColor: getDifficultyColor(game.difficulty) }]}>
                        <Text style={styles.gameDifficultyText}>{getDifficultyText(game.difficulty)}</Text>
                      </View>
                      
                      <View style={styles.gameParticipants}>
                        <Icon name="person" size={16} color="#667eea" />
                        <Text style={styles.gameParticipantsText}>{game.participants}</Text>
                      </View>
                    </View>
                  </View>
                  
                  <Icon name="chevron-right" size={24} color="#667eea" />
                </View>
              </LinearGradient>
            </TouchableOpacity>
          ))}
        </View>

        {/* 일일 챌린지 */}
        <View style={styles.challengesSection}>
          <Text style={styles.sectionTitle}>🏆 일일 챌린지</Text>
          
          {dailyChallenges.map((challenge) => (
            <View key={challenge.id} style={styles.challengeCard}>
              <View style={styles.challengeHeader}>
                <Text style={styles.challengeTitle}>{challenge.title}</Text>
                <Text style={styles.challengeReward}>+{challenge.reward}P</Text>
              </View>
              
              <Text style={styles.challengeDescription}>{challenge.description}</Text>
              
              <View style={styles.challengeProgress}>
                <View style={styles.progressBar}>
                  <View 
                    style={[
                      styles.progressFill,
                      { width: `${(challenge.progress / challenge.total) * 100}%` }
                    ]} 
                  />
                </View>
                <Text style={styles.progressText}>
                  {challenge.progress}/{challenge.total}
                </Text>
              </View>
              
              <Text style={styles.challengeTimeLeft}>{challenge.timeLeft}</Text>
            </View>
          ))}
        </View>

        {/* 리더보드 */}
        <View style={styles.leaderboardSection}>
          <Text style={styles.sectionTitle}>🥇 이번 주 리더보드</Text>
          
          <View style={styles.leaderboardCard}>
            {[
              { rank: 1, name: '김게이머', points: 3420, emoji: '🥇' },
              { rank: 2, name: '박여행가', points: 3180, emoji: '🥈' },
              { rank: 3, name: '이챌린저', points: 2950, emoji: '🥉' },
              { rank: 4, name: '나 (정유저)', points: userPoints, emoji: '👤' },
            ].map((player) => (
              <View key={player.rank} style={[
                styles.leaderboardItem,
                player.name.includes('나') && styles.leaderboardMyItem
              ]}>
                <Text style={styles.leaderboardRank}>{player.emoji}</Text>
                <Text style={styles.leaderboardName}>{player.name}</Text>
                <Text style={styles.leaderboardPoints}>{player.points.toLocaleString()}P</Text>
              </View>
            ))}
          </View>
        </View>

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
    paddingTop: 20,
    paddingHorizontal: 20,
    paddingBottom: 30,
    alignItems: 'center',
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
    marginBottom: 20,
  },
  userInfo: {
    width: '100%',
  },
  userStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 15,
    paddingVertical: 20,
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 20,
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
  gamesSection: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 15,
  },
  gameCard: {
    marginBottom: 15,
    borderRadius: 15,
    overflow: 'hidden',
    elevation: 4,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  gameCardGradient: {
    padding: 20,
  },
  gameCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  gameIcon: {
    width: 60,
    height: 60,
    borderRadius: 30,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 15,
  },
  gameInfo: {
    flex: 1,
  },
  gameTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2d3748',
    marginBottom: 4,
  },
  gameDescription: {
    fontSize: 13,
    color: '#4a5568',
    marginBottom: 8,
    lineHeight: 16,
  },
  gameMetadata: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  gamePoints: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(254, 202, 87, 0.2)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
  },
  gamePointsText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#f39c12',
    marginLeft: 4,
  },
  gameDifficulty: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
  },
  gameDifficultyText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#ffffff',
  },
  gameParticipants: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  gameParticipantsText: {
    fontSize: 12,
    color: '#667eea',
    marginLeft: 4,
  },
  challengesSection: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  challengeCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 15,
    padding: 20,
    marginBottom: 15,
  },
  challengeHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  challengeTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2d3748',
    flex: 1,
  },
  challengeReward: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#feca57',
    backgroundColor: 'rgba(254, 202, 87, 0.2)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
  },
  challengeDescription: {
    fontSize: 14,
    color: '#4a5568',
    marginBottom: 15,
  },
  challengeProgress: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  progressBar: {
    flex: 1,
    height: 8,
    backgroundColor: '#e2e8f0',
    borderRadius: 4,
    marginRight: 10,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#667eea',
    borderRadius: 4,
  },
  progressText: {
    fontSize: 12,
    color: '#4a5568',
    fontWeight: '600',
  },
  challengeTimeLeft: {
    fontSize: 12,
    color: '#e74c3c',
    fontStyle: 'italic',
  },
  leaderboardSection: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  leaderboardCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 15,
    overflow: 'hidden',
  },
  leaderboardItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  leaderboardMyItem: {
    backgroundColor: 'rgba(102, 126, 234, 0.1)',
  },
  leaderboardRank: {
    fontSize: 20,
    marginRight: 15,
  },
  leaderboardName: {
    flex: 1,
    fontSize: 16,
    fontWeight: '600',
    color: '#2d3748',
  },
  leaderboardPoints: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#667eea',
  },
  bottomPadding: {
    height: 30,
  },
});

export default GamesScreen;