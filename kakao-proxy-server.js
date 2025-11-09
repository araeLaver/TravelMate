const express = require('express');
const cors = require('cors');
const axios = require('axios');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

// CORS 설정 - 프로덕션에서는 특정 도메인만 허용
const corsOptions = {
  origin: process.env.NODE_ENV === 'production'
    ? ['https://travelmate.app', 'https://www.travelmate.app']
    : ['http://localhost:3000', 'http://localhost:3001', 'http://localhost:3002', 'http://localhost:3005'],
  credentials: true
};

app.use(cors(corsOptions));

// Kakao Maps API 프록시
app.get('/api/location/address', async (req, res) => {
  try {
    const { lat, lng } = req.query;

    // 환경변수에서 API 키 가져오기
    const kakaoApiKey = process.env.KAKAO_API_KEY;

    if (!kakaoApiKey) {
      console.error('KAKAO_API_KEY 환경변수가 설정되지 않았습니다.');
      return res.status(500).json({
        error: true,
        message: 'API 설정 오류가 발생했습니다.'
      });
    }

    console.log(`좌표를 주소로 변환: lat=${lat}, lng=${lng}`);

    const kakaoUrl = `https://dapi.kakao.com/v2/local/geo/coord2address.json?x=${lng}&y=${lat}`;

    const response = await axios.get(kakaoUrl, {
      headers: {
        'Authorization': `KakaoAK ${kakaoApiKey}`
      }
    });

    console.log('카카오맵 API 응답:', response.data);
    res.json(response.data);
  } catch (error) {
    console.error('주소 변환 실패:', error.message);

    // 실제 API 오류 시 서울 근처의 임시 주소 제공
    const lat = parseFloat(req.query.lat);
    const lng = parseFloat(req.query.lng);

    // 한국 범위 내라면 임시 주소 제공
    if (lat >= 33 && lat <= 39 && lng >= 124 && lng <= 132) {
      res.json({
        documents: [{
          address: {
            address_name: `경기도 성남시 분당구 (임시 위치: ${lat.toFixed(4)}, ${lng.toFixed(4)})`
          },
          road_address: {
            address_name: `경기도 성남시 분당구 대왕판교로 123 (임시 위치)`
          }
        }]
      });
    } else {
      res.json({
        error: true,
        message: '주소 변환에 실패했습니다.',
        fallback: `위도 ${req.query.lat}, 경도 ${req.query.lng}`
      });
    }
  }
});

app.listen(PORT, () => {
  console.log(`🚀 Kakao Maps 프록시 서버가 http://localhost:${PORT} 에서 실행 중입니다.`);
});