package com.example.pitchboxd.match.domain;

public enum MatchStatus {
    SCHEDULED, // 시작 전
    PLAYING,  // 경기 중
    FINISHED, // 경기 종료
    CANCELED // 취소
}
