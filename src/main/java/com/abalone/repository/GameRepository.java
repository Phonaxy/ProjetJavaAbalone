package com.abalone.repository;

import com.abalone.model.Game;
import com.abalone.model.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatus(GameStatus status);

    List<Game> findByPlayerBlackIdOrPlayerWhiteId(Long blackId, Long whiteId);
}
