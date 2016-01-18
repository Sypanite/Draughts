package sypan.draughts.client.manager;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import sypan.draughts.client.Client;
import sypan.utility.Logger;

import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;

/**
 * {@code SoundManager} stores in an unmodified instance of {@link AudioNode}
 * for every sound in the game. A clone of any sound can be retrieved using
 * {@code @getSound} and specifying the sound's {@code SoundType}
 * .<p>
 *
 * Sounds are stored in a {@code HashMap} for convenient storage and retrieval.
 *
 * @author Carl Linley
 **/
public class SoundManager {

    public enum SoundType {
        END_GAME, INVALID_MOVE, JUMP_PIECE, KING_EXPLOSION, MAN_EXPLOSION, MOVE_PIECE, PROMOTE_PIECE
    };

    private final HashMap<SoundType, AudioNode> soundStore;
    private final Client client;
    private final Random random;

    public SoundManager(Client client) {
        this.client = client;
        soundStore = new HashMap<>();
        random = new Random();

        loadSounds(client);
    }

    public AudioNode getSound(SoundType type) {
        return soundStore.get(type).clone();
    }

    public void playSound(SoundType soundType, Vector3f soundLocation) {
        playSound(soundType, soundLocation, true);
    }

    /**
     * Plays the specified sound at the specified position with a slightly
     * randomised pitch. The sound is enqueued due to strange issues occurring
     * when the sound is played without being enqueued.
     *
     * @param soundType - the sound type to play.
     * @param soundLocation - the world location from which the sound will play (pass <i>null</i> for non-directional).
     * @param randomisePitch - if true, pitch is randomised slightly.
     * @see SoundType
	 *
     */
    public void playSound(SoundType soundType, Vector3f soundLocation, boolean randomisePitch) {
        client.enqueue(() -> {
            AudioNode sound = getSound(soundType);
            
            if (randomisePitch) {
                sound.setPitch(0.75f + (random.nextInt(50) / 100));
            }
            
            sound.setVolume(client.getConfig().getSoundVolume());
            
            if (soundLocation != null) {
                sound.setPositional(true);
                sound.setLocalTranslation(soundLocation);
            }
            else {
                sound.setPositional(false);
            }
            sound.play();
            return null;
        });
    }

    private void loadSounds(Client draughts) {
        soundStore.clear();

        for (SoundType t : SoundType.values()) {
            soundStore.put(t, new AudioNode(draughts.getAssetManager(), "sounds/" + t + ".wav"));
        }
        Logger.logInfo("Loaded " + SoundType.values().length + " sounds.");
    }
}