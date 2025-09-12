import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { realSocialLoginService } from '../services/realSocialLoginService';
import './Auth.css';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    name: ''
  });
  const [loading, setLoading] = useState(false);
  const [socialLoading, setSocialLoading] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    
    setLoading(true);
    
    // TODO: 실제 회원가입 API 호출
    console.log('Register attempt:', formData);
    
    setTimeout(() => {
      setLoading(false);
      alert('✅ 회원가입이 완료되었습니다!');
      navigate('/dashboard');
    }, 1000);
  };

  // 소셜 로그인 핸들러들
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
          <h2>회원가입</h2>
          <p>여행 동반자와 함께할 모험을 시작하세요</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="name">이름</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="실명을 입력하세요"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="username">사용자명</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="사용자명을 입력하세요"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">이메일</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="your@email.com"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">비밀번호</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="8자 이상의 비밀번호"
              required
              minLength={8}
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">비밀번호 확인</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요"
              required
            />
          </div>

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
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
            {socialLoading === 'google' ? '구글 로그인 중...' : 'Google로 가입'}
          </button>
          <button 
            className="social-btn kakao"
            onClick={handleKakaoLogin}
            disabled={socialLoading !== null}
          >
            <span>🟡</span>
            {socialLoading === 'kakao' ? '카카오 로그인 중...' : 'KakaoTalk으로 가입'}
          </button>
          <button 
            className="social-btn naver"
            onClick={handleNaverLogin}
            disabled={socialLoading !== null}
          >
            <span>🟢</span>
            {socialLoading === 'naver' ? '네이버 로그인 중...' : 'Naver로 가입'}
          </button>
        </div>

        <div className="auth-footer">
          <p>
            이미 계정이 있으신가요? <Link to="/login">로그인</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;