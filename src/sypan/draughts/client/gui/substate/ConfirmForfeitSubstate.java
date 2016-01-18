package sypan.draughts.client.gui.substate;

import sypan.draughts.client.gui.ButtonType;
import sypan.draughts.client.gui.GUI;
import sypan.draughts.client.gui.AbstractGUIState;
import sypan.draughts.client.gui.StateType;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.Image;

import com.jme3.math.Vector2f;

/**
 * @author Carl Linley
 **/
public class ConfirmForfeitSubstate extends AbstractGUIState {

    private final static Vector2f WINDOW_DIMENSIONS = new Vector2f(410, 215);

    public ConfirmForfeitSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_CONFIRM_FORFEIT, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/DRAW_FORFEIT_BACK.png");
    }

    @Override
    protected void createState() {
        addChild(new Image(getGUI().getScreen(), "INFORMATION", new Vector2f(0, 52), getWindowDimensions().setY(18), "gui/interface/CONFIRM_FORFEIT.png"));
        addChild(new Image(getGUI().getScreen(), "QUOTE", new Vector2f(0, 15), getWindowDimensions().setY(25), "gui/interface/FORFEIT_QUOTE.png"));
        addChild(new GUIButton(getGUI(), ButtonType.FORFEIT_CONFIRM, new Vector2f(129, 115)));
        addChild(new GUIButton(getGUI(), ButtonType.FORFEIT_CANCEL, new Vector2f(129, 155)));
    }

    /**
     * @return a non-final version of this window's dimensions.
     **/
    private Vector2f getWindowDimensions() {
        return WINDOW_DIMENSIONS.clone();
    }
}