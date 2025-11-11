# 🚀 TravelMate 로컬 테스트 가이드

## 📋 사전 준비사항

### 필수 설치 항목
- [x] **Java 17** - `java -version` 확인
- [x] **Node.js 18+** - `node -v` 확인
- [x] **PostgreSQL 15** (선택: Docker로 실행 가능)
- [x] **Redis 7** (선택: Docker로 실행 가능)
- [x] **Elasticsearch 8** (선택: Docker로 실행 가능)

---

## 🎯 방법 1: Docker Compose로 전체 스택 실행 (권장)

### 1단계: 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```bash
cd C:\Develop\Down\TravelMate
notepad .env
```

`.env` 파일 내용:
```env
# Database
POSTGRES_PASSWORD=your_secure_password

# Redis
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_key_min_256_bits

# Grafana
GRAFANA_PASSWORD=admin_password
```

### 2단계: Docker Compose 실행

```bash
# 전체 스택 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 상태 확인
docker-compose ps
```

### 3단계: 서비스 접속

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin_password)
- **Elasticsearch**: http://localhost:9200

### 4단계: 헬스체크

```bash
# Backend 헬스체크
curl http://localhost:8080/actuator/health

# Elasticsearch 헬스체크
curl http://localhost:9200/_cluster/health
```

---

## 🛠️ 방법 2: 개별 실행 (개발 모드)

### 1단계: 데이터베이스 준비

#### PostgreSQL (Docker)
```bash
docker run -d \
  --name travelmate-postgres \
  -e POSTGRES_DB=travelmate \
  -e POSTGRES_USER=travelmate \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Redis (Docker)
```bash
docker run -d \
  --name travelmate-redis \
  -p 6379:6379 \
  redis:7-alpine redis-server --requirepass password
```

#### Elasticsearch (Docker)
```bash
docker run -d \
  --name travelmate-elasticsearch \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=false" \
  -p 9200:9200 \
  -p 9300:9300 \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### 2단계: Backend 실행

```bash
cd C:\Develop\Down\TravelMate\travelmate-backend

# application.yml 확인 (필요시 수정)
# src/main/resources/application.yml

# Gradle로 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/*.jar
```

**Backend 실행 확인:**
```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# API 테스트
curl http://localhost:8080/api/health
```

### 3단계: Frontend 실행

```bash
cd C:\Develop\Down\TravelMate\travelmate-web

# 의존성 설치
npm install

# 개발 서버 시작
npm start
```

**Frontend 접속:** http://localhost:3000

---

## 🧪 기능 테스트 가이드

### 1. API 테스트

#### 사용자 등록
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "nickname": "테스터",
    "fullName": "홍길동"
  }'
```

#### 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**응답에서 JWT 토큰 저장:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

#### 그룹 목록 조회 (인증 필요)
```bash
curl http://localhost:8080/api/groups \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 추천 그룹 조회
```bash
curl http://localhost:8080/api/recommendations/groups?limit=10 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 검색 테스트
```bash
curl "http://localhost:8080/api/search?q=제주도" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 알림 조회
```bash
curl http://localhost:8080/api/notifications \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. WebSocket 테스트

브라우저 콘솔에서 테스트:

```javascript
// SockJS 연결
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);

  // 채팅방 구독
  stompClient.subscribe('/topic/chat/1', function(message) {
    console.log('Received:', JSON.parse(message.body));
  });

  // 메시지 전송
  stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
    roomId: 1,
    senderId: 1,
    content: "Hello!"
  }));
});
```

### 3. Elasticsearch 인덱싱 테스트

```bash
# 전체 재색인 (관리자 권한 필요)
curl -X POST http://localhost:8080/api/search/reindex \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"

# 자동완성 테스트
curl "http://localhost:8080/api/search/autocomplete?prefix=제주" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 인기 태그
curl http://localhost:8080/api/search/popular-tags \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Redis 캐시 확인

```bash
# Redis CLI 접속
docker exec -it travelmate-redis redis-cli

# 인증
AUTH password

# 캐시 키 확인
KEYS *

# 특정 캐시 조회
GET "travelGroups::all"

# 캐시 TTL 확인
TTL "travelGroups::all"
```

---

## 🧪 테스트 실행

### Backend 테스트
```bash
cd travelmate-backend

# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests "RecommendationServiceTest"

# 테스트 리포트 생성
./gradlew test jacocoTestReport

# 리포트 확인
start build/reports/tests/test/index.html
```

### Frontend 테스트
```bash
cd travelmate-web

# 전체 테스트
npm test

# 커버리지 포함
npm run test:coverage

# 리포트 확인
start coverage/lcov-report/index.html
```

---

## 🐛 문제 해결

### 문제 1: Backend가 시작되지 않음

**증상:** `Unable to connect to database`

**해결:**
```bash
# PostgreSQL이 실행 중인지 확인
docker ps | grep postgres

