package com.abalone.controller;

import com.abalone.dto.HallOfFameEntry;
import com.abalone.dto.ScoreResponse;
import com.abalone.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /**
     * GET /api/scores/hall-of-fame — Classement general des joueurs.
     */
    @GetMapping("/hall-of-fame")
    public ResponseEntity<List<HallOfFameEntry>> getHallOfFame() {
        return ResponseEntity.ok(scoreService.getHallOfFame());
    }

    /**
     * GET /api/scores/{id} — Detail d'un score.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScoreResponse> getScore(@PathVariable Long id) {
        return ResponseEntity.ok(ScoreResponse.fromEntity(scoreService.getScore(id)));
    }

    /**
     * GET /api/scores/player/{playerId} — Scores d'un joueur.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<ScoreResponse>> getScoresByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(scoreService.getScoresByPlayer(playerId).stream()
                .map(ScoreResponse::fromEntity)
                .toList());
    }

    /**
     * GET /api/scores/game/{gameId} — Scores d'une partie.
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<ScoreResponse>> getScoresByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(scoreService.getScoresByGame(gameId).stream()
                .map(ScoreResponse::fromEntity)
                .toList());
    }
}
