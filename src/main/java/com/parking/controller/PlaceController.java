package com.parking.controller;

import com.parking.dto.request.PlaceSearchRequest;
import com.parking.dto.response.ApiResponse;
import com.parking.dto.response.PlaceInfoResponse;
import com.parking.service.PlaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/place")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<PlaceInfoResponse>> getPlaceInfo(
            @Valid @ModelAttribute PlaceSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.getPlaceInfo(request)));
    }

    // 사진 프록시 — API 키를 클라이언트에 노출하지 않고 Google 사진을 중계
    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(
            @NotBlank @RequestParam String name) {
        byte[] photo = placeService.getPhoto(name);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photo);
    }
}
