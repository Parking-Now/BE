package com.parking.repository;

import com.parking.domain.ParkingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ParkingInfoRepository extends JpaRepository<ParkingInfo, String> {

    // 좌표 반경 내 주차장 검색
    @Query(value = """
        SELECT *,
               (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) AS distance
        FROM parking_info
        WHERE (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) <= :radiusM
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<ParkingInfo> findWithinRadius(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusM") Integer radiusM
    );

    // 대체 추천용 - 제외 목록 빼고 반경 내 조회
    @Query(value = """
        SELECT *,
               (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) AS distance
        FROM parking_info
        WHERE pklt_cd NOT IN (:excludePkltCds)
          AND (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) <= :radiusM
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<ParkingInfo> findAlternativesWithinRadius(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusM") Integer radiusM,
            @Param("excludePkltCds") List<String> excludePkltCds
    );

    Optional<ParkingInfo> findByPkltCd(String pkltCd);
}