package sypan.draughts.client.gui.element;

import com.jme3.audio.AudioNode;
import sypan.draughts.client.gui.ButtonType;
import sypan.draughts.client.gui.GUI;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;

/**
 * {@code GUIButton} is a subclass of {@code Button}. It tacks on a few
 * convenience methods to the original class, most notably an associated
 * {@link Image} for the label, dynamic sizing, and built-in effect
 * configuration.
 *
 * @author Carl Linley
 **/
public class GUIButton extends Button {

    private ButtonType buttonType;
    private GUI gui;
    private Image buttonIcon;

    private AudioNode hoverSound, pressSound;

    /**
     * Creates a new button on the GUI.
     *
     * @param gui - the current instance of GUI.
     * @param buttonType - the type of the button.
     * @param buttonPosition - the position (in pixels) of the button on the screen.
     * @param labelImage - the relative path to the image.
 *
     */
    public GUIButton(GUI gui, ButtonType buttonType, Vector2f buttonPosition, String labelImage) {
        super(gui.getScreen(), buttonType.toString(), buttonPosition);
        this.gui = gui;
        this.buttonType = buttonType;

        configureEffects("gui/buttons/" + getBaseType());
        setUseButtonHoverSound(false);
        setUseButtonPressedSound(false);

        addChild((buttonIcon = new Image(screen, buttonType + "_LABEL", Vector2f.ZERO, Vector2f.ZERO, "gui/buttons/" + labelImage + ".png")));

        setDimensions(getDimensions(getBaseType()));
    }

    public GUIButton(GUI gui, ButtonType buttonType, Vector2f buttonPosition) {
        this(gui, buttonType, buttonPosition, buttonType.toString());
    }

    public void changeType(ButtonType newType) {
        buttonType = newType;
        buttonIcon.setColorMap("gui/buttons/" + buttonType + ".png");
    }

    @Override
    public final void setDimensions(Vector2f newDimensions) {
        super.setDimensions(newDimensions);
        buttonIcon.setDimensions(newDimensions);
        buttonIcon.setPosition(Vector2f.ZERO);
    }

    private void configureEffects(String baseButtonPath) {
        setColorMap(baseButtonPath + ".png");
        setButtonHoverInfo(baseButtonPath + "_HOVER.png", null);
        setButtonPressedInfo(baseButtonPath + "_PRESSED.png", null);
        addEffect(new Effect(EffectType.ImageSwap, EffectEvent.Hover, 1f));
    }

    public void setHoverSound(AudioNode audioNode) {
       hoverSound = audioNode;
    }

    public void setPressSound(AudioNode audioNode) {
       pressSound = audioNode;
    }

    public final String getBaseType() {
        switch (buttonType) {
            case GAME_FORFEIT:
            case GAME_OFFER_DRAW:
            case GAME_SETTINGS:
            case GAME_REPLAY_NEXT_MOVE:
            case GAME_REPLAY_PLAY:
            case GAME_REPLAY_PAUSE:
                return "GAME_BUTTON";

            case FORFEIT_CONFIRM:
            case OFFER_DRAW_CONFIRM:
            case DRAW_REQUEST_ACCEPT:
                return "ACCEPT";

            case FORFEIT_CANCEL:
            case OFFER_DRAW_CANCEL:
            case DRAW_REQUEST_DECLINE:
                return "DECLINE";

            case LOAD_GAME_LOAD:
            case NEW_GAME_START:
                return "LOAD_GAME";

            case LOAD_GAME_DELETE:
                return "DELETE_GAME";

            case MENU_CREDITS:
            case MENU_LOAD_GAME:
            case MENU_NEW_GAME:
            case MENU_QUIT:
            case MENU_SETTINGS:
                return "MENU_OPTION";

            case SETTINGS_ANTIALIAS_X16:
            case SETTINGS_ANTIALIAS_X8:
            case SETTINGS_ANTIALIAS_X4:
            case SETTINGS_ANTIALIAS_X2:
            case SETTINGS_ANTIALIAS_OFF:
            case SETTINGS_MODEL_HIGH:
            case SETTINGS_MODEL_MED:
            case SETTINGS_MODEL_LOW:
            case SETTINGS_MODEL_MIN:
            case SETTINGS_SHADOW_HIGH:
            case SETTINGS_SHADOW_MED:
            case SETTINGS_SHADOW_LOW:
            case SETTINGS_SHADOW_OFF:
                return "SETTING_UNSELECTED";

            case NEW_GAME_HUMAN_1:
            case NEW_GAME_APATHY_1:
            case NEW_GAME_GREEDY_1:
            case NEW_GAME_HUNGRY_1:
            case NEW_GAME_MINMAX_1:
            case NEW_GAME_HUMAN_2:
            case NEW_GAME_APATHY_2:
            case NEW_GAME_GREEDY_2:
            case NEW_GAME_HUNGRY_2:
            case NEW_GAME_MINMAX_2:
                return "NEW_GAME_PLAYER_UNSELECTED";

            case SETTINGS_SAVE:
            case SETTINGS_CANCEL:
                return "SETTINGS_BUTTON";
            default:
                break;

        }
        return null;
    }

    public static Vector2f getDimensions(String baseType) {
        switch (baseType) {
            case "GAME_BUTTON":
                return new Vector2f(50, 50);

            case "LOAD_GAME":
            case "DELETE_GAME":
                return new Vector2f(130, 30);

            case "NEW_GAME_PLAYER_UNSELECTED":
                return new Vector2f(83, 22);

            case "SETTING_SELECTED":
            case "SETTING_UNSELECTED":
                return new Vector2f(53, 18);

            case "SETTINGS_BUTTON":
                return new Vector2f(108, 37);

            case "MENU_OPTION":
                return new Vector2f(250, 40);

            case "ACCEPT":
            case "DECLINE":
                return new Vector2f(150, 30);
        }
        return null;
    }

    public ButtonType getType() {
        return buttonType;
    }

    @Override
    public void onButtonFocus(MouseMotionEvent arg0) {
        if (hoverSound != null) {
            hoverSound.playInstance();
        }
    }

    @Override
    public void onButtonLostFocus(MouseMotionEvent arg0) {
    }

    @Override
    public void onButtonMouseLeftDown(MouseButtonEvent arg0, boolean arg1) {
    }

    @Override
    public void onButtonMouseLeftUp(MouseButtonEvent arg0, boolean arg1) {
        if (pressSound != null) {
            pressSound.playInstance();
        }
        gui.onButtonClick(buttonType);
    }

    @Override
    public void onButtonMouseRightDown(MouseButtonEvent arg0, boolean arg1) {
    }

    @Override
    public void onButtonMouseRightUp(MouseButtonEvent arg0, boolean arg1) {
    }
}