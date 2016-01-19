package sypan.draughts.client.config;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import sypan.draughts.client.Client;
import sypan.utility.Logger;

/**
 * @author Carl Linley
 **/
public final class Configuration implements Serializable {

    private static final long serialVersionUID = 7244185130997590376L;

    private Dimension windowResolution = new Dimension(800, 600);

    private ModelQuality modelQuality = ModelQuality.HIGH;
    private ShadowQuality shadowQuality = ShadowQuality.HIGH;
    private AntiAliasing antiAliasing = AntiAliasing.OFF;

    private int soundVolume = 25, musicVolume = 50;

    private boolean snapToBoard, overheadCamera, usesVSync = true,
                    isFullscreen = true, useEffects = true, rotateQuickly;

    public Configuration() {
        setResolution(null);
    }

    public void save(Client c) {
        try {
            FileOutputStream fileOut = new FileOutputStream("gamecfg.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(this);

            out.close();
            fileOut.close();
            Logger.logInfo("Saved configuration.");
        }
        catch (IOException e) {
            Logger.logSevere("Failed to save configuration: " + e + "!");
        }
    }

    public void setGraphics(ModelQuality modelQuality, ShadowQuality shadowQuality, AntiAliasing antiAliasing) {
        this.modelQuality = modelQuality;
        this.shadowQuality = shadowQuality;
        this.antiAliasing = antiAliasing;
    }

    public void setAudio(int soundVolume, int musicVolume) {
        this.soundVolume = soundVolume;
        this.musicVolume = musicVolume;
    }

    public void setGameplay(boolean snapToBoard, boolean overheadCamera, boolean usesVSync, boolean isFullscreen, boolean useEffects, boolean rotateQuickly) {
        this.snapToBoard = snapToBoard;
        this.overheadCamera = overheadCamera;
        this.usesVSync = usesVSync;
        this.isFullscreen = isFullscreen;
        this.useEffects = useEffects;
        this.rotateQuickly = rotateQuickly;
    }

    public void setResolution(Dimension windowResolution) {
        if (windowResolution == null) {
            this.windowResolution = Toolkit.getDefaultToolkit().getScreenSize();
            return;
        }
        this.windowResolution = windowResolution;
    }

    public static Configuration load() {
        try {
            Configuration config;

            FileInputStream fileIn = new FileInputStream("gamecfg.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            config = (Configuration) in.readObject();

            in.close();
            fileIn.close();
            Logger.logInfo("Loaded configuration.");
            return config;
        }
        catch (FileNotFoundException e) {
            Logger.logInfo("Existing configuration not found.");
        }
        catch (IOException | ClassNotFoundException e) {
            Logger.logSevere("Failed to load configuration: " + e + "!");
        }
        return new Configuration();
    }

    public boolean movesSmoothly() {
        return !snapToBoard;
    }

    public boolean overheadCamera() {
        return overheadCamera;
    }

    public boolean useEffects() {
        return useEffects;
    }

    public boolean rotateQuickly() {
        return rotateQuickly;
    }

    public float getMusicVolume() {
        return (float) musicVolume / 100;
    }

    public float getSoundVolume() {
        return (float) soundVolume / 100;
    }

    public ModelQuality getModelQuality() {
        return modelQuality;
    }

    public int getSound() {
        return soundVolume;
    }

    public int getMusic() {
        return musicVolume;
    }

    public AntiAliasing getAntiAliasing() {
        return antiAliasing;
    }

    public ShadowQuality getShadowQuality() {
        return shadowQuality;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public boolean vSynced() {
        return usesVSync;
    }

    public Dimension getResolution() {
        return windowResolution;
    }

    public int getHeight() {
        return (int) windowResolution.getHeight();
    }

    public int getWidth() {
        return (int) windowResolution.getWidth();
    }
}