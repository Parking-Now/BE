package com.parking.service;

import com.parking.adapter.RecommendServiceAdapter;
import com.parking.domain.ParkingInfo;
import com.parking.domain.ParkingRealtime;
import com.parking.dto.request.RecommendRequest;
import com.parking.dto.response.RecommendResponse;
import com.parking.exception.ErrorCode;
import com.parking.exception.ParkingException;
import com.parking.repository.ParkingInfoRepository;
import com.parking.repository.ParkingRealtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ParkingInfoRepository parkingInfoRepository;
    private final ParkingRealtimeRepository parkingRealtimeRepository;
    private final RecommendServiceAdapter recommendServiceAdapter;

    private static final double WALKING_SPEED = 67.0;
    private static final int[] RADIUS_STEPS = {500, 1000, 2000};

    // 대체 주차장 추천 (기능 5)
    public RecommendResponse getAlternatives(RecommendRequest request) {

        List<String> excludeIds = request.getExcludePkltCds() != null
                ? request.getExcludePkltCds()
                : new ArrayList<>();

        // AI 서버 호출
        List<Map<String, Object>> aiResults = recommendServiceAdapter.requestRecommend(
                request.getLat(), request.getLng(),
                request.getArrivalTime(), excludeIds);

        if (aiResults == null || aiResults.isEmpty()) {
            throw new ParkingException(ErrorCode.NO_ALTERNATIVES);
        }

        // AI 결과를 기반으로 주차장 정보 조회 및 응답 생성
        List<RecommendResponse.AlternativeItem> alternatives = aiResults.stream()
                .map(result -> {
                    String pkltCd = (String) result.get("pkltCd");
                    Integer recommendScore = (Integer) result.get("recommendScore");

                    return parkingInfoRepository.findByPkltCd(pkltCd).map(info -> {
                        Optional<ParkingRealtime> latest = parkingRealtimeRepository
                                .findTopByPkltCdOrderByCollectedAtDesc(pkltCd);

                        int remaining = latest.map(r -> r.getRemaining() != null
                                ? r.getRemaining() : 0).orElse(0);
                        double distanceM = calcDistance(
                                request.getLat(), request.getLng(),
                                info.getLat(), info.getLng());
                        int walkingMinutes = (int) Math.ceil(distanceM / WALKING_SPEED);

                        return RecommendResponse.AlternativeItem.builder()
                                .pkltCd(pkltCd)
                                .name(info.getPkltNm())
                                .distanceM(distanceM)
                                .walkingMinutes(walkingMinutes)
                                .remaining(remaining)
                                .bscPrkCrg(info.getBscPrkCrg())
                                .recommendScore(recommendScore)
                                .reason(buildReason(remaining, walkingMinutes, info.getBscPrkCrg()))
                                .build();
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (alternatives.isEmpty()) {
            throw new ParkingException(ErrorCode.NO_ALTERNATIVES);
        }

        return RecommendResponse.builder()
                .alternatives(alternatives)
                .build();
    }

    // 추천 이유 생성
    private String buildReason(Integer remaining, int walkingMinutes, Integer bscPrkCrg) {
        StringBuilder sb = new StringBuilder();
        sb.append("잔여 ").append(remaining).append("면");
        sb.append(" · 도보 ").append(walkingMinutes).append("분");
        if (bscPrkCrg != null) {
            sb.append(" · ").append(bscPrkCrg).append("원/10분");
        }
        return sb.toString();
    }

    // Haversine 공식으로 두 좌표 간 거리 계산 (m)
    private double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}