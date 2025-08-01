package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.*;

/**
 * CheckerBoardMem is a memory-efficient implementation of the ICheckerBoard interface.
 * This class stores the board positions using a Map, where each player’s pieces are represented by a list
 * of BoardPosition objects. The board itself is a grid of alternating black and white tiles, with pieces
 * placed on the white tiles initially.
 *
 * @Corresponds The CheckerBoardMem object (self) manages the game state for both players,
 *              tracking the positions of their pieces and the current piece counts.
 *              It supports the movement and kinging of pieces as per the rules of checkers.
 *
 * @defines ROW_NUM: The number of rows in the board.
 *          COL_NUM: The number of columns in the board.
 *          EMPTY_POS: A constant representing an empty space (' ') on the board.
 *          BLACK_TILE: A constant representing the black tiles ('*') on the board.
 *
 * @Invariant The board is represented by a Map where each player's pieces and their kings are tracked
 *            in separate lists of BoardPosition objects. The pieceCount Map tracks the number of pieces
 *            and kings for each player, while the viableDirections Map stores the valid movement directions
 *            for each player and their kings. The board must always maintain valid positions for each piece
 *            and ensure that players' pieces can move according to the rules.
 */

public class CheckerBoardMem extends AbsCheckerBoard {

    private int ROW_NUM;
    private int COL_NUM;


    //Contains key, the value is a list of board position objects occupied by each key(player)
    private Map<Character, List<BoardPosition>> board;
    private HashMap<Character, Integer> pieceCount;
    private HashMap<Character, ArrayList<DirectionEnum>> viableDirections;

    public static final int NO_PIECES_LEFT = 0;
    public static final char EMPTY_POS = ' ';
    public static final char BLACK_TILE = '*';
    public static final int ODD_TILE_CHECK_MODULO = 2;  // Modulo used to check black tiles
    public static final int ROW_BOUNDARY = 0;          // Minimum row value
    public static final int COL_BOUNDARY = 0;          // Minimum column value

    /**
     * Constructor for initializing the Memory-Efficient CheckerBoard with given dimensions.
     *
     * @param aDimensions the size of the board (both width and height)
     *
     * @pre aDimensions >= 8 AND aDimensions <= 16 AND aDimensions % 2 == 0
     *
     * @post [The board is initialized with alternating black (*) and white (' ') tiles] AND
     *       [Player One's pieces are placed on rows 0 through (aDimensions / 2 - 2)] AND
     *       [Player Two's pieces are placed on rows (aDimensions / 2 + 1) through (aDimensions - 1)] AND
     *       [Empty white tiles are set to EMPTY_POS (' ')] AND
     *       [Black tiles are set to BLACK_TILE ('*')] AND
     *       ROW_NUM = aDimensions AND COL_NUM = aDimensions AND
     *       [Player One and Two piece counts are initialized to ((aDimensions / 2 - 1) * (aDimensions / 2)) each] AND
     *       [Kings' piece counts are set to 0] AND
     *       [Viable directions for regular pieces and kings are set]
     */

