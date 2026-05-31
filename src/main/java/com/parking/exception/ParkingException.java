package com.parking.exception;

import lombok.Getter;

@Getter
public class ParkingException extends RuntimeException {

    private final ErrorCode errorCode;

    public ParkingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}