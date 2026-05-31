/**
 * 외부 서비스 HTTP 호출에 사용하는 WebClient 빈 설정
 * adapter/ 에서 공공 API · 예측 서비스 호출 시 사용
 */
package com.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024))  // 최대 2MB
                .build();
    }
}