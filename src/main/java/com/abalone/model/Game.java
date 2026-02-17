package com.abalone.model;

import com.abalone.model.enums.CellState;
import com.abalone.model.enums.GameStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_color")
    private CellState currentColor;

    @Column(name = "black_out")
    private int blackOut;

    @Column(name = "white_out")
    private int whiteOut;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_black_id")
    private Player playerBlack;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_white_id")
    private Player playerWhite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id")
    private Player winner;

    @Column(name = "turn_number")
    private int turnNumber;

    @Column(name = "last_move_at")
    private LocalDateTime lastMoveAt;

    @Column(name = "turn_time_limit_seconds")
    private int turnTimeLimitSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardCell> cells = new ArrayList<>();

    public Game() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public CellState getCurrentColor() { return currentColor; }
    public void setCurrentColor(CellState currentColor) { this.currentColor = currentColor; }

    public int getBlackOut() { return blackOut; }
    public void setBlackOut(int blackOut) { this.blackOut = blackOut; }

    public int getWhiteOut() { return whiteOut; }
    public void setWhiteOut(int whiteOut) { this.whiteOut = whiteOut; }

    public Player getPlayerBlack() { return playerBlack; }
    public void setPlayerBlack(Player playerBlack) { this.playerBlack = playerBlack; }

    public Player getPlayerWhite() { return playerWhite; }
    public void setPlayerWhite(Player playerWhite) { this.playerWhite = playerWhite; }

    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public LocalDateTime getLastMoveAt() { return lastMoveAt; }
    public void setLastMoveAt(LocalDateTime lastMoveAt) { this.lastMoveAt = lastMoveAt; }

    public int getTurnTimeLimitSeconds() { return turnTimeLimitSeconds; }
    public void setTurnTimeLimitSeconds(int turnTimeLimitSeconds) { this.turnTimeLimitSeconds = turnTimeLimitSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public List<BoardCell> getCells() { return cells; }
    public void setCells(List<BoardCell> cells) { this.cells = cells; }
}
