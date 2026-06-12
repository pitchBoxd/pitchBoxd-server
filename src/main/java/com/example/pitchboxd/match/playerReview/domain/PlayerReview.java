package com.example.pitchboxd.match.playerReview.domain;


import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import com.example.pitchboxd.match.matchStatistics.domain.FanType;


@Entity
@DynamicUpdate
@Table(name = "player_reviews")
@SQLDelete(sql = "UPDATE player_reviews SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PlayerReview extends BaseEntity {

    private static final int MAX_CONTENT_LENGTH = 100;
    private static final int MAX_POINT = 10;
    private static final int MIN_POINT = 0;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer point;

    private String content;

    private Long likeCount;

    private FanType fanType;
private LocalDateTime updatedAt;

    public PlayerReview(Long matchId, Long playerId, Long userId, Integer point, String content, FanType fanType) {
        validate(content, point);
        this.matchId = matchId;
        this.playerId = playerId;
        this.userId = userId;
        this.point = point;
        this.likeCount = 0L;
        this.content = content;
        this.fanType = fanType;
    }

    public PlayerReview(Long matchId, Long playerId, Long userId, Integer point, String content) {
        this(matchId, playerId, userId, point, content, null);
    }

    public void update(String content, Integer point) {
        validate(content, point);
        this.updatedAt = LocalDateTime.now();
        this.content = content;
        this.point = point;
    }

    private void validate(String content, Integer point) {
        validateContent(content);
        validatePoint(point);
    }

    private void validateContent(String content) {
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("한줄평은 100자를 넘길 수 없습니다.");
        }
    }

    private void validatePoint(Integer point) {
        if (point > MAX_POINT || point < MIN_POINT) {
            throw new IllegalArgumentException("포인트는 0 이상, 10 이하여야 합니다.");
        }
    }

    public void minusOneLikeCount() {
        if (likeCount > 0) {
            this.likeCount--;
        }
    }

    public void addOneLikeCount() {
        this.likeCount++;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }
}
