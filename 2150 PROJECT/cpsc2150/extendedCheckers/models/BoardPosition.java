package cpsc2150.extendedCheckers.models;

/** The BoardPosition class represents a specific location on the checkerboard by storing
 * it's row and column coordinates. Each position is represented by a non-negative integer
 *values for both row and column.
 *
 * @invariant (0 <= row < ROW_NUM) AND (0 <= column < COL_NUM)
 *
 */


public class BoardPosition
{


    /**
     * Row component of the BoardPosition
     */
    private int row;

    /**
     * Column component of the BoardPosition
     */
    private int column;

    /**
     * Parametized constructor for the board position object, takes in 2 parameters
     * @param aRow the value to be set for row, an integer
     * @param aCol the value to be set for column, an integer
     *
     * @pre aRow >= 0 AND aCol >= 0
     *
     * @post row == aRow AND column == aCol
     */

    public BoardPosition(int aRow, int aCol) {
        this.row = aRow;
        this.column = aCol;
    }

    /**
     * Standard getter for the row
     * @return Row, an integer
     * @pre none
     *
     * @post getRow = row
     */
    public int getRow() {
        return row;
    }

    /**
     * Standard getter for the column
     * @return column, an integer
     * @pre none
     *
     * @post getColumn = column
     */

    public int getColumn() {
        return column;
    }

    /**
     *Checks if the BoardPosition is equal to the parameter object
     * @param obj  object compared to the BoardPosition
     * @return true if obj is a BoardPosition of same row and column values, otherwise false
     *
     * @pre obj != null
     *
     * @post equals = true IF obj [is an instance of BoardPosition] AND
     * obj.row == this.row AND obj.column == this.column
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        BoardPosition other = (BoardPosition) obj;
        return (this.row == other.row && (this.column == other.column));
    }

    /**
     * Returns a string representation of the BoardPosition
     * @return "row,column", a String
     *
     * @pre none
     *
     * @post toString = row + "," + column
     */
    @Override
    public String toString() {
        return row + "," + column;
    }

}
