package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.NPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A level of Pac-Man. A level consists of the board with the players and the
 * AIs on it.
 *
 * @author Jeroen Roosen
 */
public class Level {

    private static final int GHOST_LIMIT = 10;
    private static Level instance;
    /**
     * The board of this level.
     */
    private final Board board;
    /**
     * The lock that ensures moves are executed sequential.
     */
    private final Object moveLock = new Object();
    /**
     * The lock that ensures starting and stopping can't interfere with each
     * other.
     */
    private final Object startStopLock = new Object();
    /**
     * The NPCs of this level and, if they are running, their schedules.
     */
    private final Map<NPC, ScheduledExecutorService> npcs;
    /**
     * The squares from which players can start this game.
     */
    private final List<Square> startSquares;
    /**
     * The players on this level.
     */
    private final List<Player> players;
    /**
     * The table of possible collisions between units.
     */
    private final CollisionMap collisions;
    /**
     * The objects observing this level.
     */
    private final List<LevelObserver> observers;
    /**
     * <code>true</code> iff this level is currently in progress, i.e. players
     * and NPCs can move.
     */
    private boolean inProgress;
    /**
     * The start current selected starting square.
     */
    private int startSquareIndex;

    /**
     * Creates a new level for the board.
     *
     * @param b              The board for the level.
     * @param ghosts         The ghosts on the board.
     * @param startPositions The squares on which players start on this board.
     * @param collisionMap   The collection of collisions that should be handled.
     */
    public Level(Board b, List<NPC> ghosts, List<Square> startPositions,
                 CollisionMap collisionMap) {
        assert b != null;
        assert ghosts != null;
        assert startPositions != null;

        this.board = b;
        this.inProgress = false;
        this.npcs = new HashMap<>();
        for (NPC g : ghosts) {
            npcs.put(g, null);
        }
        this.startSquares = startPositions;
        this.startSquareIndex = 0;
        this.players = new ArrayList<>();
        this.collisions = collisionMap;
        this.observers = new ArrayList<>();
        if (Level.instance == null)
            Level.instance = this;
    }

    /**
     * Return the Level instance of the game.
     *
     * @return The level of the game.
     */
    public static Level getInstance() {
        return instance;
    }

