package com.parking.service;

import com.parking.domain.Review;
import com.parking.dto.request.ReviewRequest;
import com.parking.dto.response.ReviewResponse;
import com.parking.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 리뷰 작성
    public void createReview(String pkltCd, ReviewRequest request) {
        Review review = Review.builder()
                .pkltCd(pkltCd)
                .nickname("user_" + (1000 + new Random().nextInt(9000)))
                .rating(request.getRating())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);
    }

    // 리뷰 전체 목록 조회
    public List<ReviewResponse> getReviews(String pkltCd) {
        return reviewRepository.findByPkltCdOrderByCreatedAtDesc(pkltCd)
                .stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .nickname(r.getNickname())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}