import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home: React.FC = () => {
  return (
    <div className="home">
      <div className="hero">
        <div className="hero-content">
          <h1>🌍 TravelMate</h1>
          <p className="hero-subtitle">여행 동반자를 찾는 소셜 플랫폼</p>
          <p className="hero-description">
            전 세계 여행자들과 연결되어 완벽한 여행 동반자를 찾아보세요.
            실시간 채팅, 위치 기반 매칭, 그리고 안전한 여행 그룹 관리까지!
          </p>
          <div className="hero-actions">
            <Link to="/dashboard" className="btn btn-primary">
              🔍 여행 메이트 찾기
            </Link>
            <div className="auth-links">
              <Link to="/register" className="auth-link">
                회원가입
              </Link>
              <span className="auth-divider">|</span>
              <Link to="/login" className="auth-link">
                로그인
              </Link>
            </div>
          </div>
        </div>
      </div>

      <div className="features">
        <div className="container">
          <h2>✨ 주요 기능</h2>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">👥</div>
              <h3>스마트 매칭</h3>
              <p>AI 기반 알고리즘으로 당신의 여행 스타일에 맞는 완벽한 동반자를 찾아드립니다.</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">💬</div>
              <h3>실시간 채팅</h3>
              <p>WebSocket 기반 실시간 메시징으로 언제 어디서나 소통할 수 있습니다.</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🗺️</div>
              <h3>여행 그룹 관리</h3>
              <p>여행 계획부터 실행까지, 체계적인 그룹 관리 시스템을 제공합니다.</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">📍</div>
              <h3>위치 기반 서비스</h3>
              <p>현재 위치를 기반으로 근처의 여행자들과 즉시 연결됩니다.</p>
            </div>
          </div>
        </div>
      </div>

      <div className="stats">
        <div className="container">
          <div className="stats-grid">
            <div className="stat">
              <div className="stat-number">10,000+</div>
              <div className="stat-label">활성 사용자</div>
            </div>
            <div className="stat">
              <div className="stat-number">500+</div>
              <div className="stat-label">진행 중인 여행</div>
            </div>
            <div className="stat">
              <div className="stat-number">50+</div>
              <div className="stat-label">지원 국가</div>
            </div>
            <div className="stat">
              <div className="stat-number">99%</div>
              <div className="stat-label">만족도</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;