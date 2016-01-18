package sypan.draughts.client.gui.element;

import java.awt.Dimension;
import java.util.ArrayList;

import sypan.draughts.client.gui.GUI;
import sypan.utility.Utility;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.scrolling.ScrollArea;
import tonegod.gui.controls.scrolling.ScrollAreaAdapter;
import tonegod.gui.core.Element;
import tonegod.gui.effects.Effect;
import tonegod.gui.effects.Effect.EffectEvent;
import tonegod.gui.effects.Effect.EffectType;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;

/**
 * Creates a combo box containing every resolution supported by the player's
 * main display.<p>
 *
 * I opted to create my own select box as opposed to simply extending the
 * default one in the to allow me more freedom.
 *
 * @author Carl Linley
 **/
public class ResolutionList extends Element {

    private final ArrayList<String> resolutionList;
    private final ShadowLabel selectedResolution;

    private boolean listOpen = true;

    private ButtonAdapter dropDownButton;
    private Image dropDownImage;

    private ScrollArea listPane;
    private Dimension resolution;

    public ResolutionList(GUI gui, Vector2f position) {
        super(gui.getScreen(), "ResolutionList", position, new Vector2f(125, 100), Vector4f.ZERO, null);
        initListPane(gui);

        resolutionList = Utility.getSupportedResolutions();

        int listIndex = 0;

        for (String currentResolution : resolutionList) {
            String[] tokens = currentResolution.split(" x ");

            int resolutionX = Integer.parseInt(tokens[0]), resolutionY = Integer.parseInt(tokens[1]);

            if (resolutionX >= 800 && resolutionY >= 600) {
                listPane.addScrollableChild(getListButton(listIndex++, currentResolution));
            }
        }

        configureDropDownButton();

        addChild(new Image(screen, "resolutionBox", Vector2f.ZERO, new Vector2f(100, 20), "gui/interface/RESOLUTION_BOX.png"));

        selectedResolution = new ShadowLabel(screen, "displayedResolution", new Vector2f(1, 1), "", ColorRGBA.DarkGray);
        selectedResolution.setFont("gui/FONT1.fnt", 14);
        selectedResolution.setShadowOffset(1, 1);
        addChild(selectedResolution);

        toggleDropped(false);
    }

    private void initListPane(GUI gui) {
        listPane = new ScrollAreaAdapter(screen, "listPane", new Vector2f(0, 20), new Vector2f(125, 100));
        listPane.setColorMap("gui/interface/RESOLUTION_SCROLL_AREA.png");
        addChild(listPane);

        gui.formatScrollbar(listPane.getVScrollBar());
        ButtonAdapter downButton = listPane.getVScrollBar().getScrollButtonDown();
        downButton.moveTo(downButton.getX(), downButton.getY() + 5);
    }

    private ButtonAdapter getListButton(int index, String resolution) {
        ShadowLabel myLabel = new ShadowLabel(screen, "RES " + resolution, new Vector2f(0, -3), resolution, ColorRGBA.White);
        myLabel.setFontSize(14);
        myLabel.setIgnoreMouse(true);
        myLabel.setShadowOffset(2, -2);

        ButtonAdapter button = new ButtonAdapter(screen, new Vector2f(2, 6 + (index * 16)), new Vector2f(100, 15)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent event, boolean arg1) {
                setSelected(resolution);
            }

            @Override
            public void onButtonFocus(MouseMotionEvent event) {
                myLabel.setColour(ColorRGBA.LightGray);
            }

            @Override
            public void onButtonLostFocus(MouseMotionEvent event) {
                myLabel.setColour(ColorRGBA.White);
            }
        };
        button.setUseButtonHoverSound(false);
        button.setUseButtonPressedSound(false);
        button.setGlobalAlpha(0);
        button.addChild(myLabel);
        return button;
    }

    private void setSelected(String resolution) {
        toggleDropped(false);
        this.resolution = Utility.parseDimension(resolution);
        selectedResolution.setText(resolution + (resolution.equals("800 x 600") ? "   " : "")); // Cheap-o fix to a display bug
    }

    public final void toggleDropped(boolean isDropped) {
        if (isDropped) {
            listPane.showWithEffect();
            dropDownImage.setColorMap("gui/interface/scroll/UP.png");
        }
        else {
            listPane.hideWithEffect();
            dropDownImage.setColorMap("gui/interface/scroll/DOWN.png");
        }
        listOpen = isDropped;
    }

    private void configureDropDownButton() {
        dropDownButton = new ButtonAdapter(screen) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent arg0, boolean arg1) {
                toggleDropped(!listOpen);
            }
        };
        dropDownButton.setPosition(102, 0);
        dropDownButton.setDimensions(20, 20);

        dropDownButton.removeEffect(EffectEvent.Hover);
        dropDownButton.addEffect(new Effect(EffectType.ImageSwap, EffectEvent.Hover, 1f));

        dropDownButton.setColorMap("gui/interface/scroll/SCROLL.png");
        dropDownButton.setButtonHoverInfo("gui/interface/scroll/SCROLL_HOVER.png", null);
        dropDownButton.setButtonPressedInfo("gui/interface/scroll/SCROLL_PRESSED.png", null);

        dropDownButton.setUseButtonHoverSound(false);
        dropDownButton.setUseButtonPressedSound(false);

        dropDownImage = new Image(screen, "ARROW", Vector2f.ZERO, new Vector2f(20, 20), "gui/interface/scroll/DOWN.png");
        dropDownButton.addChild(dropDownImage);

        addChild(dropDownButton);
    }

    public void setResolution(int width, int height) {
        setSelected(width + " x " + height);
    }

    public Dimension getResolution() {
        return resolution;
    }
}