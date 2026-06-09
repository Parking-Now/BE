package com.parking.repository;

import com.parking.domain.Hotspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HotspotRepository extends JpaRepository<Hotspot, String> {

    @Query(value = """
        SELECT *
        FROM hotspot
        WHERE lat BETWEEN :minLat AND :maxLat
          AND lng BETWEEN :minLng AND :maxLng
          AND 6371000 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(lat - :lat) / 2), 2)
                + COS(RADIANS(:lat)) * COS(RADIANS(lat))
                * POWER(SIN(RADIANS(lng - :lng) / 2), 2)
              )) <= 1000
        ORDER BY 6371000 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(lat - :lat) / 2), 2)
                + COS(RADIANS(:lat)) * COS(RADIANS(lat))
                * POWER(SIN(RADIANS(lng - :lng) / 2), 2)
              ))
        LIMIT 1
        """, nativeQuery = true)
    Optional<Hotspot> findNearestWithin1km(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}
