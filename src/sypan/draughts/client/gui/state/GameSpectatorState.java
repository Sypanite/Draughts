package sypan.draughts.client.gui.state;

import sypan.draughts.client.gui.GUI;

/**
 * Active when the client is spectating in an AI v AI match. This state is
 * simply the {@link GameState} without the 'Forfeit' and 'Offer Draw' buttons.
 *
 * @author Carl Linley
 **/
public class GameSpectatorState extends GameState {

    public GameSpectatorState(GUI gui) {
        super(gui);
    }

    @Override
    public void createState() {
        super.createState();

        removeChild(gameButton[0]);
        removeChild(gameButton[1]);
    }
}
