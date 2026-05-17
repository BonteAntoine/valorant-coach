package com.valorantcoach.service;

import com.valorantcoach.dto.MatchSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiotService {

    // Données mockées en attendant la clé Riot production
    public List<MatchSummary> getRecentMatches(String name, String tag) {
        return List.of(
            MatchSummary.builder()
                .matchId("mock-1").agent("Jett")
                .kills(18).deaths(14).assists(3)
                .score(3200).headshotPct(22)
                .won(true).roundsWon(13).roundsLost(9)
                .damageDealt(2850).creditsSpent(18500)
                .build(),
            MatchSummary.builder()
                .matchId("mock-2").agent("Jett")
                .kills(11).deaths(17).assists(2)
                .score(1900).headshotPct(18)
                .won(false).roundsWon(7).roundsLost(13)
                .damageDealt(1950).creditsSpent(22000)
                .build(),
            MatchSummary.builder()
                .matchId("mock-3").agent("Reyna")
                .kills(22).deaths(12).assists(1)
                .score(4100).headshotPct(31)
                .won(true).roundsWon(13).roundsLost(6)
                .damageDealt(3400).creditsSpent(16000)
                .build(),
            MatchSummary.builder()
                .matchId("mock-4").agent("Reyna")
                .kills(14).deaths(18).assists(0)
                .score(2200).headshotPct(19)
                .won(false).roundsWon(8).roundsLost(13)
                .damageDealt(2100).creditsSpent(24000)
                .build(),
            MatchSummary.builder()
                .matchId("mock-5").agent("Jett")
                .kills(16).deaths(15).assists(4)
                .score(2800).headshotPct(24)
                .won(false).roundsWon(10).roundsLost(13)
                .damageDealt(2600).creditsSpent(19000)
                .build()
        );
    }
}
