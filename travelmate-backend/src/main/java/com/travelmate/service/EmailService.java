package com.travelmate.service;

import com.travelmate.entity.EmailVerification;
import com.travelmate.entity.User;
import com.travelmate.exception.UserException;
import com.travelmate.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    
    @Value("${spring.mail.from:noreply@travelmate.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    private static final int MAX_SEND_COUNT = 5;
    private static final int RATE_LIMIT_HOURS = 1;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 이메일 인증 코드 발송
     */
    @Transactional
    public void sendEmailVerification(User user) {
        // 기존 미완료 인증 확인
        Optional<EmailVerification> existingVerification = 
            emailVerificationRepository.findByUserAndTypeAndIsVerifiedFalse(
                user, EmailVerification.VerificationType.EMAIL_VERIFICATION);
        
        if (existingVerification.isPresent()) {
            EmailVerification verification = existingVerification.get();
            
            // Rate limiting 체크
            if (verification.getSendCount() >= MAX_SEND_COUNT) {
                throw new UserException.TooManyLoginAttemptsException();
            }
            
            // 최근 발송 시간 체크 (1시간 내 최대 3회)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(RATE_LIMIT_HOURS);
            int recentSendCount = emailVerificationRepository
                .countRecentVerificationsByUserAndType(user, 
                    EmailVerification.VerificationType.EMAIL_VERIFICATION, oneHourAgo);
            
            if (recentSendCount >= 3) {
                throw new UserException.TooManyLoginAttemptsException();
            }
            
            // 기존 토큰 업데이트
            verification.setVerificationCode(generateVerificationCode());
            verification.setSendCount(verification.getSendCount() + 1);
            verification.setLastSentAt(LocalDateTime.now());
            verification.setExpiresAt(LocalDateTime.now().plusHours(24));
            
            emailVerificationRepository.save(verification);
            sendVerificationEmail(user.getEmail(), verification.getVerificationCode(), verification.getToken());
        } else {
            // 새 인증 토큰 생성
            String token = UUID.randomUUID().toString();
            String code = generateVerificationCode();
            
            EmailVerification verification = EmailVerification.builder()
                .user(user)
                .token(token)
                .email(user.getEmail())
                .type(EmailVerification.VerificationType.EMAIL_VERIFICATION)
                .verificationCode(code)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .lastSentAt(LocalDateTime.now())
                .build();
            
            emailVerificationRepository.save(verification);
            sendVerificationEmail(user.getEmail(), code, token);
        }
        
        log.info("이메일 인증 코드 발송: {}", user.getEmail());
    }
    
    /**
     * 이메일 인증 확인
     */
    @Transactional
    public boolean verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
            .orElseThrow(() -> new UserException.InvalidTokenException());
        
        if (verification.getIsVerified()) {
            throw new UserException.InvalidTokenException();
        }
        
        if (verification.isExpired()) {
            throw new UserException.TokenExpiredException();
        }
        
        // 인증 완료 처리
        verification.setIsVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
        
        // 사용자 이메일 인증 상태 업데이트
        User user = verification.getUser();
        user.setIsEmailVerified(true);
        
        log.info("이메일 인증 완료: {}", user.getEmail());
        return true;
    }
    
    /**
     * 비밀번호 재설정 이메일 발송
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        // 사용자 확인은 보안상 여기서 하지 않고, 항상 성공 메시지 반환
        
        String token = UUID.randomUUID().toString();
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[TravelMate] 비밀번호 재설정");
        message.setText(
            "안녕하세요,\n\n" +
            "비밀번호 재설정을 요청하셨습니다.\n" +
            "아래 링크를 클릭하여 새 비밀번호를 설정해주세요:\n\n" +
            resetUrl + "\n\n" +
            "이 링크는 1시간 후에 만료됩니다.\n" +
            "만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.\n\n" +
            "감사합니다.\n" +
            "TravelMate 팀"
        );
        
        try {
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 시도: {}", email);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", email, e);
        }
    }
    
    /**
     * 의심스러운 로그인 알림
     */
    public void sendSuspiciousLoginAlert(User user, String ipAddress, String location) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("[TravelMate] 의심스러운 로그인 감지");
        message.setText(
            "안녕하세요 " + user.getNickname() + "님,\n\n" +
            "새로운 위치에서 계정에 로그인이 감지되었습니다:\n\n" +
            "IP 주소: " + ipAddress + "\n" +
            "위치: " + location + "\n" +
            "시간: " + LocalDateTime.now() + "\n\n" +
            "본인의 로그인이 아니라면 즉시 비밀번호를 변경해주세요.\n\n" +
            "감사합니다.\n" +
            "TravelMate 팀"
        );
        
        try {
            mailSender.send(message);
            log.info("의심스러운 로그인 알림 발송: {}", user.getEmail());
        } catch (Exception e) {
            log.error("의심스러운 로그인 알림 발송 실패: {}", user.getEmail(), e);
        }
    }
    
    /**
     * 만료된 인증 정리
     */
    @Transactional
    public void cleanupExpiredVerifications() {
        emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        log.info("만료된 이메일 인증 정리 완료");
    }
    
    /**
     * 이메일 인증 코드 발송 (실제 이메일)
     */
    private void sendVerificationEmail(String email, String code, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[TravelMate] 이메일 인증");
        message.setText(
            "안녕하세요,\n\n" +
            "TravelMate 계정의 이메일 인증을 위한 코드입니다:\n\n" +
            "인증 코드: " + code + "\n\n" +
            "또는 아래 링크를 클릭하여 인증을 완료하세요:\n" +
            verifyUrl + "\n\n" +
            "이 코드는 24시간 후에 만료됩니다.\n\n" +
            "감사합니다.\n" +
            "TravelMate 팀"
        );
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }
    
    /**
     * 6자리 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}