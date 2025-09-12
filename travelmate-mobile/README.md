# 📱 TravelMate Mobile App

React Native 기반 TravelMate 모바일 애플리케이션

## 🚀 주요 기능

### 📳 **진짜 폰 흔들기 매칭**
- **가속도계 센서** 활용한 실제 흔들기 감지
- **흔들기 강도**에 따른 검색 반경 조절 (1km~10km)
- **실시간 레이더 애니메이션** 및 햅틱 피드백
- **사운드 효과**와 시각적 피드백

### 🎯 **여행 무드 매칭**
- **6가지 여행 테마**: 모험, 미식, 사진, 문화, 카페, 야경
- **실시간 매칭 점수** (78-92%) 표시
- **동적 사용자 통계** 업데이트

### 💬 **실시간 채팅**
- **개인/그룹 채팅** 지원
- **읽음 상태**, **온라인 표시**
- **자동 응답** 시뮬레이션
- **검색 기능** 내장

### 🗺️ **여행 그룹 관리**
- **필터링**: 전체/참여중/참여가능
- **태그 기반** 그룹 검색
- **난이도별** 그룹 분류
- **실시간 멤버** 현황

### 🎮 **여행 게임 센터**
- **4가지 미니게임**: 랜드마크 맞추기, 포토챌린지, 룰렛매칭, 퀴즈
- **일일 챌린지** 시스템
- **포인트/레벨** 시스템
- **리더보드** 랭킹

### 👤 **프로필 관리**
- **3단계 탭**: 프로필/통계/설정
- **상세 통계**: 포인트, 레벨, 여행횟수, 평점
- **뱃지 시스템**, **여행 스타일 태그**
- **실시간 활동** 기록

## 🛠 기술 스택

### Core
- **React Native 0.72.4**
- **TypeScript**
- **React Navigation 6.x**

### 센서 & 하드웨어
- **react-native-sensors**: 가속도계
- **react-native-shake**: 흔들기 감지 
- **@react-native-community/geolocation**: GPS
- **react-native-permissions**: 권한 관리

### UI/UX
- **react-native-linear-gradient**: 그라디언트
- **react-native-vector-icons**: 아이콘
- **react-native-reanimated**: 애니메이션
- **react-native-animatable**: 효과

### 통신
- **axios**: HTTP 클라이언트
- **socket.io-client**: 실시간 통신
- **react-native-websocket**: WebSocket

### 알림 & 사운드
- **react-native-push-notification**: 푸시 알림
- **react-native-sound**: 사운드 효과

## 📦 설치 및 실행

### 1. 의존성 설치
```bash
cd travelmate-mobile
npm install
```

### 2. iOS 설정 (iOS만)
```bash
cd ios && pod install && cd ..
```

### 3. 실행
```bash
# Android
npm run android

# iOS  
npm run ios

# Metro 서버 시작
npm start
```

## 🎯 핵심 화면

### 🔍 **발견 화면** (DiscoverScreen)
- 메인 대시보드
- 실시간 통계 (주변 여행자 12명, 활동중 8명)
- 폰 흔들기 메인 버튼
- 여행 무드 선택 슬라이더
- 빠른 액션 그리드

### 📳 **흔들기 화면** (ShakeScreen) 
- 실제 가속도계 활용
- 레이더 애니메이션 (회전/펄스)
- 실시간 강도 표시
- 발견된 사용자 도트 표시
- 햅틱/사운드 피드백

### 💬 **채팅 화면** (ChatScreen)
- 채팅방 목록 + 메시지 화면
- 읽지않음 카운트
- 온라인 상태 표시
- 그룹/개인 채팅 구분

### 🗺️ **그룹 화면** (GroupsScreen)
- 여행 그룹 카드 리스트
- 필터 탭 (전체/참여중/참여가능)
- 태그 필터링
- 가입/탈퇴 기능

### 🎮 **게임 화면** (GamesScreen)
- 사용자 통계 헤더
- 4가지 게임 카드
- 일일 챌린지 진행률
- 리더보드

### 👤 **프로필 화면** (ProfileScreen)
- 프로필/통계/설정 탭
- 아바타, 뱃지, 여행스타일
- 통계 카드 그리드
- 설정 토글 스위치

## 🔧 백엔드 API 연동

모든 화면은 기존 백엔드 API와 연동 가능:

- `POST /api/users/shake` - 폰 흔들기 매칭
- `GET /api/users/nearby` - 주변 사용자
- `POST /api/chat/rooms` - 채팅방 생성  
- `GET /api/travel-groups` - 여행 그룹 목록
- 기타 40+ 엔드포인트

## 📱 플랫폼 지원

- ✅ **Android** (API 21+)
- ✅ **iOS** (iOS 11.0+)
- 📳 **진짜 가속도계** 지원
- 🔔 **푸시 알림** 완전 지원
- 📍 **위치 서비스** 권한 관리

## 🎨 디자인 시스템

- **브랜드 컬러**: `#667eea` → `#764ba2`
- **그라디언트** 활용한 모던 UI
- **카드 기반** 레이아웃
- **애니메이션** 풍부한 인터랙션
- **반응형** 디자인

## 🚀 빌드 및 배포

```bash
# Android APK
npm run android:build

# iOS Archive  
npm run ios:build
```

---

**이제 웹과 동일한 기능을 가진 완전한 네이티브 모바일 앱이 준비되었습니다!** 

웹에서는 클릭으로 대체했던 기능들을, 모바일에서는 **실제 가속도계로 진짜 폰 흔들기**를 구현했습니다. 🎉