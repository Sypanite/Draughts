package sypan.draughts.game.piece;

import sypan.draughts.game.player.Side;

/**
 * @author Carl Linley
 **/
public enum PieceType {

    MAN_WHITE, KING_WHITE, MAN_BLACK, KING_BLACK;

    public Side getSide() {
        if (this == MAN_WHITE
         || this == KING_WHITE) {
            return Side.WHITE;
        }
        return Side.BLACK;
    }
};
