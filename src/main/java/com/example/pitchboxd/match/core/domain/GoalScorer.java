package com.example.pitchboxd.match.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoalScorer {
    private String playerName;
    private Integer minute;
    private Integer addedTime;
    private boolean ownGoal;
}
