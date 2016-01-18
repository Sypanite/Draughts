package sypan.draughts.client.effect;

import java.util.concurrent.Callable;

import sypan.draughts.client.Client;
import sypan.draughts.client.gui.AbstractGUIState;
import sypan.draughts.client.manager.EffectManager;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 * Similarly to {@link AbstractGUIState}, this class was formerly an interface
 * (albeit very briefly). As this class should never be instantiated directly,
 * it is declared abstract.<p>
 *
 * It was changed to an abstract class to ensure any effects sub-classing it are
 * instances of {@code Node}. It enforces two methods,
 * {@code update(Client c, float timePerFrame)} and {@code destroy()}.<p>
 *
 * It stores a single member variable, a boolean named {@code effectComplete}.
 * The only non-overridden methods belonging to this class pertain to this
 * boolean - {@code effectComplete()} and {@code markComplete()}.<p>
 *
 * @author Carl Linley
 **/
abstract class AbstractEffect extends Node implements Effect {

    private boolean effectComplete;
    private float currentTime;
    private Client client;

    /**
     * This method should be used to update the effect. It is called by {link
     * EffectManager}'s update loop every frame.
     *
     * @param c - the current instance of {@link Client}.
     * @param timePerFrame - the current time per frame, based on the current
     * frame rate.
     **/
    @Override
    public abstract void update(Client c, float timePerFrame);

    /**
     * This is called from the {@link EffectManager}'s update loop if the effect
     * has been marked as complete. {@code EffectManager} handles the actual
     * removal of the node, that should <b>not</b>
     * be done using this method.
     *
     * @see markComplete()
     **/
    @Override
    public abstract void destroy();

    /**
     * Marks this effect as having run its course. The main update loop will
     * then remove it from the scene in the next iteration.
     **/
    protected void markComplete() {
        effectComplete = true;
    }

    /**
     * Increments the floating point storing the time since this effect's
     * beginning by the specified value.<p>
     *
     * <b>This should be the first line in the overridden {@code update}
     * method!</b>
     *
     * @param timePerFrame - the current time per frame.
     **/
    protected void incremementTime(float timePerFrame) {
        currentTime += timePerFrame;
    }

    @Override
    public void attachToScene(Node rootNode) {
        client.enqueue(() -> {
            rootNode.attachChild(AbstractEffect.this);
            return null;
        });
    }

    @Override
    public void detachFromScene(Node rootNode) {
        client.enqueue(() -> {
            rootNode.detachChild(AbstractEffect.this);
            return null;
        });
    }

    /**
     * @return the time passed since this effect begun.
     **/
    protected float getTime() {
        return currentTime;
    }

    /**
     * Stores the current instance of {@link Client}. This should be called by a
     * sub-class during construction.
     *
     * @param c - the instance of {@link Client}.
     **/
    protected void setClient(Client c) {
        this.client = c;
    }

    /**
     * @return the stored instance of Client.
     **/
    protected Client getClient() {
        return client;
    }

    /**
     * Convenience method, saves calling "{@code getClient().getAssetManager()}"
     * every time.
     *
     * @return the application's {@code AssetManager}.
     **/
    protected AssetManager getAssetManager() {
        return client.getAssetManager();
    }

    /**
     * @return true if this effect has been marked as complete.
     **/
    @Override
    public boolean isComplete() {
        return effectComplete;
    }
}
