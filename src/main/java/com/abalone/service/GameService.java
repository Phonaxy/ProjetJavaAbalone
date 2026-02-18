package com.abalone.service;

import com.abalone.dto.CreateGameRequest;
import com.abalone.dto.MoveRequest;
import com.abalone.exception.GameNotFoundException;
import com.abalone.exception.InvalidMoveException;
import com.abalone.exception.PlayerNotFoundException;
import com.abalone.model.BoardCell;
import com.abalone.model.Game;
import com.abalone.model.Player;
import com.abalone.model.enums.CellState;
import com.abalone.model.enums.GameStatus;
import com.abalone.repository.GameRepository;
import com.abalone.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final BoardService boardService;
    private final ScoreService scoreService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository,
                       BoardService boardService, ScoreService scoreService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.boardService = boardService;
        this.scoreService = scoreService;
    }

    // ========== CRUD PARTIES ==========

    /**
     * Creer une nouvelle partie entre deux joueurs existants.
     * La partie demarre directement en IN_PROGRESS avec le plateau initialise.
     */
    @Transactional
    public Game createGame(CreateGameRequest request) {
        if (request.getPlayerBlackId().equals(request.getPlayerWhiteId())) {
            throw new IllegalArgumentException("Les deux joueurs doivent etre differents.");
        }

        Player black = playerRepository.findById(request.getPlayerBlackId())
                .orElseThrow(() -> new PlayerNotFoundException("Joueur noir non trouve : " + request.getPlayerBlackId()));
        Player white = playerRepository.findById(request.getPlayerWhiteId())
                .orElseThrow(() -> new PlayerNotFoundException("Joueur blanc non trouve : " + request.getPlayerWhiteId()));

        Game game = new Game();
        game.setPlayerBlack(black);
        game.setPlayerWhite(white);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentColor(CellState.BLACK);
        game.setBlackOut(0);
        game.setWhiteOut(0);
        game.setTurnNumber(1);

        game = gameRepository.save(game);

        List<BoardCell> cells = boardService.initializeBoard(game);
        game.getCells().addAll(cells);

        return game;
    }

    /**
     * Recuperer une partie par son ID.
     */
    public Game getGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Partie non trouvee avec l'id : " + id));
    }

    /**
     * Lister toutes les parties.
     */
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    /**
     * Lister les parties par statut.
     */
    public List<Game> getGamesByStatus(GameStatus status) {
        return gameRepository.findByStatus(status);
    }

    /**
     * Supprimer une partie.
     * Seules les parties FINISHED ou ABANDONED peuvent etre supprimees.
     */
    @Transactional
    public void deleteGame(Long id) {
        Game game = getGame(id);
        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Impossible de supprimer une partie en cours. Abandonnez-la d'abord.");
        }
        gameRepository.delete(game);
    }

    // ========== CYCLE DE VIE ==========

    /**
     * Abandonner une partie.
     * L'adversaire gagne par forfait. Les scores sont calcules.
     */
    @Transactional
    public Game abandonGame(Long id, Long abandoningPlayerId) {
        Game game = getGame(id);

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Seule une partie en cours peut etre abandonnee.");
        }

        // Verifier que le joueur fait partie de cette partie
        boolean isBlack = game.getPlayerBlack().getId().equals(abandoningPlayerId);
        boolean isWhite = game.getPlayerWhite().getId().equals(abandoningPlayerId);
        if (!isBlack && !isWhite) {
            throw new InvalidMoveException("Ce joueur ne participe pas a cette partie.");
        }

        // Le gagnant est l'adversaire
        Player winner = isBlack ? game.getPlayerWhite() : game.getPlayerBlack();

        game.setStatus(GameStatus.ABANDONED);
        game.setWinner(winner);
        game.setFinishedAt(LocalDateTime.now());
        game = gameRepository.save(game);

        scoreService.calculateAndSaveScores(game, winner);

        return game;
    }

    // ========== PLATEAU ==========

    public List<BoardCell> getBoard(Long gameId) {
        getGame(gameId);
        return boardService.getBoard(gameId);
    }

    // ========== DEROULEMENT ==========

    /**
     * Retourne les infos du joueur actif (celui dont c'est le tour).
     */
    public Player getActivePlayer(Long gameId) {
        Game game = getGame(gameId);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("La partie n'est pas en cours.");
        }
        return game.getCurrentColor() == CellState.BLACK ? game.getPlayerBlack() : game.getPlayerWhite();
    }

    /**
     * Jouer un coup.
     * Valide le temps, execute le mouvement, gere le changement de tour et la fin de partie.
     */
    @Transactional
    public Game makeMove(Long gameId, MoveRequest moveRequest) {
        Game game = getGame(gameId);

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("La partie est terminee.");
        }

        CellState currentColor = game.getCurrentColor();

        // Valider le mouvement
        String validationError = boardService.validateMove(
                gameId, moveRequest.getMarbles(), moveRequest.getDirection(), currentColor);

        if (validationError != null) {
            throw new InvalidMoveException(validationError);
        }

        // Executer le mouvement
        int ejected = boardService.executeMove(
                gameId, moveRequest.getMarbles(), moveRequest.getDirection(), currentColor);

        if (ejected < 0) {
            throw new InvalidMoveException("Mouvement impossible : les billes sont bloquees.");
        }

        // Mettre a jour le score
        if (ejected > 0) {
            if (currentColor == CellState.BLACK) {
                game.setWhiteOut(game.getWhiteOut() + ejected);
            } else {
                game.setBlackOut(game.getBlackOut() + ejected);
            }
        }

        // Verifier la condition de victoire (6 billes adverses ejectees)
        if (boardService.checkWinCondition(game.getBlackOut())
                || boardService.checkWinCondition(game.getWhiteOut())) {
            // Le joueur qui vient de jouer gagne
            Player winner = currentColor == CellState.BLACK
                    ? game.getPlayerBlack() : game.getPlayerWhite();

            game.setStatus(GameStatus.FINISHED);
            game.setWinner(winner);
            game.setFinishedAt(LocalDateTime.now());
            game = gameRepository.save(game);

            scoreService.calculateAndSaveScores(game, winner);
            return game;
        }

        // Changer de tour
        game.setCurrentColor(currentColor == CellState.BLACK ? CellState.WHITE : CellState.BLACK);
        game.setTurnNumber(game.getTurnNumber() + 1);

        return gameRepository.save(game);
    }
}
