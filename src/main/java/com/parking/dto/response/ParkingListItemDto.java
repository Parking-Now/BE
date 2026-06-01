/**
 * 주차장 검색 결과 단건 DTO
 * parking_info · parking_static 두 테이블을 하나의 통일된 형태로 반환
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
    private String  name;           // pklt_nm / fclty_nm 통일
    private String  addr;
    private String  parkingType;    // parking_type / flag_nm 통일
    private Double  lat;
    private Double  lng;
    private Double  distanceM;      // 검색 기준점으로부터 거리 (m)
    private Integer tpkct;          // 총 주차 면수
    private Integer bscPrkCrg;      // 기본 요금
    private Integer bscPrkHr;       // 기본 요금 적용 시간 (분)
    private Integer addPrkCrg;      // 추가 요금 (parking_info만 있음)
    private Boolean hasRealtime;    // 실시간 데이터 제공 여부

    // 운영 시간
    private String  wdOperBgngTm;   // 평일 시작
    private String  wdOperEndTm;    // 평일 종료
    private String  weOperBgngTm;   // 주말 시작
    private String  weOperEndTm;    // 주말 종료

    // 실시간 데이터 (hasRealtime = false 이면 null)
    private Integer remaining;
    private Boolean isFull;
    private Integer nowPrkVhclCnt;

    // 예측 필드
    private Integer predictedRemaining;  // 예측 잔여 공간
    private String congestionLevel;      // LOW / MEDIUM / HIGH / UNKNOWN
    private Boolean recommendTransit;    // 대중교통 권장 여부

    private Double avgRating;  // 평균 평점

}