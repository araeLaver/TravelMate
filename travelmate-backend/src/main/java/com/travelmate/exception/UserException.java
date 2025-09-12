package com.travelmate.exception;

import org.springframework.http.HttpStatus;

public class UserException extends BusinessException {
    
    public static class EmailAlreadyExistsException extends BusinessException {
        public EmailAlreadyExistsException() {
            super(HttpStatus.CONFLICT, "USER_EMAIL_EXISTS", "이미 존재하는 이메일입니다.");
        }
    }
    
    public static class NicknameAlreadyExistsException extends BusinessException {
        public NicknameAlreadyExistsException() {
            super(HttpStatus.CONFLICT, "USER_NICKNAME_EXISTS", "이미 존재하는 닉네임입니다.");
        }
    }
    
    public static class UserNotFoundException extends BusinessException {
        public UserNotFoundException() {
            super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        
        public UserNotFoundException(Long userId) {
            super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다. ID: " + userId);
        }
    }
    
    public static class InvalidPasswordException extends BusinessException {
        public InvalidPasswordException() {
            super(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.");
        }
    }
    
    public static class AccountLockedException extends BusinessException {
        public AccountLockedException(int remainingMinutes) {
            super(HttpStatus.LOCKED, "ACCOUNT_LOCKED", 
                String.format("계정이 잠겼습니다. %d분 후에 다시 시도해주세요.", remainingMinutes));
        }
    }
    
    public static class EmailNotVerifiedException extends BusinessException {
        public EmailNotVerifiedException() {
            super(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다.");
        }
    }
    
    public static class InvalidTokenException extends BusinessException {
        public InvalidTokenException() {
            super(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }
    }
    
    public static class TokenExpiredException extends BusinessException {
        public TokenExpiredException() {
            super(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED", "만료된 토큰입니다.");
        }
    }
    
    public static class PasswordPolicyException extends BusinessException {
        public PasswordPolicyException(String details) {
            super(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION", 
                "비밀번호 정책 위반: " + details);
        }
    }
    
    public static class DuplicateReviewException extends BusinessException {
        public DuplicateReviewException() {
            super(HttpStatus.CONFLICT, "DUPLICATE_REVIEW", "이미 해당 사용자에 대한 리뷰를 작성했습니다.");
        }
    }
    
    public static class SelfReviewException extends BusinessException {
        public SelfReviewException() {
            super(HttpStatus.BAD_REQUEST, "SELF_REVIEW", "자기 자신에게는 리뷰를 작성할 수 없습니다.");
        }
    }
    
    public static class InvalidLocationException extends BusinessException {
        public InvalidLocationException() {
            super(HttpStatus.BAD_REQUEST, "INVALID_LOCATION", "유효하지 않은 위치 정보입니다.");
        }
    }
    
    public static class TooManyLoginAttemptsException extends BusinessException {
        public TooManyLoginAttemptsException() {
            super(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_LOGIN_ATTEMPTS", 
                "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    public static class InvalidVerificationCodeException extends BusinessException {
        public InvalidVerificationCodeException() {
            super(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", "잘못된 인증 코드입니다.");
        }
    }
    
    public static class VerificationCodeExpiredException extends BusinessException {
        public VerificationCodeExpiredException() {
            super(HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_EXPIRED", "인증 코드가 만료되었습니다.");
        }
    }
}