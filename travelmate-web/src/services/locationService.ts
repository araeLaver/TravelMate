export interface Location {
  latitude: number;
  longitude: number;
  address?: string;
}

export interface TravelMate {
  id: string;
  name: string;
  age: number;
  gender: 'male' | 'female' | 'other';
  location: Location;
  distance: number;
  mood: string;
  travelStyle: string;
  interests: string[];
  languages: string[];
  bio: string;
  isOnline: boolean;
  lastSeen: Date;
  matchScore: number;
  profileImage?: string;
}

class LocationService {
  private currentLocation: Location | null = null;
  private watchId: number | null = null;

  // 현재 위치 가져오기
  async getCurrentLocation(): Promise<Location> {
    console.log('🔍 위치 서비스 시작...');
    
    if (!navigator.geolocation) {
      console.log('❌ 이 브라우저는 위치 서비스를 지원하지 않습니다.');
      const defaultLocation: Location = {
        latitude: 37.5665,
        longitude: 126.9780,
        address: '서울특별시 중구 (브라우저 미지원)'
      };
      this.currentLocation = defaultLocation;
      return defaultLocation;
    }

    return new Promise((resolve) => {
      console.log('📍 브라우저에서 위치 정보를 요청합니다...');
      
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          console.log('✅ 실제 위치 정보 획득 성공!', {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
            accuracy: position.coords.accuracy
          });
          
          const location: Location = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          };
          
          // 주소 변환 시도
          try {
            const address = await this.getAddressFromCoords(location.latitude, location.longitude);
            location.address = address;
          } catch (error) {
            console.warn('주소 변환 실패:', error);
            location.address = `위도 ${location.latitude.toFixed(4)}, 경도 ${location.longitude.toFixed(4)}`;
          }

          this.currentLocation = location;
          resolve(location);
        },
        (error) => {
          console.log('⚠️ 실제 위치 접근 실패 - 기본 위치 사용');
          console.log('오류 상세:', error.message);
          
          // 위치를 가져올 수 없는 경우 서울 시청 기본값 사용
          const defaultLocation: Location = {
            latitude: 37.5665,
            longitude: 126.9780,
            address: '서울특별시 중구 (기본 위치)'
          };
          this.currentLocation = defaultLocation;
          resolve(defaultLocation);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000 // 5분
        }
      );
    });
  }

  // 주소를 좌표로 변환 (간단한 예시)
  private async getAddressFromCoords(lat: number, lng: number): Promise<string> {
    // 실제로는 Google Maps API나 카카오맵 API를 사용해야 합니다
    // 여기서는 임시로 대략적인 주소를 반환합니다
    const districts = [
      '강남구', '서초구', '송파구', '강동구', '마포구', 
      '용산구', '중구', '종로구', '성북구', '동대문구'
    ];
    
    const randomDistrict = districts[Math.floor(Math.random() * districts.length)];
    return `서울특별시 ${randomDistrict}`;
  }

  // 두 좌표 간 거리 계산 (km)
  calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371; // 지구의 반지름 (km)
    const dLat = this.deg2rad(lat2 - lat1);
    const dLon = this.deg2rad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const d = R * c;
    return Math.round(d * 10) / 10; // 소수점 1자리
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  // 근처 여행 메이트 찾기
  async findNearbyTravelMates(radius: number = 5): Promise<TravelMate[]> {
    try {
      console.log('Finding nearby travel mates with radius:', radius);
      const currentLoc = this.currentLocation || await this.getCurrentLocation();
      console.log('Current location:', currentLoc);
      
      // 실제 앱에서는 서버 API를 호출하지만, 여기서는 시뮬레이션
      const mates = this.generateMockTravelMates(currentLoc, radius);
      console.log('Generated mates:', mates);
      return mates;
    } catch (error) {
      console.error('Error in findNearbyTravelMates:', error);
      // 에러가 발생해도 기본 위치로 메이트를 생성
      const defaultLocation: Location = {
        latitude: 37.5665,
        longitude: 126.9780,
        address: '서울특별시 중구 태평로1가'
      };
      return this.generateMockTravelMates(defaultLocation, radius);
    }
  }

  private generateMockTravelMates(currentLoc: Location, radius: number): TravelMate[] {
    const names = [
      '김도현', '이서연', '박민준', '최지은', '정우진', '한소영', '송태호', '차유나',
      '강민수', '윤채원', '임성훈', '장하늘', '오현지', '신재현', '류소담', '홍준혁',
      '김나라', '이바다', '박하늘', '최별님', '정달님', '한가은', '송유진', '차민아',
      '백여행가', '김탐험가', '이모험가', '박세계인', '최글로벌', '정국제인', '한유목민', '송자유인'
    ];

    const moods = [
      '🌟 여행 중', '🍜 맛집 탐방', '🏔️ 산 좋아', '📸 인생샷 찍기', '☕ 카페 투어', 
      '🎨 문화 체험', '🏖️ 휴양지 선호', '🎭 공연 관람', '🛍️ 쇼핑 러버', '🌃 야경 덕후',
      '🚶‍♀️ 도보 탐험', '🎵 음악 투어', '🍷 와이너리 투어', '🏛️ 역사 탐방', '🌸 꽃 구경',
      '⛩️ 사찰 순례', '🎪 축제 참가', '🏄‍♂️ 액티비티', '🧘‍♀️ 명상 여행', '📚 도서관 투어'
    ];

    const travelStyles = [
      '배낭여행', '럭셔리 여행', '문화탐방', '모험가', '미식가',
      '사진가', '역사덕후', '자연러버', '도시탐험', '힐링여행'
    ];

    const interests = [
      '사진촬영', '음식탐방', '역사문화', '자연관광', '쇼핑',
      '공연관람', '스포츠', '야경감상', '카페투어', '박물관'
    ];

    const languages = [
      ['한국어', '영어'], ['한국어', '중국어'], ['한국어', '일본어'],
      ['한국어', '영어', '중국어'], ['한국어', '스페인어'], ['한국어', '프랑스어']
    ];

    const bios = [
      '세계 곳곳을 탐험하며 새로운 문화를 경험하고 싶어요! 🌍',
      '맛있는 음식과 아름다운 풍경을 함께 즐길 여행 친구를 찾아요. 🍽️✨',
      '사진 찍기 좋아하고 인생샷 남기는 걸 좋아해요. 📸',
      '여행을 통해 새로운 사람들과 인연을 만들고 싶어요. 🤝',
      '혼자 여행보다는 함께하는 여행이 더 즐거운 것 같아요! 👫',
      '현지인처럼 여행하며 진짜 문화를 체험해보고 싶어요. 🏛️',
      '자연과 함께하는 힐링 여행을 좋아해요. 🌿',
      '역사와 예술에 관심이 많아서 박물관 투어를 즐겨요. 🎨',
      '맛집 탐방이 여행의 50% 이상을 차지한다고 생각해요! 🍜',
      '새벽 일출부터 밤 야경까지 모든 순간을 담고 싶어요. 🌅🌃',
      '배낭 하나로 떠나는 자유로운 여행을 꿈꿔요. 🎒',
      '각 나라의 전통 축제와 문화를 직접 체험하고 싶어요. 🎪',
      '느린 여행, 깊은 여행을 추구합니다. ☕',
      '모험과 스릴을 즐기는 액티비티 여행러예요! 🏄‍♂️',
      '여행지의 로컬 마켓과 골목길 탐험을 좋아해요. 🛒',
      '다양한 언어와 문화 교류에 관심이 많아요. 🗣️'
    ];

    const mockMates: TravelMate[] = [];
    const count = Math.floor(Math.random() * 8) + 3; // 3-10명

    for (let i = 0; i < count; i++) {
      // 반경 내 랜덤 위치 생성
      const angle = Math.random() * 2 * Math.PI;
      const distance = Math.random() * radius;
      const deltaLat = (distance * Math.cos(angle)) / 111; // 1도 ≈ 111km
      const deltaLng = (distance * Math.sin(angle)) / (111 * Math.cos(currentLoc.latitude * Math.PI / 180));

      const mateLoc: Location = {
        latitude: currentLoc.latitude + deltaLat,
        longitude: currentLoc.longitude + deltaLng,
      };

      const actualDistance = this.calculateDistance(
        currentLoc.latitude, currentLoc.longitude,
        mateLoc.latitude, mateLoc.longitude
      );

      mockMates.push({
        id: `mate_${i + 1}_${Date.now()}`,
        name: names[Math.floor(Math.random() * names.length)],
        age: Math.floor(Math.random() * 25) + 20, // 20-44세
        gender: Math.random() > 0.5 ? 'female' : 'male',
        location: mateLoc,
        distance: actualDistance,
        mood: moods[Math.floor(Math.random() * moods.length)],
        travelStyle: travelStyles[Math.floor(Math.random() * travelStyles.length)],
        interests: this.getRandomItems(interests, 2, 4),
        languages: languages[Math.floor(Math.random() * languages.length)],
        bio: bios[Math.floor(Math.random() * bios.length)],
        isOnline: Math.random() > 0.3, // 70% 온라인
        lastSeen: new Date(Date.now() - Math.random() * 3600000), // 최근 1시간 내
        matchScore: Math.floor(Math.random() * 30) + 70, // 70-99%
        profileImage: `https://picsum.photos/150/150?random=${i + 1}`
      });
    }

    return mockMates.sort((a, b) => a.distance - b.distance);
  }

  private getRandomItems<T>(array: T[], min: number, max: number): T[] {
    const count = Math.floor(Math.random() * (max - min + 1)) + min;
    const shuffled = [...array].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  }

  // 위치 변화 감지 시작
  startWatching(callback: (location: Location) => void): void {
    if (!navigator.geolocation) return;

    this.watchId = navigator.geolocation.watchPosition(
      async (position) => {
        const location: Location = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        };
        
        try {
          const address = await this.getAddressFromCoords(location.latitude, location.longitude);
          location.address = address;
        } catch (error) {
          console.warn('Failed to get address:', error);
        }

        this.currentLocation = location;
        callback(location);
      },
      (error) => console.warn('Location watch error:', error),
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 600000 // 10분
      }
    );
  }

  // 위치 감지 중지
  stopWatching(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }

  getCurrentLocationSync(): Location | null {
    return this.currentLocation;
  }
}

export const locationService = new LocationService();