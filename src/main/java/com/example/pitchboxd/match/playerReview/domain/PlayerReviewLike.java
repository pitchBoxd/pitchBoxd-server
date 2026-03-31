package com.example.pitchboxd.match.playerReview.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "player_review_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_player_review_like_user_review",
                        columnNames = {"player_review_id", "user_id"} // DB 컬럼명 기준
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PlayerReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playerReviewId;

    private Long userId;

    public PlayerReviewLike(Long playerReviewId, Long userId) {
        this.playerReviewId = playerReviewId;
        this.userId = userId;
    }
}
