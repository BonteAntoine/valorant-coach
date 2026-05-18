package com.valorantcoach.controller;

import com.valorantcoach.dto.CoachingReport;
import com.valorantcoach.dto.MatchSummary;
import com.valorantcoach.model.User;
import com.valorantcoach.repository.UserRepository;
import com.valorantcoach.security.JwtUtil;
import com.valorantcoach.service.ClaudeService;
import com.valorantcoach.service.RiotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*")
public class CoachController {

    private final RiotService riotService;
    private final ClaudeService claudeService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public CoachController(RiotService riotService, ClaudeService claudeService,
                           UserRepository userRepository, JwtUtil jwtUtil) {
        this.riotService = riotService;
        this.claudeService = claudeService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // GET /api/coach/report?name=Pseudo&tag=TAG&style=drill|coach
    @GetMapping("/report")
    public ResponseEntity<?> getReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name,
            @RequestParam String tag,
            @RequestParam(defaultValue = "drill") String style) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Utilisateur introuvable.");
        }

        User user = userOpt.get();

        // Accès : abonné OU analyse gratuite pas encore utilisée
        if (!user.isSubscribed() && user.isFreeAnalysisUsed()) {
            return ResponseEntity.status(403).body("FREE_USED");
        }

        // Si gratuit → marquer comme utilisé
        boolean wasFree = !user.isSubscribed();
        if (wasFree) {
            user.setFreeAnalysisUsed(true);
            userRepository.save(user);
        }

        List<MatchSummary> matches = riotService.getRecentMatches(name, tag);
        String analysis = claudeService.generateCoachingReport(name + "#" + tag, matches, style);
        String weeklyPlan = claudeService.generateWeeklyPlan(name + "#" + tag, matches);

        CoachingReport report = CoachingReport.builder()
                .playerName(name + "#" + tag)
                .matches(matches)
                .analysis(analysis)
                .weeklyPlan(weeklyPlan)
                .build();

        return ResponseEntity.ok(report);
    }
}
