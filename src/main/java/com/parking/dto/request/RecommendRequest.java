/**
 * 대체 주차장 추천 요청 파라미터
 * GET /api/v1/recommend/alternatives
 */
package com.parking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RecommendRequest {

    @NotNull(message = "위도는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다.")
    private Double lng;

    private List<String> excludePkltCds;  // 제외할 주차장 코드 목록 (만차인 것)

    private String arrivalTime;           // 도착 예정 시간
}