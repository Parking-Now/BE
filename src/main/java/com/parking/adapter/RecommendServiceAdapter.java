package com.parking.adapter;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 대체 주차장 추천 서버 연동 어댑터
 * 기능 5 · AI 모델 연결 시 여기 구현
 *
 * 요청 : lat, lng, arrivalTime, excludePkltCds
 * 응답 : pkltCd + recommendScore 목록
 */
@Component
public class RecommendServiceAdapter {

    // TODO: AI 서버 URL 주입
    // @Value("${api.recommend.url}")
    // private String recommendServiceUrl;

    public List<Object> requestRecommendation(
            Double lat, Double lng,
            String arrivalTime,
            List<String> excludePkltCds) {
        // TODO: AI 모델 연결 후 구현
        return null;
    }
}