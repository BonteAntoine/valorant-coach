package com.valorantcoach.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorantcoach.dto.MatchSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {

    private final WebClient client;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeService(@Value("${anthropic.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = WebClient.builder()
                .baseUrl("https://api.anthropic.com")
                .build();
    }

    public String generateCoachingReport(String playerName, List<MatchSummary> matches, String style) {
        String prompt = buildPrompt(playerName, matches, style);

        Map<String, Object> body = Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        JsonNode response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("content").get(0).get("text").asText();
    }

    private String buildPrompt(String playerName, List<MatchSummary> matches, String style) {
        StringBuilder sb = new StringBuilder();

        if ("drill".equals(style)) {
            sb.append("Tu es un coach Valorant sans filtre, style sergent instructeur. Tu dis les vérités qui font mal, tu ne félicites que ce qui le mérite vraiment, et tu exiges le meilleur. Pas de pitié, mais pas d'insultes non plus — juste la réalité brute.");
        } else {
            sb.append("Tu es un coach Valorant professionnel bienveillant mais honnête. Tu encourages les points forts, tu identifies les axes d'amélioration sans juger, et tu donnes des conseils actionnables avec empathie.");
        }

        sb.append(" Analyse les 5 dernières parties du joueur ")
          .append(playerName)
          .append(" (niveau Platine) et donne-lui des conseils personnalisés et concrets.\n\n");

        sb.append("Données de ses parties :\n");
        for (int i = 0; i < matches.size(); i++) {
            MatchSummary m = matches.get(i);
            sb.append(String.format(
                "Partie %d : %s | %s | K/D/A: %d/%d/%d | HS%%: %d%% | Score: %d | Dégâts: %d | Rounds: %d-%d\n",
                i + 1,
                m.isWon() ? "VICTOIRE" : "DEFAITE",
                m.getAgent(),
                m.getKills(), m.getDeaths(), m.getAssists(),
                m.getHeadshotPct(),
                m.getScore(),
                m.getDamageDealt(),
                m.getRoundsWon(), m.getRoundsLost()
            ));
        }

        sb.append("\nDonne un rapport structuré avec :\n");
        sb.append("1. Points forts\n");
        sb.append("2. Points faibles (les 2-3 principales choses à améliorer)\n");
        sb.append("3. Exercices concrets\n");
        sb.append("4. Conseil spécifique sur ses agents joués\n");
        sb.append("\nParle en français. Max 400 mots.");

        return sb.toString();
    }

    public String generateWeeklyPlan(String playerName, List<MatchSummary> matches) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un coach Valorant expert. Génère un plan de progression sur 4 semaines pour le joueur ")
          .append(playerName).append(" (niveau Platine) basé sur ses statistiques.\n\n");

        sb.append("Ses stats récentes :\n");
        for (int i = 0; i < matches.size(); i++) {
            MatchSummary m = matches.get(i);
            sb.append(String.format(
                "Partie %d : %s | %s | K/D/A: %d/%d/%d | HS%%: %d%% | Score: %d | Dégâts: %d\n",
                i + 1, m.isWon() ? "V" : "D", m.getAgent(),
                m.getKills(), m.getDeaths(), m.getAssists(),
                m.getHeadshotPct(), m.getScore(), m.getDamageDealt()
            ));
        }

        sb.append("""

Génère un plan structuré semaine par semaine :

## Semaine 1 — [Objectif principal]
- Exercice quotidien 1 (durée, outil)
- Exercice quotidien 2 (durée, outil)
- Objectif mesurable à atteindre d'ici la fin de la semaine

## Semaine 2 — [Objectif principal]
...

## Semaine 3 — [Objectif principal]
...

## Semaine 4 — [Objectif principal]
...

## Objectif final
Rank cible et comment mesurer la progression.

Sois très concret : nomme les outils (Aim Lab, replay, deathmatch...), donne des durées, des métriques. Parle en français. Max 500 mots.""");

        Map<String, Object> body = Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", sb.toString()))
        );

        JsonNode response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("content").get(0).get("text").asText();
    }
}
