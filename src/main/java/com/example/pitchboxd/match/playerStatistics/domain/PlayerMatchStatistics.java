package com.example.pitchboxd.match.playerStatistics.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "player_statistics")
@SQLDelete(sql = "UPDATE player_statistics SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PlayerMatchStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playerId;
    private Long matchId;

    private Long totalScore;  // 0~10점들의 총합
    private Long reviewCount; // 리뷰 개수

    public PlayerMatchStatistics(Long playerId, Long matchId) {
        this.playerId = playerId;
        this.matchId = matchId;
        this.totalScore = 0L;
        this.reviewCount = 0L;
    }

    public void addReview(int point) {
        this.totalScore += point;
        this.reviewCount += 1;
    }

    // 조회 시점에만 계산하는 도메인 로직 (DB 저장 X)
    public double getAverageRating() {
        if (reviewCount == 0) {
            return 0.0;
        }
        return (totalScore / (double) reviewCount) / 2.0;
    }
}
