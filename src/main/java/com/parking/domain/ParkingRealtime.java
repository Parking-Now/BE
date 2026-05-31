package com.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_realtime",
        indexes = @Index(name = "idx_pklt_cd_collected_at", columnList = "pklt_cd, collected_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingRealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pklt_cd", nullable = false)
    private String pkltCd;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(name = "now_prk_vhcl_cnt")
    private Integer nowPrkVhclCnt;

    @Column(name = "remaining")
    private Integer remaining;

    @Column(name = "is_full")
    private Boolean isFull;
}