package com.example.pitchboxd.match.review.domain;

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
        name = "match_review_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_match_review_like_user_review",
                        columnNames = {"match_review_id", "user_id"} // DB 컬럼명 기준
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MatchReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchReviewId;

    private Long userId;

    public MatchReviewLike(Long matchReviewId, Long userId) {
        this.matchReviewId = matchReviewId;
        this.userId = userId;
    }
}
