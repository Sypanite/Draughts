package sypan.draughts.client;

import sypan.draughts.game.GameReplay;
import sypan.draughts.game.piece.Tile;
import sypan.utility.Logger;

import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

/**
 * {@code Input} handles game input - mouse movement, mouse clicks, and key
 * presses.
 *
 * @author Carl Linley
 **/
final class Input implements ActionListener, AnalogListener {

    private final CollisionResults collisionResults;
    private final Client client;
    private final Ray mouseRay;
    
    private Tile hoverTile;

    protected Input(Client c) {
        this.client = c;
        collisionResults = new CollisionResults();
        mouseRay = new Ray();

        init();
    }

    void init() { // Prevent leaking this
        client.getInputManager().addMapping("SELECT", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        client.getInputManager().addMapping("CONTINUE", new KeyTrigger(KeyInput.KEY_SPACE));
        client.getInputManager().addListener(this, "SELECT", "CONTINUE");
        Logger.logInfo("Input handler initialised.");
    }

    @Override
    public void onAction(String actionName, boolean isPressed, float timePerFrame) {
        if (isPressed) {
            switch (actionName) {
                case "CONTINUE":
                    if (client.isGameRunning()) {
                        if (client.getCurrentGame().isReplay()) {
                            ((GameReplay) client.getCurrentGame()).nextMove();
                        }
                    }
                break;

                case "SELECT":
                    if (client.isGameRunning() && client.getCurrentGame().isReplay()) {
                        return;
                    }
                    if (client.getGUI().substateDisplayed()) {
                        client.getGUI().closeSubstate();
                        return;
                    }
                    else if (!client.isGameRunning() || client.getCurrentGame().changingTurn() || client.getCurrentGame().movePlayed()) {
                        return;
                    }
                    else if (!client.getCurrentGame().getPlayer(client.getCurrentGame().getCurrentTurn()).isHuman()) {
                        client.getCurrentGame().notify(client.getCurrentGame().includesHuman() ? "It is not your turn." : "You are spectating.", ColorRGBA.Red, 1000);
                        return;
                    }

                    Tile t = calculateHoverTile();

                    if (t == null) {
                        return;
                    }
                    if (client.getGraphicalBoard().getSelectedTile() == null) {
                        if (!client.getCurrentGame().getLogicalBoard().pieceOccupies(t)) {
                            return;
                        }
                        if (client.getCurrentGame().getLogicalBoard().getPiece(t).belongsTo(client.getCurrentGame().getCurrentTurn())) {
                            client.getGraphicalBoard().setSelectedTile(t);
                        }
                        else {
                            client.getCurrentGame().notify("That is not your piece.", ColorRGBA.Red, 1000);
                        }
                    }
                    else {
                        client.getCurrentGame().playMove(client.getCurrentGame().getLogicalBoard().getPiece(client.getGraphicalBoard().getSelectedTile()), t);
                        client.getGraphicalBoard().setSelectedTile(null);
                    }
                break;
            }
        }
    }

    @Override
    public void onAnalog(String actionName, float keyPressed, float timePerFrame) {
    }

    /**
     * This method returns the board tile that the mouse is currently hovering
     * over. It also stores this tile in the {@code hoverTile} member variable,
     * which is then accessed via {@code getCurrentHoverTile()} to determine
     * whether or not the hover light needs altering.
     *
     * @return the tile the mouse is currently hovering over.
     **/
    protected Tile calculateHoverTile() {
        collisionResults.clear();

        if (client.getDraughtsCamera().isMoving()) {
            return null;
        }

        Vector3f origin = client.getCamera().getWorldCoordinates(client.getInputManager().getCursorPosition(), 0.0f),
                direction = client.getCamera().getWorldCoordinates(client.getInputManager().getCursorPosition(), 0.3f).subtractLocal(origin).normalizeLocal();

        mouseRay.setOrigin(origin);
        mouseRay.setDirection(direction);

        client.getGraphicalBoard().getBoardNode().collideWith(mouseRay, collisionResults);

        if (collisionResults.size() > 0) {
            Vector3f p = collisionResults.getCollision(0).getContactPoint();

            int x = (int) (p.getX() + 1) / 2,
                    y = (int) (p.getZ() + 1) / 2;

            if (x < 0 || y < 0 || x > 7 || y > 7) {
                return null;
            }

            if (hoverTile != null) {
                return hoverTile.set(x, y);
            }
            else {
                return new Tile(x, y);
            }
        }
        return null;
    }

    /**
     * @return the current tile that the player is hovering over.
     **/
    protected Tile getCurrentHoverTile() {
        return hoverTile;
    }
}