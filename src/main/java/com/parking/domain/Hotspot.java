package com.parking.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "hotspot")
@Getter
public class Hotspot {

    @Id
    @Column(name = "area_cd", nullable = false)
    private String areaCd;

    @Column(name = "area_nm")
    private String areaNm;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;
}
