package sypan.draughts.game.move;

/**
 * {@code TurnType} is an enumerated type decided at the start of a new turn. It is
 * closely related to {@link MoveType}.<p>
 * 
 * The turn's 'type' is decided based on the current state of the board. When a player
 * attempts to move a piece, the current turn's type is checked before anything else.
 * If they are trying to perform an illegal move based on the turn's type, the move is
 * blocked.
 * <p>
 * {@code TurnType} can be:
 * <p>
 * {@code FREE} - The player is free to move a piece of their choice.<br>
 * {@code TAKE_ENFORCED} - The player can take a single piece, and <b>must</b> do so. This turn is taken for them, and the message "Jump enforced!" is displayed.<br>
 * {@code TAKE_CHOICE} - The player has a choice between taking various pieces, and must pick one. They cannot make any other move.
 * <p>
 * This functionality was added on 03/03/15 - thanks to Dr. Chris Windmill for pointing out that it was missing.
 * 
 * @see MoveType
 * @author Carl Linley
 **/
public enum TurnType {
    FREE,
    TAKE_ENFORCED,
    TAKE_CHOICE,
    RESPOND_DRAW;
}