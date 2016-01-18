package sypan.draughts.game;

import sypan.draughts.client.GraphicalBoard;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.Tile;

/**
 * Handles the logical aspects of the game board. Any operations pertaining to
 * the actual game is done in this class.<p>
 *
 * {@link GraphicalBoard} handles all graphical aspects of the game board.
 *
 * @author Carl Linley
 **/
public class LogicalBoard implements Cloneable {

    /**
     * This constant array contains a 'score' for the every accessible tile, based on how exposed it is.
     *
     * Based on an image found at
     * <a href="http://ai-depot.com/articles/minimax-explained/3/">http://ai-depot.com/articles/minimax-explained/3/</a>
     **/
    private static final int[][] TILE_SCORES
    = {{4, 4, 4, 4},
       {4, 3, 3, 3},
       {3, 2, 2, 4},
       {4, 2, 1, 3},
       {3, 1, 2, 4},
       {4, 2, 2, 3},
       {3, 3, 3, 4},
       {4, 4, 4, 4}};

    private final Piece[][] gameBoard;
    private GraphicalBoard graphicalBoard;

    public LogicalBoard(GraphicalBoard graphicalBoard) {
        this.graphicalBoard = graphicalBoard;
        gameBoard = new Piece[8][8];
    }

    public static int getTileValue(Tile tile) {
        return TILE_SCORES[tile.getY()][(tile.getX() - (tile.getY() % 2 != 0 ? 1 : 0)) / 2];
    }

    /**
     * Creates the specified piece on the board both logically and graphically.
     *
     * @param toCreate - the instance of {@code Piece} to create.
     * @param originTile - the tile to create the piece at.
     **/
    protected void createPiece(Piece toCreate, Tile originTile) {
        setPiece(toCreate, originTile, true);

        if (!isSimulation()) {
            graphicalBoard.renderPiece(toCreate, originTile);
        }
    }

    protected void jumpPiece(Tile originTile, Tile destinationTile) {
        removePiece(originTile.add(destinationTile.subtract(originTile).normalise()));

        if (!isSimulation()) {
            getPiece(originTile).setJumping(true);
        }
        movePiece(originTile, destinationTile);
    }

    private boolean isSimulation() {
        return graphicalBoard == null;
    }

    protected void movePiece(Tile origin, Tile destination) {
        Piece movingPiece = getPiece(origin);

        if (!isSimulation()) {
            graphicalBoard.shiftPiece(movingPiece, destination);
        }
        setPiece(null, origin, false);
        setPiece(movingPiece, destination, false);
    }

    public void removePiece(Tile tile) {
        Piece toRemove = getPiece(tile);

        if (toRemove != null) {
            if (!isSimulation()) {
                graphicalBoard.removePiece(toRemove);
            }
            toRemove.destroy();
        }
        setPiece(null, tile, false);
    }

    private void setPiece(Piece p, Tile tile, boolean moveGeom) {
        gameBoard[tile.getX()][tile.getY()] = p;

        if (p != null) {
            if (!moveGeom) {
                p.setTile(tile);
            }
            else {
                p.setPosition(tile);
            }
        }
    }

    public boolean pieceOccupies(Tile t) {
        if (t.outOfBounds()) {
            return true;
        }
        try {
            return getPiece(t) != null;
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Retrieves a piece based on the tile it is occupying.
     *
     * @param tile - the tile the piece is occupying.
     * @return the piece on the specified tile, or {@code null} the tile is unoccupied.
     **/
    public Piece getPiece(Tile tile) {
        return gameBoard[tile.getX()][tile.getY()];
    }

    /**
     * Retrieves a piece based on its unique ID.
     *
     * @param pieceID - the unique id of the piece to retrieve.
     * @return the piece of the specified ID, or {@code null} if it does not exist. (e.g has been taken)
     **/
    public Piece getPiece(int pieceID) {
        for (int x = 0; x != 8; x++) {
            for (int y = 0; y != 8; y++) {
                if (gameBoard[x][y] != null && gameBoard[x][y].getID() == pieceID) {
                    return gameBoard[x][y];
                }
            }
        }
        return null;
    }

    public Piece[][] getPieces() {
        return gameBoard;
    }

    public void markSimulation() {
        graphicalBoard = null;
    }

    public LogicalBoard copy() {
        LogicalBoard deepCopy = new LogicalBoard(null);

        for (int x = 0; x != 8; x++) {
            for (int y = 0; y != 8; y++) {
                if (gameBoard[x][y] != null) {
                    Piece pieceClone = gameBoard[x][y].clone();
                    deepCopy.setPiece(pieceClone, pieceClone.getTile(), false);
                }
            }
        }
        return deepCopy;
    }
}