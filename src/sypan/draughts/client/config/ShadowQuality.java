package sypan.draughts.client.config;

import sypan.draughts.client.gui.ButtonType;

public enum ShadowQuality {

    HIGH, MED, LOW, OFF;

    public static ShadowQuality fromButton(ButtonType buttonType) {
        return valueOf(buttonType.toString().split("_")[2]);
    }
};
