package com.parking.service;

import com.parking.adapter.PredictServiceAdapter;
import com.parking.domain.CitydataRealtime;
import com.parking.domain.Hotspot;
import com.parking.domain.ParkingRealtime;
import com.parking.domain.ParkingStatic;
import com.parking.dto.request.ParkingSearchRequest;
import com.parking.dto.response.ParkingDetailResponse;
import com.parking.dto.response.ParkingListItemDto;
import com.parking.dto.response.ParkingSearchResponse;
import com.parking.dto.response.ReviewResponse;
import com.parking.exception.ErrorCode;
import com.parking.exception.ParkingException;
import com.parking.repository.CitydataRealtimeRepository;
import com.parking.repository.HotspotRepository;
import com.parking.repository.ParkingRealtimeRepository;
import com.parking.repository.ParkingStaticRepository;
import com.parking.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingRealtimeRepository parkingRealtimeRepository;
    private final ParkingStaticRepository parkingStaticRepository;
    private final ReviewRepository reviewRepository;
    private final PredictServiceAdapter predictServiceAdapter;
    private final HotspotRepository hotspotRepository;
    private final CitydataRealtimeRepository citydataRealtimeRepository;

    // 목적지 주변 주차장 검색 (기능 1·2)
    public ParkingSearchResponse searchParking(ParkingSearchRequest request) {

        // 1. parking_static에서 반경 내 전체 주차장 조회
        // 바운딩 박스(사각형)로 인덱스 선필터 후 Haversine 정밀 필터
        double latDelta = request.getRadius() / 111_000.0;
        double lngDelta = request.getRadius() / (111_000.0 * Math.cos(Math.toRadians(request.getLat())));

        List<ParkingStatic> staticList = parkingStaticRepository.findWithinRadius(
                request.getLat(), request.getLng(), request.getRadius(),
                request.getLat() - latDelta, request.getLat() + latDelta,
                request.getLng() - lngDelta, request.getLng() + lngDelta);

        if (staticList.isEmpty()) {
            return ParkingSearchResponse.builder()
                    .recommendTransit(false)
                    .parkingList(Collections.emptyList())
                    .build();
        }

        // 2. 실시간 데이터 일괄 조회 → Map으로 변환 (N+1 방지)
        List<String> realtimePkltCds = staticList.stream()
                .filter(p -> Boolean.TRUE.equals(p.getHasRealtime()) && p.getPkltCd() != null)
                .map(ParkingStatic::getPkltCd)
                .collect(Collectors.toList());

        Map<String, ParkingRealtime> realtimeMap = realtimePkltCds.isEmpty()
                ? Collections.emptyMap()
                : parkingRealtimeRepository.findLatestByPkltCds(realtimePkltCds).stream()
                        .collect(Collectors.toMap(ParkingRealtime::getPkltCd, rt -> rt));

        // 3. 평균 평점 일괄 조회 → Map으로 변환 (N+1 방지)
        List<String> allPkltCds = staticList.stream()
                .map(p -> p.getPkltCd() != null ? p.getPkltCd() : "static_" + p.getId())
                .collect(Collectors.toList());

        Map<String, Double> avgRatingMap = reviewRepository.findAvgRatingsByPkltCds(allPkltCds).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).doubleValue()
                ));

        // 4. DTO 변환 (DB 조회 없이 Map 룩업)
        List<ParkingListItemDto> results = staticList.stream().map(p -> {

            String pkltCd = p.getPkltCd() != null ? p.getPkltCd() : "static_" + p.getId();

            ParkingListItemDto.ParkingListItemDtoBuilder builder = ParkingListItemDto.builder()
                    .pkltCd(pkltCd)
                    .name(p.getFcltyNm())
                    .addr(p.getAddr())
                    .parkingType(p.getFlagNm())
                    .lat(p.getLat())
                    .lng(p.getLng())
                    .tpkct(p.getTpkct())
                    .bscPrkCrg(p.getBscPrkCrg())
                    .bscPrkHr(p.getBscPrkHr())
                    .wdOperBgngTm(convertOperTime(p.getWdOperBgngTm()))
                    .wdOperEndTm(convertOperTime(p.getWdOperEndTm()))
                    .satOperBgngTm(convertOperTime(p.getSatOperBgngTm()))
                    .satOperEndTm(convertOperTime(p.getSatOperEndTm()))
                    .sunOperBgngTm(convertOperTime(p.getSunOperBgngTm()))
                    .sunOperEndTm(convertOperTime(p.getSunOperEndTm()))
                    .distanceM(calcDistance(request.getLat(), request.getLng(), p.getLat(), p.getLng()))
                    .hasRealtime(p.getHasRealtime())
                    .cardYn(p.getCardYn())
                    .evChargingYn(p.getEvChargingYn())
                    .disabledYn(p.getDisabledYn());

            // 실시간 데이터 붙이기 (Map 룩업)
            ParkingRealtime rt = realtimeMap.get(p.getPkltCd());
            if (rt != null) {
                builder.remaining(rt.getRemaining())
                        .isFull(rt.getIsFull())
                        .nowPrkVhclCnt(rt.getNowPrkVhclCnt());
            }

            // 평균 평점 붙이기 (Map 룩업)
            Double avg = avgRatingMap.get(pkltCd);
            builder.avgRating(avg != null ? Math.round(avg * 10) / 10.0 : 0.0);

            return builder.build();
        }).collect(Collectors.toList());

        // TODO(임시-발표테스트): pkltCd 1366593 만차 강제 처리 — 발표 후 삭제
        results.stream()
                .filter(dto -> "1366593".equals(dto.getPkltCd()))
                .forEach(dto -> {
                    dto.setIsFull(true);
                    dto.setRemaining(0);
                });

        // AI 서버 호출해서 예측값 붙이기
        if (request.getArrivalTime() != null) {
            List<String> pkltCds = staticList.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getHasRealtime()) && p.getPkltCd() != null)
                    .map(ParkingStatic::getPkltCd)
                    .collect(Collectors.toList());

            if (!pkltCds.isEmpty()) {
                List<Map<String, Object>> predictions = predictServiceAdapter
                        .requestPrediction(pkltCds, request.getArrivalTime());

                Map<String, Map<String, Object>> predictionMap = predictions.stream()
                        .collect(Collectors.toMap(
                                p -> (String) p.get("pkltCd"),
                                p -> p,
                                (existing, replacement) -> replacement
                        ));

                results.forEach(dto -> {
                    Map<String, Object> pred = predictionMap.get(dto.getPkltCd());
                    if (pred != null) {
                        Integer predictedRemaining = (Integer) pred.get("predictedRemaining");
                        String congestionLevel = (String) pred.get("congestionLevel");
                        dto.setPredictedRemaining(predictedRemaining);
                        dto.setCongestionLevel(congestionLevel);
                        dto.setRecommendTransit("HIGH".equals(congestionLevel));
                    }
                });
            }
        }

        // 만차 제외 옵션
        if (Boolean.TRUE.equals(request.getExcludeFull())) {
            results = results.stream()
                    .filter(dto -> !Boolean.TRUE.equals(dto.getIsFull()))
                    .collect(Collectors.toList());
        }

        // 정렬
        List<ParkingListItemDto> sorted = sortParking(results, request.getSort());

        boolean recommendTransit = determineRecommendTransit(request, sorted);

        return ParkingSearchResponse.builder()
                .recommendTransit(recommendTransit)
                .parkingList(sorted)
                .build();
    }

    private boolean determineRecommendTransit(ParkingSearchRequest request, List<ParkingListItemDto> results) {
        double delta1km = 1000.0 / 111_000.0;
        double lngDelta1km = 1000.0 / (111_000.0 * Math.cos(Math.toRadians(request.getLat())));

        Optional<Hotspot> hotspotOpt = hotspotRepository.findNearestWithin1km(
                request.getLat(), request.getLng(),
                request.getLat() - delta1km, request.getLat() + delta1km,
                request.getLng() - lngDelta1km, request.getLng() + lngDelta1km);

        if (hotspotOpt.isPresent()) {
            String areaCd = hotspotOpt.get().getAreaCd();
            LocalDateTime arrivalTime = request.getArrivalTime() != null
                    ? LocalDateTime.parse(request.getArrivalTime())
                    : null;

            if (arrivalTime == null) {
                return citydataRealtimeRepository.findLatestByAreaCd(areaCd)
                        .map(r -> isCongested(r.getFcstCongestLvl()))
                        .orElse(false);
            }

            long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), arrivalTime);

            if (hoursUntil <= 12) {
                return citydataRealtimeRepository.findNearestToTime(areaCd, arrivalTime)
                        .map(r -> isCongested(r.getFcstCongestLvl()))
                        .orElse(false);
            } else {
                List<CitydataRealtime> historical = citydataRealtimeRepository
                        .findByAreaCdAndDayOfWeekAndHour(areaCd, arrivalTime);
                if (historical.isEmpty()) return false;
                long congestedCount = historical.stream()
                        .filter(r -> isCongested(r.getFcstCongestLvl()))
                        .count();
                return (double) congestedCount / historical.size() >= 0.5;
            }
        }

        // fallback: 절반 이상 HIGH면 true
        if (results.isEmpty()) return false;
        long highCount = results.stream()
                .filter(dto -> "HIGH".equals(dto.getCongestionLevel()))
                .count();
        return (double) highCount / results.size() >= 0.5;
    }

    private boolean isCongested(String level) {
        return "붐빔".equals(level);
    }

    // 주차장 상세 정보 조회 (기능 3)
    public ParkingDetailResponse getDetail(String pkltCd, Double lat, Double lng) {

        // static_ 접두사면 id로 조회, 아니면 pklt_cd로 조회
        ParkingStatic parking;
        if (pkltCd.startsWith("static_")) {
            Integer staticId = Integer.parseInt(pkltCd.replace("static_", ""));
            parking = parkingStaticRepository.findById(staticId)
                    .orElseThrow(() -> new ParkingException(ErrorCode.PARKING_NOT_FOUND));
        } else {
            parking = parkingStaticRepository.findByPkltCd(pkltCd)
                    .orElseThrow(() -> new ParkingException(ErrorCode.PARKING_NOT_FOUND));
        }

        String resolvedPkltCd = parking.getPkltCd() != null ? parking.getPkltCd() : "static_" + parking.getId();

        // 실시간 데이터
        var latest = Boolean.TRUE.equals(parking.getHasRealtime()) && parking.getPkltCd() != null
                ? parkingRealtimeRepository.findTopByPkltCdOrderByCollectedAtDesc(parking.getPkltCd()).orElse(null)
                : null;

        // 혼잡도 그래프
        int[] hourlyAvg = new int[24];
        if (Boolean.TRUE.equals(parking.getHasRealtime()) && parking.getPkltCd() != null) {
            String todayForDayOfWeek = LocalDateTime.now().toString();
            List<Object[]> avgRows = parkingRealtimeRepository
                    .findAvgByDayOfWeekAndHour(parking.getPkltCd(), todayForDayOfWeek);

            for (Object[] row : avgRows) {
                int hour = ((Number) row[0]).intValue();
                int avg = ((Number) row[1]).intValue();
                int congestionRate = parking.getTpkct() != null && parking.getTpkct() > 0
                        ? (int) ((1.0 - (double) avg / parking.getTpkct()) * 100)
                        : 0;
                hourlyAvg[hour] = congestionRate;
            }
        }

        List<Integer> hourlyList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourlyList.add(hourlyAvg[i]);
        }

        // 현재 혼잡률
        int currentRate = 0;
        if (latest != null && parking.getTpkct() != null && parking.getTpkct() > 0
                && latest.getNowPrkVhclCnt() != null) {
            currentRate = (int) ((double) latest.getNowPrkVhclCnt() / parking.getTpkct() * 100);
        }

        // 리뷰 데이터
        Double avgRating = reviewRepository.findAvgRatingByPkltCd(resolvedPkltCd);
        Long reviewCount = reviewRepository.countByPkltCd(resolvedPkltCd);
        List<ReviewResponse> recentReviews = reviewRepository
                .findByPkltCdOrderByCreatedAtDesc(resolvedPkltCd)
                .stream().limit(3)
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .nickname(r.getNickname())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // TODO(임시-발표테스트): pkltCd 1366593 만차 강제 처리 — 발표 후 삭제
        Integer detailRemaining = latest != null ? latest.getRemaining() : null;
        Boolean detailIsFull = latest != null ? latest.getIsFull() : null;
        if ("1366593".equals(resolvedPkltCd)) {
            detailRemaining = 0;
            detailIsFull = true;
        }

        return ParkingDetailResponse.builder()
                .pkltCd(resolvedPkltCd)
                .name(parking.getFcltyNm())
                .addr(parking.getAddr())
                .tpkct(parking.getTpkct())
                .bscPrkCrg(parking.getBscPrkCrg())
                .bscPrkHr(parking.getBscPrkHr())
                .cardYn(parking.getCardYn())
                .evChargingYn(parking.getEvChargingYn())
                .disabledYn(parking.getDisabledYn())
                .wdOperBgngTm(convertOperTime(parking.getWdOperBgngTm()))
                .wdOperEndTm(convertOperTime(parking.getWdOperEndTm()))
                .satOperBgngTm(convertOperTime(parking.getSatOperBgngTm()))
                .satOperEndTm(convertOperTime(parking.getSatOperEndTm()))
                .sunOperBgngTm(convertOperTime(parking.getSunOperBgngTm()))
                .sunOperEndTm(convertOperTime(parking.getSunOperEndTm()))
                .distanceM(lat != null && lng != null
                        ? calcDistance(lat, lng, parking.getLat(), parking.getLng())
                        : null)
                .hasRealtime(parking.getHasRealtime())
                .remaining(detailRemaining)
                .isFull(detailIsFull)
                .nowPrkVhclCnt(latest != null ? latest.getNowPrkVhclCnt() : null)
                .congestion(ParkingDetailResponse.CongestionInfo.builder()
                        .currentRate(currentRate)
                        .hourly(hourlyList)
                        .build())
                .avgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0)
                .reviewCount(reviewCount)
                .recentReviews(recentReviews)
                .build();
    }

    // 정렬 처리
    private List<ParkingListItemDto> sortParking(List<ParkingListItemDto> list, String sort) {

        if ("distance".equals(sort)) {
            return list.stream()
                    .sorted(Comparator.comparingDouble(dto ->
                            dto.getDistanceM() != null ? dto.getDistanceM() : Double.MAX_VALUE))
                    .collect(Collectors.toList());
        }

        if ("price".equals(sort)) {
            return list.stream()
                    .sorted(Comparator.comparingInt(dto ->
                            dto.getBscPrkCrg() != null ? dto.getBscPrkCrg() : Integer.MAX_VALUE))
                    .collect(Collectors.toList());
        }

        // 기본값 score
        return list.stream()
                .sorted(Comparator
                        .comparingInt((ParkingListItemDto dto) -> {
                            if (Boolean.FALSE.equals(dto.getIsFull())) return 0;
                            if (dto.getIsFull() == null) return 1;
                            return 2;
                        })
                        .thenComparingInt(dto -> -calcSearchScore(dto)))
                .collect(Collectors.toList());
    }

    // Haversine 공식
    private double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // 운영시간 형식 변환
    private String convertOperTime(String time) {
        if (time == null) return null;
        try {
            boolean isPm = time.startsWith("오후");
            String timePart = time.replace("오전 ", "").replace("오후 ", "");
            String[] parts = timePart.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (isPm && hour != 12) hour += 12;
            if (!isPm && hour == 12) hour = 0;

            return String.format("%02d%02d", hour, minute);
        } catch (Exception e) {
            return time;
        }
    }

    // 검색 결과 종합 점수 계산
    private int calcSearchScore(ParkingListItemDto dto) {
        double distScore = 0;
        if (dto.getDistanceM() != null) {
            distScore = Math.max(0, (1.0 - dto.getDistanceM() / 2000)) * 60;
        }
        double priceScore = 40;
        if (dto.getBscPrkCrg() != null && dto.getBscPrkCrg() > 0) {
            priceScore = Math.max(0, (1.0 - (double) dto.getBscPrkCrg() / 2000)) * 40;
        }
        return (int) (distScore + priceScore);
    }

    // 혼잡도 레벨 계산
    private String calcCongestionLevel(Integer totalSpots, int avgRemaining) {
        if (avgRemaining < 0 || totalSpots == null || totalSpots == 0) return "UNKNOWN";
        double rate = (double) avgRemaining / totalSpots;
        if (rate >= 0.5) return "LOW";
        if (rate >= 0.2) return "MEDIUM";
        return "HIGH";
    }
}