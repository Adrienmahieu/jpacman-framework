package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.level.Level;

import java.util.List;
import java.util.Random;

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

    private int sectionSizeX;
    private int sectionSizeY;

    private Random rand = new Random();

    /**
     * Creates a new board.
     *
     * @param grid The grid of squares with grid[x][y] being the square at column
     *             x, row y.
     */
    Board(Square[][] grid) {
        assert grid != null;
        this.board = grid;
        sectionSizeX = grid.length;
        sectionSizeY = grid[0].length;
        assert invariant() : "Initial grid cannot contain null squares";
    }

    /**
     * Whatever happens, the squares on the board can't be null.
     *
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
     * @param x The <code>x</code> position (column) of the requested square.
     * @param y The <code>y</code> position (row) of the requested square.
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
     * @param x The <code>x</code> position (row) to test.
     * @param y The <code>y</code> position (column) to test.
     * @return <code>true</code> iff the position is on this board.
     */
    public boolean withinBorders(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    public void expand(Direction dir) {
        Square[][] grid = null;
        int expandSize = 5;
        if (dir == Direction.EAST || dir == Direction.WEST)
            expandSize = sectionSizeX;
        else
            expandSize = sectionSizeY;
        grid = new Square[this.getWidth() + Math.abs(dir.getDeltaX() * expandSize)][this.getHeight() + Math.abs(dir.getDeltaY() * expandSize)];
        if (dir == Direction.SOUTH) {
            copyGrid(board, grid, 0, 0);
            instanceSquare(grid, 0, board[0].length, board.length, board[0].length + expandSize);
            updateLink(grid, 0, board[0].length - 1, getWidth(), expandSize);

        } else if (dir == Direction.NORTH) {
            copyGrid(board, grid, 0, expandSize);
            instanceSquare(grid, 0, 0, board.length, expandSize);
            updateLink(grid, 0, 0, getWidth(), expandSize + 1);

        } else if (dir == Direction.WEST) {
            copyGrid(board, grid, expandSize, 0);
            instanceSquare(grid, 0, 0, expandSize, board[0].length);
            updateLink(grid, 0, 0, expandSize + 1, getHeight());
        } else if (dir == Direction.EAST) {
            copyGrid(board, grid, 0, 0);
            instanceSquare(grid, board.length, 0, board.length + expandSize, board[0].length);
            updateLink(grid, board.length - 1, 0, expandSize, getHeight());
        }
        updatePosition(grid);
        updateLink(grid, 0, 0, grid.length, 1);
        updateLink(grid, 0, 0, 1, grid[0].length);
        updateLink(grid, grid.length - 1, 0, 1, grid[0].length);
        updateLink(grid, 0, grid[0].length - 1, grid.length, 1);
        this.board = grid;

    }

    /**
     * Update the position x y for all squares of board.
     */
    public void updatePosition() {
        updatePosition(this.board);
    }

    /**
     * Update the position x y for all squares.
     *
     * @param squares A table of squares.
     */
    public void updatePosition(Square[][] squares) {
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[0].length; j++) {
                squares[i][j].setX(i);
                squares[i][j].setY(j);
            }
        }
    }

    /**
     * Generate a random Square.
     *
     * @param bf
     * @return The generated Square.
     */
    public Square random(BoardFactory bf) {
        Random r = new Random();
        int i = r.nextInt(5);
        if (i == 0)
            return bf.createWall();
        else if (i > 0)
            return bf.createGround();
        return bf.createGround();
    }

    /**
     * Copy the grid.
     *
     * @param from The origin grid.
     * @param to   The destination grid.
     * @param dx   The delta x to start copy.
     * @param dy   The delta y to start copy.
     */
    public void copyGrid(Square[][] from, Square[][] to, int dx, int dy) {
        for (int x = 0; x < from.length; x++) {
            for (int y = 0; y < from[0].length; y++) {
                to[x + dx][y + dy] = from[x][y];
            }
        }
    }

    /**
     * Update the link for Square instance.
     *
     * @param grid The grid.
     * @param _x   The origin x.
     * @param _y   The origin y.
     * @param dx   The number of cases to update on x.
     * @param dy   The number of cases to update on y.
     */
    public void updateLink(Square[][] grid, int _x, int _y, int dx, int dy) {
        int width = grid.length;
        int height = grid[0].length;
        for (int x = _x; x < _x + dx; x++) {
            for (int y = _y; y < _y + dy; y++) {
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

    /**
     * Instanciate Square in grid.
     *
     * @param grid The grid.
     * @param _x   From position x.
     * @param _y   From position y.
     * @param dx   To position x.
     * @param dy   To position y.
     */
    public void instanceSquare(Square[][] grid, int _x, int _y, int dx, int dy) {
        int nbx = (dx - _x) / sectionSizeX;
        int nby = (dy - _y) / sectionSizeY;
        for (int nx = 0; nx < nbx; nx++) {
            for (int ny = 0; ny < nby; ny++) {
                Level l = Launcher.getInstance().makeLevel(this.randomBoard());
                Square[][] newMap = l.getBoard().board;
                for (int x = 0; x < newMap.length; x++) {
                    for (int y = 0; y < newMap[0].length; y++) {
                        grid[_x + (nx * sectionSizeX) + x][_y + (ny * sectionSizeY) + y] = newMap[x][y];
                    }
                }
                Level.getInstance().addNPCs(l);
            }
        }
    }

    /**
     * Select a random map from map directory.
     *
     * @return A map name.
     */
    public String randomBoard() {
        List<String> maps = Launcher.getInstance().getMaps();
        return maps.get(rand.nextInt(maps.size()));
    }

    public int getSectionSizeX() {
        return sectionSizeX;
    }

    public int getSectionSizeY() {
        return sectionSizeY;
    }

    public Square[][] getBoard() {
        return board;
    }
}
