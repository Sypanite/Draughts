package sypan.draughts.game.move;

import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.Side;

/**
 * {@code Move} has two uses - it is primarily used by the AI when calculating
 * moves, but it is also used to store move history. Move history is saved at
 * the end of the game.
 *
 * @author Carl Linley
 **/
public class Move implements Comparable<Move> {

    private Piece piece;
    private final Side side;
    private final Tile originTile, destinationTile;

    private int heuristicScore;

    public Move(Piece piece, Tile destinationTile) {
        this.piece = piece;
        this.destinationTile = destinationTile;
        this.originTile = piece.getTile().clone();
        this.side = piece.getSide();
    }

    public Move(Tile originTile, Tile destinationTile) {
        this.destinationTile = destinationTile;
        this.originTile = originTile;
        this.side = null;
    }

    public Move(Side side, Tile originTile, Tile destinationTile) {
        this.destinationTile = destinationTile;
        this.originTile = originTile;
        this.side = side;
    }

    public Tile getOrigin() {
        return originTile;
    }

    public Piece getPiece() {
        return piece;
    }

    public Tile getDestination() {
        return destinationTile;
    }

    public void setHeuristic(int heursticScore) {
        this.heuristicScore = heursticScore;
    }

    public int getHeuristicScore() {
        return heuristicScore;
    }

    @Override
    public int compareTo(Move otherMove) {
        if (getHeuristicScore() > otherMove.getHeuristicScore()) {
            return 1;
        }
        else if (getHeuristicScore() < otherMove.getHeuristicScore()) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return piece + " -> [" + destinationTile + "]";
    }

    public Side getSide() {
        return side;
    }

    public boolean isJump() {
        return originTile.distance(destinationTile) != 1;
    }

    public void alterHeuristic(int alterBy) {
        heuristicScore += alterBy;
    }
}