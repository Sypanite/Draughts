package sypan.draughts.client.gui;

import sypan.draughts.client.Client;
import sypan.draughts.client.gui.element.Image;
import sypan.draughts.client.gui.element.ShadowLabel;
import sypan.draughts.client.gui.state.*;
import sypan.draughts.client.gui.substate.*;
import sypan.draughts.game.GameReplay;
import sypan.utility.Logger;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.scrolling.VScrollBar;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

/**
 * {@code GUI} is the go-to class with regards to anything <i>Graphical User Interface</i>.<p>
 *
 * The GUI system uses JMonkeyEngine's user-contributed {@code tonegodgui}
 * library, created by the user 't0neg0d'. I really like this library, it makes
 * creating GUIs in JME applications a far more pleasant experience in my
 * opinion!
 *
 * @see
 * <a href="http://hub.jmonkeyengine.org/users/t0neg0d">http://hub.jmonkeyengine.org/users/t0neg0d</a>
 *
 * @author Carl Linley
 **/
public final class GUI {

    private final Screen screen;
    private final Client client;

    private final ShadowLabel restartLabel;

    private AbstractGUIState currentState, currentSubstate;

    public GUI(Client client) {
        this.client = client;

        screen = new Screen(client);
        screen.setUseUIAudio(false);

        client.getGuiNode().addControl(screen);

        restartLabel = new ShadowLabel(screen, "restartLabel", new Vector2f(10, getHeight() - 26), "Draughts must be restarted for the change in resolution to take effect.", ColorRGBA.Red, 16, "FONT1");
        screen.addElement(restartLabel);
        restartLabel.hide();
    }

    /**
     * @param stateType - the type of state to retrieve.
     * @return a new instance of the specified state based on <b>stateType</b>.
     * @see StateType
     **/
    private AbstractGUIState getInstance(StateType stateType) {
        switch (stateType) {
            /* States */
            case STATE_GAME:
                return new GameState(this);

            case STATE_GAME_REPLAY:
                return new GameReplayState(this);

            case STATE_GAME_SPECTATOR:
                return new GameSpectatorState(this);

            case STATE_MAIN_MENU:
                return new MainMenuState(this);

            /* Sub-states */
            case SUBSTATE_LOAD_GAME:
                return new LoadGameSubstate(this);

            case SUBSTATE_CREDITS:
                return new CreditsSubstate(this);

            case SUBSTATE_SETTINGS:
                return new SettingsSubstate(this);

            case SUBSTATE_CONFIRM_FORFEIT:
                return new ConfirmForfeitSubstate(this);

            case SUBSTATE_OFFER_DRAW:
                return new OfferDrawSubstate(this);

            case SUBSTATE_RESPOND_DRAW:
                return new RespondDrawSubstate(this);

            case SUBSTATE_NEW_GAME:
                return new NewGameSubstate(this);

            default:
                return null;
        }
    }

    public void destroyState(AbstractGUIState toDestroy, AbstractGUIState toReplace) {
        if (toDestroy != null) {
            toDestroy.hide();//WithEffect();
            screen.removeElement(toDestroy);

            if (toDestroy == currentState) {
                currentState = null;
            } else if (toDestroy == currentSubstate) {
                currentSubstate = null;
            }

            // The hover light should be displayed again
            if (currentState instanceof GameState) {
                if (toDestroy.getType() == StateType.SUBSTATE_RESPOND_DRAW) {
                    client.getCurrentGame().respondDraw(false);
                }
                if (toDestroy.isSubstate()) {
                    client.setShowHover(true);
                }
            }

            if (toReplace != null) {
                initialiseState(toReplace);
            }
        } else if (toReplace != null) {
            initialiseState(toReplace);
        }
    }

    public void setState(StateType newStateType) {
        boolean mainState = newStateType.isMainState();
        AbstractGUIState replacing = (mainState ? currentState : currentSubstate);

        if (replacing != null && newStateType == replacing.getType()) {
            destroyState(replacing, null);
            return;
        }

        AbstractGUIState newState = getInstance(newStateType);

        if (newState != null) {
            destroyState(mainState ? currentState : currentSubstate,
                    mainState ? (currentState = newState)
                            : (currentSubstate = newState));
        } else {
            Logger.logSevere("State '" + newStateType + "' has not been implemented.");
        }
    }

    private void initialiseState(AbstractGUIState toInitialise) {
        toInitialise.createState();

        if (toInitialise.isSubstate() && currentState instanceof GameState) { // The hover light should not update whilst a substate is displayed
            client.setShowHover(false);
        }
        screen.addElement(toInitialise);
        toInitialise.show();//WithEffect();
    }

