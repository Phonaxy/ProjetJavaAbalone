package com.abalone.dto;

import com.abalone.model.BoardCell;

import java.util.List;

public class BoardResponse {

    private Long gameId;
    private List<CellInfo> cells;

    public static BoardResponse fromCells(Long gameId, List<BoardCell> cells) {
        BoardResponse response = new BoardResponse();
        response.setGameId(gameId);
        response.setCells(cells.stream()
                .map(CellInfo::fromEntity)
                .toList());
        return response;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public List<CellInfo> getCells() {
        return cells;
    }

    public void setCells(List<CellInfo> cells) {
        this.cells = cells;
    }

    public static class CellInfo {
        private int row;
        private int col;
        private String state;

        public static CellInfo fromEntity(BoardCell cell) {
            CellInfo info = new CellInfo();
            info.setRow(cell.getRowIdx());
            info.setCol(cell.getColIdx());
            info.setState(cell.getState().name());
            return info;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
