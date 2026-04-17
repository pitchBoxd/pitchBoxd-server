package com.example.pitchboxd.round.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "rounds")
@SQLDelete(sql = "UPDATE rounds SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long seasonId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String naverId;

    @Column(nullable = false)
    private boolean isCurrent;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public Round(Long seasonId, LocalDateTime startDate, LocalDateTime endDate, String name, String naverId, boolean isCurrent) {
        this.seasonId = seasonId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.naverId = naverId;
        this.isCurrent = isCurrent;
    }

    public void updatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateCurrentStatus(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }
}
