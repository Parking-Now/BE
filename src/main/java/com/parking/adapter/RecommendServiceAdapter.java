package com.parking.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
        return webClient.post()
                .uri(predictUrl + "/api/v1/parking/recommend")
                .bodyValue(Map.of(
                        "lat", lat,
                        "lng", lng,
                        "arrivalTime", arrivalTime,
                        "excludePkltCds", excludePkltCds
                ))
                .retrieve()
                .bodyToFlux(Map.class)
                .cast(Map.class)
                .map(m -> (Map<String, Object>) m)
                .collectList()
                .block();
    }
}