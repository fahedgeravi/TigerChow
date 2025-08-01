package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * CheckerBoard is an implementation of the ICheckerBoard interface.
 *
 * The board is represented by dynamic ROW_NUM x COL_NUM grid (2D array) initialized with empty spaces and black tiles.
 * Pieces for two players, represented by player one and player two, are placed on white tiles.
 *
 * The pieceCount HashMap keeps track of the number of pieces each player has on the board. It should
 * always contain mappings for player one and player two.
 *
 * The viableDirections HashMap stores the valid directions a player can move, depending on whether
 * the piece is a regular piece or a kinged piece.
 *
 * @Corresponds The CheckerBoard object (self) is responsible for maintaining the state of the board.
 * The 'board' 2D array holds the positions and pieces for each player.
 *
 * @defines pieceCount: A HashMap mapping each player's character to the number of pieces they have.
 *          viableDirections: A HashMap mapping each player (or piece type) to their possible movement directions.
 */
public class CheckerBoard extends AbsCheckerBoard
{
    private char[][] board;
    private HashMap<Character, Integer> pieceCount;

    private HashMap<Character, ArrayList<DirectionEnum>> viableDirections;
    public static final char EMPTY_POS = ' ';
    private final int ROW_NUM;
    private final int COL_NUM;
    public static final int NO_PIECES_LEFT = 0;

    /**
     * Constructor for initializing the CheckerBoard with specified dimensions.
     *
     * @param aDimensions the size of the board (both width and height)
     *
     * @pre aDimensions >= 8 AND aDimensions <= 16 AND aDimensions % 2 == 0
     *
     * @post [Board is initialized as an aDimensions x aDimensions grid] AND
     *       [Player pieces are placed on white tiles] AND
     *       [Player One starts with ((aDimensions/2 - 1) * (aDimensions/2)) pieces] AND
     *       [Player Two starts with ((aDimensions/2 - 1) * (aDimensions/2)) pieces] AND
     *       ROW_NUM = aDimensions AND COL_NUM = aDimensions AND
     *       [pieceCount is initialized with starting counts] AND
     *       [viableDirections is initialized with valid moves for each player type]
     */

    public CheckerBoard(int aDimensions) {
        // Initialize the board dimensions and constants
        ROW_NUM = aDimensions;
        COL_NUM = aDimensions;
        board = new char[ROW_NUM][COL_NUM];
        pieceCount = new HashMap<>();
        viableDirections = new HashMap<>();

        int startingCount = (ROW_NUM / 2 - 1) * (COL_NUM / 2);

        // Initialize piece count for both players
        pieceCount.put(CheckersFE.getPlayerOne(), startingCount);
        pieceCount.put(CheckersFE.getPlayerTwo(), startingCount);
        pieceCount.put(Character.toUpperCase(CheckersFE.getPlayerOne()), startingCount); // Player One Kings
        pieceCount.put(Character.toUpperCase(CheckersFE.getPlayerTwo()), startingCount); // Player Two Kings

        // Initialize directions for each player
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

        // Set initial viable directions for each player
        viableDirections.put(CheckersFE.getPlayerOne(), playerOneDirections);
        viableDirections.put(CheckersFE.getPlayerTwo(), playerTwoDirections);
        viableDirections.put(Character.toUpperCase(CheckersFE.getPlayerOne()), kingDirections);
        viableDirections.put(Character.toUpperCase(CheckersFE.getPlayerTwo()), kingDirections);



        // Initialize board with empty spaces (' ') 
        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COL_NUM; j++) {
                if ((i + j) % 2 == 0) {
                    board[i][j] = ' ';  // Empty space
                } else {  // Black tile
                    board[i][j] = '*'; // Black tile
                }
            }
        }

        // Place initial pieces for both players, but only on white tiles (' ')
        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COL_NUM; j++) {
                if ((i + j) % 2 == 0) {  // Only place on white tiles (' ')
                    if (i < ROW_NUM / 2 - 1) {
                        board[i][j] = CheckersFE.getPlayerOne();  // Player One's pieces
                    } else if (i >= ROW_NUM / 2 + 1) {
                        board[i][j] = CheckersFE.getPlayerTwo();  // Player Two's pieces
                    }
                }
            }
        }
    }

    @Override
    public HashMap<Character, ArrayList<DirectionEnum>> getViableDirections() {
        return viableDirections;
    }


    @Override
    public HashMap<Character, Integer> getPieceCounts() {
        return pieceCount;
    }

    @Override
    public void placePiece(BoardPosition pos, char player) {
        int row = pos.getRow();
        int col = pos.getColumn();
        board[row][col] = player;  //place player piece
    }

    @Override
    public char whatsAtPos(BoardPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return board[row][col];
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
