package sypan.draughts.client;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import sypan.draughts.client.config.Configuration;
import sypan.draughts.client.config.ShadowQuality;
import sypan.draughts.client.gui.GUI;
import sypan.draughts.client.gui.StateType;
import sypan.draughts.client.manager.*;
import sypan.draughts.client.manager.SoundManager.SoundType;
import sypan.draughts.game.Game;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.Side;
import sypan.utility.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import sypan.utility.Utility;

/**
 * {@code Client} is the main class of the project. It extends JMonkeyEngine's
 * {@code SimpleApplication}. This class contains the main initialisation method
 * and the main update loop.
 *
 * @author Carl Linley
 **/
public class Client extends SimpleApplication {

    private final static boolean DEBUG_FREECAM = false;
    private final static boolean DEBUG_SHOW_STATS = false;
    
    private DirectionalLight directionalLight;
    private DirectionalLightShadowRenderer shadowRenderer;
    
    private ScheduledThreadPoolExecutor executor;
    private AudioNode gameMusic;

    private Configuration config;
    private DraughtsCamera draughtsCamera;
    private EffectManager effectManager;
    private Game currentGame;
    private GUI draughtsGUI;
    private GraphicalBoard graphicalBoard;
    private Input input;
    private ModelManager modelStore;
    private Piece movingPiece;
    private Side mySide;
    private SoundManager soundManager;

    private Dimension activeResolution;
    private ShadowQuality activeShadowQuality;

    private boolean pieceMoving, showHover, updateShadows;

    public static void main(String[] args) {
        Logger.init();

        Client c = new Client();
        c.setPauseOnLostFocus(false);
        c.setShowSettings(false);
        c.setDisplayStatView(DEBUG_SHOW_STATS);
        c.setDisplayFps(DEBUG_SHOW_STATS);

        c.config = Configuration.load();
        c.activeResolution = c.config.getResolution();
        c.configureApplication();

        c.start();
    }

    @Override
    public void simpleInitApp() {
        draughtsGUI = new GUI(this);

        effectManager = new EffectManager(this);
        modelStore = new ModelManager(this);
        soundManager = new SoundManager(this);

        input = new Input(this);
        executor = new ScheduledThreadPoolExecutor(2);

        graphicalBoard = new GraphicalBoard(this);
        graphicalBoard.attach();

        directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(0.5f, -0.75f, 0.5f));
        rootNode.addLight(directionalLight);

        addShadows();
        initMusic();

