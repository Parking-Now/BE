package com.parking.dto.request;

import lombok.Getter;

@Getter
public class ReviewRequest {
    private Integer rating;   // 1~5
    private String content;
}