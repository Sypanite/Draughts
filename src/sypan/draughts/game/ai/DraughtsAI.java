package sypan.draughts.game.ai;

import sypan.draughts.game.Game;
import sypan.draughts.game.move.Move;
import sypan.draughts.game.player.AIPlayer;

/**
 * {@code DraughtsAI} is implemented by all types of AI via <i>AbstractAIType</i>.<p>
 *
 * It enforces just two methods:
 * <p>
 * {@code calculateMove(AIPlayer aiPlayer, Game game)} - returns an instance of
 * {@code Move}. This method is called when it is the AI's turn to play. The
 * instance of {@code Move} returned by this method is then played by the AI.<p>
 *
 * @author Carl Linley
 **/
public interface DraughtsAI {

    /**
     * This method returns an instance of {@code Move}. What the move involves
     * is entirely up to the implementing type of AI and varies between
     * strategies.
     *
     * @param currentGame - the current game. This grants access to the current
     * state of the board, this player's pieces, and a variety of other things
     * related to the game which may be required when calculating the next move
     * (depending on the type of AI).
     *
     * @return an instance of {@code Move} containing the next move to be played
     * by the AI.
     **/
    Move calculateMove(Game currentGame);

    void initialiseType(AIPlayer aiPlayer);

    void updateMovablePieces(Game currentGame);

    /**
     * @param currentGame - the current game.
     * @return true to accept the draw offer, or false to decline.
     *
     **/
    boolean onDrawOffer(Game currentGame);
}
