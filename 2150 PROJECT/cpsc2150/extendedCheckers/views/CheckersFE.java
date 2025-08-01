
package cpsc2150.extendedCheckers.views;

import cpsc2150.extendedCheckers.models.BoardPosition;
import cpsc2150.extendedCheckers.models.CheckerBoard;
import cpsc2150.extendedCheckers.models.CheckerBoardMem;
import cpsc2150.extendedCheckers.models.ICheckerBoard;
import cpsc2150.extendedCheckers.util.DirectionEnum;

import java.util.*;

/**
 * CheckersFE class serves as the entry point for the Checkers application.
 * The primary responsibility of this class is to initialize and start the application.
 *
 * @Invariant
 * - The class does not maintain any state or fields.
 * - The main method will be the starting point of execution.
 */


public class CheckersFE {
    private static char playerOne;
    private static char playerTwo;


    /**
     * The entry point for the Checkers application.
     * This method initializes the game, handles user input, and manages the game loop.
     *
     * @param args Command-line arguments (not used in this implementation).
     *
     * @pre [The program is executed in a Java environment with standard input/output capabilities.]
     *
     * @post [The application is initialized and ready to run.]
     *       - If valid inputs are provided, a game of Checkers is played between two players.
     *       - At the end of the game, the user can choose to replay or exit.
     *       - [No state is retained after the program exits.]
     */
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;

        System.out.println("Welcome to Checkers!");

        while (playAgain) {
            ICheckerBoard board;
            System.out.println("Player 1, enter your piece: ");
            String input = scanner.nextLine();
            while (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                System.out.println("Please enter only a single lowercase letter character.");
                input = scanner.nextLine();
            }
            playerOne = input.charAt(0);

            System.out.println("Player 2, enter your piece: ");
            String input2 = scanner.nextLine();
            while (input2.length() != 1 || !Character.isLetter(input2.charAt(0))) {
                System.out.println("Please enter only a single lowercase letter character.");
                input2 = scanner.nextLine();
            }
            playerTwo = input2.charAt(0);

            // Ensures the pieces are valid and not identical
            while (playerOne == playerTwo || !Character.isLetter(playerOne) || !Character.isLetter(playerTwo)) {
                System.out.println("Invalid input. Choose unique, valid characters for your pieces.");
                System.out.println("Player 1, enter your piece: ");
                playerOne = scanner.nextLine().charAt(0);
                System.out.println("Player 2, enter your piece: ");
                playerTwo = scanner.nextLine().charAt(0);
            }

            System.out.println("Do you want a fast game (F/f) or a memory efficient game (M/m)?");
            String input3 = scanner.nextLine();
            // Get the board size input
            System.out.println("How big should the board be? It can be 8x8, 10x10, 12x12, 14x14, or 16x16. Enter one number: ");
            int boardSize = Integer.parseInt(scanner.nextLine());

            //Selected board size
            if (input3.equalsIgnoreCase("F")) {
                // Fast game: use an in-memory board
                board = new CheckerBoard(boardSize);
            } else if (input3.equalsIgnoreCase("M")) {
                // Memory-efficient game: use CheckerBoardMem
                board = new CheckerBoardMem(boardSize);
            } else {
                System.out.println("Invalid choice. Please choose F for fast or M for memory efficient.");
                continue;  // Skip to the next loop iteration
            }

            char currentPlayer = playerOne;   // Start with player one

            System.out.println(board);                      // Display the initial board

            boolean gameOver = false;
            while (!gameOver) {

                // Check if the current player has won before proceeding
                if (board.checkPlayerWin(currentPlayer)) {
                    System.out.println("Player " + currentPlayer + " wins!");
                    gameOver = true; // End the game
                    break;           // Exit the game loop
                }
                // Asks the current player to choose a piece to move
                System.out.println("Player " + currentPlayer + " which piece do you wish to move? Enter the row followed by a space followed by the column.");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                BoardPosition pos = new BoardPosition(row, col);

                // Checks if there's a valid piece at the selected position
                char pieceAtPos = board.whatsAtPos(pos);

                // If the selected position doesn't contain the current player's piece, prompt again
                if (pieceAtPos == ' ' || Character.toLowerCase(pieceAtPos) != Character.toLowerCase(currentPlayer)) {
                    System.out.println("Player " + currentPlayer + ", that isn't your piece. Pick one of your pieces.");
                    continue;  // Go back to the beginning of the loop and prompt the user for a new piece
                }

                // Normalize case before comparison
                if (pieceAtPos == ' ' || Character.toLowerCase(pieceAtPos) != Character.toLowerCase(currentPlayer)) {
                    continue;
                }

                // Get available directions for the current player
                ArrayList<DirectionEnum> validDirections = filterValidDirections(board, currentPlayer, pos);

                // If no directions are available, end turn
                if (validDirections == null || validDirections.isEmpty()) {
                    currentPlayer = (currentPlayer == playerOne) ? playerTwo : playerOne; // Switch player
                    continue;
                }

                // Display available directions
                System.out.println("In which direction do you wish to move the piece?");
                System.out.println("Enter one of these options: ");
                for (DirectionEnum dir : validDirections) {
                    System.out.println(dir); // Display each available direction
                }

                // User input for the direction
                String directionInput = scanner.next().toUpperCase();
                DirectionEnum selectedDirection = null;

                try {
                    selectedDirection = DirectionEnum.valueOf(directionInput);
                    if (!validDirections.contains(selectedDirection)) {
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }

                BoardPosition newPos;
                if (isJump(board, currentPlayer, pos, selectedDirection)) {
                    newPos = board.jumpPiece(pos, selectedDirection); // If it's a jump
                } else {
                    newPos = board.movePiece(pos, selectedDirection); // Regular move
                }

                if (currentPlayer == playerOne && newPos.getRow() == board.getRowNum() - 1) {
                    board.crownPiece(newPos);
                } else if (currentPlayer == playerTwo && newPos.getRow() ==  0) {
                    board.crownPiece(newPos);
                }

                // Check if the player wins before updating the board
                if (board.checkPlayerWin(currentPlayer)) {
                    System.out.println("Player " + currentPlayer + " wins!");
                    gameOver = true;
                    break;
                }


                System.out.println(board);  // Display the updated board

                                            // Switch to the other player
                currentPlayer = (currentPlayer == playerOne) ? playerTwo : playerOne;
            }

            // After the game ends, ask if player wants to play again
            System.out.println("Would you like to play again? Enter 'Y'or 'N'");
            char response = scanner.next().charAt(0);
            if (response == 'N' || response == 'n') {
                playAgain = false;
                // Exit the game loop
            }
        }

        scanner.close();
    }



