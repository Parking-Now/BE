package com.parking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    // 리뷰 고유 번호

    @Column(name = "pklt_cd", nullable = false)
    private String pkltCd;   // 주차장 코드 (어느 주차장의 리뷰인지)

    @Column(nullable = false)
    private String nickname;   // 닉네임(랜덤생성)

    @Column(nullable = false)
    private Integer rating;  // 별점

    private String content;  // 리뷰 내용

    @Column(name = "created_at")
    private LocalDateTime createdAt;   // 작성 시간 (자동입력)
}