/**
 * 프론트엔드 도메인에서 오는 요청을 허용하는 CORS 설정
 * 로컬 개발 주소 · 배포 주소 모두 등록
 */
package com.parking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",        // 로컬 프론트
                        "http://localhost:5173",        // Vite 사용 시
                        "https://parking-now.com"       // 배포 후 프론트 주소 (나중에 변경)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}