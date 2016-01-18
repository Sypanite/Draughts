package sypan.draughts.client.gui.substate;

import sypan.draughts.client.config.*;
import sypan.draughts.client.gui.*;
import sypan.draughts.client.gui.element.GUIButton;
import sypan.draughts.client.gui.element.ResolutionList;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.effects.Effect.EffectEvent;

import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;

/**
 * @author Carl Linley
 **/
public class SettingsSubstate extends AbstractGUIState {

    private final static Vector2f WINDOW_DIMENSIONS = new Vector2f(430, 430);
    private final static int BUTTON_TYPE_OFFSET = ButtonType.SETTINGS_MODEL_HIGH.ordinal();

    private final GUIButton[] interfaceButton;

    private CheckBox snapToBoard, overheadCamera, vSync, fullScreen, useEffects, rotateQuickly;
    private Slider soundSlider, musicSlider;
    private ResolutionList resolutionList;

    private ModelQuality modelQuality = ModelQuality.HIGH;
    private ShadowQuality shadowQuality = ShadowQuality.HIGH;
    private AntiAliasing antiAliasing = AntiAliasing.OFF;

    public SettingsSubstate(GUI gui) {
        super(gui, StateType.SUBSTATE_SETTINGS, clampCentre(gui, WINDOW_DIMENSIONS), WINDOW_DIMENSIONS, "gui/interface/SETTINGS.png");
        interfaceButton = new GUIButton[15];
    }

    @Override
    public void createState() {
        soundSlider = createSlider(3, new Vector2f(174, 221), -1);
        musicSlider = createSlider(4, new Vector2f(174, 241), -1);

        vSync = createCheckbox(0, new Vector2f(143, 140));
        fullScreen = createCheckbox(1, new Vector2f(125, 159));

        overheadCamera = createCheckbox(2, new Vector2f(179, 311));
        useEffects = createCheckbox(3, new Vector2f(179, 330));

        snapToBoard = createCheckbox(4, new Vector2f(393, 311));
        rotateQuickly = createCheckbox(5, new Vector2f(393, 330));

        interfaceButton[0] = new GUIButton(getGUI(), ButtonType.SETTINGS_MODEL_HIGH, new Vector2f(160, 62), "SETTING_HIGH");
        interfaceButton[1] = new GUIButton(getGUI(), ButtonType.SETTINGS_MODEL_MED, new Vector2f(220, 62), "SETTING_MED");
        interfaceButton[2] = new GUIButton(getGUI(), ButtonType.SETTINGS_MODEL_LOW, new Vector2f(280, 62), "SETTING_LOW");
        interfaceButton[3] = new GUIButton(getGUI(), ButtonType.SETTINGS_MODEL_MIN, new Vector2f(340, 62), "SETTING_MIN");

        /* Shadow quality buttons */
        interfaceButton[4] = new GUIButton(getGUI(), ButtonType.SETTINGS_SHADOW_HIGH, new Vector2f(160, 87), "SETTING_HIGH");
        interfaceButton[5] = new GUIButton(getGUI(), ButtonType.SETTINGS_SHADOW_MED, new Vector2f(220, 87), "SETTING_MED");
        interfaceButton[6] = new GUIButton(getGUI(), ButtonType.SETTINGS_SHADOW_LOW, new Vector2f(280, 87), "SETTING_LOW");
        interfaceButton[7] = new GUIButton(getGUI(), ButtonType.SETTINGS_SHADOW_OFF, new Vector2f(340, 87), "SETTING_OFF");

        /* Anti-alias buttons */
        interfaceButton[8] = new GUIButton(getGUI(), ButtonType.SETTINGS_ANTIALIAS_X16, new Vector2f(138, 112), "SETTING_AA_16X");
        interfaceButton[9] = new GUIButton(getGUI(), ButtonType.SETTINGS_ANTIALIAS_X8, new Vector2f(193, 112), "SETTING_AA_8X");
        interfaceButton[10] = new GUIButton(getGUI(), ButtonType.SETTINGS_ANTIALIAS_X4, new Vector2f(248, 112), "SETTING_AA_4X");
        interfaceButton[11] = new GUIButton(getGUI(), ButtonType.SETTINGS_ANTIALIAS_X2, new Vector2f(303, 112), "SETTING_AA_2X");
        interfaceButton[12] = new GUIButton(getGUI(), ButtonType.SETTINGS_ANTIALIAS_OFF, new Vector2f(358, 112), "SETTING_OFF");

        /* Save/Cancel buttons */
        interfaceButton[13] = new GUIButton(getGUI(), ButtonType.SETTINGS_SAVE, new Vector2f(83, 366));
        interfaceButton[14] = new GUIButton(getGUI(), ButtonType.SETTINGS_CANCEL, new Vector2f(243, 366));

        for (GUIButton button : interfaceButton) {
            addChild(button);
        }

        resolutionList = new ResolutionList(getGUI(), new Vector2f(296, 155));
        addChild(resolutionList);

        setExistingSettings();
    }

    private CheckBox createCheckbox(int uniqueID, Vector2f checkboxPosition) {
        CheckBox checkBox = new CheckBox(screen, "CHECKBOX_" + uniqueID, checkboxPosition);
        checkBox.setDimensions(15, 15);
        checkBox.setUseButtonHoverSound(false);
        checkBox.setUseButtonPressedSound(false);
        checkBox.removeEffect(EffectEvent.Hover);

        addChild(checkBox);
        return checkBox;
    }

