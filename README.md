# TravelMate - 여행 동반자 매칭 플랫폼

## 프로젝트 개요
TravelMate는 혼자 여행하는 사람들을 위한 동반자 매칭 플랫폼입니다. 
사용자들은 여행 계획을 공유하고, 비슷한 관심사와 여행 스타일을 가진 동반자를 찾을 수 있습니다.

## 프로젝트 구조
```
TravelMate/
├── mobile/          # React Native 모바일 앱
├── backend/         # Spring Boot 백엔드 서버
├── web/            # React 웹 애플리케이션
├── docs/           # 프로젝트 문서
└── README.md       # 이 파일
```

## 각 모듈 설명

### 📱 Mobile (`/mobile`)
- **기술스택**: React Native, TypeScript
- **설명**: iOS/Android용 모바일 애플리케이션
- **주요기능**: 
  - 사용자 인증 및 로그인
  - 흔들기 기능을 통한 동반자 매칭
  - 실시간 채팅 및 알림

### 🖥️ Backend (`/backend`)  
- **기술스택**: Spring Boot, Java
- **설명**: RESTful API 서버
- **주요기능**:
  - 사용자 관리 및 인증
  - 여행 계획 관리
  - 매칭 알고리즘
  - 실시간 통신 지원

### 🌐 Web (`/web`)
- **기술스택**: React, TypeScript
- **설명**: 웹 기반 관리자 및 사용자 포털
- **주요기능**:
  - 관리자 대시보드
  - 사용자 웹 인터페이스
  - 통계 및 분석

## 시작하기

### 전체 프로젝트 설정
1. 저장소 클론
```bash
git clone https://github.com/araeLaver/TravelMate.git
cd TravelMate
```

### 각 모듈별 설정
각 모듈(`mobile`, `backend`, `web`)의 README.md 파일을 참고하여 개별 설정을 진행하세요.

## 개발 환경
- **Node.js**: 18+ (mobile, web)
- **Java**: 17+ (backend)
- **Database**: PostgreSQL (추후 설정)

## 문서
- 프로젝트 계획서: `/docs/travel-mate-project-plan.md`
- API 문서: 추후 추가 예정
- 배포 가이드: 추후 추가 예정

## 기여하기
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 라이선스
This project is licensed under the MIT License.