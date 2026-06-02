// 삭제 가능
package com.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "parking_info",
        indexes = @Index(name = "idx_parking_info_location", columnList = "lat, lng"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingInfo {

    @Id
    @Column(name = "pklt_cd")
    private String pkltCd;

    @Column(name = "pklt_nm")
    private String pkltNm;

    @Column(name = "addr")
    private String addr;

    @Column(name = "pay_yn")
    private String payYn;

    @Column(name = "bsc_prk_crg")
    private Integer bscPrkCrg;

    @Column(name = "bsc_prk_hr")
    private Integer bscPrkHr;

    @Column(name = "add_prk_crg")
    private Integer addPrkCrg;

    @Column(name = "wd_oper_bgng_tm")
    private String wdOperBgngTm;

    @Column(name = "wd_oper_end_tm")
    private String wdOperEndTm;

    @Column(name = "we_oper_bgng_tm")
    private String weOperBgngTm;

    @Column(name = "we_oper_end_tm")
    private String weOperEndTm;

    @Column(name = "tpkct")
    private Integer tpkct;

    @Column(name = "parking_type")
    private String parkingType;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;
}