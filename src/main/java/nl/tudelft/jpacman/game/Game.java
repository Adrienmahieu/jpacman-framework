package nl.tudelft.jpacman.game;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Level.LevelObserver;
import nl.tudelft.jpacman.level.Player;

import java.util.List;

/**
 * A basic implementation of a Pac-Man game.
 *
 * @author Jeroen Roosen
 */
public abstract class Game implements LevelObserver {

    /**
     * Object that locks the start and stop methods.
     */
    private final Object progressLock = new Object();
    /**
     * <code>true</code> if the game is in progress.
     */
    private boolean inProgress;

    /**
     * Creates a new game.
     */
    protected Game() {
        inProgress = false;
    }

    /**
     * Starts or resumes the game.
     */
    public void start() {
        synchronized (progressLock) {
            if (isInProgress()) {
                return;
            }
            if (getLevel().isAnyPlayerAlive()
                    && getLevel().remainingPellets() > 0) {
                inProgress = true;
                getLevel().addObserver(this);
                getLevel().start();
            }
        }
    }

    /**
     * Pauses the game.
     */
    public void stop() {
        synchronized (progressLock) {
            if (!isInProgress()) {
                return;
            }
            inProgress = false;
            getLevel().stop();
        }
    }

    /**
     * @return <code>true</code> iff the game is started and in progress.
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * @return An immutable list of the participants of this game.
     */
    public abstract List<Player> getPlayers();

    /**
     * @return The level currently being played.
     */
    public abstract Level getLevel();

    /**
     * Moves the specified player one square in the given direction.
     *
     * @param player    The player to move.
     * @param direction The direction to move in.
     */
    public void move(Player player, Direction direction) {
        if (isInProgress()) {
            // execute player move.
            getLevel().move(player, direction);
            expand(player);
        }
    }

    public void expand(Player player) {
        int limit = 15;
        int px = player.getSquare().getX();
        int py = player.getSquare().getY();
        Board board = this.getLevel().getBoard();
        if (py > board.getHeight() - limit) {
            board.expand(Direction.SOUTH);
        }
        if (py < limit) {
            board.expand(Direction.NORTH);
        }
        if (px < limit) {
            board.expand(Direction.WEST);
        }
        if (px > board.getWidth() - limit) {
            board.expand(Direction.EAST);
        }
    }

    @Override
    public void levelWon() {
        stop();
    }

    @Override
    public void levelLost() {
        stop();
    }
}
