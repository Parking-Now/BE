package com.parking.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 주차장
    PARKING_NOT_FOUND("P001", "해당 주차장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NO_ALTERNATIVES("P002", "주변에 주차 가능한 주차장이 없습니다.", HttpStatus.NOT_FOUND),

    // 요청
    INVALID_PARAMS("C001", "요청 파라미터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_LOCATION("C002", "유효하지 않은 좌표값입니다.", HttpStatus.BAD_REQUEST),

    // 외부 서비스
    EXTERNAL_API_ERROR("E001", "외부 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    PREDICT_SERVICE_ERROR("E002", "예측 서비스 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    API_QUOTA_EXCEEDED("E003", "API 일일 호출 한도를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus status;
}