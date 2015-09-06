package ztomasze;

import java.util.*;

/**
 * Solves Sudoku puzzles.
 * <p>
 * Original version written in 3 hours, not counting algorithm
 * planning time.
 * 
 * @author Zach Tomaszewski
 * @since 09 Jun 2011
 */
public class SudokuSolver {

  /**
   * Takes a single command line argument of a board to solve.
   * <p>
   * This board should be an 81 character string containing all the initial
   * board. Cells should be given left-to-right, top-to-bottom. That is, the
   * contents of the first row, followed by the contents of the second, etc.
   * Given cell values should be digits between 1 and 9. Any other char value is
   * treated as an empty square.
   * <p>
   * Will construct a new board, solve it, and print the results.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Must provide a board to solve as a single line "
          + "(without using spaces) on the command line.");
    }else {
      SudokuBoard sudoku = new SudokuBoard(args[0]);
      // initial board
      System.out.println("Initial board: ");
      System.out.println(sudoku);
      boolean solved = sudoku.solve();
      if (solved) {
        System.out.println();
        System.out.println("Solution: ");
        System.out.println(sudoku);
      }else {
        System.out.println("Board is unsolvable.");
      }
    }
  }
}

/**
 * Represents a 9x9 Sudoku board with its 81 different cells.
 */
class SudokuBoard {

  private Cell[][] grid;
  private Stack<Cell> certain;

  /**
   * Constructs the given board. The given int[][] must be 9x9. Any value
   * outside of the 1 to 9 range is treated as an initially-empty cell.
   * <p>
   * Marks all given values as newly-certain. See {@link solve()} for more.
   */
  public SudokuBoard(int[][] board) {
    this.init(board);
  }

  private void init(int[][] board) {
    if (board.length != 9) {
      throw new IllegalArgumentException("Given board is not 9x9.");
    }

    this.grid = new Cell[9][9];
    this.certain = new Stack<Cell>();

    for (int row = 0; row < grid.length; row++) {
      if (board[row].length != 9) {
        throw new IllegalArgumentException("Given board is not 9x9");
      }
      for (int col = 0; col < grid[row].length; col++) {
        grid[row][col] = new Cell(row, col);
        // set any given cells
        if (board[row][col] >= 1 && board[row][col] <= 9) {
          grid[row][col].set(board[row][col]);
          this.certain.push(grid[row][col]);
        }
      }
    }
  }

