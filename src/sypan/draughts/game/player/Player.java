package sypan.draughts.game.player;

/**
 * {@code Player} is the superclass of both {@link HumanPlayer} and {@link AIPlayer}.
 * It contains code applicable to both types of player.<p>
 * 
 * As this class should not be instantiated directly, it is declared abstract.
 **/
public abstract class Player {

    private final PlayerType playerType;
    private final Side mySide;
    private String myName;

    protected Player(PlayerType playerType, Side mySide) {
        this.playerType = playerType;
        this.mySide = mySide;
    }

    public void setName(String myName) {
        this.myName = myName;
    }

    public boolean isHuman() {
        return playerType == PlayerType.HUMAN;
    }

    public PlayerType getType() {
        return playerType;
    }

    public Side getSide() {
        return mySide;
    }

    public String getName() {
        return myName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isAI() {
        return playerType != PlayerType.HUMAN
            && playerType != PlayerType.SPECTATOR;
    }
}