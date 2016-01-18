package sypan.draughts.client.gui;

/**
 * {@code State} is an enumeration of all possible states the GUI can take.<p>
 *
 * This was originally two separate enumerations - {@code State} and
 * {@code Substate}, but I decided it was easier to merge them into a single
 * enumeration.
 *
 * @author Carl Linley
 **/
public enum StateType {

    STATE_MAIN_MENU,
    STATE_GAME,
    STATE_GAME_REPLAY,
    STATE_GAME_SPECTATOR,
    SUBSTATE_OFFER_DRAW, SUBSTATE_RESPOND_DRAW,
    SUBSTATE_CONFIRM_FORFEIT,
    SUBSTATE_SETTINGS,
    SUBSTATE_MOVE_HISTORY,
    SUBSTATE_CREDITS,
    SUBSTATE_LOAD_GAME,
    SUBSTATE_NEW_GAME;

    /**
     * @return true if this state is a 'main' state, not an overlaid interface.
     **/
    public boolean isMainState() {
        return this == STATE_MAIN_MENU
            || this == STATE_GAME || this == STATE_GAME_REPLAY || this == STATE_GAME_SPECTATOR;
    }
};
