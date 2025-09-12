@echo off
echo.
echo ==========================================
echo  TravelMate 서비스 중지
echo ==========================================
echo.

echo 모든 컨테이너 중지 중...
docker-compose down

echo.
echo 서비스가 중지되었습니다.
echo.

pause