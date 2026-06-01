package com.parking.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .block();
    }
}