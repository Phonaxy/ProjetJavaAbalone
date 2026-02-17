package com.abalone.controller;

import com.abalone.dto.*;
import com.abalone.model.BoardCell;
import com.abalone.model.Game;
import com.abalone.model.Player;
import com.abalone.model.enums.GameStatus;
import com.abalone.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * POST /api/games — Creer une nouvelle partie.
     */
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@RequestBody CreateGameRequest request) {
        Game game = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.fromEntity(game));
    }

    /**
     * GET /api/games — Lister toutes les parties (optionnel: filtrer par status).
     */
    @GetMapping
    public ResponseEntity<List<GameResponse>> getAllGames(
            @RequestParam(required = false) GameStatus status) {
        List<Game> games = (status != null)
                ? gameService.getGamesByStatus(status)
                : gameService.getAllGames();
        return ResponseEntity.ok(games.stream().map(GameResponse::fromEntity).toList());
    }

    /**
     * GET /api/games/{id} — Recuperer l'etat d'une partie.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(GameResponse.fromEntity(gameService.getGame(id)));
    }

    /**
     * GET /api/games/{id}/board — Recuperer le plateau.
     */
    @GetMapping("/{id}/board")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long id) {
        List<BoardCell> cells = gameService.getBoard(id);
        return ResponseEntity.ok(BoardResponse.fromCells(id, cells));
    }

    /**
     * GET /api/games/{id}/active-player — Recuperer le joueur actif.
     */
    @GetMapping("/{id}/active-player")
    public ResponseEntity<PlayerResponse> getActivePlayer(@PathVariable Long id) {
        Player active = gameService.getActivePlayer(id);
        return ResponseEntity.ok(PlayerResponse.fromEntity(active));
    }

    /**
     * POST /api/games/{id}/move — Jouer un coup.
     */
    @PostMapping("/{id}/move")
    public ResponseEntity<GameResponse> makeMove(@PathVariable Long id, @RequestBody MoveRequest moveRequest) {
        Game game = gameService.makeMove(id, moveRequest);
        return ResponseEntity.ok(GameResponse.fromEntity(game));
    }

    /**
     * POST /api/games/{id}/abandon — Abandonner une partie.
     */
    @PostMapping("/{id}/abandon")
    public ResponseEntity<GameResponse> abandonGame(
            @PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long playerId = body.get("playerId");
        Game game = gameService.abandonGame(id, playerId);
        return ResponseEntity.ok(GameResponse.fromEntity(game));
    }

    /**
     * DELETE /api/games/{id} — Supprimer une partie (uniquement si FINISHED ou ABANDONED).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
