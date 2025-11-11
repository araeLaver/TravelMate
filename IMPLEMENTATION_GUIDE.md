# TravelMate 추가 구현 가이드

이 문서는 TravelMate 프로젝트의 추가 개선 사항에 대한 구현 가이드입니다.

## 완료된 개선 사항 ✅

### 1. 프론트엔드-백엔드 API 통합
- **위치**: `travelmate-web/src/services/`
- **파일**: `apiClient.ts`, `groupService.ts`, `profileService.ts`, `chatRestService.ts`
- **설명**: Mock 데이터에서 실제 백엔드 API 호출로 전환
- **주요 기능**:
  - 공통 API 클라이언트 (`apiClient.ts`)
  - JWT 토큰 자동 포함
  - 에러 처리 및 재시도 로직
  - 파일 업로드 지원

### 2. 상태 관리 개선 (Zustand + React Query)
- **위치**: `travelmate-web/src/store/`, `travelmate-web/src/hooks/`
- **파일**:
  - Zustand: `authStore.ts`, `uiStore.ts`
  - React Query: `useGroups.ts`, `useProfile.ts`, `useChat.ts`
- **설명**: 전역 상태 관리 및 서버 상태 캐싱
- **주요 기능**:
  - 자동 캐싱 및 리프레시
  - 로딩/에러 상태 자동 관리
  - Optimistic Updates
  - Query Invalidation

### 3. WebSocket 실시간 채팅
- **위치**: `travelmate-web/src/services/websocketService.ts`
- **파일**: `websocketService.ts`, `useWebSocket.ts`
- **설명**: STOMP over SockJS를 사용한 실시간 양방향 통신
- **주요 기능**:
  - 실시간 메시지 전송/수신
  - 채팅방 입장/퇴장
  - 타이핑 상태 표시
  - 자동 재연결

### 4. Redis 캐싱 전략
- **위치**: `travelmate-backend/src/main/java/com/travelmate/config/`
- **파일**: `RedisCacheConfig.java`, `CachedTravelGroupService.java`
- **설명**: Redis를 사용한 데이터 캐싱 전략
- **캐시 TTL 설정**:
  - 사용자 정보: 10분
  - 여행 그룹: 5분
  - 추천 데이터: 15분
  - 검색 결과: 3분

### 5. 이미지 처리 및 최적화
- **위치**: `travelmate-backend/src/main/java/com/travelmate/service/`
- **파일**: `ImageProcessingService.java`
- **설명**: 이미지 업로드, 리사이징, 썸네일 생성
- **주요 기능**:
  - 이미지 리사이징 (최대 1920x1080)
  - 썸네일 생성 (200x200)
  - 이미지 압축 (85% 품질)
  - 파일 검증 및 보안

---

## 향후 구현 가이드 📋

### 6. 실시간 알림 시스템

#### 백엔드 구현
```java
// NotificationService.java
@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseMessaging firebaseMessaging;

    // WebSocket 알림
    public void sendNotification(Long userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    // FCM 푸시 알림
    public void sendPushNotification(String fcmToken, String title, String body) {
        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .setToken(fcmToken)
            .build();

        firebaseMessaging.send(message);
    }
}
```

#### 프론트엔드 구현
```typescript
// useNotifications.ts
export function useNotifications() {
  const [notifications, setNotifications] = useState([]);
  const { isConnected } = useWebSocketConnection();

  useEffect(() => {
    if (!isConnected) return;

    const subscription = websocketService.subscribeToUser('/queue/notifications',
      (notification) => {
        setNotifications(prev => [notification, ...prev]);
        showToast(notification);
      }
    );

    return () => subscription.unsubscribe();
  }, [isConnected]);

  return { notifications };
}
```

### 7. 추천 알고리즘 고도화

