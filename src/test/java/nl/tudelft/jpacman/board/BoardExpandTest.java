package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class BoardExpandTest {
	
	private Launcher launcher;
	
	/**
	 * Launch the user interface.
	 */
	@Before
	public void setUpPacman() {
		launcher = new Launcher();
		launcher.launch();
	}
	
	/**
	 * Quit the user interface when we're done.
	 */
	@After
	public void tearDown() {
		launcher.dispose();
	}

    /**
     * Launch the game, and imitate what would happen in a typical game.
     * The test is only a smoke test, and not a focused small test.
     * Therefore it is OK that the method is a bit too long.
     * 
     * @throws InterruptedException Since we're sleeping in this test.
     */
    @SuppressWarnings("methodlength")
    @Test
    public void ExpandTest() throws InterruptedException {
        Game game = launcher.getGame();
        Board board = game.getLevel().getBoard();
        Player player = game.getPlayers().get(0);
 
        // start cleanly.
        assertFalse(game.isInProgress());
        game.start();
        assertTrue(game.isInProgress());

        int sizeX = board.getWidth();
        int sizeY = board.getHeight();
        board.expand(Direction.EAST);
        assertTrue(sizeX < board.getWidth());
        assertEquals(sizeY, board.getHeight());

        sizeX = board.getWidth();
        sizeY = board.getHeight();
        board.expand(Direction.WEST);
        assertTrue(sizeX < board.getWidth());
        assertEquals(sizeY, board.getHeight());

        sizeX = board.getWidth();
        sizeY = board.getHeight();
        board.expand(Direction.NORTH);
        assertTrue(sizeY < board.getHeight());
        assertEquals(sizeX, board.getWidth());

        sizeX = board.getWidth();
        sizeY = board.getHeight();
        board.expand(Direction.SOUTH);
        assertTrue(sizeY < board.getHeight());
        assertEquals(sizeX, board.getWidth());


     }
}
