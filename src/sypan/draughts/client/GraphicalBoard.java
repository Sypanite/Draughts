package sypan.draughts.client;

import java.util.ArrayList;

import sypan.draughts.client.effect.ExplosionEffect;
import sypan.draughts.client.manager.ModelManager.ModelType;
import sypan.draughts.client.manager.SoundManager.SoundType;
import sypan.draughts.game.Game;
import sypan.draughts.game.LogicalBoard;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.PieceType;
import sypan.draughts.game.piece.Tile;

import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * {@code GraphicalBoard} handles the graphical aspects of the Draughts board. It is a subclass
 * of {@code Node} so it may be attached to the scene directly.<p>
 * 
 * An instance of {@code GraphicalBoard} may exist without an associated {@link LogicalBoard} to
 * support the board (complete with pieces) appearing as backing for the main menu.
 * 
 * @author Carl Linley
 **/
public class GraphicalBoard extends Node {

    private final ColorRGBA BLACK_HOVER_COLOUR = new ColorRGBA(0.3f, 0.3f, 0.3f, 1f), WHITE_HOVER_COLOUR = new ColorRGBA(0.9f, 0.9f, 0.9f, 1f),
                            BLACK_SELECTED_COLOUR = new ColorRGBA(0.4f, 0.4f, 0.4f, 1f), WHITE_SELECTED_COLOUR = ColorRGBA.White,
                            BLACK_LOCKED_COLOUR = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f), WHITE_LOCKED_COLOUR = new ColorRGBA(0.6f, 0.6f, 0.6f, 1f);

    private final Client c;
    private final ArrayList<Piece> renderedPieces, lockedPieces;
    private final Node pieceNode, boardNode;
    private final boolean[] lightDisplayed;

    private Piece hoverPiece, selectedPiece;
    private Tile selectedTile;

    private ColorRGBA[] hoverLightColour;
    private SpotLight hoverTileLight, selectedTileLight;

    protected GraphicalBoard(Client c) {
        this.c = c;
        pieceNode = new Node();
        boardNode = new Node();
        lightDisplayed = new boolean[2];
        lockedPieces = new ArrayList<>(12);
        renderedPieces = new ArrayList<>();

        initialiseLighting();
        initialiseGeometry();
    }

    /**
     * Initialises the board's geometry - includes the floor of the board, a
     * {@link Quad} geometry, and the frame of the board - TODO - four separate
     * instances of the {@code BOARD_FRAME_SEGMENT} model.<p>
     *
     * The frame was originally a single model, but after playing with that
     * gentleman and Chris in the labs on the 03/03, I realised that it was easy
     * to lose your sense of direction given that the camera rotates so often. I
     * opted to make the frame white on the white side of the board, black on
     * black's side, and -TODO- on the remaining two sides.
     **/
    private void initialiseGeometry() {
        Geometry boardGeometry, tableGeometry;
        Material boardMaterial, tableMaterial;
        Quad boardQuad, tableQuad;
        Texture boardTexture, tableTexture;

        boardQuad = new Quad(16, 16);
        boardQuad.scaleTextureCoordinates(new Vector2f(4, 4));
        boardGeometry = new Geometry("BOARD", boardQuad);
        boardGeometry.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);
        boardTexture = c.getAssetManager().loadTexture("textures/BOARD.png");
        boardTexture.setWrap(WrapMode.Repeat);
        boardMaterial = new Material(c.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        boardMaterial.setTexture("DiffuseMap", boardTexture);
        boardGeometry.setMaterial(boardMaterial);
        boardGeometry.setLocalTranslation(-8, -0.09f, 8);
        boardGeometry.setShadowMode(ShadowMode.Receive);

        tableQuad = new Quad(160, 160);
        tableQuad.scaleTextureCoordinates(new Vector2f(16, 16));
        tableGeometry = new Geometry("TABLE", tableQuad);
        tableGeometry.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);
        tableTexture = c.getAssetManager().loadTexture("textures/TABLE.png");
        tableTexture.setWrap(WrapMode.Repeat);
        tableMaterial = new Material(c.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        tableMaterial.setTexture("DiffuseMap", tableTexture);
        tableGeometry.setMaterial(tableMaterial);
        tableGeometry.setLocalTranslation(-80, -0.5f, 80);
        tableGeometry.setShadowMode(ShadowMode.Receive);

        Spatial frameGeometry = c.getModelStore().getModel(ModelType.BOARD_FRAME);
        Material frameMaterial = new Material(c.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        frameMaterial.setColor("Diffuse", ColorRGBA.DarkGray);
        frameMaterial.setBoolean("UseMaterialColors", true);
        frameGeometry.setMaterial(frameMaterial);
        frameGeometry.setShadowMode(ShadowMode.CastAndReceive);

        boardNode.attachChild(boardGeometry);
        boardNode.attachChild(tableGeometry);
        boardNode.attachChild(frameGeometry);
        boardNode.setLocalTranslation(7, 0, 7);

        attachChild(boardNode);
        attachChild(pieceNode);
        pieceNode.setShadowMode(ShadowMode.CastAndReceive);
    }

    /**
     * Initialises the board's lighting.
     **/
    private void initialiseLighting() {
        hoverTileLight = new SpotLight();
        selectedTileLight = new SpotLight();

        hoverLightColour = new ColorRGBA[2];
        hoverLightColour[0] = ColorRGBA.White;
        hoverLightColour[1] = new ColorRGBA(.255f, .255f, .255f, 0.5f);
    }

    /**
     * Initialises the graphical representation of every piece that <i>will
     * be</i> created. This used to be done in {@link Game} during
     * initialisation - however, it must be done in here to support what I have
     * done for the main menu.
    **/
    protected void initialisePieces() {
        for (int i = 0; i != 3; i++) {
            for (int j = 1; j < 9; j += 2) {
                renderPiece(new Piece(-1, PieceType.MAN_WHITE, c.getModelStore().getModel(PieceType.MAN_WHITE)), new Tile((i != 1 ? j : j - 1), i));
            }
        }
        for (int i = 7; i != 4; i--) {
            for (int j = 1; j < 9; j += 2) {
                renderPiece(new Piece(-1, PieceType.MAN_BLACK, c.getModelStore().getModel(PieceType.MAN_BLACK)), new Tile((i == 6 ? j : j - 1), i));
            }
        }
    }

    /**
     * Sets the hover tile light to light the specified tile.
     *
     * @param hoverTile - the current hover tile, which will then be lit.
     **/
    protected void setHoverTile(Tile hoverTile) {
        if (hoverTile != null) {
            hoverTileLight.setPosition(new Vector3f(hoverTile.getX() * 2, 7.5f, hoverTile.getY() * 2));
            hoverTileLight.setColor(hoverTile.isBlack() ? hoverLightColour[0] : hoverLightColour[1]);

            if (c.getCurrentGame().getLogicalBoard().pieceOccupies(hoverTile)) {
                if (c.getCurrentGame().getLogicalBoard().getPiece(hoverTile) != selectedPiece && !isPieceLocked(c.getCurrentGame().getLogicalBoard().getPiece(hoverTile))) {
                    if (hoverPiece != null && hoverPiece != selectedPiece) {
                        hoverPiece.resetColour();
                    }

                    hoverPiece = c.getCurrentGame().getLogicalBoard().getPiece(hoverTile);
                    hoverPiece.setColour(hoverPiece.isBlack() ? BLACK_HOVER_COLOUR : WHITE_HOVER_COLOUR);
                    /*					if ((hoverPiece = c.getCurrentGame().getLogicalBoard().getPiece(hoverTile)).getSide() == c.getCurrentGame().getCurrentTurn()) {
                     hoverPiece.setColour(hoverPiece.isBlack() ? BLACK_HOVER_COLOUR : WHITE_HOVER_COLOUR);
                     }
                     else {
                     hoverPiece = null;
                     }*/
                }
            }
            else {
                if (hoverPiece != null && hoverPiece != selectedPiece) {
                    hoverPiece.resetColour();
                    hoverPiece = null;
                }
            }

            if (!lightDisplayed[0]) {
                boardNode.addLight(hoverTileLight);
                lightDisplayed[0] = true;
            }
        }
        else {
            if (lightDisplayed[0]) {
                boardNode.removeLight(hoverTileLight);
                lightDisplayed[0] = false;
            }
            if (hoverPiece != null && hoverPiece != selectedPiece) {
                hoverPiece.resetColour();
                hoverPiece = null;
            }
        }
    }

    /**
     * Sets the selected tile light to light the specified tile, and stores the
     * aforementioned tile in the member variable {@code selectedTile}.
     *
     * @param selectedTile - the newly selected tile, which will be lit and stored.
     **/
    protected void setSelectedTile(Tile selectedTile) {
        if (selectedTile != null) {

            if (c.getCurrentGame().getLogicalBoard().pieceOccupies(selectedTile)) {
                if (!isPieceLocked(c.getCurrentGame().getLogicalBoard().getPiece(selectedTile))) {
                    selectedPiece = c.getCurrentGame().getLogicalBoard().getPiece(selectedTile);
                    selectedPiece.setColour(selectedPiece.isBlack() ? BLACK_SELECTED_COLOUR : WHITE_SELECTED_COLOUR);
                }
                else {
                    return;
                }
            }

            this.selectedTile = selectedTile;
            selectedTileLight.setPosition(new Vector3f(selectedTile.getX() * 2, 7.5f, selectedTile.getY() * 2));
            selectedTileLight.setColor(ColorRGBA.White);

            if (!lightDisplayed[1]) {
                boardNode.addLight(selectedTileLight);
                lightDisplayed[1] = true;
            }
        }
        else {
            if (lightDisplayed[1]) {
                boardNode.removeLight(selectedTileLight);
                lightDisplayed[1] = false;
            }
            this.selectedTile = null;

            if (selectedPiece != null) {
                selectedPiece.resetColour();
                selectedPiece = null;
            }
        }
    }

    /**
     * Attaches the board (including both the board node and the piece node) to
     * the scene if it is not currently attached.
     **/
    protected void attach() {
        if (!c.getRootNode().hasChild(this)) {
            c.getRootNode().attachChild(this);
        }
    }

    /**
     * Detaches the board (including both the board node and the piece node)
     * from the scene if it is currently attached.
     **/
    protected void detach() {
        if (c.getRootNode().hasChild(this)) {
            c.getRootNode().detachChild(this);
        }
    }

    /**
     * Attaches the specified instance of {@code Piece} to the graphical board
     * at tile {@code originTile}.
     *
     * @param newPiece - the piece to render.
     * @param originTile - the tile to render the piece at.
     **/
    public void renderPiece(Piece newPiece, Tile originTile) {
        setPiece(newPiece, originTile);
        newPiece.getModel().setMaterial(newPiece.createMaterial(c));
        pieceNode.attachChild(newPiece.getModel());
        renderedPieces.add(newPiece);
    }

    /**
     * Removes <b>toRemove</b> from the graphical board via detonation.
     *
     * @param toRemove - the piece to remove.
     **/
    public void removePiece(Piece toRemove) {
        if (toRemove != null) {
            if (c.getConfig().useEffects()) {
                c.getEffectManager().createEffect(new ExplosionEffect(c, toRemove.isKing(), toRemove.getSide()), toRemove.getTile().getWorldLocation().add(0, 1, 0));
            }
            pieceNode.detachChild(toRemove.getModel());
            renderedPieces.remove(toRemove);
            toRemove.destroy();
        }
    }

    /**
     * Resets every locked piece to its original colour and clears the list.
     **/
    public void resetLockedPieces() {
        if (lockedPieces.isEmpty()) {
            return;
        }
        for (Piece p : lockedPieces) {
            if (p != null) {
                p.resetColour();
            }
        }
        lockedPieces.clear();
    }

    public void lockPiece(Piece toLock) {
        toLock.setColour(toLock.isBlack() ? BLACK_LOCKED_COLOUR : WHITE_LOCKED_COLOUR);
        lockedPieces.add(toLock);
    }

    public void shiftPiece(Piece movingPiece, Tile destinationTile) {
        if (!movingPiece.isJumping()) {
            c.getSoundManager().playSound(SoundType.MOVE_PIECE, movingPiece.getTile().getWorldLocation(), true);
        }
        c.setMovingPiece(movingPiece, destinationTile);
    }

    /**
     * Set piece <b>toSet</b> at tile <b>destinationTile</b> on the graphical
     * game board.
     *
     * @param toSet - the piece to set.
     * @param destinationTile - the tile to set <b>Piece toSet</b> at.
     * @param moveGeometry - {@code true} if the piece's geometry should be set.
     **/
    private void setPiece(Piece toSet, Tile destinationTile) {
        if (toSet != null) {
            toSet.setPosition(destinationTile);
        }
    }

    public Node getBoardNode() {
        return boardNode;
    }

    public ArrayList<Piece> getRenderedPieces() {
        return renderedPieces;
    }

    protected Tile getSelectedTile() {
        return selectedTile;
    }

    public boolean isPieceLocked(Piece toCheck) {
        return lockedPieces.contains(toCheck);
    }

    public void clearPieces() {
        pieceNode.detachAllChildren();
        renderedPieces.clear();
        lockedPieces.clear();
    }
}