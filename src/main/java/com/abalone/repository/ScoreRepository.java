package com.abalone.repository;

import com.abalone.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByPlayerId(Long playerId);

    List<Score> findByGameId(Long gameId);

    List<Score> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    List<Score> findAllByOrderByScorePointsDesc();
}
