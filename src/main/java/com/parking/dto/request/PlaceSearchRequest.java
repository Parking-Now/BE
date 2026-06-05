package com.parking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceSearchRequest {

    @NotBlank(message = "장소 이름은 필수입니다.")
    private String name;

    @NotNull(message = "위도는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다.")
    private Double lng;
}
