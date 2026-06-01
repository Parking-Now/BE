package com.parking.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {
    private Integer id;
    private String nickname;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}