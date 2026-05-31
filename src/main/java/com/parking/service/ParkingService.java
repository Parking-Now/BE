
/**
 * 주차장 검색 · 상세 조회 비즈니스 로직
 * parking_info · parking_static 두 테이블 결과를 합쳐서 반환
 * has_realtime 여부에 따라 실시간 데이터 포함 여부 결정
 */
package com.parking.service;

import com.parking.domain.ParkingInfo;
import com.parking.domain.ParkingStatic;
import com.parking.dto.request.ParkingSearchRequest;
import com.parking.dto.response.ParkingDetailResponse;
import com.parking.dto.response.ParkingListItemDto;
import com.parking.exception.ErrorCode;
import com.parking.exception.ParkingException;
import com.parking.repository.ParkingInfoRepository;
import com.parking.repository.ParkingRealtimeRepository;
import com.parking.repository.ParkingStaticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingInfoRepository parkingInfoRepository;
    private final ParkingRealtimeRepository parkingRealtimeRepository;
    private final ParkingStaticRepository parkingStaticRepository;

    // 목적지 주변 주차장 검색 (기능 1·2)
    public List<ParkingListItemDto> searchParking(ParkingSearchRequest request) {

        // 1. 두 테이블에서 반경 내 후보 조회
        List<ParkingInfo> infoList = parkingInfoRepository.findWithinRadius(
                request.getLat(), request.getLng(), request.getRadius());

        List<ParkingStatic> staticList = parkingStaticRepository.findWithinRadius(
                request.getLat(), request.getLng(), request.getRadius());

        // 2. parking_info → DTO 변환 (실시간 데이터 포함)
        List<ParkingListItemDto> infoResults = infoList.stream().map(info -> {

            ParkingListItemDto.ParkingListItemDtoBuilder builder = ParkingListItemDto.builder()
                    .pkltCd(info.getPkltCd())
                    .name(info.getPkltNm())
                    .addr(info.getAddr())
                    .parkingType(convertParkingType(info.getParkingType()))
                    .lat(info.getLat())
                    .lng(info.getLng())
                    .tpkct(info.getTpkct())
                    .bscPrkCrg(info.getBscPrkCrg())
                    .bscPrkHr(info.getBscPrkHr())
                    .addPrkCrg(info.getAddPrkCrg())
                    .wdOperBgngTm(info.getWdOperBgngTm())
                    .wdOperEndTm(info.getWdOperEndTm())
                    .weOperBgngTm(info.getWeOperBgngTm())
                    .weOperEndTm(info.getWeOperEndTm())
                    .distanceM(calcDistance(request.getLat(), request.getLng(), info.getLat(), info.getLng())) // ← 추가
                    .hasRealtime(true);

            // 실시간 잔여 공간 붙이기
            parkingRealtimeRepository
                    .findTopByPkltCdOrderByCollectedAtDesc(info.getPkltCd())
                    .ifPresent(rt -> builder
                            .remaining(rt.getRemaining())
                            .isFull(rt.getIsFull())
                            .nowPrkVhclCnt(rt.getNowPrkVhclCnt()));

            // 도착 예정 시간 있으면 예측값 추가(도착 예정 시간대에 혼잡도 예측) -> AI 모델 연결 시 여기 수정
            if (request.getArrivalTime() != null) {
                List<Object[]> avgRows = parkingRealtimeRepository
                        .findAvgByDayOfWeekAndHour(info.getPkltCd(), request.getArrivalTime());

                int predictedRemaining = avgRows.stream()
                        .filter(row -> ((Number) row[0]).intValue() ==
                                LocalDateTime.parse(request.getArrivalTime()).getHour())
                        .map(row -> ((Number) row[1]).intValue())
                        .findFirst()
                        .orElse(-1);

                String congestionLevel = calcCongestionLevel(info.getTpkct(), predictedRemaining);
                builder.predictedRemaining(predictedRemaining)
                        .congestionLevel(congestionLevel)
                        .recommendTransit("HIGH".equals(congestionLevel));
            }

            return builder.build();
        }).collect(Collectors.toList());

        // 3. parking_static → DTO 변환 (has_realtime=false인 것만)
        List<ParkingListItemDto> staticResults = staticList.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getHasRealtime()))
                .map(p -> ParkingListItemDto.builder()
                        .pkltCd(p.getPkltCd() != null ? p.getPkltCd() : "static_" + p.getId())
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
                        .weOperBgngTm(convertOperTime(p.getSatOperBgngTm()))
                        .weOperEndTm(convertOperTime(p.getSatOperEndTm()))
                        .distanceM(calcDistance(request.getLat(), request.getLng(), p.getLat(), p.getLng())) // ← 수정
                        .hasRealtime(false)
                        .remaining(null)
                        .isFull(null)
                        .nowPrkVhclCnt(null)
                        .build())
                .collect(Collectors.toList());

        // 4. 합치기
        List<ParkingListItemDto> combined = new ArrayList<>();
        combined.addAll(infoResults);
        combined.addAll(staticResults);

        // 만차 제외 옵션
        if (Boolean.TRUE.equals(request.getExcludeFull())) {
            combined = combined.stream()
                    .filter(dto -> !Boolean.TRUE.equals(dto.getIsFull()))
                    .collect(Collectors.toList());
        }

        // 5. 정렬
        return sortParking(combined, request.getSort());
    }

    // 주차장 상세 정보 조회 (기능 3)
    public ParkingDetailResponse getDetail(String pkltCd) {

        // static_ 접두사면 parking_static에서 조회
        if (pkltCd.startsWith("static_")) {
            Integer staticId = Integer.parseInt(pkltCd.replace("static_", ""));
            return parkingStaticRepository.findById(staticId)
                    .map(p -> ParkingDetailResponse.builder()
                            .pkltCd("static_" + p.getId())
                            .name(p.getFcltyNm())
                            .addr(p.getAddr())
                            .tpkct(p.getTpkct())
                            .payYn(null)
                            .bscPrkCrg(p.getBscPrkCrg())
                            .bscPrkHr(p.getBscPrkHr())
                            .addPrkCrg(null)
                            .wdOperBgngTm(convertOperTime(p.getWdOperBgngTm()))
                            .wdOperEndTm(convertOperTime(p.getWdOperEndTm()))
                            .weOperBgngTm(convertOperTime(p.getSatOperBgngTm()))
                            .weOperEndTm(convertOperTime(p.getSatOperEndTm()))
                            .remaining(null)
                            .isFull(null)
                            .nowPrkVhclCnt(null)
                            .congestion(ParkingDetailResponse.CongestionInfo.builder()
                                    .currentRate(0)
                                    .hourly(new ArrayList<>(java.util.Collections.nCopies(24, 0)))
                                    .build())
                            .build())
                    .orElseThrow(() -> new ParkingException(ErrorCode.PARKING_NOT_FOUND));
        }

        // parking_info에서 조회
        return parkingInfoRepository.findByPkltCd(pkltCd)
                .map(info -> {

                    // 실시간 최신 1건
                    var latest = parkingRealtimeRepository
                            .findTopByPkltCdOrderByCollectedAtDesc(pkltCd)
                            .orElse(null);

                    // 과거 동일 요일 시간대별 평균으로 혼잡도 그래프 계산
                    String todayForDayOfWeek = LocalDateTime.now().toString();
                    List<Object[]> avgRows = parkingRealtimeRepository
                            .findAvgByDayOfWeekAndHour(pkltCd, todayForDayOfWeek);

                    // 시간대별 평균 잔여 공간 매핑
                    int[] hourlyAvg = new int[24];
                    for (Object[] row : avgRows) {
                        int hour = ((Number) row[0]).intValue();
                        int avg = ((Number) row[1]).intValue();
                        hourlyAvg[hour] = avg;
                    }
                    List<Integer> hourlyList = new ArrayList<>();
                    for (int i = 0; i < 24; i++) {
                        hourlyList.add(hourlyAvg[i]);
                    }

                    // 현재 혼잡률 계산
                    int currentRate = 0;
                    if (latest != null && info.getTpkct() != null && info.getTpkct() > 0
                            && latest.getNowPrkVhclCnt() != null) {
                        currentRate = (int) ((double) latest.getNowPrkVhclCnt() / info.getTpkct() * 100);
                    }

                    return ParkingDetailResponse.builder()
                            .pkltCd(info.getPkltCd())
                            .name(info.getPkltNm())
                            .addr(info.getAddr())
                            .tpkct(info.getTpkct())
                            .payYn(info.getPayYn())
                            .bscPrkCrg(info.getBscPrkCrg())
                            .bscPrkHr(info.getBscPrkHr())
                            .addPrkCrg(info.getAddPrkCrg())
                            .wdOperBgngTm(info.getWdOperBgngTm())
                            .wdOperEndTm(info.getWdOperEndTm())
                            .weOperBgngTm(info.getWeOperBgngTm())
                            .weOperEndTm(info.getWeOperEndTm())
                            .remaining(latest != null ? latest.getRemaining() : null)
                            .isFull(latest != null ? latest.getIsFull() : null)
                            .nowPrkVhclCnt(latest != null ? latest.getNowPrkVhclCnt() : null)
                            .congestion(ParkingDetailResponse.CongestionInfo.builder()
                                    .currentRate(currentRate)
                                    .hourly(hourlyList)
                                    .build())
                            .build();
                })
                .orElseThrow(() -> new ParkingException(ErrorCode.PARKING_NOT_FOUND));
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

        // 기본값 score : 만차 맨 뒤, 그 안에서 거리+요금 점수순
        return list.stream()
                .sorted(Comparator
                        .comparingInt((ParkingListItemDto dto) -> {
                            if (Boolean.FALSE.equals(dto.getIsFull())) return 0;
                            if (dto.getIsFull() == null) return 1;
                            return 2;
                        })
                        .thenComparingInt(dto -> calcSearchScore(dto)))
                .collect(Collectors.toList());
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

    // parking_info parkingType 한글 변환
    private String convertParkingType(String parkingType) {
        if (parkingType == null) return null;
        switch (parkingType.toLowerCase()) {
            case "public": return "공영";
            case "private": return "민영";
            case "other": return "기타";
            default: return parkingType;
        }
    }

    // 운영시간 형식 변환 ("오전 12:00:00" → "0000")
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
            return time; // 변환 실패시 원본 반환
        }
    }

    // 검색 결과 종합 점수 계산 (거리 60% + 요금 40%)
    private int calcSearchScore(ParkingListItemDto dto) {

        // 거리 점수 (0~60)
        double distScore = 0;
        if (dto.getDistanceM() != null) {
            distScore = Math.max(0, (1.0 - dto.getDistanceM() / 2000)) * 60;
        }

        // 요금 점수 (0~40)
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