package com.valorantcoach.controller;

import com.valorantcoach.dto.CoachingReport;
import com.valorantcoach.dto.MatchSummary;
import com.valorantcoach.service.ClaudeService;
import com.valorantcoach.service.RiotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*")
public class CoachController {

    private final RiotService riotService;
    private final ClaudeService claudeService;

    public CoachController(RiotService riotService, ClaudeService claudeService) {
        this.riotService = riotService;
        this.claudeService = claudeService;
    }

    // GET /api/coach/report?name=Pseudo&tag=TAG&style=drill|coach
    @GetMapping("/report")
    public CoachingReport getReport(
            @RequestParam String name,
            @RequestParam String tag,
            @RequestParam(defaultValue = "drill") String style) {

        List<MatchSummary> matches = riotService.getRecentMatches(name, tag);

        String analysis = claudeService.generateCoachingReport(name + "#" + tag, matches, style);
        String weeklyPlan = claudeService.generateWeeklyPlan(name + "#" + tag, matches);

        return CoachingReport.builder()
                .playerName(name + "#" + tag)
                .matches(matches)
                .analysis(analysis)
                .weeklyPlan(weeklyPlan)
                .build();
    }
}