#### 협업 필터링 기반 추천
```java
@Service
public class AdvancedRecommendationService {

    // 사용자 기반 협업 필터링
    public List<User> recommendUsersByCollaborativeFiltering(User user) {
        // 1. 유사한 여행 스타일을 가진 사용자 찾기
        List<User> similarUsers = findSimilarUsers(user);

        // 2. 유사 사용자들이 참여한 그룹에서 추천
        return similarUsers.stream()
            .flatMap(u -> u.getGroups().stream())
            .map(Group::getMembers)
            .flatMap(List::stream)
            .filter(u -> !u.equals(user))
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }

    // 콘텐츠 기반 필터링
    public List<TravelGroup> recommendGroupsByContent(User user) {
        // 사용자 프로필 기반 그룹 추천
        return travelGroupRepository.findByTravelStyleAndInterests(
            user.getTravelStyle(),
            user.getInterests()
        );
    }

    // 하이브리드 추천 (협업 + 콘텐츠)
    public List<TravelGroup> getHybridRecommendations(User user) {
        List<TravelGroup> collaborative = recommendByCollaborative(user);
        List<TravelGroup> content = recommendByContent(user);

        // 점수 기반 병합 및 정렬
        return mergeAndRank(collaborative, content);
    }
}
```

### 8. 검색 기능 강화 (Elasticsearch)

#### Elasticsearch 설정
```java
// ElasticsearchConfig.java
@Configuration
@EnableElasticsearchRepositories
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
            new HttpHost("localhost", 9200)
        ).build();

        return new ElasticsearchClient(
            new RestClientTransport(restClient, new JacksonJsonpMapper())
        );
    }
}

// TravelGroupDocument.java
@Document(indexName = "travel_groups")
public class TravelGroupDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Date)
    private LocalDate startDate;
}

// SearchService.java
@Service
public class SearchService {
    private final ElasticsearchClient client;

    public SearchResult<TravelGroup> searchGroups(SearchRequest request) {
        // 전문 검색 쿼리
        Query query = QueryBuilders.bool()
            .should(QueryBuilders.match("title", request.getQuery()).boost(2.0f))
            .should(QueryBuilders.match("description", request.getQuery()))
            .should(QueryBuilders.term("tags", request.getQuery()).boost(1.5f))
            .build();

        // 필터 추가
        if (request.getLocation() != null) {
            query.add(QueryBuilders.geoDistance("location")
                .distance(request.getRadius() + "km")
                .point(request.getLocation()));
        }

        return executeSearch(query);
    }
}
```

### 9. 테스트 코드 작성

#### 단위 테스트 예시
```java
@ExtendWith(MockitoExtension.class)
class TravelGroupServiceTest {

    @Mock
    private TravelGroupRepository repository;

    @InjectMocks
    private TravelGroupService service;

    @Test
    @DisplayName("그룹 생성 성공")
    void createGroup_Success() {
        // Given
        TravelGroupDto.CreateRequest request = createRequest();
        when(repository.save(any())).thenReturn(createGroup());

        // When
        TravelGroupDto.Response result = service.createGroup(request, 1L);

        // Then
        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("그룹 가입 - 인원 초과 시 예외")
    void joinGroup_FullGroup_ThrowsException() {
        // Given
        TravelGroup fullGroup = createFullGroup();
        when(repository.findById(1L)).thenReturn(Optional.of(fullGroup));

        // When & Then
        assertThrows(BusinessException.class,
            () -> service.joinGroup(1L, 2L));
    }
}
```

#### 통합 테스트 예시
```java
@SpringBootTest
@AutoConfigureMockMvc
class TravelGroupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("그룹 목록 조회 통합 테스트")
    void getGroups_Integration() throws Exception {
        mockMvc.perform(get("/api/groups")
                .param("purpose", "LEISURE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").exists());
    }
}
```

### 10. CI/CD 파이프라인

#### GitHub Actions 워크플로우
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Build with Maven
      run: mvn clean install

    - name: Run Tests
      run: mvn test

    - name: Generate Coverage Report
      run: mvn jacoco:report

    - name: Upload Coverage
      uses: codecov/codecov-action@v3

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
    - name: Build Docker Image
      run: docker build -t travelmate:${{ github.sha }} .

    - name: Push to Docker Hub
      run: |
        echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
        docker tag travelmate:${{ github.sha }} ${{ secrets.DOCKER_USERNAME }}/travelmate:latest
        docker push ${{ secrets.DOCKER_USERNAME }}/travelmate:latest

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest

    steps:
    - name: Deploy to Production
      run: |
        ssh ${{ secrets.SSH_USER }}@${{ secrets.SSH_HOST }} '
          docker pull ${{ secrets.DOCKER_USERNAME }}/travelmate:latest &&
          docker-compose up -d
        '
