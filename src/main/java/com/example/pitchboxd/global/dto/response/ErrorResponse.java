package com.example.pitchboxd.global.dto.response;


import com.example.pitchboxd.global.exception.ErrorCode;

public record ErrorResponse(
        String code,
        String message,
        int status
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errorCode.getStatus().value());
    }
}
