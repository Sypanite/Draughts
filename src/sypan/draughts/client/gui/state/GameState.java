package sypan.draughts.client.gui.state;

import java.util.concurrent.Callable;

import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.ShadowLabel;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

/**
 * {@code GameState} is the {@code GUIState} active when the game is being
 * played. It has four buttons pertaining to game-play: <i>Forfeit</i>, <i>Offer
 * Draw</i>, <i>Settings</i>, and
 * <i>Move History</i>.<p>
 *
 * It also has a {@code ShadowLabel} element used to display game-related
 * notifications via the {@code notify} method (not to be confused with
 * {@code Object.notify()}).
 *
 * @author Carl Linley
 **/
public class GameState extends AbstractGUIState {

    private ShadowLabel notificationLabel;
    private int activeNotifications;

    protected ButtonType currentlyPressed;
    protected GUIButton[] gameButton;

    public GameState(GUI gui) {
        super(gui, StateType.STATE_GAME);
    }

    @Override
    public void createState() {
        gameButton = new GUIButton[3];

        // Clamp these buttons, Forfeit and Offer Draw, to the top left of the screen.
        gameButton[0] = new GUIButton(getGUI(), ButtonType.GAME_FORFEIT, Vector2f.ZERO);
        gameButton[1] = new GUIButton(getGUI(), ButtonType.GAME_OFFER_DRAW, new Vector2f(52, 0));

        // Clamp these buttons, Settings and Move History, to the top right of the screen.
        gameButton[2] = new GUIButton(getGUI(), ButtonType.GAME_SETTINGS, new Vector2f(getGUI().getWidth() - 50, 0));
        //gameButton[3] = new GUIButton(getGUI(), ButtonType.GAME_MOVE_HISTORY, new Vector2f(getGUI().getWidth() - 102, 0));

        for (GUIButton guiButton : gameButton) {
            addChild(guiButton);
        }

        Effect fadeIn = new Effect(EffectType.FadeIn, EffectEvent.Show, 0.25f),
               fadeOut = new Effect(EffectType.FadeOut, EffectEvent.Hide, 0.25f);

        // Initialise the game notification label
        notificationLabel = new ShadowLabel(screen, "NOTIFICATION_LABEL", Vector2f.ZERO, "", ColorRGBA.White);
        notificationLabel.setDimensions(getGUI().getWidth(), 24);
        notificationLabel.setFont("gui/FONT1.fnt", 24);
        notificationLabel.setIgnoreMouse(true);
        notificationLabel.addEffect(fadeIn);
        notificationLabel.addEffect(fadeOut);
        addChild(notificationLabel);
    }

    public void setPressedGameButton(ButtonType buttonType) {
        if (currentlyPressed != null) {
            resetPressedGameButton();
        }
        int buttonIndex = buttonType.ordinal() - 5;

        gameButton[buttonIndex].setColorMap("gui/buttons/GAME_BUTTON_PRESSED.png");
        gameButton[buttonIndex].setButtonHoverInfo("gui/buttons/GAME_BUTTON_PRESSED_HOVER.png", null);
        gameButton[buttonIndex].onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        gameButton[buttonIndex].onGetFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        currentlyPressed = buttonType;
    }

    public void resetPressedGameButton() {
        if (currentlyPressed != null) {
            int buttonIndex = currentlyPressed.ordinal() - 5;

            gameButton[buttonIndex].setColorMap("gui/buttons/GAME_BUTTON.png");
            gameButton[buttonIndex].setButtonHoverInfo("gui/buttons/GAME_BUTTON_HOVER.png", null);
            currentlyPressed = null;
        }
    }

    /**
     * Displays the specified notification using the default colour (white) for
     * the default amount of time (three seconds).
     *
     * @param newNotification - the notification to display.
     **/
    public void notify(String newNotification) {
        notify(newNotification, ColorRGBA.White, 3000);
    }

    /**
     * Displays the specified notification, using the specified colour, for the
     * specified amount of time.
     *
     * @param newNotification - the notification to display.
     * @param notificationColour - the colour the notification should display
     * in.
     * @param displayTimeMS - the number of milliseconds that the notification
     * will be visible.
     **/
    public void notify(String newNotification, ColorRGBA notificationColour, int displayTimeMS) {
        notificationLabel.setText(newNotification);
        notificationLabel.setColour(notificationColour);
        notificationLabel.setPosition((getGUI().getWidth() / 2) - (notificationLabel.getTextWidth() / 2) + 10, getGUI().getHeight() - 30);

        if (activeNotifications == 0) {
            notificationLabel.showWithEffect();
        }
        activeNotifications++;

        getClient().getExecutor().submit(() -> {
            Thread.sleep(displayTimeMS);
            
            if (activeNotifications == 1) {
                getClient().enqueue(() -> {
                    notificationLabel.hideWithEffect();
                    return null;
                });
            }
            activeNotifications--;
            return null;
        });
    }
}