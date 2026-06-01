/**
 * AI 혼잡도 예측 서버 연동 어댑터
 * 기능 4 · AI 모델 연결
 */
package com.parking.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PredictServiceAdapter {

    private final WebClient webClient;

    @Value("${api.predict.url}")
    private String predictUrl;

    public List<Map<String, Object>> requestPrediction(List<String> pkltCds, String arrivalTime) {
        return webClient.post()
                .uri(predictUrl + "/api/v1/parking/congestion")
                .bodyValue(Map.of(
                        "pkltCds", pkltCds,
                        "arrivalTime", arrivalTime
                ))
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(m -> (Map<String, Object>) m)
                .collectList()
                .block();
    }
}