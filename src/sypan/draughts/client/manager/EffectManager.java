package sypan.draughts.client.manager;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import sypan.draughts.client.Client;
import sypan.draughts.client.effect.Effect;

import com.jme3.math.Vector3f;

/**
 * {@code EffectManager} manages all effects in the game. I used to do this in
 * {@link Client}, but once I hit three different effects, I decided a separate
 * class would make for much neater code.
 *
 * @author Carl Linley
 **/
public class EffectManager {

    private final Client client;

    private final ArrayList<Effect> activeEffects;

    public EffectManager(Client c) {
        this.client = c;
        activeEffects = new ArrayList<>();
    }

    /**
     * Creates the specified effect. From here onwards, it will be handled
     * entirely by {@code EffectManager}.
     *
     * @param effect - a new instance of the effect to create.
     * @param effectLocation - the position where the effect will take place.
     **/
    public void createEffect(Effect effect, Vector3f effectLocation) {
        effect.setLocalTranslation(effectLocation);

        activeEffects.add(effect);
        effect.attachToScene(client.getRootNode());
    }

    /**
     * Updates every effect currently in the scene - this is done using a
     * conventional
     * <i>for</i> loop as opposed to an <i>advanced for</i> loop to prevent
     * concurrent modification.
     *
     * @param timePerFrame - the current time per frame.
     * @see ConcurrentModificationException
     **/
    public void update(float timePerFrame) {
        for (int i = 0; i != activeEffects.size(); i++) {
            if (i >= activeEffects.size()) {//A double check is necessary to stop it going out of bounds (this happens quite often otherwise).
                return;
            }

            Effect e = activeEffects.get(i);

            if (e != null) {
                e.update(client, timePerFrame);

                if (e.isComplete()) {
                    e.detachFromScene(client.getRootNode());
                    activeEffects.remove(e);
                }
            }
        }
    }
}