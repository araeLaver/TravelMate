package com.travelmate.security.oauth2;

import com.travelmate.service.EnhancedJwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final EnhancedJwtService jwtService;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(oAuth2User.getUser());
        String refreshToken = jwtService.generateRefreshToken(
            oAuth2User.getUser(),
            extractDeviceId(request),
            "OAuth2 Device",
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        // 프론트엔드로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString();
        
        log.info("OAuth2 로그인 성공: userId={}, email={}", 
                oAuth2User.getId(), oAuth2User.getEmail());
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private String extractDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId == null) {
            String userAgent = request.getHeader("User-Agent");
            deviceId = userAgent != null ? Integer.toString(userAgent.hashCode()) : "unknown";
        }
        return deviceId;
    }
}