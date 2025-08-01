package cpsc2150.extendedCheckers.tests;

import cpsc2150.extendedCheckers.models.*;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestCheckerBoard
{

    private String boardToString(char[][] board) {
        StringBuilder boardToString = new StringBuilder("|  |");
        int numRows = board.length;
        int numCols = board[0].length;

        //Create column headers
        for (int i = 0; i < numCols; i++) {
            if (i < 10) {
                boardToString.append(" ").append(i).append("|");
            } else {
                boardToString.append("|");
            }
        }
        boardToString.append("\n");

        for (int i = 0; i < numRows; i++) {
            if (i < 10) {
                boardToString.append("|").append(i).append(" ");
            } else {
                boardToString.append("|").append(i);
            }

            for (int j = 0; j < numCols; j++) {
                boardToString.append("|");
                char pos = board[i][j];
                if (pos != '*') {
                    boardToString.append(pos).append(" ");
                } else {
                    boardToString.append("* ");
                }
            }
            boardToString.append("|\n");
        }
        return boardToString.toString();
    }


    private ICheckerBoard makeBoard(int dim)
    {
        return new CheckerBoard(dim);
    }

    @Test
    public void testConstructor_DefaultBoard_8x8() {
        ICheckerBoard cb = makeBoard(8);

        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };


        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_whatsAtPos_EmptySquare_row3_column1() {
        ICheckerBoard cb = makeBoard(8);
        assertEquals(' ', cb.whatsAtPos(new BoardPosition(3, 1)));
    }

    @Test
    public void Test_whatsAtPos_BlackPiece_row0_column0() {
        ICheckerBoard cb = makeBoard(8);
        assertEquals('x', cb.whatsAtPos(new BoardPosition(0, 0)));

}

    @Test
    public void Test_whatsAtPos_WhitePiece_row5_column1(){
        ICheckerBoard cb = makeBoard(8);
        assertEquals('o', cb.whatsAtPos(new BoardPosition(5, 1)));

    }

    @Test
    public void Test_whatsAtPos_Black_Tile_row0_column1() {
        ICheckerBoard cb = makeBoard(8);
        assertEquals('*', cb.whatsAtPos(new BoardPosition(0, 1)));

    }

    @Test
    public void Test_whatsAtPos_BlackPiece_row7_column7(){
        ICheckerBoard cb = makeBoard(8);
        assertEquals('o', cb.whatsAtPos(new BoardPosition(7, 7)));
    }

    @Test
    public void TestPlacePiece_ValidPlacement_row3_column1() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 1);

        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', ' ', '*', ' ', '*', ' '}, //'x' placed at (3,1)
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        cb.placePiece(pos, 'x');

        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_placePiece_InvalidPlacement_Black_Tile_row0_column1() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(0, 1); //  new BoardPosition for (0, 1)

        cb.placePiece(pos, 'x');

        char[][] expectedBoard = {
                {'x', 'x', 'x', '*', 'x', '*', 'x', '*'}, //'x' placed at (0,1)
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        assertEquals(boardToString(expectedBoard), cb.toString());

    }

    @Test
    public void Test_placePiece_OccupiedSquare_row3_column3(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 3); //  new BoardPosition for (3, 3)

        // Initial placement of piece
        cb.placePiece(pos, 'X');

        // Expected board after initial placement at (3,3)
        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', 'X', '*', ' ', '*', ' '}, //'X' placed at (3,3)
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        assertEquals(boardToString(expectedBoard), cb.toString());

        // place another piece on the same position
        cb.placePiece(pos, 'o');
        char[][] expectedBoard2 = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', 'o', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        assertEquals(boardToString(expectedBoard2), cb.toString());
    }

    @Test
    public void Test_placePiece_InvalidCharacter_row4_column4(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(4, 4); //  new BoardPosition for (4, 4)

        cb.placePiece(pos, 'z');
        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', 'z', '*', ' ', '*'}, //'z' placed at (4,4)
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_placePiece_CrownedPiece_row2_column0(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(2, 0); // new BoardPosition for (2, 0)

        cb.placePiece(pos, 'X');

        // Expected board after placing crowned piece at (2,0)
        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'X', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };
        // Board state matches the expected state
        assertEquals(boardToString(expectedBoard), cb.toString());

    }

    @Test
    public void Test_getPieceCounts_AfterPlacement(){
        ICheckerBoard cb = makeBoard(8);
        HashMap<Character, Integer> counts = cb.getPieceCounts();

        assertEquals(12, (int) counts.get(CheckersFE.getPlayerOne())); // Black pieces
        assertEquals(12, (int) counts.get(CheckersFE.getPlayerTwo())); // White pieces
    }

    @Test
    public void Test_getViableDirections_InitialSetup() {
        ICheckerBoard cb = makeBoard(8);
        HashMap<Character, ArrayList<DirectionEnum>> directions = cb.getViableDirections();

        ArrayList<DirectionEnum> expectedPlayerOne = new ArrayList<>(Arrays.asList(DirectionEnum.SE, DirectionEnum.SW));
        ArrayList<DirectionEnum> expectedPlayerOneKing = new ArrayList<>(Arrays.asList(DirectionEnum.SE, DirectionEnum.SW, DirectionEnum.NE, DirectionEnum.NW));

        ArrayList<DirectionEnum> expectedPlayerTwo = new ArrayList<>(Arrays.asList(DirectionEnum.NE, DirectionEnum.NW));
        ArrayList<DirectionEnum> expectedPlayerTwoKing = new ArrayList<>(Arrays.asList(DirectionEnum.SE, DirectionEnum.SW, DirectionEnum.NE, DirectionEnum.NW));



        assertEquals(expectedPlayerOne, directions.get(CheckersFE.getPlayerOne()));
        assertEquals(expectedPlayerOneKing, directions.get(Character.toUpperCase(CheckersFE.getPlayerOne())));

        assertEquals(expectedPlayerTwo, directions.get(CheckersFE.getPlayerTwo()));
        assertEquals(expectedPlayerTwoKing, directions.get(Character.toUpperCase(CheckersFE.getPlayerTwo())));
    }

    @Test
    public void Test_checkPlayerWin_PlayerWins(){
        ICheckerBoard cb = makeBoard(8);

        cb.getPieceCounts().put('o', CheckerBoard.NO_PIECES_LEFT);

        assertFalse(cb.checkPlayerWin('o'));
        assertTrue(cb.checkPlayerWin('x'));   // 'x' should win since 'o' is 0
    }

    @Test
    public void Test_checkPlayerWin_PlayerNotWin(){
        ICheckerBoard cb = makeBoard(8);

        assertFalse(cb.checkPlayerWin('x'));
        assertFalse(cb.checkPlayerWin('o'));
    }

    @Test
    public void Test_crownPiece_PLAYER_ONE_row2_column6(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(2, 6); // new BoardPosition for (2, 6)

        cb.placePiece(pos, 'x');
        // Crown the piece at (2,6)
        cb.crownPiece(pos);

        // Expected board after crowning piece at (2,6)
        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'X', '*'}, // 'X' crowned at (2,6)
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_crownPiece_PLAYER_TWO_row6_column0(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(6, 0); // new BoardPosition for (6, 0)

        cb.placePiece(pos, 'o');

        // Crown piece 'o' at (6, 0)
        cb.crownPiece(pos);

        // Expected board after crowning piece at (6,0)
        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'O', '*', 'o', '*', 'o', '*', 'o', '*'}, // 'O' crowned at (6,0)
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        // Board state matches the expected state
        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_crownPiece_NonPlayer_row0_column1(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(0, 1); // new BoardPosition for (0, 1)

        cb.placePiece(pos, ' ');
        cb.crownPiece(pos);   // crown non-player

        char[][] expectedBoard = {
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', 'x', '*', 'x', '*', 'x', '*', 'x'},
                {'x', '*', 'x', '*', 'x', '*', 'x', '*'},
                {'*', ' ', '*', ' ', '*', ' ', '*', ' '},
                {' ', '*', ' ', '*', ' ', '*', ' ', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'},
                {'o', '*', 'o', '*', 'o', '*', 'o', '*'},
                {'*', 'o', '*', 'o', '*', 'o', '*', 'o'}
        };

        //Board remains the same due to no error checking
        assertEquals(boardToString(expectedBoard), cb.toString());
    }

    @Test
    public void Test_movePiece_ValidMove_SE_row2_column2(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(2, 2);
        DirectionEnum dir = DirectionEnum.SE;

        cb.placePiece(startingPos, 'x');
        BoardPosition newPos = cb.movePiece(startingPos, dir);

        cb.placePiece(startingPos, ' ');
        cb.placePiece(newPos, 'x');

        BoardPosition expectedPos = new BoardPosition(3, 3);

        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals('x', cb.whatsAtPos(expectedPos));
    }


    @Test
    public void Test_movePiece_ValidMove_NW_row5_column1() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(5, 1);
        DirectionEnum dir = DirectionEnum.NW;

        cb.placePiece(startingPos, 'o');
        BoardPosition newPos = cb.movePiece(startingPos, dir);

        cb.placePiece(startingPos, ' ');
        cb.placePiece(newPos, 'o');

        BoardPosition expectedPos = new BoardPosition(4, 0);

        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals('o', cb.whatsAtPos(expectedPos));
    }

    @Test
    public void Test_movePiece_Crowned_SE_row0_column2(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(0, 2);
        DirectionEnum dir = DirectionEnum.SE;

        cb.placePiece(startingPos, 'O');
        BoardPosition newPos = cb.movePiece(startingPos, dir);

        cb.placePiece(startingPos, ' ');
        cb.placePiece(newPos, 'O');

        BoardPosition expectedPos = new BoardPosition(1,3);

        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals('O', cb.whatsAtPos(expectedPos));
    }

    @Test
    public void Test_jumpPiece_ValidJump_NW_row4_column2() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(4, 2);
        BoardPosition middlePos = new BoardPosition(3, 1);
        DirectionEnum dir = DirectionEnum.NW;

        cb.placePiece(startingPos, 'o');
        cb.placePiece(middlePos, 'x');
        BoardPosition landingPos = cb.jumpPiece(startingPos, dir);

        cb.placePiece(startingPos, ' ');
        cb.placePiece(middlePos, ' ');
        cb.placePiece(landingPos, 'o');

        BoardPosition expectedPos = new BoardPosition(2, 0);

        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals(' ', cb.whatsAtPos(middlePos));
        assertEquals('o', cb.whatsAtPos(expectedPos));
    }

    @Test
    public void Test_jumpPiece_ValidJump_SE_row2_column4() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(2, 4);
        BoardPosition middlePos = new BoardPosition(3, 5);
        DirectionEnum dir = DirectionEnum.SE;

       cb.placePiece(startingPos, 'x');
       cb.placePiece(middlePos, 'x');
       BoardPosition landingPos = cb.jumpPiece(startingPos, dir);

       cb.placePiece(startingPos, ' ');
       cb.placePiece(middlePos, ' ');
       cb.placePiece(landingPos, 'x');

        BoardPosition expectedPos = new BoardPosition(4,6);

        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals(' ', cb.whatsAtPos(middlePos));
        assertEquals('x', cb.whatsAtPos(expectedPos));
    }

    @Test
    public void Test_jumpPiece_Crowned_SE_row0_column2() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(0, 2);
        BoardPosition middlePos = new BoardPosition(1, 3);
        DirectionEnum dir = DirectionEnum.SE;

        cb.placePiece(startingPos, 'O');
        cb.placePiece(middlePos, 'x');
        BoardPosition landingPos = cb.jumpPiece(startingPos, dir);

        cb.placePiece(startingPos, ' ');
        cb.placePiece(middlePos, ' ');
        cb.placePiece(landingPos, 'O');

        BoardPosition expectedPos = new BoardPosition(2,4);


        assertEquals(' ', cb.whatsAtPos(startingPos));
        assertEquals(' ', cb.whatsAtPos(middlePos));
        assertEquals('O', cb.whatsAtPos(expectedPos));
    }

    @Test
    public void Test_scanSurroundingPositions_row2_column2() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(2, 2);

        cb.placePiece(new BoardPosition(1,1), 'x'); //NW
        cb.placePiece(new BoardPosition(1,3), 'x'); //NE
        cb.placePiece(new BoardPosition(3,1), ' '); //SW (empty)
        cb.placePiece(new BoardPosition(3,3), ' '); //SE (empty)


        Map<DirectionEnum, Character> surrounding = cb.scanSurroundingPositions(startingPos);


        assertEquals('x', surrounding.get(DirectionEnum.NW).charValue());
        assertEquals('x', surrounding.get(DirectionEnum.NE).charValue());
        assertEquals(' ', surrounding.get(DirectionEnum.SW).charValue());
        assertEquals(' ', surrounding.get(DirectionEnum.SE).charValue());
    }

    @Test
    public void Test_scanSurrondingPositions_row6_column6() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(6, 6);

        cb.placePiece(new BoardPosition(5,5), 'o'); //NW
        cb.placePiece(new BoardPosition(5,7), 'o'); //NE
        cb.placePiece(new BoardPosition(7,5), 'o'); //SW
        cb.placePiece(new BoardPosition(7,7), 'o'); //SE


        Map<DirectionEnum, Character> surrounding = cb.scanSurroundingPositions(startingPos);


        assertEquals('o', surrounding.get(DirectionEnum.NW).charValue());
        assertEquals('o', surrounding.get(DirectionEnum.NE).charValue());
        assertEquals('o', surrounding.get(DirectionEnum.SW).charValue());
        assertEquals('o', surrounding.get(DirectionEnum.SE).charValue());
    }

    @Test
    public void Test_scanSurrondingPositions_Crowned_row1_column3(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition startingPos = new BoardPosition(1, 3);

        cb.placePiece(new BoardPosition(0,2), 'O'); //NW
        cb.placePiece(new BoardPosition(0,4), 'x'); //NE
        cb.placePiece(new BoardPosition(2,2), 'x'); //SW
        cb.placePiece(new BoardPosition(2,4), ' '); //SE (empty)


        Map<DirectionEnum, Character> surrounding = cb.scanSurroundingPositions(startingPos);


        assertEquals('O', surrounding.get(DirectionEnum.NW).charValue());
        assertEquals('x', surrounding.get(DirectionEnum.NE).charValue());
        assertEquals('x', surrounding.get(DirectionEnum.SW).charValue());
        assertEquals(' ', surrounding.get(DirectionEnum.SE).charValue());
    }

    @Test
    public void Test_getDirection_ValidDirection_SE() {
        ICheckerBoard cb = makeBoard(8);
        DirectionEnum dir = DirectionEnum.SE;

        BoardPosition expectedPosition = new BoardPosition(1, 1);

        BoardPosition actualPosition = ICheckerBoard.getDirection(dir);
        assertEquals(expectedPosition, actualPosition);
    }


}