```

### 11. 관리자 대시보드

#### 백엔드 API
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    // 대시보드 통계
    @GetMapping("/dashboard/stats")
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
            .totalUsers(userRepository.count())
            .totalGroups(groupRepository.count())
            .activeUsers(userRepository.countActiveUsers())
            .todaySignups(userRepository.countTodaySignups())
            .build();
    }

    // 사용자 관리
    @GetMapping("/users")
    public Page<UserDto> getUsers(@PageableDefault Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @PutMapping("/users/{id}/status")
    public void updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        userService.updateStatus(id, status);
    }

    // 그룹 관리
    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable Long id, @RequestParam String reason) {
        groupService.deleteByAdmin(id, reason);
    }

    // 신고 관리
    @GetMapping("/reports")
    public Page<ReportDto> getReports(@PageableDefault Pageable pageable) {
        return reportService.getAllReports(pageable);
    }
}
```

#### 프론트엔드 컴포넌트
```tsx
// AdminDashboard.tsx
export function AdminDashboard() {
  const { data: stats } = useQuery(['admin', 'stats'], () =>
    apiClient.get('/admin/dashboard/stats')
  );

  return (
    <div className="admin-dashboard">
      <StatCards stats={stats} />
      <UserTable />
      <GroupTable />
      <ReportTable />
      <ActivityChart />
    </div>
  );
}
```

### 12. 모니터링 및 로깅

#### Prometheus + Grafana 설정
```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards

  loki:
    image: grafana/loki
    ports:
      - "3100:3100"
    volumes:
      - loki_data:/loki

  promtail:
    image: grafana/promtail
    volumes:
      - /var/log:/var/log
      - ./promtail-config.yml:/etc/promtail/config.yml
    command: -config.file=/etc/promtail/config.yml
```

#### 애플리케이션 메트릭 노출
```java
// MetricsConfig.java
@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "travelmate");
    }

    // 커스텀 메트릭
    @Bean
    public Counter groupCreationCounter(MeterRegistry registry) {
        return Counter.builder("travelmate.groups.created")
            .description("Number of travel groups created")
            .register(registry);
    }

    @Bean
    public Timer searchTimer(MeterRegistry registry) {
        return Timer.builder("travelmate.search.duration")
            .description("Search operation duration")
            .register(registry);
    }
}
```

---

## 패키지 설치 명령어

### 백엔드 (pom.xml에 추가)
```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<!-- Firebase Admin SDK (푸시 알림) -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>

<!-- Micrometer Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 프론트엔드
```bash
cd travelmate-web
npm install @tanstack/react-query @tanstack/react-query-devtools
npm install zustand
npm install @stomp/stompjs sockjs-client
npm install firebase  # FCM 푸시 알림
npm install recharts  # 차트 라이브러리 (관리자 대시보드)
```

---

## 다음 단계

1. **패키지 설치**: `npm install` (프론트엔드), `mvn clean install` (백엔드)
2. **환경 변수 설정**: `.env` 파일 생성 (`.env.example` 참고)
3. **데이터베이스 마이그레이션**: Flyway 또는 Liquibase 사용 권장
4. **서버 실행**: `docker-compose up` 또는 개별 실행
5. **테스트**: `mvn test` (백엔드), `npm test` (프론트엔드)

## 참고 자료

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Query Documentation](https://tanstack.com/query/latest)
- [Zustand Documentation](https://docs.pmnd.rs/zustand)
- [WebSocket STOMP](https://stomp-js.github.io/stomp-websocket/)
- [Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Prometheus Monitoring](https://prometheus.io/docs/introduction/overview/)
