package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.ArrayList;
import java.util.HashMap;



/**
 * ICheckerBoard is an interface for a typical checkerboard. It defines the behaviors expected
 * of any class implementing it, such as managing pieces, checking valid moves, and determining wins.
 * This interface serves as the contract for the CheckerBoard class, which implements the core logic
 * of a checkers game.
 *
 * @initialization_ensures:
 *          initialize self to exist, while board is initialized to a getRowNum x getColNum array grid
 *          in their starting positions and empty spaces where applicable. pieceCount is initialized to have x amount of pieces
 *          depending on the dimensions of the board for each player. viableDirections is initialized with getPlayerOne mapped to SE and SW, and getPlayerTwo mapped
 *           to NE and NW. Kings for both players mapped to SE,SW,NE,NW.
 *
 * @defines:
 *          self: the checkerboard object
 *          board: representation of the checkerboard layout
 *          pieceCount: tracks the number of pieces for each player
 *          viableDirections: valid movement directions for each player
 *
 * @constraints:
 *          board must always be  getRowNum x getColNum
 *          pieceCount must only contain exactly two entries, player, AND pieceCount > 0
 *          viableDirections for each player must be valid
 *          Kings must be able to move in all directions
 */

public interface ICheckerBoard {
    public static final int FIRST_ROW       = 0;
    public static final int FIRST_COLUMN    = 0;
    public static final int JUMP_DISTANCE   = 2;

    public static final int ONE_ROW_UP      =-1;
    public static final int ONE_ROW_DOWN    = 1;
    public static final int ONE_COLUMN_LEFT =-1;
    public static final int ONE_COLUMN_RIGHT= 1;

    public static final int NO_PIECES_LEFT  = 0;



    /**
     * Returns a mapping that shows which directions each player can move their pieces.
     * Each player has specific directions they can move: getPlayerOne pieces move southeast and
     * southwest, while getPlayerTwo pieces move northeast and northwest. Kings contain all directions.
     *
     * @return A HashMap where each player character maps to their allowed movement directions
     *
     * @pre none
     *
     * @post getViableDirections = [A HashMap containing movement directions] where:
     *      -getPlayerOne maps to [SE, SW] directions
     *      -getPlayerTwo maps to [NE, NW] directions
     *      AND self = #self
     */
    public HashMap<Character, ArrayList<DirectionEnum>> getViableDirections();


    /**
     * Maintains and returns a count of how many pieces each player currently has on the board.
     *
     * @return A HashMap containing the current piece count for each player
     *
     * @pre none
     *
     * @post getPieceCounts = [A HashMap containing the current piece counts] where:
     *      -Keys are player characters
     *      -Values are non-negative integers representing piece counts
     *      AND self = #self
     */

    public HashMap<Character, Integer> getPieceCounts();


    /**
     * Places a piece on the board at the specified position. The piece can be a player piece,
     * an empty space (' '), or a black tile marker ('*')
     *
     * @param pos the position on the board where the piece will be placed
     * @param player the character to place at the position
     *
     * @pre pos is within the bounds of the board AND
     *      player is a valid character
     *
     * @post [the character at position] pos = player AND
     *      [all other board positions remain unchanged]
     */

    public void placePiece(BoardPosition pos, char player);


    /**
     * Returns the character representation of the piece at the specified board
     *
     * @param pos is the position on the board to check, represented as a BoardPosition object
     * @return The character of the piece at the specified position
     *
     * @pre pos is within the bounds of the board
     *
     * @post [returns the character of the piece at the specified position]
     */

    public char whatsAtPos(BoardPosition pos);

    /**
     * Returns the number of rows in the checkerboard
     *
     * @return The number of rows in the board (ROW_NUM)
     *
     * @pre none
     *
     * @post getRowNum = ROW_NUM AND self = #self
     */

    public int getRowNum();

    /**
     * Returns the total number of columns in the checkerboard
     *
     * @return The total number of columns in the board (COL_NUM)
     *
     * @pre none
     *
     * @post getColNum = COL_NUM AND self = #self
     */

    public int getColNum();



    /**
     * Promotes a piece to a king if it reaches the opposite end of the board.
     *
     * @param posOfPlayer is the position of the piece to potentially crown, represented as a BoardPosition object
     *
     * @return void
     *
     * @pre posOfPlayer is within the bounds of the board AND
     *      whatsAtPos(posOfPlayer) is a valid player character AND
     *      posOfPlayer.getRow() is the first row for player one or the last row for player two
     *
     * @post [the piece at posOfPlayer is promoted to a king]
     */

    default public void crownPiece(BoardPosition posOfPlayer) {
        char player = whatsAtPos(posOfPlayer); //gets curr position of player
        placePiece(posOfPlayer, Character.toUpperCase(player)); //simply places the player and crowns
    }

    /**
     * Moves a piece from the starting position to a new position based on the given direction.
     *
     * @param startingPos the initial position of the game piece, represented as a BoardPosition object
     * @param dir the direction in which the game piece should be moved, represented as a DirectionEnum value
     * @return The new position of the game piece after the move or jump
     *
     * @pre startingPos is within the bounds of the board AND
     *      the direction is a valid DirectionEnum value
     *
     * @post [The piece is moved or jumped from the starting position to the destination position.]
     *
     */
    default public BoardPosition movePiece(BoardPosition startingPos, DirectionEnum dir)
    {
        // Get the direction to move
        BoardPosition newPos = getDirection(dir); //gets direction "logic" ~offset

        // Calculate the destination position for a regular move (one step in the specified direction)
        BoardPosition destination = new BoardPosition(startingPos.getRow() + newPos.getRow(),
                                                      startingPos.getColumn() + newPos.getColumn());
        //Actually move the piece
        char player = whatsAtPos(startingPos);
        placePiece(startingPos, ' ');
        placePiece(destination, player);

        // Returns the destination ~values
        return destination;
    }



