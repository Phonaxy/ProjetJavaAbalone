package com.abalone.dto;

import com.abalone.model.Game;
import java.time.LocalDateTime;

public class GameResponse {

    private Long id;
    private String status;
    private String currentColor;
    private int blackOut;
    private int whiteOut;
    private int turnNumber;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private PlayerResponse playerBlack;
    private PlayerResponse playerWhite;
    private PlayerResponse winner;

    public static GameResponse fromEntity(Game game) {
        GameResponse r = new GameResponse();
        r.setId(game.getId());
        r.setStatus(game.getStatus().name());
        r.setCurrentColor(game.getCurrentColor() != null ? game.getCurrentColor().name() : null);
        r.setBlackOut(game.getBlackOut());
        r.setWhiteOut(game.getWhiteOut());
        r.setTurnNumber(game.getTurnNumber());
        r.setCreatedAt(game.getCreatedAt());
        r.setFinishedAt(game.getFinishedAt());
        if (game.getPlayerBlack() != null) {
            r.setPlayerBlack(PlayerResponse.fromEntity(game.getPlayerBlack()));
        }
        if (game.getPlayerWhite() != null) {
            r.setPlayerWhite(PlayerResponse.fromEntity(game.getPlayerWhite()));
        }
        if (game.getWinner() != null) {
            r.setWinner(PlayerResponse.fromEntity(game.getWinner()));
        }
        return r;
    }

    // Getters et Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentColor() { return currentColor; }
    public void setCurrentColor(String currentColor) { this.currentColor = currentColor; }

    public int getBlackOut() { return blackOut; }
    public void setBlackOut(int blackOut) { this.blackOut = blackOut; }

    public int getWhiteOut() { return whiteOut; }
    public void setWhiteOut(int whiteOut) { this.whiteOut = whiteOut; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public PlayerResponse getPlayerBlack() { return playerBlack; }
    public void setPlayerBlack(PlayerResponse playerBlack) { this.playerBlack = playerBlack; }

    public PlayerResponse getPlayerWhite() { return playerWhite; }
    public void setPlayerWhite(PlayerResponse playerWhite) { this.playerWhite = playerWhite; }

    public PlayerResponse getWinner() { return winner; }
    public void setWinner(PlayerResponse winner) { this.winner = winner; }
}
