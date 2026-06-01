package com.parking.controller;

import com.parking.dto.request.ReviewRequest;
import com.parking.dto.response.ApiResponse;
import com.parking.dto.response.ReviewResponse;
import com.parking.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{pkltCd}/reviews")
    public ResponseEntity<ApiResponse<Void>> createReview(
            @PathVariable String pkltCd,
            @RequestBody ReviewRequest request) {
        reviewService.createReview(pkltCd, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 리뷰 전체 목록 조회 (더보기 버튼 클릭 시)
    @GetMapping("/{pkltCd}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable String pkltCd) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getReviews(pkltCd)));
    }
}