package com.example.pitchboxd.matchLineup.domain;

import com.example.pitchboxd.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "match_lineups")
@SQLDelete(sql = "UPDATE match_lineups SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MatchLineup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private Long playerId;

    private Integer backNumber;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    public MatchLineup(Long matchId, Long playerId, Integer backNumber, ParticipationStatus status) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.backNumber = backNumber;
        this.status = status;
    }

    public boolean isParticipated() {
        return status == ParticipationStatus.STARTER || status == ParticipationStatus.SUBSTITUTED_IN;
    }
}
