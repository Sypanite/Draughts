package sypan.draughts.client.gui.substate;

import sypan.draughts.client.gui.AbstractGUIState;
import sypan.draughts.client.gui.GUI;
import sypan.draughts.client.gui.StateType;

import com.jme3.math.Vector2f;

/**
 * @author Carl Linley
 **/
public class CreditsSubstate extends AbstractGUIState {

    private final static Vector2f WINDOW_DIMENSIONS = new Vector2f(410, 405);

    public CreditsSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_CREDITS, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/CREDITS.png");
    }

    @Override
    protected void createState() {
    }
}
