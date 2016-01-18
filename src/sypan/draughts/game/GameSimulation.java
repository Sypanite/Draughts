package sypan.draughts.game;

import sypan.draughts.game.move.MoveType;
import sypan.draughts.game.move.TurnType;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.PieceType;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.Side;
import sypan.utility.Utility;

import com.jme3.math.ColorRGBA;

/**
 * {@code GameSimulation} handles a simulated game. It's used by some AI players to
 * create a branch off the current game for move/risk calculation.
 *
 * @author Carl Linley
 **/
public class GameSimulation extends Game {

    private Side currentTurn, gameWinner;
    private boolean changingTurn, gameEnded, jumpedPiece, justThisMove;

    public GameSimulation(Game game, Piece piece, Tile tile, boolean justThisMove) {
        super(game.getLogicalBoard().copy());
        currentTurn = game.getCurrentTurn();
        playMove(piece, tile);
    }

    @Override
    protected boolean movePiece(MoveType moveType, Piece p, Tile destinationTile) {
        if (moveType == MoveType.VALID_SHIFT) {
            getLogicalBoard().movePiece(p.getTile(), destinationTile);
        }
        else if (moveType == MoveType.VALID_JUMP) {
            getLogicalBoard().jumpPiece(p.getTile(), destinationTile);
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public final void playMove(Piece toMove, Tile destinationTile) {
        if (destinationTile.equals(toMove.getTile())) {
            return;
        }

        MoveType moveType = checkMove(toMove, destinationTile);

        if (moveType.isValid()) {
            movePiece(moveType, toMove, destinationTile);

            jumpedPiece = (moveType == MoveType.VALID_JUMP);

            while (jumpedPiece) {
                Tile destTile;

                if (canJump(toMove.getType(), toMove.getTile(), (destTile = toMove.getTile().add(2, 2)))
                 || canJump(toMove.getType(), toMove.getTile(), (destTile = toMove.getTile().add(-2, 2)))
                 || canJump(toMove.getType(), toMove.getTile(), (destTile = toMove.getTile().add(2, -2)))
                 || canJump(toMove.getType(), toMove.getTile(), (destTile = toMove.getTile().add(-2, -2)))) {
                    movePiece(MoveType.VALID_JUMP, toMove, destTile);
                }
                else {
                    jumpedPiece = false;
                }
            }
            endTurn(toMove);
        }
    }

    @Override
    public void promotePiece(Piece toPromote) {
        applyPromotion(toPromote);
    }

    @Override
    protected void endTurn(Piece pieceMoved) {
        if (!canMove(currentTurn.oppose())) {
            endGame(countPieces(currentTurn.oppose()) > 0 ? 1 : 2, currentTurn);
        } else {
            changeTurn();
        }
    }

    @Override
    public Piece[] getMovablePieces(Side forSide) {
        Piece[] myPieces = new Piece[countMovablePieces(forSide)];
        int currentIndex = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(forSide)
                        && !p.isLocked() && (canMovePiece(p) || canJumpPiece(p))) {
                    myPieces[currentIndex++] = p;
                }
            }
        }
        return myPieces;
    }

    @Override
    protected void changeTurn() {
        setTurn(currentTurn.oppose());
        TurnType turnType = getCurrentTurnType();

        if (justThisMove) {
            return;
        }

        if (turnType == TurnType.TAKE_ENFORCED) {
            enforceJump();
        }
    }

    @Override
    public TurnType getCurrentTurnType() {
        int canTakeCount = 0;

        for (Piece p : getMovablePieces(currentTurn)) {
            if (p != null && canJumpPiece(p)) {
                canTakeCount++;
            }
        }
        if (canTakeCount == 0) {
            return TurnType.FREE;//You can move unconditionally.
        }
        if (canTakeCount == 1) {
            return TurnType.TAKE_ENFORCED;//I'm playing this move for you.
        } else {
            return TurnType.TAKE_CHOICE;//You can move conditionally.
        }
    }

    @Override
    protected void enforceJump() {
        for (Piece p : getMovablePieces(currentTurn)) {
            if (p != null && canJumpPiece(p)) {
                playMove(p, Utility.getJumpDestination(this, p, p.getTile()));
                changingTurn = false;
                endTurn(p);
            }
        }
    }

    @Override
    protected void endGame(int endCode, Side victor) {
        gameEnded = true;
        gameWinner = victor;
    }

    @Override
    public boolean gameEnded() {
        return gameEnded;
    }

    @Override
    public void notify(String notification, ColorRGBA notificationColour, int displayTimeMS) {
    }

    @Override
    protected void setTurn(Side side) {
        currentTurn = side;
    }

    @Override
    public Side getCurrentTurn() {
        return currentTurn;
    }

    @Override
    public boolean changingTurn() {
        return changingTurn;
    }

    @Override
    public void applyPromotion(Piece toPromote) {
        toPromote.setType(toPromote.isBlack() ? PieceType.KING_BLACK : PieceType.KING_WHITE);
    }

    /**
     * @return the side that won the game, or {@code null} if the game is still
     * going or it was a draw.
	 *
     */
    @Override
    public Side getWinner() {
        return gameWinner;
    }

    /**
     * Assess the current state of the simulated game board relative to the
     * specified {@code Side}.
     *
     * @param side - the side we're assessing.
     * @return a heuristic scoring of the entire board as it currently is in this simulation.
     **/
    public int assess(Side side) {
        Side enemy = side.oppose();

        int myMen = countPiecesOfType(side.getMan()),
                enemyMen = countPiecesOfType(enemy.getMan()),
                myKings = countPiecesOfType(side.getKing()),
                enemyKings = countPiecesOfType(enemy.getKing());

        int piecePositions = 0;

        for (Piece p : getAllPieces(side)) {
            piecePositions += LogicalBoard.getTileValue(p.getTile());
        }
        return ((myMen - enemyMen) * 4) + ((myKings - enemyKings) * 8) + piecePositions;
    }
}