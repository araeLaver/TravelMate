# TravelMate 소셜 로그인 API 키 설정 가이드

## 1. Google OAuth 2.0 설정

### 단계별 설정 방법:
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. 좌측 메뉴 → "API 및 서비스" → "사용자 인증 정보"
4. "사용자 인증 정보 만들기" → "OAuth 클라이언트 ID"
5. 애플리케이션 유형: "웹 애플리케이션" 선택
6. 승인된 JavaScript 원본: `http://localhost:3000` 추가
7. 승인된 리디렉션 URI: `http://localhost:3000/auth/callback` 추가
8. 생성된 클라이언트 ID 복사

### 환경변수 설정:
```
REACT_APP_GOOGLE_CLIENT_ID=생성된_클라이언트_ID.apps.googleusercontent.com
```

---

## 2. Kakao 로그인 설정

### 단계별 설정 방법:
1. [Kakao Developers](https://developers.kakao.com/) 접속
2. "내 애플리케이션" → "애플리케이션 추가하기"
3. 앱 이름, 사업자명 입력 후 생성
4. 생성된 앱 선택 → "앱 키" 메뉴
5. JavaScript 키 복사
6. "플랫폼" 메뉴 → "Web 플랫폼 등록"
7. 사이트 도메인: `http://localhost:3000` 추가
8. "카카오 로그인" 메뉴 → 활성화 설정 ON
9. Redirect URI: `http://localhost:3000/auth/callback` 등록

### 환경변수 설정:
```
REACT_APP_KAKAO_CLIENT_ID=JavaScript_키_입력
```

---

## 3. Naver 로그인 설정

### 단계별 설정 방법:
1. [Naver Developers](https://developers.naver.com/apps/#/register) 접속
2. "애플리케이션 등록" 클릭
3. 애플리케이션 이름 입력
4. 사용 API: "네이버 로그인" 선택
5. 제공 정보 선택:
   - 필수: 회원이름, 이메일, 프로필사진
6. 서비스 환경:
   - PC 웹 체크
   - 서비스 URL: `http://localhost:3000`
   - 네이버 로그인 Callback URL: `http://localhost:3000/auth/callback`
7. 등록 완료 후 "내 애플리케이션" → 생성한 앱 선택
8. Client ID와 Client Secret 복사

### 환경변수 설정:
```
REACT_APP_NAVER_CLIENT_ID=Client_ID_입력
REACT_APP_NAVER_CLIENT_SECRET=Client_Secret_입력
```

---

## 4. 환경변수 파일 설정

### `.env.local` 파일 수정:
`travelmate-web/.env.local` 파일을 열고 다음과 같이 수정:

```env
# Google OAuth
REACT_APP_GOOGLE_CLIENT_ID=실제_구글_클라이언트_ID.apps.googleusercontent.com

# Kakao OAuth
REACT_APP_KAKAO_CLIENT_ID=실제_카카오_JavaScript_키

# Naver OAuth
REACT_APP_NAVER_CLIENT_ID=실제_네이버_Client_ID
REACT_APP_NAVER_CLIENT_SECRET=실제_네이버_Client_Secret

# Redirect URI
REACT_APP_REDIRECT_URI=http://localhost:3000/auth/callback
```

---

## 5. 프로덕션 배포 시 주의사항

### 각 플랫폼별 추가 설정:

#### Google:
- 프로덕션 도메인을 승인된 JavaScript 원본에 추가
- OAuth 동의 화면 설정 완료
- 앱 검증 프로세스 진행 (필요시)

#### Kakao:
- 프로덕션 도메인을 플랫폼에 추가
- 비즈니스 인증 진행 (선택)
- 사용량 제한 확인

#### Naver:
- 프로덕션 서비스 URL 추가
- 서비스 검수 신청 (필수)
- API 호출량 제한 확인

---

## 6. 테스트 방법

1. 프론트엔드 서버 시작:
```bash
cd travelmate-web
npm start
```

2. 백엔드 서버 시작:
```bash
cd travelmate-backend
./mvnw spring-boot:run
```

3. 브라우저에서 `http://localhost:3000/register` 접속
4. 각 소셜 로그인 버튼 클릭하여 테스트

---

## 7. 문제 해결

### 공통 문제:
- **401 Unauthorized**: API 키가 올바르게 설정되지 않음
- **Redirect URI mismatch**: 등록한 Redirect URI와 실제 사용 URI가 다름
- **CORS 에러**: 도메인이 플랫폼에 등록되지 않음

### 디버깅:
1. 브라우저 개발자 도구 Console 확인
2. Network 탭에서 API 호출 확인
3. 각 플랫폼의 대시보드에서 에러 로그 확인

---

## 8. 보안 주의사항

⚠️ **중요**:
- `.env.local` 파일은 절대 Git에 커밋하지 마세요
- Client Secret은 프론트엔드에 노출되면 안 됩니다
- 프로덕션 환경에서는 환경변수를 서버에서 안전하게 관리하세요
- API 키는 주기적으로 재발급하여 보안을 유지하세요