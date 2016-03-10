package nl.tudelft.jpacman.board;

import java.util.Random;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.game.GameFactory;
import nl.tudelft.jpacman.level.PlayerFactory;

/**
 * A top-down view of a matrix of {@link Square}s.
 * 
 * @author Jeroen Roosen 
 */
public class Board {

	/**
	 * The grid of squares with board[x][y] being the square at column x, row y.
	 */
	private Square[][] board;

	/**
	 * Creates a new board.
	 * 
	 * @param grid
	 *            The grid of squares with grid[x][y] being the square at column
	 *            x, row y.
	 */
	Board(Square[][] grid) {
		assert grid != null;
		this.board = grid;
		assert invariant() : "Initial grid cannot contain null squares";
	}
	
	/**
	 * Whatever happens, the squares on the board can't be null.
	 * @return false if any square on the board is null.
	 */
	public boolean invariant() {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y] == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the width of this board, i.e. the amount of columns.
	 * 
	 * @return The width of this board.
	 */
	public int getWidth() {
		return board.length;
	}

	/**
	 * Returns the height of this board, i.e. the amount of rows.
	 * 
	 * @return The height of this board.
	 */
	public int getHeight() {
		return board[0].length;
	}

	/**
	 * Returns the square at the given <code>x,y</code> position.
	 * 
	 * @param x
	 *            The <code>x</code> position (column) of the requested square.
	 * @param y
	 *            The <code>y</code> position (row) of the requested square.
	 * @return The square at the given <code>x,y</code> position (never null).
	 */
	public Square squareAt(int x, int y) {
		assert withinBorders(x, y);
		Square result = board[x][y];
		assert result != null : "Follows from invariant.";
		return result;
	}

	/**
	 * Determines whether the given <code>x,y</code> position is on this board.
	 * 
	 * @param x
	 *            The <code>x</code> position (row) to test.
	 * @param y
	 *            The <code>y</code> position (column) to test.
	 * @return <code>true</code> iff the position is on this board.
	 */
	public boolean withinBorders(int x, int y) {
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}
	
	public void expand(Direction dir) {
		Square[][] grid = null;
		//GameFactory gf = new GameFactory(new PlayerFactory(Launcher.getSPRITE_STORE()));
		BoardFactory bf = BoardFactory.instance;
		if(dir == Direction.SOUTH) {
			grid = new Square[this.getWidth()][this.getHeight()+5];
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x][y] = board[x][y];
				}
			}
			for (int x = 0; x < board.length; x++) {
				for (int y = board[x].length; y < board[x].length+5; y++) {
					grid[x][y] = this.random(bf);
				}
			}

		}
		else if(dir == Direction.NORTH) {
			grid = new Square[this.getWidth()][this.getHeight()+5];
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x][y+5] = board[x][y];
				}
			}
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < 5; y++) {
					grid[x][y] = this.random(bf);
				}
			}

		}
		else if(dir == Direction.WEST) {
			grid = new Square[this.getWidth()+5][this.getHeight()];
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x+5][y] = board[x][y];
				}
			}
			for (int x = 0; x < 5; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x][y] = this.random(bf);
				}
			}

		}
		else if(dir == Direction.EAST) {
			grid = new Square[this.getWidth()+5][this.getHeight()];
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x][y] = board[x][y];
				}
			}
			for (int x = board.length; x < board.length+5; x++) {
				for (int y = 0; y < board[0].length; y++) {
					grid[x][y] = this.random(bf);
				}
			}

		}
		this.board = grid;
		int width = grid.length;
		int height = grid[0].length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Square square = grid[x][y];
				for (Direction d : Direction.values()) {
					int dirX = (width + x + d.getDeltaX()) % width;
					int dirY = (height + y + d.getDeltaY()) % height;
					Square neighbour = grid[dirX][dirY];
					square.link(neighbour, d);
				}
			}
		}
	}
	
	public Square random(BoardFactory bf) {
		Random r = new Random();
		int i = r.nextInt(5);
		if(i==0)
			return bf.createWall();
		else if(i>0)
			return bf.createGround();
		return bf.createGround();
	}
}
