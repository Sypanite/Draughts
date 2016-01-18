package sypan.draughts.game.player;

import sypan.draughts.game.piece.PieceType;
import sypan.utility.Utility;

/**
 * {@code Side} is a small yet efficient enumeration containing only two values
 * - {@code BLACK} and {@code WHITE}. Of course, each value represents its respective
 * 'side' (and thus piece colour). It is used throughout the project.
 * 
 * @author Carl Linley
 **/
public enum Side {

    BLACK, WHITE, SPECTATOR;

    /**
     * @return the opposing side.
     **/
    public Side oppose() {
        return (this == WHITE ? BLACK : WHITE);
    }

    /**
     * @return this side's king.
     **/
    public PieceType getKing() {
        return (this == Side.BLACK ? PieceType.KING_BLACK : PieceType.KING_WHITE);
    }

    /**
     * @return this side's man.
     **/
    public PieceType getMan() {
        return (this == Side.BLACK ? PieceType.MAN_BLACK : PieceType.MAN_WHITE);
    }

    /**
     * @return this side's name, formatted nicely.
     **/
    public String getName() {
        return Utility.formatName(toString());
    }

    /**
     * @return "B" if {@code BLACK}, "W" if {@code WHITE}.
     **/
    public String getID() {
        return this.toString().substring(0, 1);
    }

    /**
     * @return which side of the board this side is. 0 for {@code BLACK}, 7 for {@code WHITE}.
     **/
    public int getY() {
        return (this == BLACK ? 0 : 7);
    }

    /**
     * @return true if this side is not a spectator.
     **/
    public boolean isPlaying() {
        return this != SPECTATOR;
    }

    /**
     * @param sideIndex - the ordinal of the side to retrieve - 0 for BLACK, 1 for WHITE, 2 for SPECTATOR.
     * 
     * @return the specified side.
     **/
    public static Side get(int sideIndex) {
        return values()[sideIndex];
    }
}
