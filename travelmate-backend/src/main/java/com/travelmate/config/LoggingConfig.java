package com.travelmate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Slf4j
public class LoggingConfig {

    /**
     * 요청/응답 로깅을 위한 필터 설정
     */
    @Bean
    public AbstractRequestLoggingFilter requestLoggingFilter() {
        AbstractRequestLoggingFilter filter = new AbstractRequestLoggingFilter() {
            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
                // 성능상 이유로 비활성화
                // 대신 RequestLoggingFilter 사용
            }

            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                // 성능상 이유로 비활성화
                // 대신 RequestLoggingFilter 사용
            }
        };
        
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false); // 보안상 페이로드는 제외
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false); // 보안상 헤더는 제외
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        
        return filter;
    }
}