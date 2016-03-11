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
		int expandSize = 5;
		grid = new Square[this.getWidth() + Math.abs(dir.getDeltaX()*expandSize)][this.getHeight()+ Math.abs(dir.getDeltaY()*expandSize)];
		if(dir == Direction.SOUTH) {
			copyGrid(board, grid, 0, 0);
			for (int x = 0; x < board.length; x++) {
				for (int y = board[x].length; y < board[x].length+expandSize; y++) {
					grid[x][y] = this.random(bf);
				}
			}
			updateLink(grid, 0, board[0].length-1, getWidth(), expandSize);

		}
		else if(dir == Direction.NORTH) {
			copyGrid(board, grid, 0, expandSize);
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < expandSize; y++) {
					grid[x][y] = this.random(bf);
				}
			}
			updateLink(grid, 0, 0, getWidth(), expandSize+1);

		}
		else if(dir == Direction.WEST) {
			copyGrid(board, grid, expandSize, 0);
			for (int x = 0; x < expandSize; x++) {
				for (int y = 0; y < board[x].length; y++) {
					grid[x][y] = this.random(bf);
				}
			}
			updateLink(grid, 0, 0, expandSize+1, getHeight());
		}
		else if(dir == Direction.EAST) {
			copyGrid(board, grid, 0, 0);
			for (int x = board.length; x < board.length+expandSize; x++) {
				for (int y = 0; y < board[0].length; y++) {
					grid[x][y] = this.random(bf);
				}
			}
			updateLink(grid, board.length-1, 0, expandSize, getHeight());
		}
		this.board = grid;

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
	
	public void copyGrid(Square[][] from, Square[][] to, int dx, int dy) {
		for (int x = 0; x < from.length; x++) {
			for (int y = 0; y < from[0].length; y++) {
				to[x+dx][y+dy] = from[x][y];
			}
		}
	}
	
	public void updateLink(Square[][] grid, int _x, int _y, int dx, int dy) {
		int width = grid.length;
		int height = grid[0].length;
		for (int x = _x; x < _x+dx; x++) {
			for (int y = _y; y < _y+dy; y++) {
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
}
