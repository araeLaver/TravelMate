import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { realSocialLoginService } from '../services/realSocialLoginService';

const AuthCallback: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleAuthCallback = async () => {
      const urlParams = new URLSearchParams(location.search);
      const code = urlParams.get('code');
      const state = urlParams.get('state');
      const error = urlParams.get('error');

      if (error) {
        console.error('OAuth 에러:', error);
        alert('로그인 중 오류가 발생했습니다.');
        navigate('/login');
        return;
      }

      if (code) {
        try {
          // OAuth 코드를 사용하여 토큰 교환 및 사용자 정보 가져오기
          console.log('OAuth 코드 수신:', code);
          console.log('State:', state);
          
          // 여기서 실제로는 백엔드 API를 호출하여 토큰을 교환해야 합니다
          // 현재는 프론트엔드에서만 처리하는 예시입니다
          
          alert('✅ OAuth 로그인이 성공적으로 처리되었습니다!');
          navigate('/dashboard');
        } catch (error) {
          console.error('OAuth 콜백 처리 오류:', error);
          alert('로그인 처리 중 오류가 발생했습니다.');
          navigate('/login');
        }
      } else {
        // 콜백 파라미터가 없는 경우 로그인 페이지로 리다이렉트
        navigate('/login');
      }
    };

    handleAuthCallback();
  }, [location, navigate]);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100vh',
      textAlign: 'center'
    }}>
      <div style={{ marginBottom: '20px', fontSize: '24px' }}>🔄</div>
      <h2>로그인 처리 중...</h2>
      <p>잠시만 기다려주세요.</p>
    </div>
  );
};

export default AuthCallback;