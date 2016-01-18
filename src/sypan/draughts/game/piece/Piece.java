package sypan.draughts.game.piece;

import sypan.draughts.client.Client;
import sypan.draughts.game.player.Side;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import sypan.utility.Logger;

/**
 * Represents a piece on the board.
 *
 * @author Carl Linley
 **/
public final class Piece {

    private final Tile currentTile;
    private final int pieceID;

    private PieceType type;

    private Spatial model;
    private Material pieceMaterial;
    private Vector3f targetPosition;

    private boolean isJumping, isFalling, isClone, isLocked;

    public Piece(int pieceID, PieceType type, Spatial model) {
        this.type = type;
        this.pieceID = pieceID;

        if (model != null) {
            setModel(model);
        }
        else {
            isClone = true;
        }
        currentTile = new Tile();
    }

    public void resetColour() {
        setColour(isBlack() ? ColorRGBA.DarkGray : ColorRGBA.LightGray);
    }

    public void setColour(ColorRGBA newColour) {
        pieceMaterial.setColor("Diffuse", newColour);
    }

    public void moveTo(Tile destinationTile) {
        Logger.logInfo("Tile: " + currentTile + " / Destrination: " + destinationTile);
        targetPosition = new Vector3f(destinationTile.getX() * 2, 0, destinationTile.getY() * 2);
    }

    public void setModel(Spatial model) {
        if (this.model != null) {
            model.setLocalTranslation(this.model.getLocalTranslation());
            model.setMaterial(pieceMaterial);

            if (isBlack()) {
                model.rotate(0, 180 * FastMath.DEG_TO_RAD, 0);
            }

            Node n = this.model.getParent();
            this.model.removeFromParent();
            n.attachChild(model);
        }
        this.model = model;
        model.setShadowMode(ShadowMode.CastAndReceive);
    }

    public void setPosition(int tileX, int tileY) {
        currentTile.set(tileX, tileY);

        if (!isClone) {
            model.setLocalTranslation(tileX * 2, 0, tileY * 2);

            if (isBlack()) {
                model.rotate(0, 180 * FastMath.DEG_TO_RAD, 0);
            }
        }
    }

    public void destroy() {
        model = null;
    }

    /**
     * @return a shallow clone of this piece. Only clones type and position.
     **/
    @Override
    public Piece clone() {
        Piece lightClone = new Piece(pieceID, type, null);
        lightClone.setPosition(currentTile.clone());
        return lightClone;
    }

    public void setPosition(Tile newTile) {
        setPosition(newTile.getX(), newTile.getY());
    }

    public void setTile(Tile newTile) {
        currentTile.set(newTile.getX(), newTile.getY());
    }

    public void setType(PieceType newType) {
        type = newType;
    }

    public void setAtTarget() {
        model.setLocalTranslation(targetPosition);
        targetPosition = null;
    }

    public void setFalling(boolean isFalling) {
        this.isFalling = isFalling;
    }

    public void setJumping(boolean isJumping) {
        this.isJumping = isJumping;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isBlack() {
        return type.toString().endsWith("BLACK");
    }

    public boolean isWhite() {
        return !isBlack();
    }

    public Material createMaterial(Client c) {
        pieceMaterial = new Material(c.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        pieceMaterial.setColor("Specular", ColorRGBA.White);
        pieceMaterial.setFloat("Shininess", 128.0f);
        pieceMaterial.setBoolean("UseMaterialColors", true);
        resetColour();

        return pieceMaterial;
    }

    public boolean belongsTo(Side checkSide) {
        if (type == null) {
            return false;
        }
        return (isBlack() ? checkSide == Side.BLACK : checkSide == Side.WHITE);
    }

    public boolean isFalling() {
        return isFalling;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public boolean isKing() {
        return type == PieceType.KING_BLACK || type == PieceType.KING_WHITE;
    }

    public Spatial getModel() {
        return model;
    }

    public PieceType getType() {
        return type;
    }

    public Side getSide() {
        return (isBlack() ? Side.BLACK : Side.WHITE);
    }

    @Override
    public String toString() {
        return type.toString() + " [" + currentTile + "]";
    }

    public Tile getTile() {
        return currentTile;
    }

    public Vector3f getTargetPosition() {
        return targetPosition;
    }

    public int getX() {
        return currentTile.getX();
    }

    public int getY() {
        return currentTile.getY();
    }

    public int getID() {
        return pieceID;
    }

    public boolean isLocked() {
        return isLocked;
    }
}