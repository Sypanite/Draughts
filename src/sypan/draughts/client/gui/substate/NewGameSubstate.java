package sypan.draughts.client.gui.substate;

import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.game.Game;
import sypan.draughts.game.player.PlayerType;

import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;

public class NewGameSubstate extends AbstractGUIState {

    private static final Vector2f WINDOW_DIMENSIONS = new Vector2f(270, 270);
    private static final int BUTTON_TYPE_OFFSET = ButtonType.NEW_GAME_START.ordinal();

    private final GUIButton[] interfaceButton;

    private PlayerType playerOne = PlayerType.HUMAN,
            playerTwo = PlayerType.HUMAN;

    public NewGameSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_NEW_GAME, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/NEW_GAME_BACKING.png");
        interfaceButton = new GUIButton[11];
    }

    @Override
    protected void createState() {
        interfaceButton[0] = new GUIButton(getGUI(), ButtonType.NEW_GAME_START, new Vector2f(70, 220));

        createColumn(1);
        createColumn(2);
        setSelected(1);
        setSelected(6);
        interfaceButton[1].onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        interfaceButton[6].onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));

        for (GUIButton b : interfaceButton) {
            addChild(b);
        }
    }

    private void createColumn(int playerID) {
        int arrayIndex = (playerID == 2 ? 6 : 1),
            positionX = (playerID == 1 ? 30 : 155);

        interfaceButton[arrayIndex++] = new GUIButton(getGUI(), ButtonType.valueOf("NEW_GAME_HUMAN_" + playerID), new Vector2f(positionX, 66), "PLAYER_HUMAN");
        interfaceButton[arrayIndex++] = new GUIButton(getGUI(), ButtonType.valueOf("NEW_GAME_APATHY_" + playerID), new Vector2f(positionX, 101), "PLAYER_APATHY");
        interfaceButton[arrayIndex++] = new GUIButton(getGUI(), ButtonType.valueOf("NEW_GAME_GREEDY_" + playerID), new Vector2f(positionX, 125), "PLAYER_GREEDY");
        interfaceButton[arrayIndex++] = new GUIButton(getGUI(), ButtonType.valueOf("NEW_GAME_HUNGRY_" + playerID), new Vector2f(positionX, 149), "PLAYER_HUNGRY");
        interfaceButton[arrayIndex] = new GUIButton(getGUI(), ButtonType.valueOf("NEW_GAME_MINMAX_" + playerID), new Vector2f(positionX, 173), "PLAYER_MIN_MAX");
    }

    private void setSelected(int buttonIndex) {
        interfaceButton[buttonIndex].setColorMap("gui/buttons/NEW_GAME_PLAYER_SELECTED.png");
        interfaceButton[buttonIndex].setButtonHoverInfo("gui/buttons/NEW_GAME_PLAYER_SELECTED_HOVER.png", null);
        interfaceButton[buttonIndex].setButtonPressedInfo("gui/buttons/NEW_GAME_PLAYER_SELECTED_PRESSED.png", null);

        interfaceButton[buttonIndex].onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        interfaceButton[buttonIndex].onGetFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
    }

    private void setUnselected(int buttonIndex) {
        interfaceButton[buttonIndex].setColorMap("gui/buttons/NEW_GAME_PLAYER_UNSELECTED.png");
        interfaceButton[buttonIndex].setButtonHoverInfo("gui/buttons/NEW_GAME_PLAYER_UNSELECTED_HOVER.png", null);
        interfaceButton[buttonIndex].setButtonPressedInfo("gui/buttons/NEW_GAME_PLAYER_UNSELECTED_PRESSED.png", null);
    }

    @Override
    public void onClick(ButtonType buttonType) {
        if (buttonType == ButtonType.NEW_GAME_START) {
            getClient().setCurrentGame(new Game(getClient(), playerOne, playerTwo));
        }
        else {
            String asString = buttonType.toString();
            int playerID = Integer.parseInt(asString.substring(asString.length() - 1, asString.length()));

            switchButton(playerID == 1 ? playerOne : playerTwo, buttonType, playerID == 1 ? 1 : 6);
            setSelection(playerID, PlayerType.valueOf(asString.split("_")[2]));
        }
    }

    private void setSelection(int playerID, PlayerType playerType) {
        if (playerID == 1) {
            playerOne = playerType;
        }
        else {
            playerTwo = playerType;
        }
    }

    private void switchButton(Enum<?> toDeselect, ButtonType buttonType, int offset) {
        setUnselected(toDeselect.ordinal() + offset);
        setSelected(buttonType.ordinal() - BUTTON_TYPE_OFFSET);
    }
}