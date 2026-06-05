package com.parking.service;

import com.parking.dto.request.PlaceSearchRequest;
import com.parking.dto.response.PlaceInfoResponse;
import com.parking.exception.ErrorCode;
import com.parking.exception.ParkingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final WebClient webClient;

    @Value("${google.places.api-key}")
    private String apiKey;

    private static final String PLACES_BASE_URL = "https://places.googleapis.com/v1";

    private static final String FIELD_MASK =
            "displayName,formattedAddress,internationalPhoneNumber," +
                    "currentOpeningHours,regularOpeningHours,parkingOptions,photos";

    public PlaceInfoResponse getPlaceInfo(PlaceSearchRequest request) {
        // 1단계: 텍스트 검색으로 place_id 획득
        Map<String, Object> searchBody = Map.of(
                "textQuery", request.getName(),
                "languageCode", "ko",
                "locationBias", Map.of(
                        "circle", Map.of(
                                "center", Map.of("latitude", request.getLat(), "longitude", request.getLng()),
                                "radius", 500.0
                        )
                )
        );

        Map response = webClient.post()
                .uri(PLACES_BASE_URL + "/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.id")
                .bodyValue(searchBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> places = (List<Map>) response.get("places");
        if (places == null || places.isEmpty()) {
            throw new ParkingException(ErrorCode.PLACE_NOT_FOUND);
        }

        String placeId = (String) places.get(0).get("id");

        // 2단계: place_id로 상세정보 조회
        Map detail = webClient.get()
                .uri(PLACES_BASE_URL + "/places/" + placeId + "?languageCode=ko")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", FIELD_MASK)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return mapToResponse(detail);
    }

    // 사진 프록시: API 키를 클라이언트에 노출하지 않기 위해 백엔드에서 중계
    public byte[] getPhoto(String photoName) {
        return webClient.get()
                .uri(PLACES_BASE_URL + "/" + photoName + "/media?maxHeightPx=400&skipHttpRedirect=true")
                .header("X-Goog-Api-Key", apiKey)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    private PlaceInfoResponse mapToResponse(Map detail) {
        // displayName
        String name = null;
        Map displayName = (Map) detail.get("displayName");
        if (displayName != null) name = (String) displayName.get("text");

        // formattedAddress
        String address = (String) detail.get("formattedAddress");

        // phoneNumber
        String phone = (String) detail.get("internationalPhoneNumber");

        // openNow + weekdayDescriptions
        Boolean isOpenNow = null;
        List<String> weekdayDescriptions = null;
        Map currentOpeningHours = (Map) detail.get("currentOpeningHours");
        if (currentOpeningHours != null) {
            isOpenNow = (Boolean) currentOpeningHours.get("openNow");
            weekdayDescriptions = (List<String>) currentOpeningHours.get("weekdayDescriptions");
        }

        // parkingOptions
        PlaceInfoResponse.ParkingOptions parkingOptions = null;
        Map parking = (Map) detail.get("parkingOptions");
        if (parking != null) {
            parkingOptions = PlaceInfoResponse.ParkingOptions.builder()
                    .freeParkingLot((Boolean) parking.get("freeParkingLot"))
                    .paidParkingLot((Boolean) parking.get("paidParkingLot"))
                    .streetParking((Boolean) parking.get("streetParking"))
                    .valetParking((Boolean) parking.get("valetParking"))
                    .freeStreetParking((Boolean) parking.get("freeStreetParking"))
                    .build();
        }

        // photos (최대 3장) — API 키 노출 방지를 위해 백엔드 프록시 URL로 변환
        List<String> photoUrls = null;
        List<Map> photos = (List<Map>) detail.get("photos");
        if (photos != null) {
            photoUrls = photos.stream()
                    .limit(3)
                    .map(p -> "/api/v1/place/photo?name=" +
                            URLEncoder.encode((String) p.get("name"), StandardCharsets.UTF_8))
                    .toList();
        }

        return PlaceInfoResponse.builder()
                .name(name)
                .address(address)
                .phoneNumber(phone)
                .isOpenNow(isOpenNow)
                .weekdayDescriptions(weekdayDescriptions)
                .parkingOptions(parkingOptions)
                .photoUrls(photoUrls)
                .build();
    }
}