    public void onButtonClick(ButtonType buttonType) {
        String asString = buttonType.toString();

        if (asString.startsWith("SETTINGS")
         || asString.startsWith("LOAD")
         || asString.startsWith("NEW_GAME")) {
            currentSubstate.onClick(buttonType);
        }
        else {
            switch (buttonType) {
                /* Main menu */
                case MENU_CREDITS:
                    setState(StateType.SUBSTATE_CREDITS);
                break;

                case MENU_LOAD_GAME:
                    setState(StateType.SUBSTATE_LOAD_GAME);
                break;

                case MENU_NEW_GAME:
                    setState(StateType.SUBSTATE_NEW_GAME);
                break;

                case MENU_SETTINGS:
                    setState(StateType.SUBSTATE_SETTINGS);
                break;

                case MENU_QUIT:
                    client.stop();
                break;

                /*
                 * GameState
                 */
                case GAME_OFFER_DRAW:
                    setState(StateType.SUBSTATE_OFFER_DRAW);
                break;

                case GAME_FORFEIT:
                    setState(StateType.SUBSTATE_CONFIRM_FORFEIT);
                break;

                case GAME_SETTINGS:
                    setState(StateType.SUBSTATE_SETTINGS);
                break;

                /* OfferDrawSubstate */
                case OFFER_DRAW_CONFIRM:
                    client.getCurrentGame().offerDraw();

                case OFFER_DRAW_CANCEL:
                    closeSubstate();
                break;

                /* ForfeitGameSubstate */
                case FORFEIT_CONFIRM:
                    client.getCurrentGame().forfeitGame();

                case FORFEIT_CANCEL:
                    closeSubstate();
                break;

                /* MoveHistorySubstate */

                /* ReplayGameState */
                case GAME_REPLAY_NEXT_MOVE:
                    ((GameReplay) client.getCurrentGame()).nextMove();
                break;

                case GAME_REPLAY_PLAY:
                case GAME_REPLAY_PAUSE:
                    ((GameReplay) client.getCurrentGame()).toggleAutoplay();
                break;

                default:
                    Logger.logWarning("Unhandled button click - " + buttonType + "!");
                break;
            }
        }
        if (asString.startsWith("GAME_")) {
            ((GameState) currentState).setPressedGameButton(buttonType);
        }
    }

    public void closeSubstate() {
        if (currentSubstate != null) {
            currentSubstate.hide();//WithEffect();

            destroyState(currentSubstate, null);

            if (stateDisplayed(StateType.STATE_GAME)) {
                ((GameState) getEnforcedState(StateType.STATE_GAME)).resetPressedGameButton();
            }
        }
    }

    public void formatScrollbar(VScrollBar scrollBar) {
        scrollBar.getScrollThumb().setWidth(20);
        scrollBar.getScrollThumb().setColorMap("gui/interface/scroll/THUMB.png");
        scrollBar.getScrollThumb().setButtonHoverInfo("gui/interface/scroll/THUMB_HOVER.png", null);
        scrollBar.getScrollThumb().setButtonPressedInfo("gui/interface/scroll/THUMB_PRESSED.png", null);
        scrollBar.getScrollThumb().addEffect(new Effect(EffectType.ImageSwap, EffectEvent.Hover, 1f));
        scrollBar.getScrollThumb().setUseButtonHoverSound(false);
        scrollBar.getScrollThumb().setUseButtonPressedSound(false);

        scrollBar.getScrollTrack().setWidth(20);
        scrollBar.getScrollTrack().setColorMap("gui/interface/scroll/TRACK.png");

        for (int i = 0; i != 2; i++) {
            ButtonAdapter toConfigure = (i == 0 ? scrollBar.getScrollButtonUp() : scrollBar.getScrollButtonDown());
            toConfigure.removeAllChildren();

            toConfigure.setDimensions(20, 20);
            toConfigure.addEffect(new Effect(EffectType.ImageSwap, EffectEvent.Hover, 1f));
            toConfigure.setColorMap("gui/interface/scroll/SCROLL.png");
            toConfigure.setButtonHoverInfo("gui/interface/scroll/SCROLL_HOVER.png", null);
            toConfigure.setButtonPressedInfo("gui/interface/scroll/SCROLL_PRESSED.png", null);
            toConfigure.addChild(new Image(screen, "BUTTON DOWN LABEL", Vector2f.ZERO, new Vector2f(20, 20), "gui/interface/scroll/" + (i == 0 ? "UP" : "DOWN") + ".png"));
            toConfigure.setUseButtonHoverSound(false);
            toConfigure.setUseButtonPressedSound(false);
        }
    }

    /**
     * This should be called prior to calling {@code getEnforcedState} to ensure
     * type safety.
     *
     * @param stateType - the state to check for.
     * @return true if a state of the specified type is currently displayed on
     * the GUI.
	 *
     */
    public boolean stateDisplayed(StateType stateType) {
        if (stateType.isMainState()) {
            return (currentState != null && currentState.getType() == stateType);
        }
        return (currentSubstate != null && currentSubstate.getType() == stateType);
    }

    /**
     * Returns the active {@code GUIState} of the specified type if such a state
     * exists, or null if not.<p>
     *
     * This should be used in conjunction with a {@code stateDisplayed} check to
     * ensure type safety.
     *
     * @param stateType - the type of state required.
     * @return the active {@code GUIState} for the specified type if it exists,
     * or {@code null} if not.
     **/
    public AbstractGUIState getEnforcedState(StateType stateType) {
        if (currentState != null && currentState.getType() == stateType) {
            return currentState;
        }
        else if (currentSubstate != null && currentSubstate.getType() == stateType) {
            return currentSubstate;
        }
        return null;
    }

    public Client getClient() {
        return client;
    }

    public Screen getScreen() {
        return screen;
    }

    public float getWidth() {
        return client.getScreenDimensions().getX();
    }

    public float getHeight() {
        return client.getScreenDimensions().getY();
    }

    public AbstractGUIState getState() {
        return currentState;
    }

    public AbstractGUIState getSubstate() {
        return currentSubstate;
    }

    public boolean substateDisplayed() {
        return currentSubstate != null;
    }

    public void markRestartRequired() {
        restartLabel.show();
    }

    /**
     * Allows any states/substates with sound to update their volume.
     **/
    public void updateVolume() {
        if (currentState != null) {
            currentState.updateVolume();
        }
        if (currentSubstate != null) {
            currentSubstate.updateVolume();
        }
    }
}