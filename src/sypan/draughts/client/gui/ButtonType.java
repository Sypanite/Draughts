package sypan.draughts.client.gui;

/**
 * {@code ButtonType} contains enumerations for every button that exists.
 *
 * @author Carl Linley
 **/
public enum ButtonType {

    /**
     * States
     **/

    /* MenuState - the main menu */
    MENU_NEW_GAME, MENU_LOAD_GAME, MENU_SETTINGS, MENU_CREDITS, MENU_QUIT,
    /* GameState - the in-game state */
    GAME_FORFEIT, GAME_OFFER_DRAW, GAME_SETTINGS,
    /* GameReplayState - replaying a game */
    GAME_REPLAY_NEXT_MOVE, GAME_REPLAY_PLAY, GAME_REPLAY_PAUSE,
 
    /**
     * Sub-states
     **/
    
    /* ConfirmForfeitSubstate - confirm forfeit attempt */
    FORFEIT_CONFIRM, FORFEIT_CANCEL,
    /* OfferDrawSubstate - confirm draw offer */
    OFFER_DRAW_CONFIRM, OFFER_DRAW_CANCEL,
    /* DrawRequestSubstate - accept or decline opponent's offer of a draw */
    DRAW_REQUEST_ACCEPT, DRAW_REQUEST_DECLINE,
    /* GameSettingsSubstate - configure the game's settings */
    SETTINGS_MODEL_HIGH, SETTINGS_MODEL_MED, SETTINGS_MODEL_LOW, SETTINGS_MODEL_MIN,
    SETTINGS_SHADOW_HIGH, SETTINGS_SHADOW_MED, SETTINGS_SHADOW_LOW, SETTINGS_SHADOW_OFF,
    SETTINGS_ANTIALIAS_X16, SETTINGS_ANTIALIAS_X8, SETTINGS_ANTIALIAS_X4, SETTINGS_ANTIALIAS_X2, SETTINGS_ANTIALIAS_OFF,
    SETTINGS_SAVE, SETTINGS_CANCEL,
    /* LoadGameSubstate */
    LOAD_GAME_LOAD, LOAD_GAME_DELETE,
    /* NewGameSubstate */
    NEW_GAME_START,
    NEW_GAME_HUMAN_1, NEW_GAME_APATHY_1, NEW_GAME_GREEDY_1, NEW_GAME_HUNGRY_1, NEW_GAME_MINMAX_1,
    NEW_GAME_HUMAN_2, NEW_GAME_APATHY_2, NEW_GAME_GREEDY_2, NEW_GAME_HUNGRY_2, NEW_GAME_MINMAX_2;

    /**
     * @param ordinal - the index to retrieve.
     * @return the button of the specified index.
     **/
    public static ButtonType getButton(int ordinal) {
        return values()[ordinal];
    }
}