package com.parking.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendServiceAdapter {

    private final WebClient webClient;

    @Value("${api.predict.url}")
    private String predictUrl;

    public List<Map<String, Object>> requestRecommend(
            Double lat, Double lng, String arrivalTime, List<String> excludePkltCds) {

        Map<String, Object> body = new HashMap<>();
        body.put("lat", lat);
        body.put("lng", lng);
        body.put("excludePkltCds", excludePkltCds);
        if (arrivalTime != null) {
            body.put("arrivalTime", arrivalTime);
        }

        return webClient.post()
                .uri(predictUrl + "/api/v1/parking/recommend")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(m -> (Map<String, Object>) m)
                .collectList()
                .onErrorResume(e -> {
                    log.error("추천 서버 호출 실패 [url={}, lat={}, lng={}]: {}", predictUrl, lat, lng, e.getMessage(), e);
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }
}