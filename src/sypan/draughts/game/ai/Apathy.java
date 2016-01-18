package sypan.draughts.game.ai;

import java.util.ArrayList;

import sypan.draughts.game.Game;
import sypan.draughts.game.move.Move;

/**
 * {@code Apathy} is the most basic AI and the first one to be written.<p>
 *
 * {@code Apathy} by name, apathetic by nature - it simply does not care about
 * the game and moves a pseudo-random piece in a pseudo-random direction, not
 * taking into account any potential consequences of said move or using any form
 * of strategy. For this reason, it is easy to beat.
 *
 * @author Carl Linley
 **/
public class Apathy extends AbstractAIType {

    @Override
    public Move calculateMove(Game game) {
        ArrayList<Move> possibleMoves = getPossibleMoves(getSide(), game);

        return possibleMoves.get(getRandom().nextInt(possibleMoves.size()));
    }

    /*
     * Accept the draw offer if we get two 'true' values from Random's nextBoolean() method.
     */
    @Override
    public boolean onDrawOffer(Game currentGame) {
        return getRandom().nextBoolean() && getRandom().nextBoolean();
    }
}