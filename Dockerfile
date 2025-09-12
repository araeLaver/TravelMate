# 멀티스테이지 빌드를 사용한 TravelMate 통합 이미지

# Stage 1: 웹 프론트엔드 빌드
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

# 패키지 파일 복사 및 의존성 설치
COPY travelmate-web/package*.json ./
RUN npm ci --only=production

# 소스 코드 복사 및 빌드
COPY travelmate-web/src ./src
COPY travelmate-web/public ./public
COPY travelmate-web/tsconfig.json ./
RUN npm run build

# Stage 2: 백엔드 빌드
FROM maven:3.8.4-openjdk-17-slim AS backend-build
WORKDIR /app

# Maven 설정 파일 복사
COPY travelmate-backend/pom.xml ./
RUN mvn dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY travelmate-backend/src ./src
RUN mvn clean package -DskipTests

# Stage 3: 실행 이미지
FROM openjdk:17-jdk-slim

# 시스템 업데이트 및 필수 패키지 설치
RUN apt-get update && apt-get install -y \
    nginx \
    supervisor \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 백엔드 애플리케이션 복사
COPY --from=backend-build /app/target/*.jar app.jar

# 프론트엔드 빌드 결과물 복사
COPY --from=frontend-build /app/frontend/build /var/www/html

# Nginx 설정
COPY docker/nginx.conf /etc/nginx/nginx.conf

# Supervisor 설정 (백엔드와 Nginx 동시 실행)
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# 포트 노출
EXPOSE 80 8080

# Supervisor로 서비스 시작
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]