# 포트 확인
netstat -an | findstr :5432

# 데이터베이스 재시작
docker restart travelmate-postgres
```

### 문제 2: Redis 연결 오류

**증상:** `Could not connect to Redis`

**해결:**
```bash
# Redis 상태 확인
docker exec -it travelmate-redis redis-cli ping

# Redis 재시작
docker restart travelmate-redis
```

### 문제 3: Elasticsearch 연결 실패

**증상:** `Connection refused: elasticsearch:9200`

**해결:**
```bash
# Elasticsearch 헬스 확인
curl http://localhost:9200/_cluster/health

# 로그 확인
docker logs travelmate-elasticsearch

# 재시작
docker restart travelmate-elasticsearch
```

### 문제 4: Frontend 빌드 오류

**증상:** `Module not found`

**해결:**
```bash
# node_modules 삭제 후 재설치
rm -rf node_modules package-lock.json
npm install

# 캐시 정리
npm cache clean --force
npm install
```

### 문제 5: 포트 충돌

**증상:** `Port 8080 is already in use`

**해결:**
```bash
# Windows: 포트 사용 확인
netstat -ano | findstr :8080

# 프로세스 종료 (관리자 권한)
taskkill /PID <PID> /F

# 또는 다른 포트 사용
# application.yml에서 server.port 변경
```

---

## 📊 모니터링 확인

### Prometheus
1. http://localhost:9090 접속
2. Status > Targets에서 모든 타겟이 UP 상태인지 확인
3. Graph에서 쿼리 테스트:
   - `jvm_memory_used_bytes` - JVM 메모리 사용량
   - `http_server_requests_seconds_count` - HTTP 요청 수
   - `redis_connected_clients` - Redis 연결 수

### Grafana
1. http://localhost:3000 접속 (admin/admin_password)
2. Data Sources에서 Prometheus 연결 확인
3. 대시보드 확인:
   - JVM Metrics
   - Application Metrics
   - Database Metrics

---

## 🎯 주요 기능 시나리오 테스트

### 시나리오 1: 회원가입 → 로그인 → 그룹 조회

1. Frontend (http://localhost:3000) 접속
2. 회원가입 페이지에서 계정 생성
3. 로그인
4. 그룹 목록 확인
5. DevTools > Network 탭에서 API 호출 확인

### 시나리오 2: 그룹 생성 → WebSocket 채팅

1. 로그인 후 그룹 생성
2. 채팅방 입장
3. 메시지 전송
4. 실시간 메시지 수신 확인

### 시나리오 3: 검색 → 추천 → 알림

1. 검색바에서 "제주도" 검색
2. 자동완성 확인
3. 검색 결과 확인
4. 추천 페이지 이동
5. 알림 센터에서 알림 확인

---

## 📝 데이터 초기화

### 데이터베이스 초기화
```bash
# PostgreSQL 데이터 삭제
docker exec -it travelmate-postgres psql -U travelmate -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# 또는 컨테이너 재생성
docker-compose down -v
docker-compose up -d postgres
```

### Redis 캐시 초기화
```bash
# 모든 캐시 삭제
docker exec -it travelmate-redis redis-cli -a password FLUSHALL
```

### Elasticsearch 인덱스 초기화
```bash
# 모든 인덱스 삭제
curl -X DELETE "http://localhost:9200/travel_groups"

# 재색인
curl -X POST http://localhost:8080/api/search/reindex \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

---

## 🔍 로그 확인

### Backend 로그
```bash
# Docker로 실행 시
docker logs -f travelmate-backend

# Gradle로 실행 시
# 콘솔에 직접 출력됨
```

### Frontend 로그
```bash
# 브라우저 DevTools > Console 확인
# 또는 npm start 콘솔 확인
```

### 전체 스택 로그
```bash
docker-compose logs -f
```

---

## 🎉 테스트 완료 체크리스트

- [ ] Backend API 응답 정상 (200 OK)
- [ ] Frontend 페이지 로드 성공
- [ ] 회원가입/로그인 동작
- [ ] 그룹 목록 조회 가능
- [ ] 검색 기능 동작
- [ ] 추천 시스템 동작
- [ ] WebSocket 채팅 연결
- [ ] 알림 수신 확인
- [ ] Redis 캐싱 동작 확인
- [ ] Elasticsearch 검색 정상
- [ ] Prometheus 메트릭 수집
- [ ] Grafana 대시보드 확인

---

## 📞 문제 발생 시

1. **로그 확인**: `docker-compose logs -f`
2. **헬스체크**: `curl http://localhost:8080/actuator/health`
3. **재시작**: `docker-compose restart`
4. **완전 재시작**: `docker-compose down && docker-compose up -d`

---

**✨ 모든 서비스가 정상 작동하면 로컬 테스트 완료입니다!**
