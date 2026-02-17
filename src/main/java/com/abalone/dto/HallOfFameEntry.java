package com.abalone.dto;

public class HallOfFameEntry {

    private Long playerId;
    private String playerName;
    private int totalGames;
    private int wins;
    private int losses;
    private int abandons;
    private int totalMarblesEjected;
    private int totalScorePoints;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getAbandons() { return abandons; }
    public void setAbandons(int abandons) { this.abandons = abandons; }

    public int getTotalMarblesEjected() { return totalMarblesEjected; }
    public void setTotalMarblesEjected(int totalMarblesEjected) { this.totalMarblesEjected = totalMarblesEjected; }

    public int getTotalScorePoints() { return totalScorePoints; }
    public void setTotalScorePoints(int totalScorePoints) { this.totalScorePoints = totalScorePoints; }
}