        if (!DEBUG_FREECAM) {
            draughtsCamera = new DraughtsCamera(this);
        }
        displayMainMenu();
    }

    public void displayMainMenu() {
        if (!DEBUG_FREECAM) {
            draughtsCamera.idleRoam(this);
            setState(StateType.STATE_MAIN_MENU);
        }
        graphicalBoard.initialisePieces();
        gameMusic.play();
    }

    private void initMusic() {
        gameMusic = new AudioNode(assetManager, "music/TOUGH_CHOICES.wav", false);
        gameMusic.setVolume(config.getMusicVolume());
        gameMusic.setPositional(false);
        gameMusic.setLooping(true);
    }

    public void stopMusic() {
        enqueue(() -> {
            gameMusic.stop();
            return null;
        });
    }

    @Override
    public void simpleRender(RenderManager renderManager) {
        if (updateShadows) {
            addShadows();
            updateShadows = false;
        }
    }

    @Override
    public void simpleUpdate(float timePerFrame) {
        effectManager.update(timePerFrame);

        if (showHover) {
            Tile currentHoverTile = input.calculateHoverTile();

            if (isGameRunning()) {
                if (currentHoverTile != null) {
                    if (currentHoverTile != input.getCurrentHoverTile()) {
                        if (currentGame.getLogicalBoard().pieceOccupies(currentHoverTile)) {
                            if (!graphicalBoard.isPieceLocked(currentGame.getLogicalBoard().getPiece(currentHoverTile))) {
                                graphicalBoard.setHoverTile(currentHoverTile);
                            }
                            else {
                                graphicalBoard.setHoverTile(null);
                            }
                        }
                        else {
                            graphicalBoard.setHoverTile(currentHoverTile);
                        }
                    }
                }
                else {
                    graphicalBoard.setHoverTile(null);
                }
            }
        }
        if (pieceMoving) {
            if (!config.movesSmoothly() || handlePieceInterpolation(movingPiece, timePerFrame)) {
                movingPiece.setAtTarget();
                movingPiece = null;
                pieceMoving = false;
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
        config.save(this);

        if (!DEBUG_FREECAM) {
            draughtsCamera.destroy();
        }
    }

    private void configureApplication() {
        AppSettings s = new AppSettings(true);

        s.setTitle("Draughts");
        s.setFrameRate(-1);
        s.setFullscreen(config.isFullscreen());
        s.setSamples(config.getAntiAliasing().toInteger());
        s.setVSync(config.vSynced());

        s.setResolution((int) activeResolution.getWidth(), (int) activeResolution.getHeight());

        try {
            s.setIcons(new BufferedImage[]{Utility.getImage("icons/ICON_64.png"),
                                           Utility.getImage("icons/ICON_32.png"),
                                           Utility.getImage("icons/ICON_16.png")});
        }
        catch (IOException e) {
            Logger.logInfo("Failed to load icons: " + e);
        }
        setSettings(s);
    }

    private void clearShadows() {
        if (shadowRenderer != null) {
            viewPort.removeProcessor(shadowRenderer);
            shadowRenderer = null;
        }
    }

    public void addShadows() {
        if (config.getShadowQuality() == ShadowQuality.OFF) {
            return;
        }

        int shadowMapSize = 128 << (4 - config.getShadowQuality().ordinal()),
            shadowRenderPasses = (4 - config.getShadowQuality().ordinal());
 
        shadowRenderer = new DirectionalLightShadowRenderer(assetManager, shadowMapSize, shadowRenderPasses);
        shadowRenderer.setLight(directionalLight);

        switch(config.getShadowQuality()) {
            case HIGH:
                shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.PCF4); // PCF8 looks awesome but is really expensive
            break;

            default:
                shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
            break;
        }
        viewPort.addProcessor(shadowRenderer);

        activeShadowQuality = config.getShadowQuality();
        Logger.logInfo("Added shadows (quality: " + activeShadowQuality + " - " + "map size: " + shadowMapSize + ", " + "renderer passes: " + shadowRenderPasses + ")");
    }

    /**
     * Handles the interpolation of pieces.
     *
     * @param toHandle - the interpolating piece.
     * @param timePerFrame - the current time per frame.
     * @return {@code true} if piece <b>toHandle</b> has reached its destination.
     **/
    private boolean handlePieceInterpolation(Piece toHandle, float timePerFrame) {
        if (getCurrentGame().gameEnded()) {
            return false;
        }

        Vector3f currentVector = toHandle.getModel().getLocalTranslation(),
                 newVector = currentVector.interpolateLocal(toHandle.getTargetPosition(), (toHandle.isJumping() ? 0.075f : 0.1f) * (timePerFrame * 60));

        if (toHandle.isJumping()) {
            if (currentVector.getY() >= 1.65f) {
                toHandle.setJumping(false);
                toHandle.setFalling(true);
            }
            else {
                newVector.addLocal(0, 0.1f * (timePerFrame * 100), 0);
            }
        }
        else if (toHandle.isFalling()) {
            if (currentVector.getY() <= 0.1f) {
                toHandle.setFalling(false);
                currentVector.setY(0);
                soundManager.playSound(SoundType.JUMP_PIECE, movingPiece.getTargetPosition());
            }
            else {
                newVector.subtractLocal(0, 0.1f * (timePerFrame * 100), 0);
            }
        }
        toHandle.getModel().setLocalTranslation(newVector);
        return toHandle.getTargetPosition().distance(currentVector) < 0.01f;
    }

    /**
     * Called after the game's settings are changed.
     **/
    public void refreshConfig() {
        if (!activeResolution.equals(config.getResolution())) {
            draughtsGUI.markRestartRequired();
        }

        modelStore.reloadModels(this);
        gameMusic.setVolume(config.getMusicVolume());
        draughtsGUI.getScreen().setUIAudioVolume(config.getSoundVolume());

        clearShadows();
        configureApplication();
        restart();
        updateShadows = true;
    }

    public void setCurrentGame(Game game) {
        this.currentGame = game;

        if (game != null) {
            draughtsGUI.closeSubstate();

            if (game.isReplay()) {
                setState(StateType.STATE_GAME_REPLAY);
            }
            else if (game.includesHuman()) {
                setState(StateType.STATE_GAME);
            }
            else {
                setState(StateType.STATE_GAME_SPECTATOR);
            }
        }
    }

    public void setMovingPiece(Piece piece, Tile destination) {
        piece.moveTo(destination);
        movingPiece = piece;
        pieceMoving = true;
    }

    public void setShowHover(boolean showHover) {
        this.showHover = showHover;

        if (!showHover) {
            enqueue(() -> {
                graphicalBoard.setHoverTile(null);
                return null;
            });
        }
    }

    public void setState(StateType newState) {
        draughtsGUI.setState(newState);
    }

    public Configuration getConfig() {
        return config;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public DraughtsCamera getDraughtsCamera() {
        return draughtsCamera;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }

    public GUI getGUI() {
        return draughtsGUI;
    }

    public GraphicalBoard getGraphicalBoard() {
        return graphicalBoard;
    }

    public ModelManager getModelStore() {
        return modelStore;
    }

    public Vector2f getScreenDimensions() {
        return new Vector2f(this.settings.getWidth(), this.settings.getHeight());
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public Side getSide() {
        return mySide;
    }

    public boolean isPieceMoving() {
        return pieceMoving;
    }

    public boolean isHoverShown() {
        return showHover;
    }

    public boolean isGameRunning() {
        return currentGame != null;
    }

    public Dimension getActiveResolution() {
        return activeResolution;
    }
}