    /**
     * Adds an observer that will be notified when the level is won or lost.
     *
     * @param observer The observer that will be notified.
     */
    public void addObserver(LevelObserver observer) {
        if (observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    /**
     * Removes an observer if it was listed.
     *
     * @param observer The observer to be removed.
     */
    public void removeObserver(LevelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Registers a player on this level, assigning him to a starting position. A
     * player can only be registered once, registering a player again will have
     * no effect.
     *
     * @param p The player to register.
     */
    public void registerPlayer(Player p) {
        assert p != null;
        assert !startSquares.isEmpty();

        if (players.contains(p)) {
            return;
        }
        players.add(p);
        Square square = startSquares.get(startSquareIndex);
        p.occupy(square);
        startSquareIndex++;
        startSquareIndex %= startSquares.size();
    }

    /**
     * Returns the board of this level.
     *
     * @return The board of this level.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Moves the unit into the given direction if possible and handles all
     * collisions.
     *
     * @param unit      The unit to move.
     * @param direction The direction to move the unit in.
     */
    public void move(Unit unit, Direction direction) {
        assert unit != null;
        assert direction != null;

        if (!isInProgress()) {
            return;
        }

        synchronized (moveLock) {
            unit.setDirection(direction);
            Square location = unit.getSquare();
            Square destination = location.getSquareAt(direction);

            if (destination.isAccessibleTo(unit)) {
                List<Unit> occupants = destination.getOccupants();
                unit.occupy(destination);
                for (Unit occupant : occupants) {
                    collisions.collide(unit, occupant);
                }
            }
            updateObservers();
        }
    }

    /**
     * Starts or resumes this level, allowing movement and (re)starting the
     * NPCs.
     */
    public void start() {
        synchronized (startStopLock) {
            if (isInProgress()) {
                return;
            }
            startNPCs();
            inProgress = true;
            updateObservers();
        }
    }

    /**
     * Stops or pauses this level, no longer allowing any movement on the board
     * and stopping all NPCs.
     */
    public void stop() {
        synchronized (startStopLock) {
            if (!isInProgress()) {
                return;
            }
            stopNPCs();
            inProgress = false;
        }
    }

    /**
     * Starts all NPC movement scheduling.
     */
    public void startNPCs() {
        for (final NPC npc : npcs.keySet()) {
            ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
            service.schedule(new NpcMoveTask(service, npc),
                    npc.getInterval() / 2, TimeUnit.MILLISECONDS);
            npcs.put(npc, service);
        }
    }

    /**
     * Stops all NPC movement scheduling and interrupts any movements being
     * executed.
     */
    public void stopNPCs() {
        for (Entry<NPC, ScheduledExecutorService> e : npcs.entrySet()) {
            e.getValue().shutdownNow();
        }
    }

    /**
     * Start the NPCs from l and add them to the current game.
     *
     * @param l A level
     */
    public void addNPCs(Level l) {
        if (this.isInProgress())
            l.start();
        this.getNpcs().putAll(l.getNpcs());
        List<NPC> toRemove = new ArrayList<NPC>();
        for (NPC npc : npcs.keySet()) {
            npc.speedUp(2);
            if (npcs.size() > GHOST_LIMIT)
                toRemove.add(npc);
        }
        remove(toRemove);
    }

    /**
     * Remove farest NPCs from list.
     *
     * @param npcs The list of npcs.
     */
    public void remove(List<NPC> npcs) {
        while (npcs.size() > GHOST_LIMIT && !npcs.isEmpty()) {
            int dmax = 0;
            NPC remove = null;
            for (NPC npc : npcs) {
                int distance = distance(this.players.get(0), npc);
                if (distance > dmax) {
                    remove = npc;
                    dmax = distance;
                }
            }
            npcs.remove(remove);
            this.remove(remove);
        }
    }

    /**
     * Remove the npc from the game.
     *
     * @param npc The NPC to remove.
     */
    public void remove(NPC npc) {
        ScheduledExecutorService e = npcs.remove(npc);
        if (e != null)
            e.shutdown();
        npcs.remove(npc);
        npc.leaveSquare();
    }

    /**
     * Compute the distance between player and NPC.
     *
     * @param player The player.
     * @param npc    The npc.
     * @return The distance.
     */
    public int distance(Player player, NPC npc) {
        Square sp = player.getSquare();
        Square snpc = npc.getSquare();
        return Math.abs(sp.getX() - snpc.getX()) + Math.abs(sp.getY() - snpc.getY());
    }

    /**
     * Returns whether this level is in progress, i.e. whether moves can be made
     * on the board.
     *
     * @return <code>true</code> iff this level is in progress.
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * Updates the observers about the state of this level.
     */
    private void updateObservers() {
        if (!isAnyPlayerAlive()) {
            for (LevelObserver o : observers) {
                o.levelLost();
            }
        }
        if (remainingPellets() == 0) {
            for (LevelObserver o : observers) {
                o.levelWon();
            }
        }
    }

    /**
     * Returns <code>true</code> iff at least one of the players in this level
     * is alive.
     *
     * @return <code>true</code> if at least one of the registered players is
     * alive.
     */
    public boolean isAnyPlayerAlive() {
        for (Player p : players) {
            if (p.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts the pellets remaining on the board.
     *
     * @return The amount of pellets remaining on the board.
     */
    public int remainingPellets() {
        Board b = getBoard();
        int pellets = 0;
        for (int x = 0; x < b.getWidth(); x++) {
            for (int y = 0; y < b.getHeight(); y++) {
                for (Unit u : b.squareAt(x, y).getOccupants()) {
                    if (u instanceof Pellet) {
                        pellets++;
                    }
                }
            }
        }
        return pellets;
    }

    /**
     * Return the Map of NPCs and ScheduledService.
     *
     * @return The Map of NPCs and ScheduledService.
     */
    public Map<NPC, ScheduledExecutorService> getNpcs() {
        return npcs;
    }

    /**
     * An observer that will be notified when the level is won or lost.
     *
     * @author Jeroen Roosen
     */
    public interface LevelObserver {

        /**
         * The level has been won. Typically the level should be stopped when
         * this event is received.
         */
        void levelWon();

        /**
         * The level has been lost. Typically the level should be stopped when
         * this event is received.
         */
        void levelLost();
    }

    /**
     * A task that moves an NPC and reschedules itself after it finished.
     *
     * @author Jeroen Roosen
     */
    private final class NpcMoveTask implements Runnable {

        /**
         * The service executing the task.
         */
        private final ScheduledExecutorService service;

        /**
         * The NPC to move.
         */
        private final NPC npc;

        /**
         * Creates a new task.
         *
         * @param s The service that executes the task.
         * @param n The NPC to move.
         */
        private NpcMoveTask(ScheduledExecutorService s, NPC n) {
            this.service = s;
            this.npc = n;
        }

        @Override
        public void run() {
            Direction nextMove = npc.nextMove();
            if (nextMove != null) {
                move(npc, nextMove);
            }
            long interval = npc.getInterval();
            service.schedule(this, interval, TimeUnit.MILLISECONDS);
        }
    }
}
