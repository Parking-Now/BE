package com.parking.repository;

import com.parking.domain.ParkingStatic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ParkingStaticRepository extends JpaRepository<ParkingStatic, Integer> {

    // 좌표 반경 내 주차장 검색 (바운딩 박스 인덱스 선필터 → Haversine 정밀 필터)
    @Query(value = """
        SELECT *,
               (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) AS distance
        FROM parking_static
        WHERE lat BETWEEN :minLat AND :maxLat
          AND lng BETWEEN :minLng AND :maxLng
          AND (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) <= :radiusM
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<ParkingStatic> findWithinRadius(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusM") Integer radiusM,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );

    // 대체 추천용 - 제외 목록 빼고 반경 내 조회 (바운딩 박스 인덱스 선필터 → Haversine 정밀 필터)
    @Query(value = """
        SELECT *,
               (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) AS distance
        FROM parking_static
        WHERE has_realtime = true
          AND pklt_cd NOT IN (:excludePkltCds)
          AND lat BETWEEN :minLat AND :maxLat
          AND lng BETWEEN :minLng AND :maxLng
          AND (6371000 * acos(
                   cos(radians(:lat)) * cos(radians(lat))
                   * cos(radians(lng) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(lat))
               )) <= :radiusM
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<ParkingStatic> findAlternativesWithinRadius(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusM") Integer radiusM,
            @Param("excludePkltCds") List<String> excludePkltCds,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );

    Optional<ParkingStatic> findByPkltCd(String pkltCd);
}