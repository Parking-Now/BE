/**
 * 주차장 검색 결과 단건 DTO
 * parking_static 기반으로 통일
 * has_realtime = false 이면 remaining · isFull · nowPrkVhclCnt 는 null
 */
package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ParkingListItemDto {

    private String  pkltCd;
    private String  name;
    private String  addr;
    private String  parkingType;    // flag_nm
    private Double  lat;
    private Double  lng;
    private Double  distanceM;
    private Integer tpkct;
    private Integer bscPrkCrg;
    private Integer bscPrkHr;
    private Boolean hasRealtime;
    private Boolean cardYn;

    // 운영 시간
    private String wdOperBgngTm;    // 평일 시작
    private String wdOperEndTm;     // 평일 종료
    private String satOperBgngTm;   // 토요일 시작
    private String satOperEndTm;    // 토요일 종료
    private String sunOperBgngTm;   // 일요일 시작
    private String sunOperEndTm;    // 일요일 종료

    // 실시간 데이터 (hasRealtime=false면 null)
    private Integer remaining;
    private Boolean isFull;
    private Integer nowPrkVhclCnt;

    // 예측 필드
    private Integer predictedRemaining;
    private String  congestionLevel;
    private Boolean recommendTransit;

    private Double avgRating;
}