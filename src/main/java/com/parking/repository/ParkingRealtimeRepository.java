package com.parking.repository;

import com.parking.domain.ParkingRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingRealtimeRepository extends JpaRepository<ParkingRealtime,Integer> {

    // 특정 주차장 최신 1건
    Optional<ParkingRealtime> findTopByPkltCdOrderByCollectedAtDesc(String pkltCd);

    // 최근 24시간 데이터 (혼잡도 그래프용)
    List<ParkingRealtime> findByPkltCdAndCollectedAtAfterOrderByCollectedAtAsc(
            String pkltCd,
            LocalDateTime since
    );

    // 예측용 - 과거 동일 요일 · 시간대 평균 잔여율
    @Query(value = """
        SELECT EXTRACT(HOUR FROM collected_at) AS hour,
               ROUND(AVG(remaining))           AS avg_remaining
        FROM parking_realtime
        WHERE pklt_cd = :pkltCd
          AND EXTRACT(DOW FROM collected_at) = EXTRACT(DOW FROM CAST(:arrivalTime AS timestamp))
          AND collected_at >= now() - INTERVAL '5 weeks'
        GROUP BY hour
        ORDER BY hour
        """, nativeQuery = true)
    List<Object[]> findAvgByDayOfWeekAndHour(
            @Param("pkltCd") String pkltCd,
            @Param("arrivalTime") String arrivalTime
    );
}