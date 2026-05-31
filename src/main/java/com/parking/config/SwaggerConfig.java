/**
 * Swagger API 문서 자동 생성 설정
 * /swagger-ui/index.html 접속하면 API 목록 확인 가능
 * 프론트 팀원과 API 스펙 공유할 때 사용
 */
package com.parking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Parking Now API")
                        .description("주차장 검색 · 실시간 정보 · 혼잡도 예측 서비스")
                        .version("v1.0.0"));
    }
}