    /**
     * Filters the valid move directions for a given player from a specified position.
     * The method considers whether the piece is crowned (king) and adjusts the possible
     * directions accordingly. It checks if the destination squares are either empty or
     * can be jumped over (if an opponent's piece is present).
     *
     * @param board The current game board, represented as an ICheckerBoard object.
     * @param player The current player, represented as a char.
     * @param pos The position of the piece to check from, represented as a BoardPosition object.
     *
     * @return A list of valid directions the player can move to from the given position,
     *         represented as an ArrayList of DirectionEnum values.
     *
     * @pre board is an initialized game board; player is a valid character;
     *      pos is a valid BoardPosition object representing the current position of the piece.
     *
     * @post [Returns a list of valid DirectionEnum values representing directions
     *       the player can move to, based on the piece's position, whether it's crowned,
     *       and whether jumps are possible in the given directions.]
     */

    public static ArrayList<DirectionEnum> filterValidDirections(ICheckerBoard board, char player, BoardPosition pos) {
        char pieceAtPos = board.whatsAtPos(pos);
        boolean isKing = Character.isUpperCase(pieceAtPos);

        // Retrieve all directions if the piece is crowned; otherwise, limit directions
        ArrayList<DirectionEnum> possibleDirections = isKing
                ? new ArrayList<>(List.of(DirectionEnum.NE, DirectionEnum.SE, DirectionEnum.NW, DirectionEnum.SW))
                : board.getViableDirections().get(player);

        // Create a new list to store valid directions
        ArrayList<DirectionEnum> validDirections = new ArrayList<>();

        // Iterate through all directions to add valid moves or jumps
        for (DirectionEnum direction : possibleDirections) {
            int row = pos.getRow();
            int col = pos.getColumn();

            switch (direction) {
                case NE:
                    if (row - 1 >= 0 && col + 1 < board.getColNum() && board.whatsAtPos(new BoardPosition(row - 1, col + 1)) == CheckerBoard.EMPTY_POS) {
                        validDirections.add(direction);
                    }
                    if (row - 2 >= 0 && col + 2 < board.getColNum()) {
                        char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                        if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row - 1, col + 1))) == opponent &&
                                board.whatsAtPos(new BoardPosition(row - 2, col + 2)) == CheckerBoard.EMPTY_POS)
                        {
                            validDirections.add(direction);
                        }
                    }
                    break;

                case SE:
                    if (row + 1 < board.getColNum() && col + 1 < board.getColNum() && board.whatsAtPos(new BoardPosition(row + 1, col + 1)) == CheckerBoard.EMPTY_POS) {
                        validDirections.add(direction);
                    }
                    if (row + 2 < board.getColNum() && col + 2 < board.getColNum()) {
                        char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                        if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row + 1, col + 1))) == opponent &&
                                board.whatsAtPos(new BoardPosition(row + 2, col + 2)) == CheckerBoard.EMPTY_POS) {
                            validDirections.add(direction);
                        }
                    }
                    break;

                case NW:
                    if (row - 1 >= 0 && col - 1 >= 0 && board.whatsAtPos(new BoardPosition(row - 1, col - 1)) == CheckerBoard.EMPTY_POS) {
                        validDirections.add(direction);
                    }
                    if (row - 2 >= 0 && col - 2 >= 0) {
                        char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                        if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row - 1, col - 1))) == opponent &&
                                board.whatsAtPos(new BoardPosition(row - 2, col - 2)) == CheckerBoard.EMPTY_POS) {
                            validDirections.add(direction);
                        }
                    }
                    break;

                case SW:
                    if (row + 1 < board.getRowNum() && col - 1 >= 0 && board.whatsAtPos(new BoardPosition(row + 1, col - 1)) == CheckerBoard.EMPTY_POS) {
                        validDirections.add(direction);
                    }
                    if (row + 2 < board.getRowNum() && col - 2 >= 0) {
                        char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                        if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row + 1, col - 1))) == opponent &&
                                board.whatsAtPos(new BoardPosition(row + 2, col - 2)) == CheckerBoard.EMPTY_POS) {
                            validDirections.add(direction);
                        }
                    }
                    break;
            }
        }

        // Return the new list with valid directions
        return validDirections;
    }

    /**
     * Checks if a valid jump is possible in the specified direction for a given player.
     * A jump is considered valid if there is an opponent's piece in the intermediate position
     * and the landing position is empty.
     *
     * @param board The current game board, represented as an ICheckerBoard object.
     * @param player The current player, represented as a char.
     * @param startPos The starting position of the piece to check from, represented as a BoardPosition object.
     * @param direction The direction of the potential jump, represented as a DirectionEnum value.
     *
     * @return true if a valid jump is possible in the specified direction; false otherwise.
     *
     * @pre board is an initialized game board; player is a valid character;
     *      startPos is a valid BoardPosition object representing the position of the piece;
     *      direction is a valid DirectionEnum value representing the direction of the jump.
     *
     * @post [Returns true if a valid jump exists in the specified direction (the jump
     *       is valid based on the piece's position and the presence of an opponent's piece
     *       in the intermediate position and an empty landing position); false otherwise.]
     */

    private static boolean isJump(ICheckerBoard board, char player, BoardPosition startPos, DirectionEnum direction) {
        int row = startPos.getRow();
        int col = startPos.getColumn();

        switch (direction) {
            case NE:
                if (row - 2 >= 0 && col + 2 < board.getColNum()) {
                    char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                    if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row - 1, col + 1))) == opponent &&
                            board.whatsAtPos(new BoardPosition(row - 2, col + 2)) == CheckerBoard.EMPTY_POS) {
                        return true;
                    }
                }
                break;

            case SE:
                if (row + 2 < board.getRowNum() && col + 2 < board.getColNum()) {
                    char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                    if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row + 1, col + 1))) == opponent &&
                            board.whatsAtPos(new BoardPosition(row + 2, col + 2)) == CheckerBoard.EMPTY_POS) {
                        return true;
                    }
                }
                break;

            case NW:
                if (row - 2 >= 0 && col - 2 >= 0) {
                    char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                    if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row - 1, col - 1))) == opponent &&
                            board.whatsAtPos(new BoardPosition(row - 2, col - 2)) == CheckerBoard.EMPTY_POS) {
                        return true;
                    }
                }
                break;

            case SW:
                if (row + 2 < board.getRowNum() && col - 2 >= 0) {
                    char opponent = (Character.toLowerCase(player) == CheckersFE.getPlayerOne()) ? CheckersFE.getPlayerTwo() : CheckersFE.getPlayerOne();
                    if (Character.toLowerCase(board.whatsAtPos(new BoardPosition(row + 1, col - 1))) == opponent &&
                            board.whatsAtPos(new BoardPosition(row + 2, col - 2)) == CheckerBoard.EMPTY_POS) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }


    /**
     * Standard getter for player one
     *
     * @return The character of playerOne.
     *
     * @post [Returns the character that represents playerOne.]
     */
    public static char getPlayerOne()
    {
        return playerOne;
    }


    /**
     * Standard getter for player two
     *
     * @return The character of playerTwo.
     *
     * @post [Returns the character that represents playerTwo.]
     */
    public static char getPlayerTwo()
    {
        return playerTwo;
    }
}

