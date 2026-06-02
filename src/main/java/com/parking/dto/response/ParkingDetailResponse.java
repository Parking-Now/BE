/**
 * 주차장 상세 정보 응답 DTO
 */
package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ParkingDetailResponse {

    private String  pkltCd;         // 주차장 코드
    private String  name;           // 주차장 이름
    private String  addr;           // 주차장 주소
    private Integer tpkct;          // 총 주차 면수
    private Integer bscPrkCrg;      // 기본 요금
    private Integer bscPrkHr;       // 기본 요금 적용 시간 (분)
    private Double  distanceM;      // 목적지로부터 거리 (m)
    private Boolean cardYn;         // 카드결제 가능 여부

    // 운영 시간
    private String wdOperBgngTm;    // 평일 운영 시작
    private String wdOperEndTm;     // 평일 운영 종료
    private String satOperBgngTm;   // 토요일 운영 시작
    private String satOperEndTm;    // 토요일 운영 종료
    private String sunOperBgngTm;   // 일요일 운영 시작
    private String sunOperEndTm;    // 일요일 운영 종료

    // 실시간
    private Integer remaining;
    private Boolean isFull;
    private Integer nowPrkVhclCnt;

    // 혼잡도 그래프
    private CongestionInfo congestion;

    @Getter
    @Builder
    public static class CongestionInfo {
        private Integer currentRate;
        private List<Integer> hourly;
    }

    // 리뷰
    private Double avgRating;
    private Long reviewCount;
    private List<ReviewResponse> recentReviews;
}