package com.travelmate.security.oauth2;

import com.travelmate.entity.User;
import com.travelmate.repository.UserRepository;
import com.travelmate.security.oauth2.user.OAuth2UserInfo;
import com.travelmate.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            oAuth2UserRequest.getClientRegistration().getRegistrationId(),
            oAuth2User.getAttributes()
        );
        
        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("OAuth2 제공자로부터 이메일을 받을 수 없습니다.");
        }
        
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }
        
        return new CustomOAuth2User(oAuth2User.getAttributes(), user);
    }
    
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(User.AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setNickname(generateUniqueNickname(oAuth2UserInfo.getName()));
        user.setFullName(oAuth2UserInfo.getName());
        user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        user.setIsEmailVerified(true); // OAuth2를 통한 이메일은 검증된 것으로 간주
        user.setLastActivityAt(LocalDateTime.now());
        
        // 소셜 로그인 사용자는 비밀번호 없음
        user.setPassword("OAUTH2_USER_NO_PASSWORD");
        
        User savedUser = userRepository.save(user);
        
        log.info("새 OAuth2 사용자 등록: email={}, provider={}", 
                savedUser.getEmail(), savedUser.getProvider());
        
        return savedUser;
    }
    
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // 기존 로컬 계정에 소셜 계정 연동
        if (existingUser.getProvider() == User.AuthProvider.LOCAL) {
            existingUser.setProvider(User.AuthProvider.valueOf(
                oAuth2UserInfo.getProvider().toUpperCase()));
            existingUser.setProviderId(oAuth2UserInfo.getId());
        }
        
        // 프로필 정보 업데이트 (사용자가 동의한 경우에만)
        if (oAuth2UserInfo.getName() != null && !oAuth2UserInfo.getName().isEmpty()) {
            existingUser.setFullName(oAuth2UserInfo.getName());
        }
        
        if (oAuth2UserInfo.getImageUrl() != null && !oAuth2UserInfo.getImageUrl().isEmpty()) {
            existingUser.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        }
        
        existingUser.setLastActivityAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(existingUser);
        
        log.info("기존 사용자 정보 업데이트: userId={}, provider={}", 
                savedUser.getId(), savedUser.getProvider());
        
        return savedUser;
    }
    
    /**
     * 중복되지 않는 닉네임 생성
     */
    private String generateUniqueNickname(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "TravelMate";
        }
        
        // 특수문자 제거 및 길이 제한
        String cleanName = baseName.replaceAll("[^a-zA-Z0-9가-힣]", "")
                                  .substring(0, Math.min(baseName.length(), 20));
        
        if (cleanName.isEmpty()) {
            cleanName = "User";
        }
        
        String nickname = cleanName;
        int counter = 1;
        
        // 중복 체크 및 숫자 추가
        while (userRepository.existsByNickname(nickname)) {
            nickname = cleanName + counter;
            counter++;
        }
        
        return nickname;
    }
}