/**
 * 주차장 검색 · 상세 조회 컨트롤러
 * GET /api/v1/parking/search
 * GET /api/v1/parking/{pkltCd}
 */
package com.parking.controller;

import com.parking.dto.request.ParkingSearchRequest;
import com.parking.dto.response.ApiResponse;
import com.parking.dto.response.ParkingDetailResponse;
import com.parking.dto.response.ParkingSearchResponse;
import com.parking.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    // 목적지 주변 주차장 검색 (기능 1·2)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ParkingSearchResponse>> searchParking(
            @Valid ParkingSearchRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(parkingService.searchParking(request))
        );
    }

    // 주차장 상세 조회 (기능 3)
    @GetMapping("/{pkltCd}")
    public ResponseEntity<ApiResponse<ParkingDetailResponse>> getDetail(
            @PathVariable String pkltCd,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(parkingService.getDetail(pkltCd, lat, lng))
        );
    }
}