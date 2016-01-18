package sypan.draughts.client.gui.element;

import tonegod.gui.controls.text.Label;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;

/**
 * {@code ShadowLabel} creates a label of a specified size, font, and colour
 * with an underlying shadow - this makes things much more aesthetic in my
 * opinion.
 *
 * @author Carl Linley
 *
 */
public class ShadowLabel extends Element {

    private Label shadowLabel, colouredLabel;
    private int shadowOffsetX = 2, shadowOffsetY = 2;

    public ShadowLabel(ElementManager screen, String uniqueID, Vector2f labelPosition) {
        super(screen, uniqueID, labelPosition, Vector2f.ZERO, Vector4f.ZERO, null);
        shadowLabel = new Label(screen, uniqueID + "_shadow", new Vector2f(shadowOffsetX, shadowOffsetY), Vector2f.ZERO);
        colouredLabel = new Label(screen, uniqueID, Vector2f.ZERO, Vector2f.ZERO);
        shadowLabel.setFontColor(ColorRGBA.Black);

        addChild(shadowLabel);
        addChild(colouredLabel);
    }

    public ShadowLabel(ElementManager screen, String uniqueID, Vector2f labelPosition, String labelText, ColorRGBA textColour) {
        this(screen, uniqueID, labelPosition);
        setFont("gui/FONT.fnt", 16f);
        setText(labelText);
        setColour(textColour);
    }

    public ShadowLabel(ElementManager screen, String uniqueID, Vector2f labelPosition, String labelText, ColorRGBA textColour, float fontSize) {
        this(screen, uniqueID, labelPosition, labelText, textColour);
        setFont("gui/FONT.fnt", fontSize);
    }

    public ShadowLabel(ElementManager screen, String uniqueID, Vector2f labelPosition, String labelText, ColorRGBA textColour, float fontSize, String font) {
        this(screen, uniqueID, labelPosition, labelText, textColour);
        setFont("gui/" + font + ".fnt", fontSize);
    }

    @Override
    public void setDimensions(float dimensionX, float dimensionY) {
        super.setDimensions(dimensionX, dimensionY);
        colouredLabel.setDimensions(dimensionX, dimensionY);
        shadowLabel.setDimensions(dimensionX, dimensionY);
    }

    public final void setFont(String fontPath, float fontSize) {
        shadowLabel.setFont(fontPath);
        colouredLabel.setFont(fontPath);

        shadowLabel.setFontSize(fontSize);
        colouredLabel.setFontSize(fontSize);
    }

    @Override
    public void setFontSize(float fontSize) {
        shadowLabel.setFontSize(fontSize);
        colouredLabel.setFontSize(fontSize);
    }

    @Override
    public void setIgnoreMouse(boolean ignoreMouse) {
        shadowLabel.setIgnoreMouse(ignoreMouse);
        colouredLabel.setIgnoreMouse(ignoreMouse);
        super.setIgnoreMouse(ignoreMouse);
    }

    public final void setColour(ColorRGBA newColour) {
        colouredLabel.setFontColor(newColour);
    }

    public void setShadowOffset(int shadowOffsetX, int shadowOffsetY) {
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetX = shadowOffsetY;
        shadowLabel.setPosition(shadowOffsetX, shadowOffsetY);
    }

    @Override
    public final void setText(String newMessage) {
        shadowLabel.setText(newMessage);
        colouredLabel.setText(newMessage);

        setDimensions(getTextWidth(), getFontSize());
    }

    @Override
    public String getText() {
        return colouredLabel.getText();
    }

    public ColorRGBA getColour() {
        return colouredLabel.getFontColor();
    }

    public float getTextWidth() {
        return colouredLabel.getFont().getLineWidth(colouredLabel.getText()) * (colouredLabel.getFontSize() / 30);
    }
}