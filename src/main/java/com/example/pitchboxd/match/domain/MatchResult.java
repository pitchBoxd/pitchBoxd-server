package com.example.pitchboxd.match.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchResult {

    @Column(nullable = false)
    private Integer homeScore;

    @Column(nullable = false)
    private Integer awayScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "home_scorers", columnDefinition = "json")
    private List<GoalScorer> homeScorers;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "away_scorers", columnDefinition = "json")
    private List<GoalScorer> awayScorers;

    public MatchResult(Integer homeScore, Integer awayScore,
                       List<GoalScorer> homeScorers,
                       List<GoalScorer> awayScorers) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeScorers = homeScorers;
        this.awayScorers = awayScorers;
    }
}
