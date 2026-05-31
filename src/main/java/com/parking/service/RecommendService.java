/**
 * 대체 주차장 추천 비즈니스 로직
 * 반경 자동 확장 (500m → 1km → 2km)
 * 추천 점수 = 잔여율 40% + 거리 역산 35% + 요금 역산 25%
 * 대상 : hasRealtime=true 주차장만
 */
package com.parking.service;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ParkingInfoRepository parkingInfoRepository;
    private final ParkingRealtimeRepository parkingRealtimeRepository;

    // 도보 속도 (m/분)
    private static final double WALKING_SPEED = 67.0;

    // 반경 확장 단계 (m)
    private static final int[] RADIUS_STEPS = {500, 1000, 2000};

    // 대체 주차장 추천 (기능 5)
    public RecommendResponse getAlternatives(RecommendRequest request) {

        List<String> excludeIds = request.getExcludePkltCds() != null
                ? request.getExcludePkltCds()
                : new ArrayList<>();

        // 반경 자동 확장하며 후보 탐색
        List<RecommendResponse.AlternativeItem> alternatives = new ArrayList<>();

        for (int radius : RADIUS_STEPS) {
            alternatives = findAndScore(
                    request.getLat(), request.getLng(), radius, excludeIds);

            if (!alternatives.isEmpty()) break;
        }

        if (alternatives.isEmpty()) {
            throw new ParkingException(ErrorCode.NO_ALTERNATIVES);
        }

        return RecommendResponse.builder()
                .alternatives(alternatives)
                .build();
    }

    // 후보 탐색 + 점수 계산 (hasRealtime=true만)
    private List<RecommendResponse.AlternativeItem> findAndScore(
            Double lat, Double lng, int radius, List<String> excludeIds) {

        // parking_info 후보 조회
        List<ParkingInfo> infoList = excludeIds.isEmpty()
                ? parkingInfoRepository.findWithinRadius(lat, lng, radius)
                : parkingInfoRepository.findAlternativesWithinRadius(lat, lng, radius, excludeIds);

        List<RecommendResponse.AlternativeItem> results = new ArrayList<>();

        for (ParkingInfo info : infoList) {
            Optional<ParkingRealtime> latest = parkingRealtimeRepository
                    .findTopByPkltCdOrderByCollectedAtDesc(info.getPkltCd());

            // 만차이면 제외
            if (latest.isPresent() && Boolean.TRUE.equals(latest.get().getIsFull())) continue;

            int remaining = latest.map(r -> r.getRemaining() != null ? r.getRemaining() : 0).orElse(0);
            double distanceM = calcDistance(lat, lng, info.getLat(), info.getLng());
            int score = calcScore(remaining, info.getTpkct(), distanceM, info.getBscPrkCrg(), radius);
            int walkingMinutes = (int) Math.ceil(distanceM / WALKING_SPEED);

            results.add(RecommendResponse.AlternativeItem.builder()
                    .pkltCd(info.getPkltCd())
                    .name(info.getPkltNm())
                    .distanceM(distanceM)
                    .walkingMinutes(walkingMinutes)
                    .remaining(remaining)
                    .bscPrkCrg(info.getBscPrkCrg())
                    .recommendScore(score)
                    .reason(buildReason(remaining, walkingMinutes, info.getBscPrkCrg()))
                    .build());
        }

        // 점수 높은 순 정렬
        return results.stream()
                .sorted(Comparator.comparingInt(RecommendResponse.AlternativeItem::getRecommendScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // 추천 점수 계산
    private int calcScore(int remaining, Integer totalSpots, double distanceM,
                          Integer bscPrkCrg, int maxRadius) {

        // 잔여율 점수 (0~40)
        double remainScore = 0;
        if (totalSpots != null && totalSpots > 0) {
            remainScore = Math.min((double) remaining / totalSpots, 1.0) * 40;
        }

        // 거리 역산 점수 (0~35)
        double distScore = Math.max(0, (1.0 - distanceM / maxRadius)) * 35;

        // 요금 역산 점수 (0~25)
        double priceScore = 25;
        if (bscPrkCrg != null && bscPrkCrg > 0) {
            priceScore = Math.max(0, (1.0 - (double) bscPrkCrg / 2000)) * 25;
        }

        return (int) (remainScore + distScore + priceScore);
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