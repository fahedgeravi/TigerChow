package cpsc2150.extendedCheckers.models;

/**
 * AbsCheckerBoard is an abstract class that implements the ICheckerBoard interface.
 * It serves as the base class for specific board implementations, such as CheckerBoard.
 * providing default implementation for the toString method, which
 * outputs a string representation of the game board.
 *
 * @Corresponds The AbsCheckerBoard object (self) is responsible for providing the
 *              common functionality that all concrete board classes must implement,
 *              such as rendering the board to a string format.
 *
 * @defines BOARD_DIMENSION: The dimension of the square board, (8x8, 10x10, 12x12, 14x14, or 16x16).
 *
 * @Invariant The toString method provides a string that represents the current state of the board,
 *            with rows and columns labeled, and the appropriate pieces (any single lowercase letter of Player One's
 *            choice, same goes for Player Two)
 *            placed on their respective positions. The black tiles are denoted by '*' and empty spaces by ' '.
 */

public abstract class AbsCheckerBoard implements ICheckerBoard {

    private int dimensionCheck;


    /**
     * Providing a string representation of current state of checkerboard.
     *
     * @return A formatted string showing current board state with row and column labels
     *
     * @pre none
     *
     * @post toString = [A string representation of the current board state with:
     *       - Column numbers as the headers
     *       - Row numbers as the labels
     *       - Current positions of total pieces
     *       - Black tiles marked with '*'
     *       - Empty positions marked with ' '] AND
     *       self = #self
     */

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder("|  |");

        // Print column numbers with right alignment
        for (int col = 0; col < getColNum(); col++) {
            boardString.append(String.format("%2d", col)).append("|");
        }
        boardString.append("\n");

        // Iterate over each row to print row data
        for (int row = 0; row < getRowNum(); row++) {
            // Row label with consistent spacing
            boardString.append("|").append(String.format("%-2d", row));


            // Iterate over each column in the current row
            for (int col = 0; col < getColNum(); col++) {
                BoardPosition pos = new BoardPosition(row, col);
                char piece = whatsAtPos(pos);  // Get the piece at the position

                // If there's no piece, display the background pattern: alternating black ('*') and white (' ')
                if (piece == ' ') {
                    // Alternating pattern for black and white tiles
                    if ((row + col) % 2 == 0) {
                        piece = ' ';  // White tile (empty space)
                    } else {
                        piece = '*';  // Black tile
                    }
                }

                // Append the piece (or tile) with no spaces before it, just the frame
                boardString.append("|").append(piece).append(" "); // Add piece
            }

            // End of the row
            boardString.append("|\n");
        }

        return boardString.toString();
    }


}

