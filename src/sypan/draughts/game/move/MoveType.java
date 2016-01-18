package sypan.draughts.game.move;


/**
 * {@code MoveType} is an enumerated type containing values for every type of move a player could make. It is
 * closely related to {@link TurnType}.
 * <p>
 * {@code MoveType} can be:
 * <p>
 * {@code INVALID} - The attempted move is invalid, it is not in accordance with the rules of <i>Draughts</i>.<p>
 * 
 * {@code INVALID_SHIFT} - The attempted move is in accordance with the rules of <i>Draughts</i> <b>but</b> as
 * 						   the player is capable of jumping an enemy piece, the move cannot be performed.<p>
 * 
 * {@code VALID_SHIFT} | {@code VALID_JUMP} - The attempted move is in accordance with the rules of <i>Draughts</i> and can be performed.
 * <p>
 * This functionality was added on 03/03/15 - thanks to Dr. Chris Windmill for pointing out that I was missing a
 * gameplay mechanic!
 * 
 * @see TurnType
 * @author Carl Linley
 **/
public enum MoveType {

    INVALID, INVALID_SHIFT, VALID_SHIFT, VALID_JUMP;

    public boolean isValid() {
        return this != INVALID && this != INVALID_SHIFT;
    }
};
