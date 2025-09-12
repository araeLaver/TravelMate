import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  Switch,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';

interface UserProfile {
  name: string;
  username: string;
  email: string;
  bio: string;
  location: string;
  joinDate: string;
  points: number;
  level: number;
  travelCount: number;
  reviewScore: number;
  badges: string[];
  travelStyle: string[];
  languages: string[];
}

interface Settings {
  notifications: boolean;
  locationSharing: boolean;
  autoMatching: boolean;
  showOnlineStatus: boolean;
}

const mockProfile: UserProfile = {
  name: '김여행러버',
  username: 'travel_lover_kim',
  email: 'kim@example.com',
  bio: '새로운 곳을 탐험하고 다양한 문화를 경험하는 것을 좋아합니다. 함께 멋진 여행 만들어요! ✈️',
  location: '서울, 대한민국',
  joinDate: '2023-05-15',
  points: 1250,
  level: 8,
  travelCount: 23,
  reviewScore: 4.8,
  badges: ['🏆', '🌟', '📸', '🥇', '⛰️'],
  travelStyle: ['모험가', '미식가', '문화체험'],
  languages: ['한국어', '영어', '일본어'],
};

const ProfileScreen: React.FC = () => {
  const [profile, setProfile] = useState<UserProfile>(mockProfile);
  const [settings, setSettings] = useState<Settings>({
    notifications: true,
    locationSharing: true,
    autoMatching: false,
    showOnlineStatus: true,
  });
  const [activeTab, setActiveTab] = useState<'profile' | 'settings' | 'stats'>('profile');

  const handleLogout = () => {
    Alert.alert(
      '로그아웃',
      '정말로 로그아웃 하시겠습니까?',
      [
        { text: '취소', style: 'cancel' },
        { text: '로그아웃', style: 'destructive', onPress: () => {
          Alert.alert('로그아웃 완료', '성공적으로 로그아웃되었습니다.');
        }},
      ]
    );
  };

  const handleEditProfile = () => {
    Alert.alert('프로필 편집', '프로필 편집 기능을 개발 중입니다.');
  };

  const handleSettingToggle = (key: keyof Settings) => {
    setSettings(prev => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const formatJoinDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
  };

  const renderProfileTab = () => (
    <View style={styles.tabContent}>
      {/* 프로필 정보 */}
      <View style={styles.profileInfo}>
        <View style={styles.avatarContainer}>
          <LinearGradient colors={['#667eea', '#764ba2']} style={styles.avatar}>
            <Text style={styles.avatarText}>{profile.name.charAt(0)}</Text>
          </LinearGradient>
          <View style={styles.onlineIndicator} />
        </View>
        
        <Text style={styles.profileName}>{profile.name}</Text>
        <Text style={styles.profileUsername}>@{profile.username}</Text>
        <Text style={styles.profileLocation}>📍 {profile.location}</Text>
        
        <TouchableOpacity onPress={handleEditProfile} style={styles.editButton}>
          <LinearGradient colors={['#667eea', '#764ba2']} style={styles.editButtonGradient}>
            <Icon name="edit" size={16} color="#ffffff" />
            <Text style={styles.editButtonText}>프로필 편집</Text>
          </LinearGradient>
        </TouchableOpacity>
      </View>

      {/* 소개 */}
      <View style={styles.bioSection}>
        <Text style={styles.sectionTitle}>📝 소개</Text>
        <Text style={styles.bioText}>{profile.bio}</Text>
      </View>

      {/* 여행 스타일 */}
      <View style={styles.travelStyleSection}>
        <Text style={styles.sectionTitle}>✈️ 여행 스타일</Text>
        <View style={styles.tagContainer}>
          {profile.travelStyle.map(style => (
            <View key={style} style={styles.styleTag}>
              <Text style={styles.styleTagText}>{style}</Text>
            </View>
          ))}
        </View>
      </View>

      {/* 언어 */}
      <View style={styles.languageSection}>
        <Text style={styles.sectionTitle}>🗣️ 사용 언어</Text>
        <View style={styles.tagContainer}>
          {profile.languages.map(language => (
            <View key={language} style={styles.languageTag}>
              <Text style={styles.languageTagText}>{language}</Text>
            </View>
          ))}
        </View>
      </View>

      {/* 뱃지 */}
      <View style={styles.badgeSection}>
        <Text style={styles.sectionTitle}>🏅 획득 뱃지</Text>
        <View style={styles.badgeContainer}>
          {profile.badges.map((badge, index) => (
            <View key={index} style={styles.badge}>
              <Text style={styles.badgeEmoji}>{badge}</Text>
            </View>
          ))}
        </View>
      </View>
    </View>
  );

  const renderStatsTab = () => (
    <View style={styles.tabContent}>
      {/* 통계 카드들 */}
      <View style={styles.statsGrid}>
        <View style={styles.statCard}>
          <LinearGradient colors={['#ff6b6b', '#feca57']} style={styles.statCardGradient}>
            <Icon name="star" size={30} color="#ffffff" />
            <Text style={styles.statNumber}>{profile.points.toLocaleString()}</Text>
            <Text style={styles.statLabel}>포인트</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={['#54a0ff', '#2e86de']} style={styles.statCardGradient}>
            <Icon name="trending-up" size={30} color="#ffffff" />
            <Text style={styles.statNumber}>Lv.{profile.level}</Text>
            <Text style={styles.statLabel}>레벨</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={['#5f27cd', '#341f97']} style={styles.statCardGradient}>
            <Icon name="flight-takeoff" size={30} color="#ffffff" />
            <Text style={styles.statNumber}>{profile.travelCount}</Text>
            <Text style={styles.statLabel}>여행 횟수</Text>
          </LinearGradient>
        </View>

        <View style={styles.statCard}>
          <LinearGradient colors={['#00d2d3', '#01a3a4']} style={styles.statCardGradient}>
            <Icon name="thumb-up" size={30} color="#ffffff" />
            <Text style={styles.statNumber}>{profile.reviewScore}</Text>
            <Text style={styles.statLabel}>평점</Text>
          </LinearGradient>
        </View>
      </View>

      {/* 가입 정보 */}
      <View style={styles.joinInfoSection}>
        <Text style={styles.sectionTitle}>📅 가입 정보</Text>
        <Text style={styles.joinInfoText}>
          {formatJoinDate(profile.joinDate)}부터 TravelMate와 함께하고 있어요!
        </Text>
      </View>

      {/* 최근 활동 */}
      <View style={styles.activitySection}>
        <Text style={styles.sectionTitle}>⚡ 최근 활동</Text>
        <View style={styles.activityList}>
          <View style={styles.activityItem}>
            <Icon name="group-add" size={20} color="#667eea" />
            <Text style={styles.activityText}>"제주도 힐링 여행" 그룹에 참여했습니다</Text>
            <Text style={styles.activityTime}>2시간 전</Text>
          </View>
          <View style={styles.activityItem}>
            <Icon name="star" size={20} color="#feca57" />
            <Text style={styles.activityText}>새로운 뱃지 "포토그래퍼"를 획득했습니다</Text>
            <Text style={styles.activityTime}>1일 전</Text>
          </View>
          <View style={styles.activityItem}>
            <Icon name="chat" size={20} color="#2ecc71" />
            <Text style={styles.activityText}>"서울 맛집 투어" 채팅에 참여했습니다</Text>
            <Text style={styles.activityTime}>3일 전</Text>
          </View>
        </View>
      </View>
    </View>
  );

  const renderSettingsTab = () => (
    <View style={styles.tabContent}>
      {/* 알림 설정 */}
      <View style={styles.settingsSection}>
        <Text style={styles.sectionTitle}>🔔 알림 설정</Text>
        
        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Text style={styles.settingLabel}>푸시 알림</Text>
            <Text style={styles.settingDescription}>메이트 매칭, 채팅 메시지 등</Text>
          </View>
          <Switch
            value={settings.notifications}
            onValueChange={() => handleSettingToggle('notifications')}
            trackColor={{ false: '#e2e8f0', true: '#667eea' }}
            thumbColor={settings.notifications ? '#ffffff' : '#f4f3f4'}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Text style={styles.settingLabel}>위치 공유</Text>
            <Text style={styles.settingDescription}>다른 사용자에게 위치 표시</Text>
          </View>
          <Switch
            value={settings.locationSharing}
            onValueChange={() => handleSettingToggle('locationSharing')}
            trackColor={{ false: '#e2e8f0', true: '#667eea' }}
            thumbColor={settings.locationSharing ? '#ffffff' : '#f4f3f4'}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Text style={styles.settingLabel}>자동 매칭</Text>
            <Text style={styles.settingDescription}>조건에 맞는 메이트 자동 추천</Text>
          </View>
          <Switch
            value={settings.autoMatching}
            onValueChange={() => handleSettingToggle('autoMatching')}
            trackColor={{ false: '#e2e8f0', true: '#667eea' }}
            thumbColor={settings.autoMatching ? '#ffffff' : '#f4f3f4'}
          />
        </View>

        <View style={styles.settingItem}>
          <View style={styles.settingInfo}>
            <Text style={styles.settingLabel}>온라인 상태 표시</Text>
            <Text style={styles.settingDescription}>다른 사용자에게 활동 상태 표시</Text>
          </View>
          <Switch
            value={settings.showOnlineStatus}
            onValueChange={() => handleSettingToggle('showOnlineStatus')}
            trackColor={{ false: '#e2e8f0', true: '#667eea' }}
            thumbColor={settings.showOnlineStatus ? '#ffffff' : '#f4f3f4'}
          />
        </View>
      </View>

      {/* 계정 관리 */}
      <View style={styles.settingsSection}>
        <Text style={styles.sectionTitle}>⚙️ 계정 관리</Text>
        
        <TouchableOpacity style={styles.menuItem}>
          <Icon name="vpn-key" size={20} color="#667eea" />
          <Text style={styles.menuItemText}>비밀번호 변경</Text>
          <Icon name="chevron-right" size={20} color="#999" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.menuItem}>
          <Icon name="email" size={20} color="#667eea" />
          <Text style={styles.menuItemText}>이메일 변경</Text>
          <Icon name="chevron-right" size={20} color="#999" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.menuItem}>
          <Icon name="delete" size={20} color="#e74c3c" />
          <Text style={[styles.menuItemText, { color: '#e74c3c' }]}>계정 삭제</Text>
          <Icon name="chevron-right" size={20} color="#999" />
        </TouchableOpacity>
      </View>

      {/* 앱 정보 */}
      <View style={styles.settingsSection}>
        <Text style={styles.sectionTitle}>ℹ️ 앱 정보</Text>
        
        <TouchableOpacity style={styles.menuItem}>
          <Icon name="info" size={20} color="#667eea" />
          <Text style={styles.menuItemText}>버전 정보</Text>
          <Text style={styles.versionText}>v1.0.0</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.menuItem}>
          <Icon name="help" size={20} color="#667eea" />
          <Text style={styles.menuItemText}>도움말</Text>
          <Icon name="chevron-right" size={20} color="#999" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.menuItem}>
          <Icon name="description" size={20} color="#667eea" />
          <Text style={styles.menuItemText}>개인정보처리방침</Text>
          <Icon name="chevron-right" size={20} color="#999" />
        </TouchableOpacity>
      </View>

      {/* 로그아웃 버튼 */}
      <TouchableOpacity onPress={handleLogout} style={styles.logoutButton}>
        <Text style={styles.logoutButtonText}>로그아웃</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <LinearGradient colors={['#667eea', '#764ba2']} style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Text style={styles.title}>👤 프로필</Text>
      </View>

      {/* 탭 네비게이션 */}
      <View style={styles.tabNavigation}>
        {[
          { key: 'profile', label: '프로필', icon: 'person' },
          { key: 'stats', label: '통계', icon: 'bar-chart' },
          { key: 'settings', label: '설정', icon: 'settings' },
        ].map(tab => (
          <TouchableOpacity
            key={tab.key}
            onPress={() => setActiveTab(tab.key as any)}
            style={[
              styles.tabButton,
              activeTab === tab.key && styles.tabButtonActive,
            ]}
          >
            <Icon
              name={tab.icon}
              size={18}
              color={activeTab === tab.key ? '#667eea' : 'rgba(255, 255, 255, 0.7)'}
            />
            <Text
              style={[
                styles.tabButtonText,
                activeTab === tab.key && styles.tabButtonTextActive,
              ]}
            >
              {tab.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* 탭 콘텐츠 */}
      <View style={styles.contentContainer}>
        <ScrollView showsVerticalScrollIndicator={false} style={styles.scrollView}>
          {activeTab === 'profile' && renderProfileTab()}
          {activeTab === 'stats' && renderStatsTab()}
          {activeTab === 'settings' && renderSettingsTab()}
          
          <View style={styles.bottomPadding} />
        </ScrollView>
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
  },
  tabNavigation: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  tabButton: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 20,
    marginHorizontal: 4,
  },
  tabButtonActive: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
  },
  tabButtonText: {
    color: 'rgba(255, 255, 255, 0.7)',
    fontSize: 14,
    fontWeight: '600',
    marginLeft: 6,
  },
  tabButtonTextActive: {
    color: '#667eea',
  },
  contentContainer: {
    flex: 1,
    backgroundColor: '#f8fafc',
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
  },
  scrollView: {
    flex: 1,
  },
  tabContent: {
    padding: 20,
  },
  // 프로필 탭 스타일
  profileInfo: {
    alignItems: 'center',
    marginBottom: 30,
  },
  avatarContainer: {
    position: 'relative',
    marginBottom: 15,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  onlineIndicator: {
    position: 'absolute',
    bottom: 5,
    right: 5,
    width: 16,
    height: 16,
    backgroundColor: '#2ecc71',
    borderRadius: 8,
    borderWidth: 2,
    borderColor: '#ffffff',
  },
  profileName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2d3748',
    marginBottom: 4,
  },
  profileUsername: {
    fontSize: 16,
    color: '#667eea',
    marginBottom: 4,
  },
  profileLocation: {
    fontSize: 14,
    color: '#4a5568',
    marginBottom: 20,
  },
  editButton: {
    borderRadius: 20,
    overflow: 'hidden',
  },
  editButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 10,
  },
  editButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
    marginLeft: 6,
  },
  bioSection: {
    marginBottom: 30,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2d3748',
    marginBottom: 15,
  },
  bioText: {
    fontSize: 16,
    color: '#4a5568',
    lineHeight: 24,
  },
  travelStyleSection: {
    marginBottom: 30,
  },
  tagContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  styleTag: {
    backgroundColor: 'rgba(102, 126, 234, 0.1)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
    marginRight: 8,
    marginBottom: 8,
  },
  styleTagText: {
    color: '#667eea',
    fontSize: 14,
    fontWeight: '500',
  },
  languageSection: {
    marginBottom: 30,
  },
  languageTag: {
    backgroundColor: 'rgba(254, 202, 87, 0.1)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
    marginRight: 8,
    marginBottom: 8,
  },
  languageTagText: {
    color: '#f39c12',
    fontSize: 14,
    fontWeight: '500',
  },
  badgeSection: {
    marginBottom: 30,
  },
  badgeContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  badge: {
    width: 50,
    height: 50,
    backgroundColor: '#ffffff',
    borderRadius: 25,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
    marginBottom: 10,
    elevation: 2,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  badgeEmoji: {
    fontSize: 24,
  },
  // 통계 탭 스타일
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 30,
  },
  statCard: {
    width: '48%',
    borderRadius: 15,
    overflow: 'hidden',
    marginBottom: 15,
    elevation: 3,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  statCardGradient: {
    alignItems: 'center',
    padding: 20,
  },
  statNumber: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    marginTop: 10,
    marginBottom: 5,
  },
  statLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.9)',
  },
  joinInfoSection: {
    marginBottom: 30,
  },
  joinInfoText: {
    fontSize: 16,
    color: '#4a5568',
    lineHeight: 24,
  },
  activitySection: {
    marginBottom: 30,
  },
  activityList: {},
  activityItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  activityText: {
    flex: 1,
    fontSize: 14,
    color: '#4a5568',
    marginLeft: 15,
  },
  activityTime: {
    fontSize: 12,
    color: '#999',
  },
  // 설정 탭 스타일
  settingsSection: {
    backgroundColor: '#ffffff',
    borderRadius: 15,
    padding: 20,
    marginBottom: 20,
    elevation: 2,
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f1f5f9',
  },
  settingInfo: {
    flex: 1,
  },
  settingLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2d3748',
    marginBottom: 4,
  },
  settingDescription: {
    fontSize: 14,
    color: '#4a5568',
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f1f5f9',
  },
  menuItemText: {
    flex: 1,
    fontSize: 16,
    color: '#2d3748',
    marginLeft: 15,
  },
  versionText: {
    fontSize: 14,
    color: '#999',
  },
  logoutButton: {
    backgroundColor: '#e74c3c',
    borderRadius: 12,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 20,
  },
  logoutButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  bottomPadding: {
    height: 30,
  },
});

export default ProfileScreen;