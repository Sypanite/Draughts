package sypan.draughts.client.config;

import sypan.draughts.client.gui.ButtonType;

public enum AntiAliasing {

    X16, X8, X4, X2, OFF;

    public static AntiAliasing fromButton(ButtonType buttonType) {
        return valueOf(buttonType.toString().split("_")[2]);
    }

    public int toInteger() {
        if (this == OFF) {
            return 0;
        }
        return Integer.parseInt(this.toString().substring(1));
    }
};
