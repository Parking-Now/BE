/**
 * 주차장 검색 요청 파라미터
 * GET /api/v1/parking/search
 */
package com.parking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingSearchRequest {

    @NotNull(message = "위도는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다.")
    private Double lng;

    private Integer radius = 700;       // 기본 반경 700m

    private String arrivalTime;         // 도착 예정 시간 (ISO 8601)

    private String sort = "score";      // score | distance | price

    private Boolean excludeFull = false;  // 만차 제외 유무
}