package com.abalone.service;

import com.abalone.dto.HallOfFameEntry;
import com.abalone.model.Game;
import com.abalone.model.Player;
import com.abalone.model.Score;
import com.abalone.model.enums.CellState;
import com.abalone.repository.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    /**
     * Calcule et enregistre les scores pour les deux joueurs en fin de partie.
     *
     * Calcul du score :
     * - Victoire : +100 points
     * - Bille adverse ejectee : +10 points chacune
     * - Bille perdue : -5 points chacune
     * - Bonus efficacite : +50 si victoire en moins de 30 tours
     */
    @Transactional
    public void calculateAndSaveScores(Game game, Player winner) {
        Player black = game.getPlayerBlack();
        Player white = game.getPlayerWhite();

        int blackEjected = game.getWhiteOut(); // billes blanches ejectees par noir
        int whiteEjected = game.getBlackOut(); // billes noires ejectees par blanc
        int totalTurns = game.getTurnNumber();

        // Score joueur noir
        Score blackScore = new Score();
        blackScore.setPlayer(black);
        blackScore.setGame(game);
        blackScore.setWon(winner != null && winner.getId().equals(black.getId()));
        blackScore.setMarblesEjected(blackEjected);
        blackScore.setMarblesLost(whiteEjected);
        blackScore.setTurnsPlayed(totalTurns);
        blackScore.setScorePoints(calculatePoints(blackScore.isWon(), blackEjected, whiteEjected, totalTurns));
        scoreRepository.save(blackScore);

        // Score joueur blanc
        Score whiteScore = new Score();
        whiteScore.setPlayer(white);
        whiteScore.setGame(game);
        whiteScore.setWon(winner != null && winner.getId().equals(white.getId()));
        whiteScore.setMarblesEjected(whiteEjected);
        whiteScore.setMarblesLost(blackEjected);
        whiteScore.setTurnsPlayed(totalTurns);
        whiteScore.setScorePoints(calculatePoints(whiteScore.isWon(), whiteEjected, blackEjected, totalTurns));
        scoreRepository.save(whiteScore);
    }

    private int calculatePoints(boolean won, int ejected, int lost, int turns) {
        int points = 0;
        if (won) points += 100;
        points += ejected * 10;
        points -= lost * 5;
        if (won && turns <= 30) points += 50;
        return Math.max(0, points);
    }

    public Score getScore(Long id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Score non trouve avec l'id : " + id));
    }

    public List<Score> getScoresByPlayer(Long playerId) {
        return scoreRepository.findByPlayerIdOrderByCreatedAtDesc(playerId);
    }

    public List<Score> getScoresByGame(Long gameId) {
        return scoreRepository.findByGameId(gameId);
    }

    /**
     * Hall of Fame : classement des joueurs par total de points, tries par score decroissant.
     */
    public List<HallOfFameEntry> getHallOfFame() {
        List<Score> allScores = scoreRepository.findAll();

        Map<Long, List<Score>> byPlayer = allScores.stream()
                .collect(Collectors.groupingBy(s -> s.getPlayer().getId()));

        List<HallOfFameEntry> entries = new ArrayList<>();

        for (Map.Entry<Long, List<Score>> entry : byPlayer.entrySet()) {
            List<Score> scores = entry.getValue();
            Player player = scores.get(0).getPlayer();

            HallOfFameEntry hof = new HallOfFameEntry();
            hof.setPlayerId(player.getId());
            hof.setPlayerName(player.getDisplayName());
            hof.setTotalGames(scores.size());
            hof.setWins((int) scores.stream().filter(Score::isWon).count());
            hof.setLosses((int) scores.stream().filter(s -> !s.isWon()).count());
            hof.setTotalMarblesEjected(scores.stream().mapToInt(Score::getMarblesEjected).sum());
            hof.setTotalScorePoints(scores.stream().mapToInt(Score::getScorePoints).sum());

            entries.add(hof);
        }

        entries.sort(Comparator.comparingInt(HallOfFameEntry::getTotalScorePoints).reversed());
        return entries;
    }
}
