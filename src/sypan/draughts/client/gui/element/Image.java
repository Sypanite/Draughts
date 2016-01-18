package sypan.draughts.client.gui.element;

import tonegod.gui.controls.text.Label;
import tonegod.gui.core.ElementManager;

import com.jme3.math.Vector2f;

/**
 * Convenience class - creates a label using an image instead of text using the
 * specified path. Making a separate class to handle this made sense given how
 * often I do this.
 *
 * @author Carl Linley
 **/
public class Image extends Label {

    public Image(ElementManager screen, String string, Vector2f imagePosition, Vector2f imageDimensions, String imagePath) {
        super(screen, string, imagePosition, imageDimensions);
        setColorMap(imagePath);
        setIgnoreMouse(true);
    }
}