    private Slider createSlider(int uniqueID, Vector2f sliderPosition, int stepCount) {
        Slider newSlider = new Slider(screen, "SLIDER_" + uniqueID, sliderPosition, Slider.Orientation.HORIZONTAL, true) {
            @Override
            public void onChange(int value, Object arg1) {
            }
        };

        newSlider.setDimensions(230, 24);

        if (stepCount != -1) {
            for (int i = 0; i != stepCount; i++) {
                newSlider.addStepValue(i);
            }
        }
        newSlider.setColorMap("gui/interface/SLIDER_TRACK.png");
        newSlider.getThumb().setColorMap("gui/interface/SLIDER_MARKER.png");
        newSlider.getThumb().setButtonHoverInfo("gui/interface/SLIDER_MARKER.png", null);
        newSlider.getThumb().setButtonPressedInfo("gui/interface/SLIDER_MARKER.png", null);
        newSlider.getThumb().setUseButtonHoverSound(false);

        addChild(newSlider);
        return newSlider;
    }

    /**
     * Sets the state of the interface based on existing configuration.
     **/
    private void setExistingSettings() {
        Configuration config = getClient().getConfig();

        modelQuality = config.getModelQuality();
        shadowQuality = config.getShadowQuality();
        antiAliasing = config.getAntiAliasing();

        onClick(interfaceButton[config.getModelQuality().ordinal()].getType());
        onClick(interfaceButton[config.getShadowQuality().ordinal() + 4].getType());
        onClick(interfaceButton[config.getAntiAliasing().ordinal() + 8].getType());

        soundSlider.setSelectedIndex(config.getSound());
        musicSlider.setSelectedIndex(config.getMusic());

        resolutionList.setResolution(config.getWidth(), config.getHeight());

        vSync.setIsChecked(config.vSynced());
        fullScreen.setIsChecked(config.isFullscreen());
        overheadCamera.setIsChecked(config.overheadCamera());
        useEffects.setIsChecked(config.useEffects());
        snapToBoard.setIsChecked(!config.movesSmoothly());
        rotateQuickly.setIsChecked(config.rotateQuickly());
    }

    @Override
    public void onClick(ButtonType buttonType) {
        switch (buttonType) {
            case SETTINGS_MODEL_HIGH:
            case SETTINGS_MODEL_MED:
            case SETTINGS_MODEL_LOW:
            case SETTINGS_MODEL_MIN:
                switchButton(modelQuality, buttonType, 0);
                modelQuality = ModelQuality.fromButton(buttonType);
            break;

            case SETTINGS_SHADOW_HIGH:
            case SETTINGS_SHADOW_LOW:
            case SETTINGS_SHADOW_MED:
            case SETTINGS_SHADOW_OFF:
                switchButton(shadowQuality, buttonType, 4);
                shadowQuality = ShadowQuality.fromButton(buttonType);
            break;

            case SETTINGS_ANTIALIAS_X16:
            case SETTINGS_ANTIALIAS_X8:
            case SETTINGS_ANTIALIAS_X4:
            case SETTINGS_ANTIALIAS_X2:
            case SETTINGS_ANTIALIAS_OFF:
                switchButton(antiAliasing, buttonType, 8);
                antiAliasing = AntiAliasing.fromButton(buttonType);
            break;

            case SETTINGS_SAVE:
                saveNewConfiguration();

            case SETTINGS_CANCEL:
                getGUI().closeSubstate();
            break;

            default:
            break;
        }
    }

    /**
     * Convenience method, makes for much neater code.
     **/
    private void switchButton(Enum<?> toDeselect, ButtonType buttonType, int offset) {
        setUnselected(toDeselect.ordinal() + offset);
        setSelected(buttonType.ordinal() - BUTTON_TYPE_OFFSET);
    }

    private void setSelected(int buttonIndex) {
        interfaceButton[buttonIndex].setColorMap("gui/buttons/SETTING_SELECTED.png");
        interfaceButton[buttonIndex].setButtonHoverInfo("gui/buttons/SETTING_SELECTED_HOVER.png", null);
        interfaceButton[buttonIndex].setButtonPressedInfo("gui/buttons/SETTING_SELECTED_PRESSED.png", null);

        // As the colour map does not update immediately, this code is required to trigger
        // events which force it to do so. It took a lot of messing around to get this right!
        interfaceButton[buttonIndex].onLoseFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
        interfaceButton[buttonIndex].onGetFocus(new MouseMotionEvent(0, 0, 1, 1, 0, 0));
    }

    private void setUnselected(int buttonIndex) {
        interfaceButton[buttonIndex].setColorMap("gui/buttons/SETTING_UNSELECTED.png");
        interfaceButton[buttonIndex].setButtonHoverInfo("gui/buttons/SETTING_UNSELECTED_HOVER.png", null);
        interfaceButton[buttonIndex].setButtonPressedInfo("gui/buttons/SETTING_UNSELECTED_PRESSED.png", null);
    }

    public void saveNewConfiguration() {
        getClient().getConfig().setGraphics(modelQuality, shadowQuality, antiAliasing);
        getClient().getConfig().setAudio(soundSlider.getSelectedIndex(), musicSlider.getSelectedIndex());
        getClient().getConfig().setGameplay(snapToBoard.getIsChecked(), overheadCamera.getIsChecked(),
                                            vSync.getIsChecked(), fullScreen.getIsChecked(),
                                            useEffects.getIsChecked(), rotateQuickly.getIsChecked());
        getClient().getConfig().setResolution(resolutionList.getResolution());

        getClient().refreshConfig();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}