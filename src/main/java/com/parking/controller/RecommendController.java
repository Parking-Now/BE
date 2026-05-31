/**
 * 대체 주차장 추천 컨트롤러
 * GET /api/v1/recommend/alternatives
 */
package com.parking.controller;

import com.parking.dto.request.RecommendRequest;
import com.parking.dto.response.ApiResponse;
import com.parking.dto.response.RecommendResponse;
import com.parking.service.RecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    // 만차 시 대체 주차장 추천 (기능 5)
    @GetMapping("/alternatives")
    public ResponseEntity<ApiResponse<RecommendResponse>> getAlternatives(
            @Valid RecommendRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(recommendService.getAlternatives(request))
        );
    }
}