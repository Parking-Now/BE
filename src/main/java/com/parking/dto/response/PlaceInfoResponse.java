package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PlaceInfoResponse {
    private String name;
    private String address;
    private String phoneNumber;
    private Boolean isOpenNow;
    private List<String> weekdayDescriptions; // "월요일: 08:00~22:00" 형태
    private ParkingOptions parkingOptions;
    private List<String> photoUrls;

    @Getter
    @Builder
    public static class ParkingOptions {
        private Boolean freeParkingLot;
        private Boolean paidParkingLot;
        private Boolean streetParking;
        private Boolean valetParking;
        private Boolean freeStreetParking;
    }
}