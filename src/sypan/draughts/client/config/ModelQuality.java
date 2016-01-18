package sypan.draughts.client.config;

import sypan.draughts.client.gui.ButtonType;

public enum ModelQuality {

    HIGH, MED, LOW, MIN;

    public static ModelQuality fromButton(ButtonType buttonType) {
        return valueOf(buttonType.toString().split("_")[2]);
    }
};