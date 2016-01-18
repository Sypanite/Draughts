package sypan.draughts.game.player;

/**
 * A shell player, used in replaying.
 * 
 * @author Carl Linley
 **/
public class Spectator extends Player {

    public Spectator() {
        super(PlayerType.SPECTATOR, Side.SPECTATOR);
    }
}