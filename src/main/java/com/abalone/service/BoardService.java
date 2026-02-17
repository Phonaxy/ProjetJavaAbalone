package com.abalone.service;

import com.abalone.model.BoardCell;
import com.abalone.model.Game;
import com.abalone.model.enums.CellState;
import com.abalone.model.enums.Direction;
import com.abalone.repository.BoardCellRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BoardService {

    private static final int[] ROW_SIZES = {5, 6, 7, 8, 9, 8, 7, 6, 5};
    private static final int MARBLES_TO_WIN = 6;

    private final BoardCellRepository boardCellRepository;

    public BoardService(BoardCellRepository boardCellRepository) {
        this.boardCellRepository = boardCellRepository;
    }

    // ========== INITIALISATION ==========

    public List<BoardCell> initializeBoard(Game game) {
        List<BoardCell> cells = new ArrayList<>();
        for (int row = 0; row < 9; row++) {
            int size = ROW_SIZES[row];
            for (int col = 0; col < size; col++) {
                CellState state = getInitialState(row, col);
                cells.add(new BoardCell(game, row, col, state));
            }
        }
        return boardCellRepository.saveAll(cells);
    }

    private CellState getInitialState(int row, int col) {
        if (row == 0 || row == 1) return CellState.BLACK;
        if (row == 2 && col >= 2 && col <= 4) return CellState.BLACK;
        if (row == 8 || row == 7) return CellState.WHITE;
        if (row == 6 && col >= 2 && col <= 4) return CellState.WHITE;
        return CellState.EMPTY;
    }

    // ========== ACCESSEURS ==========

    public List<BoardCell> getBoard(Long gameId) {
        return boardCellRepository.findByGameId(gameId);
    }

    public boolean isValidPosition(int row, int col) {
        if (row < 0 || row >= 9) return false;
        return col >= 0 && col < ROW_SIZES[row];
    }

    public Optional<BoardCell> getCell(Long gameId, int row, int col) {
        return boardCellRepository.findByGameIdAndRowIdxAndColIdx(gameId, row, col);
    }

    // ========== VOISINAGE HEXAGONAL ==========

    /**
     * Calcule la position voisine dans une direction donnée.
     *
     * Le plateau Abalone est un hexagone :
     *   Row 0:     O O O O O           (5 cases)
     *   Row 1:    O O O O O O          (6 cases)
     *   Row 2:   O O O O O O O         (7 cases)
     *   Row 3:  O O O O O O O O        (8 cases)
     *   Row 4: O O O O O O O O O       (9 cases)  <- centre
     *   Row 5:  O O O O O O O O        (8 cases)
     *   Row 6:   O O O O O O O         (7 cases)
     *   Row 7:    O O O O O O          (6 cases)
     *   Row 8:     O O O O O           (5 cases)
     *
     * Quand on va vers une ligne PLUS GRANDE (expand) :
     *   - diag gauche : même col
     *   - diag droite : col + 1
     * Quand on va vers une ligne PLUS PETITE (shrink) :
     *   - diag gauche : col - 1
     *   - diag droite : même col
     */
    public int[] getNeighbor(int row, int col, Direction direction) {
        return switch (direction) {
            case LEFT -> new int[]{row, col - 1};
            case RIGHT -> new int[]{row, col + 1};
            case UP_LEFT -> {
                if (row <= 4) yield new int[]{row - 1, col - 1};  // shrink
                else yield new int[]{row - 1, col};                // expand
            }
            case UP_RIGHT -> {
                if (row <= 4) yield new int[]{row - 1, col};      // shrink
                else yield new int[]{row - 1, col + 1};           // expand
            }
            case DOWN_LEFT -> {
                if (row < 4) yield new int[]{row + 1, col};       // expand
                else yield new int[]{row + 1, col - 1};           // shrink
            }
            case DOWN_RIGHT -> {
                if (row < 4) yield new int[]{row + 1, col + 1};   // expand
                else yield new int[]{row + 1, col};                // shrink
            }
        };
    }

    // ========== DÉTECTION INLINE / BROADSIDE ==========

    /**
     * Tente de chaîner les billes dans la direction donnée.
     * Retourne la liste ordonnée [tête → queue] si inline, ou null si broadside/invalide.
     *
     * "Tête" = la bille la plus avancée dans la direction du mouvement.
     */
    private List<int[]> tryGetInlineChain(List<int[]> marbles, Direction direction) {
        if (marbles.size() == 1) return marbles;

        Set<String> marbleSet = new HashSet<>();
        for (int[] m : marbles) marbleSet.add(key(m));

        // Trouver la tête : la bille dont le voisin DEVANT n'est pas dans le groupe
        int[] head = null;
        for (int[] m : marbles) {
            int[] fwd = getNeighbor(m[0], m[1], direction);
            if (!marbleSet.contains(key(fwd))) {
                head = m;
                break;
            }
        }
        if (head == null) return null;

        // Chaîner de la tête vers la queue (direction opposée)
        List<int[]> chain = new ArrayList<>();
        chain.add(head);
        int[] current = head;
        Direction opposite = direction.opposite();

        while (chain.size() < marbles.size()) {
            int[] next = getNeighbor(current[0], current[1], opposite);
            if (marbleSet.contains(key(next))) {
                chain.add(next);
                current = next;
            } else {
                return null; // Trou dans la chaîne → pas inline
            }
        }

        return chain;
    }

    private String key(int[] pos) {
        return pos[0] + "," + pos[1];
    }

    // ========== VALIDATION ==========

    public String validateMove(Long gameId, List<int[]> marblePositions, Direction direction, CellState playerColor) {
        if (marblePositions.isEmpty() || marblePositions.size() > 3) {
            return "Vous devez selectionner entre 1 et 3 billes.";
        }

        // Vérifier que toutes les billes appartiennent au joueur
        for (int[] pos : marblePositions) {
            if (!isValidPosition(pos[0], pos[1])) {
                return "Position invalide : [" + pos[0] + "," + pos[1] + "]";
            }
            Optional<BoardCell> cell = getCell(gameId, pos[0], pos[1]);
            if (cell.isEmpty() || cell.get().getState() != playerColor) {
                return "La case [" + pos[0] + "," + pos[1] + "] ne contient pas une de vos billes.";
            }
        }

        // Vérifier alignement et contiguïté
        if (marblePositions.size() > 1 && !areMarblesAlignedAndContiguous(marblePositions)) {
            return "Les billes selectionnees ne sont pas alignees ou pas contigues.";
        }

        CellState opponentColor = (playerColor == CellState.BLACK) ? CellState.WHITE : CellState.BLACK;
        List<int[]> inlineChain = tryGetInlineChain(marblePositions, direction);

        if (inlineChain != null) {
            // === MOUVEMENT EN LIGNE (INLINE) ===
            int[] head = inlineChain.get(0);
            int[] target = getNeighbor(head[0], head[1], direction);

            if (!isValidPosition(target[0], target[1])) {
                return "Impossible de deplacer vos billes hors du plateau.";
            }

            Optional<BoardCell> targetCell = getCell(gameId, target[0], target[1]);
            if (targetCell.isEmpty()) {
                return "Position cible invalide.";
            }

            CellState targetState = targetCell.get().getState();

            if (targetState == playerColor) {
                return "Une de vos billes bloque le mouvement.";
            }

            if (targetState == opponentColor) {
                // Compter les billes adverses devant
                int opponentCount = 0;
                int[] current = target;
                while (isValidPosition(current[0], current[1])) {
                    Optional<BoardCell> cell = getCell(gameId, current[0], current[1]);
                    if (cell.isEmpty() || cell.get().getState() != opponentColor) break;
                    opponentCount++;
                    current = getNeighbor(current[0], current[1], direction);
                }

                if (opponentCount >= inlineChain.size()) {
                    return "Pas assez de billes pour pousser (sumito necessite une superiorite numerique).";
                }

                // Vérifier que derrière les adversaires c'est libre ou hors plateau (éjection)
                if (isValidPosition(current[0], current[1])) {
                    Optional<BoardCell> afterCell = getCell(gameId, current[0], current[1]);
                    if (afterCell.isPresent() && afterCell.get().getState() != CellState.EMPTY) {
                        return "Les billes adverses sont bloquees, poussee impossible.";
                    }
                }
            }

        } else {
            // === MOUVEMENT LATÉRAL (BROADSIDE) ===
            for (int[] pos : marblePositions) {
                int[] dest = getNeighbor(pos[0], pos[1], direction);
                if (!isValidPosition(dest[0], dest[1])) {
                    return "Mouvement impossible : une bille sortirait du plateau.";
                }
                Optional<BoardCell> destCell = getCell(gameId, dest[0], dest[1]);
                if (destCell.isEmpty() || destCell.get().getState() != CellState.EMPTY) {
                    return "Mouvement lateral impossible : la destination n'est pas vide.";
                }
            }
        }

        return null; // Valide
    }

    // ========== EXÉCUTION ==========

    public int executeMove(Long gameId, List<int[]> marblePositions, Direction direction, CellState playerColor) {
        CellState opponentColor = (playerColor == CellState.BLACK) ? CellState.WHITE : CellState.BLACK;
        List<int[]> inlineChain = tryGetInlineChain(marblePositions, direction);

        if (inlineChain != null) {
            return executeInlineMove(gameId, inlineChain, direction, playerColor, opponentColor);
        } else {
            executeBroadsideMove(gameId, marblePositions, direction, playerColor);
            return 0;
        }
    }

    private int executeInlineMove(Long gameId, List<int[]> chain, Direction direction,
                                  CellState playerColor, CellState opponentColor) {
        // chain[0] = tête (la plus avancée dans la direction)
        int[] head = chain.get(0);
        int[] target = getNeighbor(head[0], head[1], direction);

        // Compter et collecter les billes adverses devant
        List<int[]> opponentPositions = new ArrayList<>();
        int[] current = target;
        while (isValidPosition(current[0], current[1])) {
            Optional<BoardCell> cell = getCell(gameId, current[0], current[1]);
            if (cell.isEmpty() || cell.get().getState() != opponentColor) break;
            opponentPositions.add(current);
            current = getNeighbor(current[0], current[1], direction);
        }

        // Déterminer si une bille est éjectée
        int ejected = 0;
        if (!opponentPositions.isEmpty()) {
            if (!isValidPosition(current[0], current[1])) {
                ejected = 1; // La dernière bille adverse tombe du plateau
            }

            // Déplacer les adversaires du plus éloigné au plus proche
            for (int i = opponentPositions.size() - 1; i >= 0; i--) {
                int[] pos = opponentPositions.get(i);
                int[] dest = getNeighbor(pos[0], pos[1], direction);
                BoardCell cell = getCell(gameId, pos[0], pos[1]).get();
                cell.setState(CellState.EMPTY);
                boardCellRepository.save(cell);

                if (isValidPosition(dest[0], dest[1])) {
                    BoardCell destCell = getCell(gameId, dest[0], dest[1]).get();
                    destCell.setState(opponentColor);
                    boardCellRepository.save(destCell);
                }
                // Sinon : éjecté, on ne place rien
            }
        }

        // Déplacer les billes du joueur : de la tête vers la queue
        // La queue libère sa case, chaque bille prend la place de celle devant
        for (int i = 0; i < chain.size(); i++) {
            int[] pos = chain.get(i);
            int[] dest = getNeighbor(pos[0], pos[1], direction);

            BoardCell srcCell = getCell(gameId, pos[0], pos[1]).get();
            srcCell.setState(CellState.EMPTY);
            boardCellRepository.save(srcCell);

            BoardCell destCell = getCell(gameId, dest[0], dest[1]).get();
            destCell.setState(playerColor);
            boardCellRepository.save(destCell);
        }

        return ejected;
    }

    private void executeBroadsideMove(Long gameId, List<int[]> marbles, Direction direction, CellState playerColor) {
        // D'abord vider toutes les positions source
        for (int[] pos : marbles) {
            BoardCell cell = getCell(gameId, pos[0], pos[1]).get();
            cell.setState(CellState.EMPTY);
            boardCellRepository.save(cell);
        }
        // Puis remplir toutes les destinations
        for (int[] pos : marbles) {
            int[] dest = getNeighbor(pos[0], pos[1], direction);
            BoardCell destCell = getCell(gameId, dest[0], dest[1]).get();
            destCell.setState(playerColor);
            boardCellRepository.save(destCell);
        }
    }

    // ========== ALIGNEMENT & CONTIGUÏTÉ ==========

    /**
     * Vérifie que les billes sont alignées sur un des 3 axes hex ET contiguës (pas de trou).
     */
    private boolean areMarblesAlignedAndContiguous(List<int[]> positions) {
        if (positions.size() <= 1) return true;

        // Tester les 3 axes (6 directions, mais chaque axe couvre 2 directions opposées)
        Direction[] axes = {Direction.LEFT, Direction.UP_LEFT, Direction.UP_RIGHT};

        for (Direction dir : axes) {
            List<int[]> chain = tryBuildChain(positions, dir);
            if (chain != null) return true;
        }

        return false;
    }

    /**
     * Essaie de construire une chaîne contiguë dans la direction donnée (ou son opposée).
     */
    private List<int[]> tryBuildChain(List<int[]> positions, Direction dir) {
        Set<String> posSet = new HashSet<>();
        for (int[] p : positions) posSet.add(key(p));

        // Trouver un bout de chaîne : la bille dont le voisin dans dir N'EST PAS dans le set
        int[] start = null;
        for (int[] p : positions) {
            int[] neighbor = getNeighbor(p[0], p[1], dir);
            if (!posSet.contains(key(neighbor))) {
                start = p;
                break;
            }
        }
        if (start == null) return null;

        // Chaîner dans la direction opposée
        List<int[]> chain = new ArrayList<>();
        chain.add(start);
        int[] current = start;
        Direction opposite = dir.opposite();

        while (chain.size() < positions.size()) {
            int[] next = getNeighbor(current[0], current[1], opposite);
            if (posSet.contains(key(next))) {
                chain.add(next);
                current = next;
            } else {
                return null;
            }
        }

        return chain;
    }

    // ========== CONDITION DE VICTOIRE ==========

    public boolean checkWinCondition(int ejectedCount) {
        return ejectedCount >= MARBLES_TO_WIN;
    }

    public static int[] getRowSizes() {
        return ROW_SIZES;
    }
}
