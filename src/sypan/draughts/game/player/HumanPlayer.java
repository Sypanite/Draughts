package sypan.draughts.game.player;

/**
 * {@code HumanPlayer} is a very small class used for handling human players. It,
 * like {@link AIPlayer}, extends the abstract class {@link Player}.<p>
 * 
 * Originally, a simple instance of the superclass handled human players. However, I feel
 * having a separate class makes things neater. {@link Player} was made abstract as it
 * should not be initialised directly.
 * 
 * @author Carl Linley
 **/
public class HumanPlayer extends Player {

    public HumanPlayer(Side mySide) {
        super(PlayerType.HUMAN, mySide);
        setName("Human");
    }
}