package sypan.draughts.client.gui;

import sypan.draughts.client.Client;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Element;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

import com.jme3.math.Vector2f;

/**
 * {@code AbstractGUIState} is the superclass of every class in the
 * {@code sypan.draughts.client.gui.state} and
 * {@code sypan.draughts.client.gui.substate} packages. {@code GUIState} itself
 * is a subclass of {@code Panel}, allowing it and all subclasses to be added to
 * the GUI.<p>
 *
 * {@code AbstractGUIState} was originally an interface. I changed this because
 * a GUI state <i>must</i> extend Panel - keeping things safe and enforcing this
 * using an abstract class made sense.<p>
 *
 * It forces subclasses to define one method: {@code void createState()}. This
 * is used during the initialisation of GUI states.
 *
 * @author Carl Linley
 *
 */
public abstract class AbstractGUIState extends Panel {

    private final GUI gui;
    private final StateType stateType;

    /**
     * This constructor should be used by 'main' states. By default, it is
     * invisible, immovable, non-resizable, and fills the entire screen.
     *
     * @param gui - the instance of {@code GUI} in use.
     * @param stateType - the state we're creating.
     **/
    protected AbstractGUIState(GUI gui, StateType stateType) {
        super(gui.getScreen(), stateType.toString(), Vector2f.ZERO, gui.getClient().getScreenDimensions());
        this.gui = gui;
        this.stateType = stateType;

        setIgnoreMouse(true);
        setIsResizable(false);
        setIsMovable(false);
        setGlobalAlpha(0);
    }

    public boolean isSubstate() {
        return !stateType.isMainState();
    }

    @Override
    public final void setIgnoreMouse(boolean ignoreMouse) {
        for (Element e : this.getElements()) {
            e.setIgnoreMouse(ignoreMouse);
        }
        super.setIgnoreMouse(ignoreMouse);
    }

    /**
     * This constructor should be used by substates to create a window. By
     * default, It is immovable, non-resizable, and uses the specified position
     * and dimension vectors.<p>
     *
     * This constructor also includes a default fade-in/fade-out effect. This
     * must be activated in {@code createState} via the {@code showWithEffect()}
     * method.
     *
     * @param gui - the instance of {@code GUI} in use.
     * @param stateType - the state we're creating.
     * @param windowPosition - the position of the window in pixels.
     * @param windowDimensions - the dimensions of the window in pixels.
     * @param backgroundPath - the path to the window's background image.
     **/
    protected AbstractGUIState(GUI gui, StateType stateType, Vector2f windowPosition, Vector2f windowDimensions, String backgroundPath) {
        super(gui.getScreen(), "GAMESTATE", windowPosition, windowDimensions);
        this.gui = gui;
        this.stateType = stateType;

        setIsResizable(false);
        setIsMovable(false);
        setColorMap(backgroundPath);

        addEffect(new Effect(EffectType.FadeIn, EffectEvent.Show, 0.25f));
        addEffect(new Effect(EffectType.FadeOut, EffectEvent.Hide, 0.25f));
    }

    /**
     * This method must be implemented by all subclasses. It is called by
     * {@code GUI} during initialisation of the state.
     **/
    protected abstract void createState();

    public void onClick(ButtonType buttonType) {
    }

    /**
     * @return the instance of {@code GUI}.
     **/
    protected GUI getGUI() {
        return gui;
    }

    /**
     * Convenience method - saves all that "{@code getGUI().getClient()}"
     * such-and-such.<p>
     *
     * @return the instance of {@code Client}.
     **/
    protected Client getClient() {
        return gui.getClient();
    }

    /**
     * @return this state's type.
     **/
    protected StateType getType() {
        return stateType;
    }

    /**
     * @param gui - the active instance of GUI.
     * @param elementDimensions - the dimensions of the element to centre.
     *
     * @return a two dimensional vector storing the position an element of the
     * specified dimensions must be located at in order to be in the centre of
     * the screen.
     **/
    protected static Vector2f clampCentre(GUI gui, Vector2f elementDimensions) {
        return new Vector2f((gui.getWidth() / 2) - (elementDimensions.getX() / 2),
                            (gui.getHeight() / 2) - (elementDimensions.getY() / 2));
    }

    /**
     * @param gui - the active instance of GUI.
     * @param xOffset - the distance the element should be from the left of the screen.
     * @param yOffset - the distance the element should be from the bottom of the screen.
     *
     * @return a two dimensional vector storing the position an element must be
     * located at in order to be in the bottom left of the screen with the given
     * offset.
     **/
    protected static Vector2f clampBottomLeft(GUI gui, int xOffset, int yOffset) {
        return new Vector2f(xOffset, gui.getClient().getScreenDimensions().getY() - yOffset);
    }
}