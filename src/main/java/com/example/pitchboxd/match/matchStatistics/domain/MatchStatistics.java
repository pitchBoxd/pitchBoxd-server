package com.example.pitchboxd.match.matchStatistics.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "match_statistics")
@SQLDelete(sql = "UPDATE match_statistics SET deleted_at = NOW() WHERE match_id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MatchStatistics extends BaseEntity {

    @Id
    private Long matchId;

    @Column(nullable = false)
    private Long totalRatingSum;

    @Column(nullable = false)
    private Integer totalReviewCount;

    @Column(nullable = false)
    private Long homeFanRatingSum;

    @Column(nullable = false)
    private Integer homeFanReviewCount;

    @Column(nullable = false)
    private Long awayFanRatingSum;

    @Column(nullable = false)
    private Integer awayFanReviewCount;

    public MatchStatistics(Long matchId) {
        this.matchId = matchId;
        this.totalRatingSum = 0L;
        this.totalReviewCount = 0;
        this.homeFanRatingSum = 0L;
        this.homeFanReviewCount = 0;
        this.awayFanRatingSum = 0L;
        this.awayFanReviewCount = 0;
    }

    /***
     * 통계 계산은 다음과 같이 이루어집니다.
     * 중립팀 팬: 전체 통계에 들어갑니다.
     * 홈팀 팬: 전체 통계 + 홈팀 관련 통계에 들어갑니다.
     * 원정 팬: 전체 통계 + 원정 관련 통계에 들어갑니다.
     ***/
    public void addNewReview(int rating, FanType fanType) {
        this.totalRatingSum += rating;
        this.totalReviewCount++;

        if (fanType == FanType.HOME) {
            this.homeFanRatingSum += rating;
            this.homeFanReviewCount++;
        } else if (fanType == FanType.AWAY) {
            this.awayFanRatingSum += rating;
            this.awayFanReviewCount++;
        }
    }

    public void adjustRating(int ratingDelta, FanType fanType) {
        this.totalRatingSum += ratingDelta;

        if (fanType == FanType.HOME) {
            this.homeFanRatingSum += ratingDelta;
        } else if (fanType == FanType.AWAY) {
            this.awayFanRatingSum += ratingDelta;
        }
    }

    public double getTotalAverage() {
        return calculate(totalRatingSum, totalReviewCount);
    }

    public double getHomeAverage() {
        return calculate(homeFanRatingSum, homeFanReviewCount);
    }

    public double getAwayAverage() {
        return calculate(awayFanRatingSum, awayFanReviewCount);
    }

    private double calculate(Long sum, Integer count) {
        if (count == 0) {
            return 0.0;
        }
        return (sum / (double) count) / 2.0; // 5점 만점 변환 로직 포함
    }
}
