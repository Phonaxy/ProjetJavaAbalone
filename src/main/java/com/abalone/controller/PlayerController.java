package com.abalone.controller;

import com.abalone.dto.PlayerRequest;
import com.abalone.dto.PlayerResponse;
import com.abalone.model.Player;
import com.abalone.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * GET /api/players — Lister tous les joueurs.
     */
    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers().stream()
                .map(PlayerResponse::fromEntity)
                .toList());
    }

    /**
     * GET /api/players/{id} — Recuperer un joueur.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(PlayerResponse.fromEntity(playerService.getPlayer(id)));
    }

    /**
     * POST /api/players — Creer un joueur.
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@RequestBody PlayerRequest request) {
        Player player = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlayerResponse.fromEntity(player));
    }

    /**
     * PUT /api/players/{id} — Modifier un joueur.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(@PathVariable Long id, @RequestBody PlayerRequest request) {
        Player player = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(PlayerResponse.fromEntity(player));
    }

    /**
     * DELETE /api/players/{id} — Supprimer un joueur.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
