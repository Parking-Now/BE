package com.parking.repository;

import com.parking.domain.CitydataRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitydataRealtimeRepository extends JpaRepository<CitydataRealtime, Integer> {

    // 해당 area_cd의 가장 최신 레코드
    @Query(value = """
        SELECT *
        FROM citydata_realtime
        WHERE area_cd = :areaCd
        ORDER BY collected_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<CitydataRealtime> findLatestByAreaCd(@Param("areaCd") String areaCd);

    // arrivalTime에 가장 가까운 collected_at의 레코드
    @Query(value = """
        SELECT *
        FROM citydata_realtime
        WHERE area_cd = :areaCd
        ORDER BY ABS(EXTRACT(EPOCH FROM (collected_at - CAST(:targetTime AS timestamp))))
        LIMIT 1
        """, nativeQuery = true)
    Optional<CitydataRealtime> findNearestToTime(
            @Param("areaCd") String areaCd,
            @Param("targetTime") LocalDateTime targetTime
    );

    // 과거 동일 요일·시간대 레코드 전체 조회 (붐빔 비율 계산용)
    @Query(value = """
        SELECT *
        FROM citydata_realtime
        WHERE area_cd = :areaCd
          AND EXTRACT(DOW FROM collected_at) = EXTRACT(DOW FROM CAST(:targetTime AS timestamp))
          AND EXTRACT(HOUR FROM collected_at) = EXTRACT(HOUR FROM CAST(:targetTime AS timestamp))
        """, nativeQuery = true)
    List<CitydataRealtime> findByAreaCdAndDayOfWeekAndHour(
            @Param("areaCd") String areaCd,
            @Param("targetTime") LocalDateTime targetTime
    );
}
