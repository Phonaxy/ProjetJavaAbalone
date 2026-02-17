package com.abalone.model;

import com.abalone.model.enums.CellState;
import jakarta.persistence.*;

@Entity
@Table(name = "board_cell")
public class BoardCell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "row_idx")
    private int rowIdx;

    @Column(name = "col_idx")
    private int colIdx;

    @Enumerated(EnumType.STRING)
    private CellState state;

    public BoardCell() {
    }

    public BoardCell(Game game, int rowIdx, int colIdx, CellState state) {
        this.game = game;
        this.rowIdx = rowIdx;
        this.colIdx = colIdx;
        this.state = state;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public void setRowIdx(int rowIdx) {
        this.rowIdx = rowIdx;
    }

    public int getColIdx() {
        return colIdx;
    }

    public void setColIdx(int colIdx) {
        this.colIdx = colIdx;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }
}