  /**
   * Returns a copy of the current board.
   */
  public SudokuBoard(SudokuBoard board) {
    // copy all cells of board
    this.grid = new Cell[9][9];
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        this.grid[row][col] = new Cell(board.grid[row][col]);
      }
    }
    // copy stack too
    // (Should be unnecessary for normal solving use,
    // but done anyway to constructor is correct)
    this.certain = new Stack<Cell>();
    for (Cell c : board.certain) {
      this.certain.push(c);
    }
  }

  /**
   * This board should be an 81 character string containing all of the initial
   * board. Cells should be given left-to-right, top-to-bottom.  That is, the
   * contents of the first row, followed by the contents of the second, etc.
   * Given cell values should be digits between 1 and 9. Any other char value is
   * treated as an empty square, except for line-break characters, which are
   * ignored.  
   * 
   * @throws IllegalArgumentException if the given String does not contain
   * exactly 81 characters (after ignoring linebreak characters).
   */
  public SudokuBoard(String boardAsString) {
    // valid board, so load 81 chars into 9x9 array
    int[][] board = new int[9][9];
    int i = 0;
    for (int row = 0; row < board.length; row++) {
      for (int col = 0; col < board[row].length; col++) {
        char val = boardAsString.charAt(i);
        if (val == '\r' || val == '\n') {
          //skip over
          col--;  //repeat this col
        }else if (Character.isDigit(val) && val != '0') {
          // val is 1 to 9
          board[row][col] = val - '0'; // convert from char to int
        }else {
          board[row][col] = 0;
        }
        i++;
      }
    }
    if (i < boardAsString.length()) {
      throw new IllegalArgumentException("Given board contains more than 81 cells.");
    }
    this.init(board);
  }

  /**
   * Returns whether all cells in this board are certain.
   */
  public boolean isSolved() {
    for (Cell[] row : grid) {
      for (Cell c : row) {
        if (!c.isCertain()) {
          return false;
        }
      }
    }
    return true; // all cells were certain
  }

  /**
   * Solves this board using a depth-first search of logical possiblities.
   * <p>
   * Each newly-certain cell is processed. This means its value is marked off of
   * each other cell in the same row, column, and 3x3 square. If this reduces
   * any other cell to a single possibility, that cell becomes newly-certain
   * as well.
   * <p>
   * If, after all newly-certain cells have been processed, the board is solved,
   * returns true.
   * <p>
   * If the board is not yet solved, makes a guess. Specifically, searches for
   * one of the cells with the fewest possiblities. Then a snapshot is made of
   * the current board and one of the selected cell's possiblities is made
   * certain. Then solve() is called recursively on the new board copy. If this
   * does not result in a solved board, the guess is undone and a different
   * possibility for that cell is selected. If all possibilities for the
   * selected cell fail, the board is deemed unsolvable and this method returns
   * false.
   * <p>
   * Returns whether this board was solved or not.
   */
  public boolean solve() {
    return this.solve(0);
  }

  /**
   * A version of solve that allows recursive guess tracking. Actually does the
   * work.
   */
  private boolean solve(int guess) {
    // process any newly-certain cells first
    while (this.certain.size() > 0) {
      Cell cell = this.certain.pop();

      // determine top-left cell of 3row3 square this cell is in
      int squareRow, squareCol;
      if (cell.row <= 2) {
        squareRow = 0;
      }else if (cell.row <= 5) {
        squareRow = 3;
      }else {
        assert cell.row <= 8;
        squareRow = 6;
      }
      if (cell.col <= 2) {
        squareCol = 0;
      }else if (cell.col <= 5) {
        squareCol = 3;
      }else {
        assert cell.col <= 8;
        squareCol = 6;
      }

      // mark row
      for (int col = 0; col < grid[cell.row].length; col++) {
        this.mark(grid[cell.row][col], cell);
      }
      // mark column
      for (int row = 0; row < grid[cell.col].length; row++) {
        this.mark(grid[row][cell.col], cell);
      }
      // mark 3x3 square
      for (int row = squareRow; row < squareRow + 3; row++) {
        for (int col = squareCol; col < squareCol + 3; col++) {
          this.mark(grid[row][col], cell);
        }
      }
    }

    if (this.isSolved()) {
      return true;
    }else {
      // not any easy one; will need to make a guess.
      // First, find a candiate cell
      int poss = 10;
      Cell selected = null;
      for (Cell[] row : grid) {
        for (Cell c : row) {
          if (!c.isCertain() && c.possible.size() < poss) {
            selected = c;
            poss = c.possible.size();
          }
        }
      }
      assert selected != null : "Didn't find a cell to select.";

      // now, try each possible guess with a different/copied board
      guess++;
      for (int option : selected.possible) {
        SudokuBoard copy = new SudokuBoard(this);
        copy.grid[selected.row][selected.col].set(option); // make guess
        copy.certain.push(copy.grid[selected.row][selected.col]);
        // System.out.println("Guess " + guess + ": " + option + " from " +
        // selected);
        try {
          boolean solved = copy.solve(guess);
          if (solved) {
            // HACK: oops, need to save copy back into this
            this.grid = copy.grid;
            this.certain = copy.certain;
            return true;
          }
        } catch (IllegalArgumentException iae) {
          // logic error due to invalid guess
          // Ignore, and try a different guess
        }
      }
    }
    return false; // could not solve
  }

  /**
   * Marks the given curr cell with the value taken from the given certain cell.
   * If curr == certain, safely does nothing. If the change means the cell
   * changes from uncertain to certain, will add it to the newly-certain stack.
   */
  private void mark(Cell curr, Cell certain) {
    assert certain.isCertain();
    if (curr == certain) {
      return; // do nothing
    }
    boolean wasCertain = curr.isCertain();
    curr.markOff(certain.possible.get(0));
    if (!wasCertain && curr.isCertain()) {
      this.certain.push(curr);
    }
  }

  /**
   * Returns a 9x9 String version of this board.
   * <p>
   * Cells that are not certain are shown as letters, which indicate the number
   * of current possibilities using the scale a=1, b=2, c=3, ... i=9. (Since a
   * cell with only 1 possibility is certain, the letter a will never actually
   * be seen.)
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Cell[] row : grid) {
      for (Cell c : row) {
        if (c.isCertain()) {
          sb.append(c.possible.get(0));
        }else {
          sb.append((char) ('a' + (c.possible.size() - 1)));
        }
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * A single Cell within a Sudoku board.
   * <p>
   * Each cell has an (row,col) position on the board. row and col values both
   * range from 0 to 8.
   * <p>
   * A Cell begins with a list of all possible values from 1 to 9. These are
   * then marked off during the course of solving the board. A Cell becomes
   * certain when only one possible value remains.
   * <p>
   * A Cell can also be set to a specific certain value, which is useful during
   * board setup when certain cell values are given.
   */
  private static class Cell {
    private List<Integer> possible;
    private int row;
    private int col;

    /**
     * Builds a new cell with the given (row,col) location. The cell contains
     * all possible values.
     */
    public Cell(int row, int col) {
      assert row >= 0 && row <= 8 && col >= 0 && col <= 8;
      this.row = row;
      this.col = col;

      this.possible = new LinkedList<Integer>();
      for (int i = 1; i <= 9; i++) {
        this.possible.add(i);
      }
    }

    /**
     * Constructs a separate copcol of the given Cell.
     */
    public Cell(Cell orig) {
      this.row = orig.row;
      this.col = orig.col;
      this.possible = new LinkedList<Integer>(orig.possible);
    }

    /**
     * Returns whether this Cell has only one possible value.
     */
    public boolean isCertain() {
      return this.possible.size() == 1;
    }

    /**
     * Removes the given value from this Cell's possible values. If trying to
     * remove the value of a certain cell (that is, one with only a single
     * possibility), throws an IllegalArgumentException, since this represents a
     * logic error somewhere in the solving.
     */
    public void markOff(int val) {
      assert val >= 1 && val <= 9;
      if (this.isCertain() && val == this.possible.get(0)) {
        throw new IllegalArgumentException("Tried to remove " + val
            + " from " + this + ".");
      }
      // make sure to call remove(Object), not remove(int)
      this.possible.remove((Integer) val);
    }

    /**
     * Sets this Cell's list of possible values to contain only the given value.
     */
    public void set(int val) {
      assert val >= 1 && val <= 9;
      this.possible.clear();
      this.possible.add(val);
    }

    /**
     * Returns a representation of this Cell in the form of
     * "Cell(row,col)[list, of, possibles]"
     */
    public String toString() {
      return "Cell(" + this.row + "," + this.col + ")" + this.possible;
    }
  }
}
