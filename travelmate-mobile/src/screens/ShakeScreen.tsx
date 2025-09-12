import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Animated,
  Vibration,
  Alert,
  TouchableOpacity,
  Dimensions,
  PermissionsAndroid,
  Platform,
} from 'react-native';
import { accelerometer, SensorTypes, setUpdateIntervalForType } from 'react-native-sensors';
import Geolocation from '@react-native-community/geolocation';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';
import Sound from 'react-native-sound';

const { width, height } = Dimensions.get('window');

interface NearbyUser {
  id: number;
  username: string;
  name: string;
  distance: number;
  mood: string;
  travelStyle: string;
  currentLocation: string;
  isOnline: boolean;
  matchScore: number;
}

const ShakeScreen: React.FC = () => {
  const [isShakeActive, setIsShakeActive] = useState(false);
  const [shakeIntensity, setShakeIntensity] = useState(0);
  const [nearbyUsers, setNearbyUsers] = useState<NearbyUser[]>([]);
  const [isDiscovering, setIsDiscovering] = useState(false);
  const [location, setLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  
  // 애니메이션 refs
  const radarAnimation = useRef(new Animated.Value(0)).current;
  const pulseAnimation = useRef(new Animated.Value(1)).current;
  const shakeButtonScale = useRef(new Animated.Value(1)).current;
  
  // 사운드 효과
  const discoverySound = useRef<Sound | null>(null);

  useEffect(() => {
    // 센서 업데이트 간격 설정
    setUpdateIntervalForType(SensorTypes.accelerometer, 100);
    
    // 사운드 초기화
    Sound.setCategory('Playback');
    discoverySound.current = new Sound('discovery_sound.mp3', Sound.MAIN_BUNDLE, (error) => {
      if (error) {
        console.log('사운드 로드 실패:', error);
      }
    });

    // 위치 권한 요청 및 현재 위치 가져오기
    requestLocationPermission();

    return () => {
      if (discoverySound.current) {
        discoverySound.current.release();
      }
    };
  }, []);

  // 위치 권한 요청
  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            title: 'TravelMate 위치 권한',
            message: '주변 여행메이트를 찾기 위해 위치 정보가 필요합니다.',
            buttonNeutral: '나중에',
            buttonNegative: '거부',
            buttonPositive: '허용',
          },
        );
        
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          getCurrentLocation();
        }
      } catch (err) {
        console.warn(err);
      }
    } else {
      getCurrentLocation();
    }
  };

  // 현재 위치 가져오기
  const getCurrentLocation = () => {
    Geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      (error) => {
        console.log('위치 가져오기 실패:', error.message);
        Alert.alert('위치 오류', '위치 정보를 가져올 수 없습니다.');
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 },
    );
  };

  // 가속도계 구독 시작
  const startShakeDetection = () => {
    setIsShakeActive(true);
    
    const subscription = accelerometer.subscribe(({ x, y, z }) => {
      const intensity = Math.sqrt(x * x + y * y + z * z);
      setShakeIntensity(intensity);
      
      // 흔들기 감지 (임계값: 15.0)
      if (intensity > 15.0 && !isDiscovering) {
        handleShakeDetected(intensity);
      }
    });

    // 10초 후 자동 중단
    setTimeout(() => {
      subscription.unsubscribe();
      setIsShakeActive(false);
      if (!isDiscovering) {
        Alert.alert('시간 초과', '흔들기를 감지하지 못했습니다. 더 강하게 흔들어보세요!');
      }
    }, 10000);
  };

  // 흔들기 감지 시 처리
  const handleShakeDetected = (intensity: number) => {
    setIsDiscovering(true);
    setIsShakeActive(false);
    
    // 햅틱 피드백
    Vibration.vibrate([100, 50, 100, 50, 200]);
    
    // 사운드 효과
    if (discoverySound.current) {
      discoverySound.current.play();
    }
    
    // 버튼 애니메이션
    Animated.sequence([
      Animated.timing(shakeButtonScale.current, {
        toValue: 1.2,
        duration: 150,
        useNativeDriver: true,
      }),
      Animated.timing(shakeButtonScale.current, {
        toValue: 1,
        duration: 150,
        useNativeDriver: true,
      }),
    ]).start();

    // 레이더 애니메이션 시작
    startRadarAnimation();
    
    // 백엔드 API 호출
    discoverNearbyUsers(intensity);
  };

  // 레이더 애니메이션
  const startRadarAnimation = () => {
    // 회전 애니메이션
    Animated.loop(
      Animated.timing(radarAnimation.current, {
        toValue: 1,
        duration: 2000,
        useNativeDriver: true,
      }),
    ).start();

    // 펄스 애니메이션
    Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnimation.current, {
          toValue: 1.3,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnimation.current, {
          toValue: 1,
          duration: 1000,
          useNativeDriver: true,
        }),
      ]),
    ).start();
  };

  // 주변 사용자 발견 API
  const discoverNearbyUsers = async (intensity: number) => {
    if (!location) {
      Alert.alert('위치 오류', '위치 정보가 없습니다.');
      setIsDiscovering(false);
      return;
    }

    try {
      // 실제 백엔드 API 호출 (현재는 목업 데이터)
      const mockUsers: NearbyUser[] = [
        {
          id: 1,
          username: 'adventure_kim',
          name: '김모험가',
          distance: 0.2,
          mood: '🏔️ 산 좋아',
          travelStyle: '모험가',
          currentLocation: '명동역 2번 출구',
          isOnline: true,
          matchScore: 94
        },
        {
          id: 2,
          username: 'foodie_park',
          name: '박미식가',
          distance: 0.5,
          mood: '🍜 맛집 탐방',
          travelStyle: '미식가',
          currentLocation: '홍대입구역',
          isOnline: true,
          matchScore: 89
        },
        {
          id: 3,
          username: 'photo_lee',
          name: '이포토그래퍼',
          distance: 0.8,
          mood: '📸 인생샷 찍기',
          travelStyle: '사진가',
          currentLocation: '성수동 카페거리',
          isOnline: false,
          matchScore: 82
        }
      ];

      // 2.5초 후 결과 표시 (실제 API 호출 시뮬레이션)
      setTimeout(() => {
        setNearbyUsers(mockUsers);
        setIsDiscovering(false);
        radarAnimation.current.stopAnimation();
        pulseAnimation.current.stopAnimation();
        
        // 성공 햅틱
        Vibration.vibrate(200);
        
        Alert.alert(
          '🎉 발견 완료!',
          `흔들기 강도 ${intensity.toFixed(1)}로 ${mockUsers.length}명의 여행메이트를 발견했습니다!`,
          [{ text: '확인', style: 'default' }]
        );
      }, 2500);
      
    } catch (error) {
      console.error('Discovery failed:', error);
      setIsDiscovering(false);
      Alert.alert('오류', '메이트 발견에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const resetDiscovery = () => {
    setNearbyUsers([]);
    setShakeIntensity(0);
    radarAnimation.current.setValue(0);
    pulseAnimation.current.setValue(1);
  };

  const radarRotation = radarAnimation.current.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '360deg'],
  });

  return (
    <LinearGradient colors={['#667eea', '#764ba2']} style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>📱 폰 흔들기 메이트 발견</Text>
        <Text style={styles.headerSubtitle}>
          폰을 흔들어서 주변의 여행메이트를 찾아보세요!
        </Text>
      </View>

      {/* 레이더 화면 */}
      <View style={styles.radarContainer}>
        <View style={styles.radarCircle}>
          {/* 동심원 */}
          <View style={styles.innerCircle1} />
          <View style={styles.innerCircle2} />
          
          {/* 중심점 */}
          <Animated.View
            style={[
              styles.centerDot,
              { transform: [{ scale: pulseAnimation.current }] }
            ]}
          />
          
          {/* 레이더 스위프 */}
          {isDiscovering && (
            <Animated.View
              style={[
                styles.radarSweep,
                { transform: [{ rotate: radarRotation }] }
              ]}
            />
          )}
          
          {/* 발견된 사용자 점들 */}
          {nearbyUsers.map((user, index) => (
            <Animated.View
              key={user.id}
              style={[
                styles.userDot,
                {
                  top: 50 + user.distance * 40 + Math.sin(index) * 30,
                  left: 50 + user.distance * 30 + Math.cos(index) * 40,
                }
              ]}
            />
          ))}
        </View>
      </View>

      {/* 상태 표시 */}
      <View style={styles.statusContainer}>
        {isShakeActive && (
          <Text style={styles.statusText}>
            📳 흔들기 감지 중... (강도: {shakeIntensity.toFixed(1)})
          </Text>
        )}
        {isDiscovering && (
          <Text style={styles.statusText}>
            🔍 주변 메이트 탐색 중...
          </Text>
        )}
        {nearbyUsers.length > 0 && (
          <Text style={styles.statusText}>
            🎉 {nearbyUsers.length}명의 메이트를 발견했습니다!
          </Text>
        )}
      </View>

      {/* 흔들기 버튼 */}
      <Animated.View
        style={[
          styles.shakeButton,
          { transform: [{ scale: shakeButtonScale.current }] }
        ]}
      >
        <TouchableOpacity
          onPress={isShakeActive || isDiscovering ? undefined : startShakeDetection}
          style={[
            styles.shakeButtonInner,
            { opacity: isShakeActive || isDiscovering ? 0.6 : 1 }
          ]}
          disabled={isShakeActive || isDiscovering}
        >
          <Icon name="vibration" size={40} color="#667eea" />
          <Text style={styles.shakeButtonText}>
            {isShakeActive ? '흔들어주세요!' : isDiscovering ? '탐색 중...' : '시작하기'}
          </Text>
        </TouchableOpacity>
      </Animated.View>

      {/* 발견된 사용자 목록 */}
      {nearbyUsers.length > 0 && (
        <View style={styles.usersList}>
          <TouchableOpacity onPress={resetDiscovery} style={styles.resetButton}>
            <Text style={styles.resetButtonText}>다시 찾기</Text>
          </TouchableOpacity>
        </View>
      )}
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 40,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 8,
  },
  headerSubtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    textAlign: 'center',
  },
  radarContainer: {
    width: 280,
    height: 280,
    marginBottom: 40,
  },
  radarCircle: {
    width: '100%',
    height: '100%',
    borderRadius: 140,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    position: 'relative',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  innerCircle1: {
    position: 'absolute',
    width: '70%',
    height: '70%',
    borderRadius: 100,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    top: '15%',
    left: '15%',
  },
  innerCircle2: {
    position: 'absolute',
    width: '40%',
    height: '40%',
    borderRadius: 60,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.4)',
    top: '30%',
    left: '30%',
  },
  centerDot: {
    position: 'absolute',
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#ffffff',
    top: '50%',
    left: '50%',
    marginTop: -6,
    marginLeft: -6,
    shadowColor: '#ffffff',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.8,
    shadowRadius: 10,
    elevation: 8,
  },
  radarSweep: {
    position: 'absolute',
    width: '50%',
    height: 2,
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    top: '50%',
    left: '50%',
    transformOrigin: 'left center',
    marginTop: -1,
  },
  userDot: {
    position: 'absolute',
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#feca57',
    shadowColor: '#feca57',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 1,
    shadowRadius: 8,
    elevation: 5,
  },
  statusContainer: {
    alignItems: 'center',
    marginBottom: 30,
    minHeight: 30,
  },
  statusText: {
    fontSize: 18,
    color: '#ffffff',
    fontWeight: '600',
    textAlign: 'center',
  },
  shakeButton: {
    marginBottom: 30,
  },
  shakeButtonInner: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 80,
    width: 160,
    height: 160,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 12,
  },
  shakeButtonText: {
    marginTop: 8,
    fontSize: 16,
    fontWeight: 'bold',
    color: '#667eea',
    textAlign: 'center',
  },
  usersList: {
    alignItems: 'center',
  },
  resetButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
  },
  resetButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default ShakeScreen;