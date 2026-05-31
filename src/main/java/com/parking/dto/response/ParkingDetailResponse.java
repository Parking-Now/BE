/**
 * 주차장 상세 정보 응답 DTO
 * 기본 정보 + 실시간 잔여 공간 + 시간대별 혼잡도 그래프 데이터 포함
 */
package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ParkingDetailResponse {

    private String  pkltCd;;        // 주차장 코드
    private String  name;;          // 주차장 이름
    private String  addr;           // 주차장 주소
    private Integer tpkct;          // 총 주차 면수
    private String  payYn;          // 유료 여부
    private Integer bscPrkCrg;      // 기본 요금
    private Integer bscPrkHr;       // 기본 요금 적용 시간 (분)
    private Integer addPrkCrg;      // 추가 요금

    // 운영 시간
    private String  wdOperBgngTm;
    private String  wdOperEndTm;
    private String  weOperBgngTm;
    private String  weOperEndTm;

    // 실시간
    private Integer remaining;      // 현재 잔여 공간
    private Boolean isFull;         // 현재 만차 여부
    private Integer nowPrkVhclCnt;  // 현재 주차 중인 차량 수

    // 혼잡도 그래프 (최근 24시간)
    private CongestionInfo congestion;

    @Getter
    @Builder
    public static class CongestionInfo {
        private Integer currentRate;        // 현재 혼잡률 (%)
        private List<Integer> hourly;       // 시간대별 평균 잔여 공간 (24개)
    }
}