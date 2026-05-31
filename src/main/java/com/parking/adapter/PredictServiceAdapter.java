/**
 * AI 혼잡도 예측 서버 연동 어댑터
 * 기능 4 · AI 모델 연결 시 여기 구현
 *
 * 요청 : pkltCds (목록), arrivalTime
 * 응답 : pkltCd + predictedRemaining + congestionLevel 목록
 */
package com.parking.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PredictServiceAdapter {

    private final WebClient webClient;

    @Value("${api.predict.url}")
    private String predictUrl;

    // TODO: AI 모델 연결 후 구현
    // 요청 예시
    // {
    //   "pkltCds": ["3187200", "1040225"],
    //   "arrivalTime": "2026-06-01T15:00:00"
    // }
    // 응답 예시
    // [
    //   { "pkltCd": "3187200", "predictedRemaining": 50, "congestionLevel": "MEDIUM" },
    //   { "pkltCd": "1040225", "predictedRemaining": 80, "congestionLevel": "LOW" }
    // ]
    public List<Object> requestPrediction(List<String> pkltCds, String arrivalTime) {
        // TODO: AI 서버 URL · 입력값 · 출력값 스펙 확정 후 구현
        return null;
    }
}