package com.valorantcoach.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchSummary {
    private String matchId;
    private String agent;
    private int kills;
    private int deaths;
    private int assists;
    private int score;
    private int headshotPct;
    private boolean won;
    private int roundsWon;
    private int roundsLost;
    private int damageDealt;
    private int creditsSpent;
}
