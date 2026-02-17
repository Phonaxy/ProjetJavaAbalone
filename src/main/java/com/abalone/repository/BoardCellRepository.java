package com.abalone.repository;

import com.abalone.model.BoardCell;
import com.abalone.model.enums.CellState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCellRepository extends JpaRepository<BoardCell, Long> {

    List<BoardCell> findByGameId(Long gameId);

    Optional<BoardCell> findByGameIdAndRowIdxAndColIdx(Long gameId, int rowIdx, int colIdx);

    List<BoardCell> findByGameIdAndState(Long gameId, CellState state);
}
