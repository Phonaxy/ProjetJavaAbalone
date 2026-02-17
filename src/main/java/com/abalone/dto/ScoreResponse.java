package com.abalone.dto;

import com.abalone.model.Score;
import java.time.LocalDateTime;

public class ScoreResponse {

    private Long id;
    private Long playerId;
    private String playerName;
    private Long gameId;
    private boolean won;
    private int marblesEjected;
    private int marblesLost;
    private int turnsPlayed;
    private int scorePoints;
    private LocalDateTime createdAt;

    public static ScoreResponse fromEntity(Score score) {
        ScoreResponse r = new ScoreResponse();
        r.setId(score.getId());
        r.setPlayerId(score.getPlayer().getId());
        r.setPlayerName(score.getPlayer().getDisplayName());
        r.setGameId(score.getGame().getId());
        r.setWon(score.isWon());
        r.setMarblesEjected(score.getMarblesEjected());
        r.setMarblesLost(score.getMarblesLost());
        r.setTurnsPlayed(score.getTurnsPlayed());
        r.setScorePoints(score.getScorePoints());
        r.setCreatedAt(score.getCreatedAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public int getMarblesEjected() { return marblesEjected; }
    public void setMarblesEjected(int marblesEjected) { this.marblesEjected = marblesEjected; }

    public int getMarblesLost() { return marblesLost; }
    public void setMarblesLost(int marblesLost) { this.marblesLost = marblesLost; }

    public int getTurnsPlayed() { return turnsPlayed; }
    public void setTurnsPlayed(int turnsPlayed) { this.turnsPlayed = turnsPlayed; }

    public int getScorePoints() { return scorePoints; }
    public void setScorePoints(int scorePoints) { this.scorePoints = scorePoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
