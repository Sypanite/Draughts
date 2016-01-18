package sypan.draughts.client.gui.substate;

import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.Image;

import com.jme3.math.Vector2f;

public class RespondDrawSubstate extends AbstractGUIState {

    private final static Vector2f WINDOW_DIMENSIONS = new Vector2f(410, 215);

    public RespondDrawSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_RESPOND_DRAW, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/DRAW_FORFEIT_BACK.png");
    }

    @Override
    protected void createState() {
        addChild(new Image(getGUI().getScreen(), "INFORMATION", new Vector2f(7, 52), getWindowDimensions().setY(18), "gui/interface/DRAW_OFFER.png"));
        addChild(new Image(getGUI().getScreen(), "QUOTE", new Vector2f(4, 15), getWindowDimensions().setY(25), "gui/interface/DRAW_QUOTE.png"));
        addChild(new GUIButton(getGUI(), ButtonType.DRAW_REQUEST_ACCEPT, new Vector2f(129, 115)));
        addChild(new GUIButton(getGUI(), ButtonType.DRAW_REQUEST_DECLINE, new Vector2f(129, 155)));
    }

    /**
     * @return a non-final version of this window's dimensions.
	 *
     */
    private Vector2f getWindowDimensions() {
        return WINDOW_DIMENSIONS.clone();
    }
}
