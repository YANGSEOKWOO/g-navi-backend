package com.sk.growthnav.global.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로
                .allowedOriginPatterns("*")  // 모든 도메인 허용
                .allowedMethods("*")         // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE, PATCH, OPTIONS 등)
                .allowedHeaders("*")         // 모든 헤더 허용
                .allowCredentials(true)      // 쿠키, 인증 정보 허용
                .maxAge(3600);              // preflight 캐시 1시간
    }
}
