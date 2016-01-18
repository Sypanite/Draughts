package sypan.draughts.client.gui.state;

import sypan.draughts.client.gui.ButtonType;
import sypan.draughts.client.gui.GUI;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

/**
 * {@code GameReplayState} is the {@code GUIState} active when the game has been
 * loaded and is being viewed.
 *
 * @author Carl Linley
 *
 */
public class GameReplayState extends GameState {

    public GameReplayState(GUI gui) {
        super(gui);
    }

    @Override
    public void createState() {
        super.createState();

        // Change Forfeit and Offer Draw to Last Move and Next Move.
        gameButton[0].changeType(ButtonType.GAME_REPLAY_PLAY);
        gameButton[1].changeType(ButtonType.GAME_REPLAY_NEXT_MOVE);
    }

    public void lockButtons() {
        for (int i = 0; i != 2; i++) {
            lockButton(i);
        }
    }

    public void unlockButtons() {
        for (int i = 0; i != 2; i++) {
            unlockButton(i);
        }
    }

    public void lockButton(int i) {
        gameButton[i].removeEffect(EffectEvent.Hover);
        gameButton[i].removeEffect(EffectEvent.Press);
        gameButton[i].setGlobalAlpha(0.75f);
    }

    public void unlockButton(int i) {
        gameButton[i].addEffect(EffectEvent.Hover, new Effect(EffectType.ImageSwap, EffectEvent.Hover, 1f));
        gameButton[i].addEffect(EffectEvent.Press, new Effect(EffectType.ImageSwap, EffectEvent.Press, 1f));
        gameButton[i].setGlobalAlpha(1f);
    }

    public void toggleAutoReplay(boolean isAutoReplaying) {
        if (isAutoReplaying) {
            gameButton[0].changeType(ButtonType.GAME_REPLAY_PAUSE);
        }
        else {
            gameButton[0].changeType(ButtonType.GAME_REPLAY_PLAY);
        }
    }

    @Override
    public void setPressedGameButton(ButtonType buttonType) {
        switch (buttonType) {
            case GAME_REPLAY_PLAY:
            case GAME_REPLAY_PAUSE:
            case GAME_REPLAY_NEXT_MOVE:
            break;

            default:
                super.setPressedGameButton(buttonType);
            break;
        }
    }
}