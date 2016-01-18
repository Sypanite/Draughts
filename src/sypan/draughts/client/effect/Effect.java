package sypan.draughts.client.effect;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import sypan.draughts.client.Client;

/**
 * @author Carl Linley
 **/
public interface Effect {

    void update(Client c, float timePerFrame);

    void destroy();

    void setLocalTranslation(Vector3f effectLocation);

    void attachToScene(Node rootNode);

    void detachFromScene(Node rootNode);

    boolean isComplete();
}