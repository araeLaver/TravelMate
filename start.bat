@echo off
echo.
echo ==========================================
echo  TravelMate 서비스 시작
echo ==========================================
echo.

echo [1/3] Docker 환경 확인 중...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker가 설치되지 않았거나 실행되지 않았습니다.
    echo Docker Desktop을 설치하고 실행한 후 다시 시도해주세요.
    pause
    exit /b 1
)

echo [2/3] 기존 컨테이너 정리 중...
docker-compose down >nul 2>&1

echo [3/3] TravelMate 서비스 시작 중...
docker-compose up --build -d

echo.
echo ==========================================
echo  서비스 시작 완료!
echo ==========================================
echo.
echo 웹 애플리케이션: http://localhost
echo API 서버: http://localhost:8080/api
echo H2 콘솔: http://localhost:8080/h2-console
echo.
echo 서비스를 중지하려면 'stop.bat'를 실행하세요.
echo.

timeout /t 3 >nul
start http://localhost

pause