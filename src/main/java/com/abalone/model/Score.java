package com.abalone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "score")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    private boolean won;

    @Column(name = "marbles_ejected")
    private int marblesEjected;

    @Column(name = "marbles_lost")
    private int marblesLost;

    @Column(name = "turns_played")
    private int turnsPlayed;

    @Column(name = "score_points")
    private int scorePoints;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Score() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

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
