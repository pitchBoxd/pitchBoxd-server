package com.example.pitchboxd.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("G-001", "오류가 발생했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("G-002", "유효하지 않은 입력입니다.", HttpStatus.BAD_REQUEST),
    EXTERNAL_API_ERROR("G-003", "외부 API 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_NOT_FOUND("U-001", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND),
    USER_EMAIL_CONFLICT("U-002", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),

    MATCH_NOT_FOUND("M-001", "존재하지 않는 경기입니다.", HttpStatus.NOT_FOUND),
    MATCH_NOT_ENDED("M-002", "경기가 진행중입니다.", HttpStatus.BAD_REQUEST),

    PLAYER_NOT_FOUND("P-001", "존재하지 않는 선수입니다.", HttpStatus.NOT_FOUND),

    MATCH_LINEUP_NOT_FOUND("ML-001", "존재하지 않는 라인업입니다.", HttpStatus.NOT_FOUND),
    MATCH_LINEUP_DID_NOT_PARTICIPATE("ML-002", "선수가 경기에 참여하지 않았습니다.", HttpStatus.BAD_REQUEST),

    MATCH_REVIEW_ALREADY_REVIEWED("MR-001", "이미 경기 리뷰에 참여했습니다.", HttpStatus.CONFLICT),
    MATCH_REVIEW_INVALID_REVIEW_TIME("MR-002", "경기 리뷰 가능 시간이 아닙니다.", HttpStatus.BAD_REQUEST),
    MATCH_REVIEW_NOT_FOUND("MR-003", "경기 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    MATCH_STATISTICS_NOT_FOUND("MS-001", "경기 통계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    PLAYER_REVIEW_ALREADY_REVIEWED("PR-001", "이미 선수 리뷰에 참여했습니다.", HttpStatus.CONFLICT),
    PLAYER_REVIEW_NOT_FAN("PR-001", "타팀 선수의 리뷰엔 참여할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PLAYER_REVIEW_NOT_FOUND("PR-003", "선수 리뷰가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    PLAYER_REVIEW_INVALID_REVIEW_TIME("PR-004", "선수 리뷰 가능 시간이 아닙니다.", HttpStatus.BAD_REQUEST),

    PLAYER_STATISTICS_NOT_FOUND("PS-001", "선수 통계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    TEAM_NOT_FOUND("T-001", "팀을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    PASSWORD_NOT_MATCH("A-001", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("A-002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("A-003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("A-004", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("A-005", "해당 리소스에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_UNAUTHENTICATED("A-006", "로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_GOOGLE("A-007", "유효하지 않은 구글 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("A-008", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_MISSING("A-009", "리프레시 토큰이 없습니다.", HttpStatus.NOT_FOUND),
    GOOGLE_AUTH_ERROR("A-010", "구글 인증 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
