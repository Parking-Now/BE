package com.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "parking_static",
        indexes = @Index(name = "idx_parking_static_location", columnList = "lat, lng"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingStatic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "manage_no")
    private String manageNo;

    @Column(name = "fclty_nm")
    private String fcltyNm;

    @Column(name = "addr")
    private String addr;

    @Column(name = "flag_nm")
    private String flagNm;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "tpkct")
    private Integer tpkct;

    @Column(name = "bsc_prk_crg")
    private Integer bscPrkCrg;

    @Column(name = "bsc_prk_hr")
    private Integer bscPrkHr;

    @Column(name = "wd_oper_bgng_tm")
    private String wdOperBgngTm;

    @Column(name = "wd_oper_end_tm")
    private String wdOperEndTm;

    @Column(name = "sat_oper_bgng_tm")
    private String satOperBgngTm;

    @Column(name = "sat_oper_end_tm")
    private String satOperEndTm;

    @Column(name = "sun_oper_bgng_tm")
    private String sunOperBgngTm;

    @Column(name = "sun_oper_end_tm")
    private String sunOperEndTm;

    @Column(name = "has_realtime")
    private Boolean hasRealtime;

    @Column(name = "pklt_cd")
    private String pkltCd;

    @Column(name = "card_yn")
    private Boolean cardYn;
}