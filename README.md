# 🌍 TravelMate

여행 동반자를 찾는 소셜 플랫폼

## ✨ 주요 기능

- **👥 사용자 관리**: 회원가입, 로그인, 프로필 관리, OAuth2 소셜 로그인
- **🗺️ 여행 그룹**: 여행 동반자 모집 및 그룹 관리
- **💬 실시간 채팅**: WebSocket 기반 실시간 메시징
- **📍 위치 기반 서비스**: 근처 여행지 및 동반자 추천
- **📱 모바일 호환**: 반응형 웹 디자인

## 🚀 빠른 시작

### 1. Docker로 실행 (권장)

```bash
# Windows
start.bat

# Linux/Mac
docker-compose up --build
```

### 2. 수동 실행

**백엔드 실행:**
```bash
cd travelmate-backend
mvn spring-boot:run
```

**프론트엔드 실행:**
```bash
cd travelmate-web
npm install
npm start
```

## 🌐 서비스 접속

- **웹 애플리케이션**: http://localhost
- **API 서버**: http://localhost:8080/api
- **H2 콘솔** (개발용): http://localhost:8080/h2-console

## 🛠 기술 스택

### 백엔드
- **프레임워크**: Spring Boot 3.2.0
- **언어**: Java 17
- **데이터베이스**: H2 (개발) / PostgreSQL (운영)
- **보안**: Spring Security + JWT
- **실시간 통신**: WebSocket
- **빌드 도구**: Maven

### 프론트엔드
- **프레임워크**: React 18 + TypeScript
- **스타일링**: CSS3 (반응형)
- **HTTP 클라이언트**: Axios
- **빌드 도구**: Create React App

### 인프라
- **컨테이너화**: Docker + Docker Compose
- **웹 서버**: Nginx
- **프로세스 관리**: Supervisor

## 📁 프로젝트 구조

```
TravelMate/
├── travelmate-backend/     # Spring Boot API 서버
├── travelmate-web/         # React 웹 애플리케이션
├── travelmate-shared/      # 공통 DTO 클래스
├── docker/                 # Docker 설정 파일
├── Dockerfile             # 통합 이미지 빌드
├── docker-compose.yml     # 서비스 오케스트레이션
├── start.bat             # Windows 실행 스크립트
└── stop.bat              # Windows 중지 스크립트
```

## ⚙️ 환경 설정

환경 변수는 `.env` 파일 또는 시스템 환경 변수로 설정:

```bash
# JWT 보안 키
JWT_SECRET=your-secret-key

# 데이터베이스 (PostgreSQL 사용 시)
DB_PASSWORD=your-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

## 🔧 개발 환경 설정

### 요구사항
- **Java**: 17 이상
- **Node.js**: 18 이상
- **Docker**: 최신 버전
- **Maven**: 3.8 이상

### IDE 설정
- **백엔드**: IntelliJ IDEA, Eclipse
- **프론트엔드**: VS Code, WebStorm

## 📊 모니터링 (선택사항)

운영 환경에서 모니터링 서비스 활성화:

```bash
docker-compose --profile production up
```

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001

## 🤝 기여하기

1. Fork 프로젝트
2. 기능 브랜치 생성 (`git checkout -b feature/새기능`)
3. 변경사항 커밋 (`git commit -am '새기능 추가'`)
4. 브랜치 푸시 (`git push origin feature/새기능`)
5. Pull Request 생성

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 📞 지원

문제가 발생하면 GitHub Issues를 통해 문의해주세요.