import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { realSocialLoginService } from '../services/realSocialLoginService';
import './Auth.css';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [socialLoading, setSocialLoading] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    // TODO: 실제 로그인 API 호출
    console.log('Login attempt:', { email, password });
    
    setTimeout(() => {
      setLoading(false);
      navigate('/dashboard');
    }, 1000);
  };

  const handleGoogleLogin = async () => {
    setSocialLoading('google');
    try {
      const result = await realSocialLoginService.loginWithGoogle();
      if (result.success) {
        alert(`✅ 구글 로그인 성공! 환영합니다, ${result.user?.name}님!`);
        navigate('/dashboard');
      } else {
        console.log('구글 로그인 실패:', result.error);
      }
    } catch (error) {
      console.error('구글 로그인 에러:', error);
      alert('구글 로그인 중 오류가 발생했습니다.');
    } finally {
      setSocialLoading(null);
    }
  };

  const handleKakaoLogin = async () => {
    setSocialLoading('kakao');
    try {
      const result = await realSocialLoginService.loginWithKakao();
      if (result.success) {
        alert(`✅ 카카오 로그인 성공! 환영합니다, ${result.user?.name}님!`);
        navigate('/dashboard');
      } else {
        console.log('카카오 로그인 실패:', result.error);
      }
    } catch (error) {
      console.error('카카오 로그인 에러:', error);
      alert('카카오 로그인 중 오류가 발생했습니다.');
    } finally {
      setSocialLoading(null);
    }
  };

  const handleNaverLogin = async () => {
    setSocialLoading('naver');
    try {
      const result = await realSocialLoginService.loginWithNaver();
      if (result.success) {
        alert(`✅ 네이버 로그인 성공! 환영합니다, ${result.user?.name}님!`);
        navigate('/dashboard');
      } else {
        console.log('네이버 로그인 실패:', result.error);
      }
    } catch (error) {
      console.error('네이버 로그인 에러:', error);
      alert('네이버 로그인 중 오류가 발생했습니다.');
    } finally {
      setSocialLoading(null);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>🌍 TravelMate</h1>
          <h2>로그인</h2>
          <p>여행 동반자와 다시 만나보세요</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">이메일</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="your@email.com"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">비밀번호</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              required
            />
          </div>

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="auth-divider">
          <span>또는</span>
        </div>

        <div className="social-login">
          <button 
            className="social-btn google"
            onClick={handleGoogleLogin}
            disabled={socialLoading !== null}
          >
            <span>🔵</span>
            {socialLoading === 'google' ? '구글 로그인 중...' : 'Google로 로그인'}
          </button>
          <button 
            className="social-btn kakao"
            onClick={handleKakaoLogin}
            disabled={socialLoading !== null}
          >
            <span>🟡</span>
            {socialLoading === 'kakao' ? '카카오 로그인 중...' : 'KakaoTalk으로 로그인'}
          </button>
          <button 
            className="social-btn naver"
            onClick={handleNaverLogin}
            disabled={socialLoading !== null}
          >
            <span>🟢</span>
            {socialLoading === 'naver' ? '네이버 로그인 중...' : 'Naver로 로그인'}
          </button>
        </div>

        <div className="auth-footer">
          <p>
            계정이 없으신가요? <Link to="/register">회원가입</Link>
          </p>
          <p>
            <Link to="/forgot-password">비밀번호를 잊으셨나요?</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;