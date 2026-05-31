/**
 * 대체 주차장 추천 결과 응답 DTO
 * 추천 점수 = 잔여율 40% + 거리 역산 35% + 요금 역산 25% --> 이거 어떻게 할지 생각 해봐야 할 듯;
 */
package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class RecommendResponse {

    private List<AlternativeItem> alternatives;

    @Getter
    @Builder
    public static class AlternativeItem {
        private String  pkltCd;
        private String  name;
        private Double  distanceM;          // 거리 (m)
        private Integer walkingMinutes;     // 도보 이동 시간 (분)
        private Integer remaining;          // 잔여 공간
        private Integer bscPrkCrg;         // 기본 요금
        private Integer recommendScore;     // 추천 점수 (0~100)
        private String  reason;             // 추천 이유 요약
    }
}