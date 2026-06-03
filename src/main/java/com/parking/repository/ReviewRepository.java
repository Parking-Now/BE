package com.parking.repository;

import com.parking.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 특정 주차장 리뷰 목록 (최신순)
    List<Review> findByPkltCdOrderByCreatedAtDesc(String pkltCd);

    // 특정 주차장 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.pkltCd = :pkltCd")
    Double findAvgRatingByPkltCd(@Param("pkltCd") String pkltCd);

    // pkltCd 목록에 대해 평균 평점 일괄 조회 (N+1 방지)
    @Query("SELECT r.pkltCd, AVG(r.rating) FROM Review r WHERE r.pkltCd IN :pkltCds GROUP BY r.pkltCd")
    List<Object[]> findAvgRatingsByPkltCds(@Param("pkltCds") List<String> pkltCds);

    // 특정 주차장 리뷰 수
    Long countByPkltCd(String pkltCd);
}