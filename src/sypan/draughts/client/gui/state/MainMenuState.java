package sypan.draughts.client.gui.state;

import com.jme3.audio.AudioNode;
import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.Image;

import com.jme3.math.Vector2f;
import sypan.utility.Logger;

/**
 * {@code MainMenuState} is the {@code GUIState} active when the player is at
 * the main menu.
 *
 * @author Carl Linley
 **/
public class MainMenuState extends AbstractGUIState {

    private final Vector2f LOGO_DIMENSIONS = new Vector2f(414, 71),
                           BUTTON_DIMENSIONS = GUIButton.getDimensions("MENU_OPTION");

    private final GUIButton[] guiButton;
    private AudioNode hoverSound, pressSound;

    public MainMenuState(GUI gui) {
        super(gui, StateType.STATE_MAIN_MENU);
        guiButton = new GUIButton[5];
    }

    @Override
    public void createState() {
        hoverSound = new AudioNode(getClient().getAssetManager(), "sounds/gui/HOVER.wav");
        pressSound = new AudioNode(getClient().getAssetManager(), "sounds/gui/PRESS.wav");
        pressSound.setPositional(false);
        updateVolume();

        addChild(new Image(getGUI().getScreen(), "LOGO", clampCentre(getGUI(), LOGO_DIMENSIONS).setY(30), LOGO_DIMENSIONS, "gui/menu/LOGO.png"));

        for (int i = 0; i != 5; i++) {
            guiButton[i] = new GUIButton(getGUI(), ButtonType.getButton(i), clampCentre(getGUI(), BUTTON_DIMENSIONS).setY(150 + (i * 50)));
            guiButton[i].setHoverSound(hoverSound);
            guiButton[i].setPressSound(pressSound);

            addChild(guiButton[i]);
        }
    }

    @Override
    protected void updateVolume() {
        hoverSound.setVolume(getClient().getConfig().getSoundVolume());
        pressSound.setVolume(getClient().getConfig().getSoundVolume());
        Logger.logInfo("Updated volume.");
    }
}