    public CheckerBoardMem(int aDimensions) {
        ROW_NUM = aDimensions;
        COL_NUM = aDimensions;
        board = new HashMap<>();
        pieceCount = new HashMap<>();
        viableDirections = new HashMap<>();

        // Initialize empty lists for each player and their kings
        board.put(CheckersFE.getPlayerOne(), new ArrayList<>());  // Player one's positions
        board.put(CheckersFE.getPlayerTwo(), new ArrayList<>());  // Player two's positions
        board.put(Character.toUpperCase(CheckersFE.getPlayerOne()), new ArrayList<>()); // Player one kings' positions
        board.put(Character.toUpperCase(CheckersFE.getPlayerTwo()), new ArrayList<>()); // Player two kings' positions


        // Simply initialize piece count for both players
        pieceCount.put(CheckersFE.getPlayerOne(), NO_PIECES_LEFT);
        pieceCount.put(CheckersFE.getPlayerTwo(), NO_PIECES_LEFT);
        pieceCount.put(Character.toUpperCase(CheckersFE.getPlayerOne()), NO_PIECES_LEFT); // Player One Kings
        pieceCount.put(Character.toUpperCase(CheckersFE.getPlayerTwo()), NO_PIECES_LEFT); // Player Two Kings

        // Initialize directions for each player and their kings
        ArrayList<DirectionEnum> playerOneDirections = new ArrayList<>();
        playerOneDirections.add(DirectionEnum.SE);
        playerOneDirections.add(DirectionEnum.SW);

        ArrayList<DirectionEnum> playerTwoDirections = new ArrayList<>();
        playerTwoDirections.add(DirectionEnum.NE);
        playerTwoDirections.add(DirectionEnum.NW);

        ArrayList<DirectionEnum> kingDirections = new ArrayList<>();
        kingDirections.add(DirectionEnum.SE);
        kingDirections.add(DirectionEnum.SW);
        kingDirections.add(DirectionEnum.NE);
        kingDirections.add(DirectionEnum.NW);

        // Set initial directions
        viableDirections.put(CheckersFE.getPlayerOne(), playerOneDirections);
        viableDirections.put(CheckersFE.getPlayerTwo(), playerTwoDirections);
        viableDirections.put(Character.toUpperCase(CheckersFE.getPlayerOne()), kingDirections);
        viableDirections.put(Character.toUpperCase(CheckersFE.getPlayerTwo()), kingDirections);


        // Initialize the board with empty spaces (' ') and place pieces for both players
        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COL_NUM; j++) {
                if ((i + j) % 2 == 0) {  // Only place on white tiles (' ')
                    if (i < ROW_NUM / 2 - 1) {
                        // Player One's pieces
                        BoardPosition pos = new BoardPosition(i, j);
                        this.board.get(CheckersFE.getPlayerOne()).add(pos);
                        pieceCount.put(CheckersFE.getPlayerOne(), pieceCount.get(CheckersFE.getPlayerOne()) + 1);
                    } else if (i >= ROW_NUM / 2 + 1) {
                        // Player Two's pieces
                        BoardPosition pos = new BoardPosition(i, j);
                        this.board.get(CheckersFE.getPlayerTwo()).add(pos);
                        pieceCount.put(CheckersFE.getPlayerTwo(), pieceCount.get(CheckersFE.getPlayerTwo()) + 1);
                    }
                }
            }
        }
    }

    @Override
    public HashMap<Character, ArrayList<DirectionEnum>> getViableDirections() {
        return this.viableDirections;
    }


    @Override
    public HashMap<Character, Integer> getPieceCounts() {
        return this.pieceCount;
    }

    @Override
    public void placePiece(BoardPosition pos, char player) {
        char currentPiece = whatsAtPos(pos); // Get the current piece at the position
        if (player == EMPTY_POS) {
            // Remove a piece from the board
            if (currentPiece != EMPTY_POS && currentPiece != BLACK_TILE) {
                this.board.get(currentPiece).remove(pos);
            }
        } else {
            // Place a new piece on the board
            if (currentPiece != EMPTY_POS && currentPiece != BLACK_TILE) {
                // Remove the existing piece from the position
                this.board.get(currentPiece).remove(pos);
            }
            // Add the new piece
            this.board.get(player).add(pos);
            if (player == CheckersFE.getPlayerOne() && pos.getRow() == ROW_NUM - 1) {
                // King Player One's piece
                this.board.get(CheckersFE.getPlayerOne()).remove(pos);
                this.board.get(Character.toUpperCase(CheckersFE.getPlayerOne())).add(pos);
                getPieceCounts().put(CheckersFE.getPlayerOne(), getPieceCounts().get(CheckersFE.getPlayerOne()) - 1);
                getPieceCounts().put(Character.toUpperCase(CheckersFE.getPlayerOne()), getPieceCounts().getOrDefault(Character.toUpperCase(CheckersFE.getPlayerOne()), 0) + 1);
            } else if (player == CheckersFE.getPlayerTwo() && pos.getRow() == 0) {
                // King Player Two's piece
                this.board.get(CheckersFE.getPlayerTwo()).remove(pos);
                this.board.get(Character.toUpperCase(CheckersFE.getPlayerTwo())).add(pos);
                getPieceCounts().put(CheckersFE.getPlayerTwo(), getPieceCounts().get(CheckersFE.getPlayerTwo()) - 1);
                getPieceCounts().put(Character.toUpperCase(CheckersFE.getPlayerTwo()), getPieceCounts().getOrDefault(Character.toUpperCase(CheckersFE.getPlayerTwo()), 0) + 1);
            }
        }
    }

    @Override
    public char whatsAtPos(BoardPosition pos) {
        if((pos.getRow() + pos.getColumn()) % ODD_TILE_CHECK_MODULO == 1 || pos.getRow() >= this.getRowNum() || pos.getRow() < ROW_BOUNDARY
                || pos.getColumn() >= this.getColNum() || pos.getColumn() < COL_BOUNDARY)
        {
            return BLACK_TILE;
        }
        if(this.board.get(CheckersFE.getPlayerOne()).contains(pos))
        {
            return CheckersFE.getPlayerOne();
        }
        else if (this.board.get(CheckersFE.getPlayerTwo()).contains(pos))
        {
            return CheckersFE.getPlayerTwo();
        }
        else if(this.board.get(Character.toUpperCase(CheckersFE.getPlayerOne())).contains(pos))
        {
            return Character.toUpperCase(CheckersFE.getPlayerOne());
        }
        else if (this.board.get(Character.toUpperCase(CheckersFE.getPlayerTwo())).contains(pos))
        {
            return Character.toUpperCase(CheckersFE.getPlayerTwo());
        }
        else
        {
            return EMPTY_POS;
        }
    }


    @Override
    public int getRowNum()
    {
        return ROW_NUM;
    }

    @Override
    public int getColNum()
    {
        return COL_NUM;
    }
}
