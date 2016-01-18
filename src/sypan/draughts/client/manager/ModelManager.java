package sypan.draughts.client.manager;

import java.util.HashMap;

import sypan.draughts.client.Client;
import sypan.draughts.client.config.ModelQuality;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.PieceType;
import sypan.utility.Logger;

import com.jme3.scene.Spatial;

/**
 * {@code ModelManager} manages the game's models. It does not just store them -
 * if the player changes the model quality value in Settings, it reloads the
 * models using the new quality and updates every existing model.<p>
 *
 * Models are stored in a {@code HashMap} for convenient storage and retrieval.
 *
 * @author Carl Linley
 **/
public class ModelManager {

    public enum ModelType {
        BOARD_FRAME, PIECE_MAN, PIECE_KING
    };

    private final HashMap<ModelType, Spatial> modelStore;
    
    private ModelQuality currentQuality;

    public ModelManager(Client c) {
        modelStore = new HashMap<>();
        currentQuality = c.getConfig().getModelQuality();

        loadModels(c);
    }

    public final void loadModels(Client c) {
        modelStore.clear();

        try {
            for (ModelType modelType : ModelType.values()) {
                modelStore.put(modelType, c.getAssetManager().loadModel("models/" + currentQuality + "/" + modelType + ".j3o"));
            }
            Logger.logInfo("Loaded models (quality: " + currentQuality + ").");
        }
        catch (Exception e) {
            Logger.logWarning("Failed to load models: " + e + " - " + e.getMessage());
        }
    }

    public void reloadModels(Client c) {
        if (c.getConfig().getModelQuality() != currentQuality) {
            currentQuality = c.getConfig().getModelQuality();

            loadModels(c);

            //Update existing models.
            for (Piece p : c.getGraphicalBoard().getRenderedPieces()) {
                p.setModel(getModel(p.getType()));
            }
        }
    }

    public Spatial getModel(ModelType modelType) {
        return modelStore.get(modelType).clone();
    }

    public Spatial getModel(PieceType pieceType) {
        return getModel(ModelType.valueOf("PIECE_" + pieceType.toString().split("_")[0]));
    }
}