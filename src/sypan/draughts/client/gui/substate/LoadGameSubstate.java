package sypan.draughts.client.gui.substate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.ShadowLabel;
import sypan.draughts.game.GameReplay;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.scrolling.ScrollAreaAdapter;
import tonegod.gui.controls.scrolling.VScrollBar;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import sypan.utility.Logger;

public class LoadGameSubstate extends AbstractGUIState {

    private final static Vector2f WINDOW_DIMENSIONS = new Vector2f(430, 285);

    private ScrollAreaAdapter scrollArea;

    private ArrayList<ButtonAdapter> gameHistory;

    private String gameSelected;
    private int gameSelectedIndex;

    private GUIButton loadGame, deleteGame;
    private boolean buttonsLocked;

    public LoadGameSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_LOAD_GAME, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/LOAD_GAME_BACK.png");
    }

    @Override
    protected void createState() {
        scrollArea = new ScrollAreaAdapter(screen, "scrollArea", new Vector2f(13, 11), new Vector2f(408, 220), Vector4f.ZERO, "gui/interface/LOAD_GAME_FORE.png");

        VScrollBar scrollBar = scrollArea.getVScrollBar();
        getGUI().formatScrollbar(scrollBar);
        addScrollbarPlaceholder();

        ButtonAdapter downButton = scrollBar.getScrollButtonDown();
        downButton.moveTo(downButton.getX(), downButton.getY() + 3);

        loadGame = new GUIButton(getGUI(), ButtonType.LOAD_GAME_LOAD, new Vector2f(13, 239));
        deleteGame = new GUIButton(getGUI(), ButtonType.LOAD_GAME_DELETE, new Vector2f(158, 239));

        addChild(scrollArea);
        addChild(loadGame);
        addChild(deleteGame);

        loadHistory();
        lockButtons();
    }

    @Override
    public void onClick(ButtonType buttonType) {
        if (buttonsLocked) {
            return;
        }

        if (buttonType == ButtonType.LOAD_GAME_LOAD) {
            getGUI().getClient().setCurrentGame(new GameReplay(gameSelected, getGUI().getClient()));
        }
        else {
            if (new File(gameSelected).delete()) {
                gameSelected = null;
                lockButtons();

                float optionY = gameHistory.get(gameSelectedIndex).getY();

                ButtonAdapter toRemove = gameHistory.get(gameSelectedIndex);
                toRemove.removeAllChildren();
                removeChild(toRemove);

                gameHistory.set(gameSelectedIndex, null);

                for (ButtonAdapter b : gameHistory) {
                    if (b != null && b.getY() < optionY) {
                        b.setPosition(b.getX(), b.getY() + 20);
                    }
                }
            }
        }
    }

    /**
     * Creates an empty label on the scroll area, forcing the scroll bar to
     * appear.
     *
     * I like scrollbars, even the pointless ones!
     **/
    private void addScrollbarPlaceholder() {
        scrollArea.addScrollableChild(new ShadowLabel(screen, "PLACEHOLDER", new Vector2f(3, 185), "", ColorRGBA.White, 14));
    }

    /**
     * Based on example code written by 'rich', found at
     * <a href="http://stackoverflow.com/questions/1844688/read-all-files-in-a-folder">http://stackoverflow.com/questions/1844688/read-all-files-in-a-folder</a>
     * <p>
     * First time using a lambda expression, oooh.
     *
     * @author Carl Linley
     * @author rich
	 *
     */
    private void loadHistory() {
        gameHistory = new ArrayList<ButtonAdapter>();

        if (new File("history").exists()) {

            try {
                Files.walk(Paths.get("history")).forEach(filePath -> {
                    if (filePath.toString().endsWith(".csv")) {
                        createButtonFor(filePath);
                    }
                });
            }
            catch (IOException e) {
                Logger.logSevere("Threw exception whilst parsing previous games: " + e + " - " + e.getMessage());
            }
        }
        else {
            new File("history").mkdir();
        }

        if (gameHistory.isEmpty()) {
            scrollArea.addScrollableChild(new ShadowLabel(screen, "NO GAMES FOUND LABEL", new Vector2f(3, 23), "No saved games found.", ColorRGBA.White, 14));
            scrollArea.addScrollableChild(new ShadowLabel(screen, "INFORMATION LABEL I", new Vector2f(3, 43), "Completed games will automatically be saved", ColorRGBA.White, 14));
            scrollArea.addScrollableChild(new ShadowLabel(screen, "INFORMATION LABEL II", new Vector2f(3, 58), "and appear here.", ColorRGBA.White, 14));
        }
    }

    private void lockButtons() {
        lock(loadGame);
        lock(deleteGame);
        buttonsLocked = true;
    }

    private void unlockButtons() {
        unlock(loadGame);
        unlock(deleteGame);
        buttonsLocked = false;
    }

    private void lock(GUIButton toLock) {
        toLock.setColorMap("gui/buttons/LOAD_LOCKED.png");
        toLock.setButtonHoverInfo("gui/buttons/LOAD_LOCKED.png", null);
        toLock.setButtonPressedInfo("gui/buttons/LOAD_LOCKED.png", null);

        toLock.onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        toLock.onGetFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
    }

    private void unlock(GUIButton toUnlock) {
        toUnlock.setColorMap("gui/buttons/" + toUnlock.getBaseType() + ".png");
        toUnlock.setButtonHoverInfo("gui/buttons/" + toUnlock.getBaseType() + "_HOVER.png", null);
        toUnlock.setButtonPressedInfo("gui/buttons/" + toUnlock.getBaseType() + "_PRESSED.png", null);

        toUnlock.onGetFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        toUnlock.onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
    }

    private void createButtonFor(Path filePath) {
        int buttonID = gameHistory.size();

        ShadowLabel myLabel = new ShadowLabel(screen, "OPTION " + buttonID + " LABEL", Vector2f.ZERO, formatFilePath(filePath), ColorRGBA.White, 14);
        myLabel.setIgnoreMouse(true);

        ButtonAdapter myButton = new ButtonAdapter(screen, "OPTION " + buttonID, new Vector2f(3, 3 + (buttonID * 20)), new Vector2f(280, 20))
        {
            @Override
            public void onButtonMouseLeftDown(MouseButtonEvent event, boolean toggled) {
                if (gameSelected == null || !gameSelected.equals(filePath.toString())) {
                    gameSelected = filePath.toString();
                    gameSelectedIndex = buttonID;
                }
                else {
                    gameSelected = null;
                }

                for (ButtonAdapter b : gameHistory) {
                    if (b != null) {
                        b.onButtonLostFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
                    }
                }

                if (gameSelected == null && !buttonsLocked) {
                    lockButtons();
                }
                else if (buttonsLocked) {
                    unlockButtons();
                }
            }

            @Override
            public void onButtonFocus(MouseMotionEvent event) {
                if (!filePath.toString().equals(gameSelected)) {
                    myLabel.setColour(ColorRGBA.LightGray);
                }
            }

            @Override
            public void onButtonLostFocus(MouseMotionEvent event) {
                myLabel.setColour((filePath.toString().equals(gameSelected) ? ColorRGBA.Yellow : ColorRGBA.White));
            }
        };
        myButton.setGlobalAlpha(0);
        myButton.addChild(myLabel);
        myButton.setUseButtonHoverSound(false);
        myButton.setUseButtonPressedSound(false);
        scrollArea.addScrollableChild(myButton);
        gameHistory.add(myButton);
    }

    private String formatFilePath(Path filePath) {
        String[] args = filePath.toString().split(" - ");

        return args[0].substring(8, 18).replaceAll("-", "/") + " - " + args[1].replace('.', ':') + " - " + args[2].substring(0, args[2].length() - 4);
    }
}
