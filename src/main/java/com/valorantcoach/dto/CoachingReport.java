package com.valorantcoach.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CoachingReport {
    private String playerName;
    private List<MatchSummary> matches;
    private String analysis;
    private String weeklyPlan;
}
