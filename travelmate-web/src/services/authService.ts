const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081/api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    nickname: string;
    fullName?: string;
    profileImageUrl?: string;
    rating: number;
    reviewCount: number;
    isEmailVerified: boolean;
    createdAt: string;
  };
}

export interface RegisterRequest {
  email: string;
  password: string;
  nickname: string;
  fullName?: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
  nickname: string;
  fullName?: string;
  createdAt: string;
}

class AuthService {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('authToken');
  }

  async login(request: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/users/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`로그인 실패: ${response.status} - ${errorData}`);
      }

      const data: LoginResponse = await response.json();

      // 토큰을 localStorage에 저장
      this.token = data.token;
      localStorage.setItem('authToken', data.token);

      return data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  async register(request: RegisterRequest): Promise<RegisterResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`회원가입 실패: ${response.status} - ${errorData}`);
      }

      const data: RegisterResponse = await response.json();
      return data;
    } catch (error) {
      console.error('Register error:', error);
      throw error;
    }
  }

  logout(): void {
    this.token = null;
    localStorage.removeItem('authToken');
  }

  getToken(): string | null {
    return this.token;
  }

  isAuthenticated(): boolean {
    return this.token !== null;
  }

  async checkEmailDuplicate(email: string): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/users/check-email?email=${encodeURIComponent(email)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`이메일 중복 체크 실패: ${response.status}`);
      }

      const data: { exists: boolean } = await response.json();
      return data.exists;
    } catch (error) {
      console.error('Email duplicate check error:', error);
      throw error;
    }
  }

  async checkNicknameDuplicate(nickname: string): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/users/check-nickname?nickname=${encodeURIComponent(nickname)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`닉네임 중복 체크 실패: ${response.status}`);
      }

      const data: { exists: boolean } = await response.json();
      return data.exists;
    } catch (error) {
      console.error('Nickname duplicate check error:', error);
      throw error;
    }
  }

  getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    return headers;
  }
}

export const authService = new AuthService();