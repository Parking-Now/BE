package com.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Table(name = "citydata_realtime",
        indexes = @Index(name = "idx_citydata_area_cd_collected_at", columnList = "area_cd, collected_at"))
@Getter
public class CitydataRealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "area_cd", nullable = false)
    private String areaCd;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(name = "fcst_congest_lvl", nullable = false)
    private String fcstCongestLvl;
}
