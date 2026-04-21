package com.example.pitchboxd.match.core.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_matches_naver_id",
                columnNames = {"naver_id"}
        )
})
@SQLDelete(sql = "UPDATE matches SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Match extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long seasonId;

    @Column(nullable = false)
    private String round;

    @Column(nullable = false)
    private Long homeTeamId;

    @Column(nullable = false)
    private Long awayTeamId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime finishedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(nullable = false)
    private String location;

    @Embedded
    private MatchResult matchResult;

    @Column(nullable = false)
    private String naverId;

    public Match(Long seasonId, String round, Long homeTeamId, Long awayTeamId, LocalDateTime startTime,
                 MatchStatus status, String location, String naverId) {
        this.seasonId = seasonId;
        this.round = round;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.startTime = startTime;
        this.status = status;
        this.location = location;
        this.naverId = naverId;
    }

    public FanType determineFanType(Long teamId) {
        if (homeTeamId.equals(teamId)) {
            return FanType.HOME;
        }

        if (awayTeamId.equals(teamId)) {
            return FanType.AWAY;
        }

        return FanType.NEUTRAL;
    }

    public void finish(LocalDateTime now) {
        this.status = MatchStatus.FINISHED;
        this.finishedAt = now;
    }

    public void decideMatchResult(MatchResult matchResult) {
        if (this.matchResult == null) {
            this.matchResult = matchResult;
            return;
        }

        throw new IllegalArgumentException("이미 결정된 경기 결과는 바꿀 수 없습니다.");
    }

    public boolean isEnd(LocalDateTime now) {
        if (finishedAt == null || status == null) {
            return false;
        }

        return status == MatchStatus.FINISHED && finishedAt.isBefore(now);
    }

    public boolean isPassed(LocalDateTime now, Duration duration) {
        if (!isEnd(now)) {
            return false;
        }

        return this.finishedAt.plus(duration).isBefore(now);
    }
}