    /**
     * Jumps a piece from the starting position to a landing position in the specified direction.
     *
     * @param startingPos the position of the piece to jump from, representing as a BoardPosition object
     * @param dir the direction to jump in, represented as a DirectionEnum value
     *
     * @return the BoardPosition where the piece landed are the jump
     *
     * @pre startingPos is occupied within the bounds of the board AND the direction is a valid DirectionEnum value
     * AND there is an opponent's piece at the intermediate position AND the landing position is empty
     *
     * @post [the piece is moved from the starting position to the landing position]
     * AND startingPos = ' ' AND middlePos = ' ' AND landingPos = currentPiece AND pieceCount -= 1
     *
     */
    default public BoardPosition jumpPiece(BoardPosition startingPos, DirectionEnum dir)
    {
        BoardPosition newPos = getDirection(dir);         // Get direction values to jump

        // Calculate the landing position (double the direction from the starting position)
        BoardPosition landingPos = new BoardPosition(startingPos.getRow() + JUMP_DISTANCE  * newPos.getRow(),
                                                startingPos.getColumn() + JUMP_DISTANCE * newPos.getColumn());
        //Calculate middle position (piece being jumped over)
        BoardPosition middlePos = new BoardPosition((startingPos.getRow()) + newPos.getRow(),
                                                startingPos.getColumn() + newPos.getColumn());

        //Actually jump the piece
        char player = whatsAtPos(startingPos);
        char jumpedPiece = whatsAtPos(middlePos);

        placePiece(startingPos, ' ');
        placePiece(middlePos, ' ');
        getPieceCounts().put(jumpedPiece, getPieceCounts().get(jumpedPiece) - 1);
        placePiece(landingPos, player);

        //Return landing position ~values
        return landingPos;
    }




    /**
     * Determines if the specified player has won the game by checking if the opposing player
     * has no remaining pieces on the board.
     *
     * @param player the player to check for victory
     *
     *@return true if the opposing player has zero pieces remaining on the board; false otherwise
     *
     * @pre none
     *
     * @post checkPlayerWin = true IF getPieceCounts().get(opponent) == NO_PIECES_LEFT AND self = #self
     *
     */
    default public boolean checkPlayerWin(Character player)
    {
        // Check if the opponent has no pieces left
        char opponent = (player == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
        return getPieceCounts().get(opponent) == NO_PIECES_LEFT;
    }



    /**
     * Scans and retrieves the positions immediately surrounding a given starting position on the board,
     * returning the pieces located in each valid direction from that position
     *
     * @param startingPos the position on the board to scan around, represented as a BoardPosition object
     *
     * @return A HashMap where each DirectionEnum key maps to a character representing the piece at the corresponding
     * position surrounding startingPos.
     *
     * @pre startingPos is within the bounds of the board
     *
     * @post self = #self AND scanSurroundingPositions = [A HashMap with entries for each non-empty surrounding position] where:
     *      -Keys are directions (DirectionEnum values) pointing to positions adjacent to startingPos
     *      -Values are character's representing pieces or an empty space in those directions
     *
     */
    default public HashMap<DirectionEnum, Character> scanSurroundingPositions(BoardPosition startingPos)
    {
        HashMap<DirectionEnum, Character> surroundingPositions = new HashMap<>();
        DirectionEnum[] directions = DirectionEnum.values();

        for (DirectionEnum dir : directions) { //iterate through each value dir in directions^^
            BoardPosition offset = getDirection(dir); //gets direction "logic" ~offset
            BoardPosition newPos = new BoardPosition(startingPos.getRow() + offset.getRow(),
                                                startingPos.getColumn() + offset.getColumn());
            //Check if newPos is within bounds: crash prevention
            if(newPos.getRow() >= FIRST_ROW && newPos.getRow() < getRowNum() &&
            newPos.getColumn() >= FIRST_COLUMN && newPos.getColumn() < getColNum())
            {
                surroundingPositions.put(dir, whatsAtPos(newPos));
            }
        }
        return surroundingPositions;
    }



    /**
     * Returns the BoardPosition corresponding to the given direction relative to a starting position.
     * This implementation checks if the piece is a king and calculates the new position accordingly,
     * as well as handling player-specific movement constraints.
     *
     * @param dir the direction (DirectionEnum) to get the relative BoardPosition for.
     *
     * @return The BoardPosition representing the new position relative to the starting position.
     * OR null if the direction is invalid
     */

    public static BoardPosition getDirection(DirectionEnum dir)
    {

        return switch (dir) {
            case NE -> new BoardPosition(ONE_ROW_UP, ONE_COLUMN_RIGHT);  // Move NE
            case NW -> new BoardPosition(ONE_ROW_UP, ONE_COLUMN_LEFT);   // Move NW
            case SE -> new BoardPosition(ONE_ROW_DOWN, ONE_COLUMN_RIGHT); // Move SE
            case SW -> new BoardPosition(ONE_ROW_DOWN, ONE_COLUMN_LEFT);  // Move SW
            default -> null; // Invalid direction
        };
    }

}
