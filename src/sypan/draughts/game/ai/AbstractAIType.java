package sypan.draughts.game.ai;

import java.util.ArrayList;
import java.util.Random;

import sypan.draughts.game.Game;
import sypan.draughts.game.move.Move;
import sypan.draughts.game.move.TurnType;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.PieceType;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.AIPlayer;
import sypan.draughts.game.player.Side;

/**
 * The superclass of all AI players. Stores various methods used by most, if not all, AI players.
 * 
 * @author Carl Linley
 **/
public abstract class AbstractAIType implements DraughtsAI {

    private final Random random;
 
    private Piece[] movablePieces;
    private AIPlayer myPlayer;
    private Tile testedTile;

    protected AbstractAIType() {
        random = new Random();
    }

    @Override
    public abstract Move calculateMove(Game currentGame);

    @Override
    public void initialiseType(AIPlayer aiPlayer) {
        this.myPlayer = aiPlayer;
    }

    @Override
    public void updateMovablePieces(Game game) {
        movablePieces = game.getMovablePieces(getSide());
    }

    protected boolean tryMove(Game game, PieceType pieceType, Tile originTile, int moveType) {
        if (moveType < 4 && game.getCurrentTurnType() != TurnType.FREE) {
            testedTile = null;
            return false;
        }
        switch (moveType) {
            case 0:
                return game.canShift(pieceType, originTile, (testedTile = originTile.add(1, 1)));

            case 1:
                return game.canShift(pieceType, originTile, (testedTile = originTile.add(-1, 1)));

            case 2:
                return game.canShift(pieceType, originTile, (testedTile = originTile.add(1, -1)));

            case 3:
                return game.canShift(pieceType, originTile, (testedTile = originTile.add(-1, -1)));

            case 4:
                return game.canJump(pieceType, originTile, (testedTile = originTile.add(2, 2)));

            case 5:
                return game.canJump(pieceType, originTile, (testedTile = originTile.add(-2, 2)));

            case 6:
                return game.canJump(pieceType, originTile, (testedTile = originTile.add(2, -2)));

            case 7:
                return game.canJump(pieceType, originTile, (testedTile = originTile.add(-2, -2)));
        }
        return false;
    }

    protected ArrayList<Move> getPossibleMoves(Side currentSide, Game gameState) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        for (Piece p : gameState.getMovablePieces(currentSide)) {
            if (p != null) {
                for (int i = 0; i != 8; i++) {
                    if (tryMove(gameState, p.getType(), p.getTile(), i)) {
                        possibleMoves.add(new Move(p, getTestedTile()));
                    }
                }
            }
        }
        return possibleMoves;
    }

    protected void resetTestedTIle() {
        testedTile = null;
    }

    protected Tile getTestedTile() {
        return testedTile;
    }

    protected Side getOpposingSide() {
        return getSide().oppose();
    }

    protected AIPlayer getPlayer() {
        return myPlayer;
    }

    protected Piece[] getMovablePieces() {
        return movablePieces;
    }

    protected Random getRandom() {
        return random;
    }

    protected Side getSide() {
        return myPlayer.getSide();
    }
}