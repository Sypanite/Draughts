package sypan.draughts.game.ai;

import sypan.draughts.game.Game;
import sypan.draughts.game.ai.HungryAIMove.HungryMoveType;
import sypan.draughts.game.move.Move;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.Side;

/**
 * This class was formerly named {@code Greedy}, but for the aforementioned
 * reasons I decided that due to its limitations (see the enumeration
 * <i>HungryMoveType</i>), it did not meet the criteria I was aiming for in a
 * 'greedy' AI. {@link Greedy} <i>(an advanced version of 'Hungry' - pun
 * intended)</i>, my third AI class, sought to rectify these issues.
 *
 * @author Carl Linley	
 **/
public class Hungry extends AbstractAIType {

    private HungryAIMove[] potentialMoves;
    private Tile destTile;

    private int currentMoveIndex = 0;

    @Override
    public Move calculateMove(Game game) {
        potentialMoves = new HungryAIMove[24];
        currentMoveIndex = 0;

        HungryAIMove chosenMove = null;

        for (Piece p : getMovablePieces()) {
            if (p != null) {
                switch (game.getCurrentTurnType()) {
                    case FREE:
                        check_promotePiece(p, game);		// If I can promote a piece, I will so so.
                        check_negateOrDefendThreats(p, game);	// If not, and one of my pieces is under threat, I will attempt to move or defend it.
                        check_genericMove(p, game);		// I will attempt to make a generic move.
                    break;

                    case TAKE_CHOICE:
                        check_takeEnemyPiece(p, game);		// Choose a piece to take.
                    break;

                    default:
                    break;
                }
            }
        }

        for (HungryAIMove move : potentialMoves) {
            if (move != null) {
                if (chosenMove == null) {
                    chosenMove = move;
                }
                else {
                    if (move.getPriority() < chosenMove.getPriority()) {
                        chosenMove = move;
                    }
                    else if (move.getPriority() == chosenMove.getPriority()) {
                        //Same priority - pick between them pseudo-randomly.

                        if (getRandom().nextBoolean()) {
                            chosenMove = move;
                        }
                    }
                }
            }
        }
        return chosenMove;
    }

    private void check_genericMove(Piece p, Game game) {
        considerMove(checkMove(game, p, (destTile = p.getTile().add(1, 1))));
        considerMove(checkMove(game, p, (destTile = p.getTile().add(-1, 1))));
        considerMove(checkMove(game, p, (destTile = p.getTile().add(1, -1))));
        considerMove(checkMove(game, p, (destTile = p.getTile().add(-1, -1))));
    }

    private void check_negateOrDefendThreats(Piece p, Game game) {
        boolean negatedThreat = false;

        if (game.pieceUnderThreat(p)) {
            if ((game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(1, 1))) && !game.isTileUnderThreatFrom(getSide().oppose(), destTile))) {
                considerMove(p, destTile, HungryMoveType.NEGATE_THREAT);
                negatedThreat = true;
            }
            if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(-1, 1))) && !game.isTileUnderThreatFrom(getSide().oppose(), destTile)) {
                considerMove(p, destTile, HungryMoveType.NEGATE_THREAT);
                negatedThreat = true;
            }
            if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(1, -1))) && !game.isTileUnderThreatFrom(getSide().oppose(), destTile)) {
                considerMove(p, destTile, HungryMoveType.NEGATE_THREAT);
                negatedThreat = true;
            }
            if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(-1, -1))) && !game.isTileUnderThreatFrom(getSide().oppose(), destTile)) {
                considerMove(p, destTile, HungryMoveType.NEGATE_THREAT);
                negatedThreat = true;
            }
            if (!negatedThreat) {
                //The piece is under threat and cannot move out of the way - can I block the attack?

                for (Piece q : getMovablePieces()) {
                    if (q != p) {
                        if (game.canShift(q.getType(), q.getTile(), (destTile = p.getTile().add(-1, -1)))) {
                            considerMove(q, destTile, HungryMoveType.DEFEND_PIECE);
                        }
                        if (game.canShift(q.getType(), q.getTile(), (destTile = p.getTile().add(1, -1)))) {
                            considerMove(q, destTile, HungryMoveType.DEFEND_PIECE);
                        }
                        if (game.canShift(q.getType(), q.getTile(), (destTile = p.getTile().add(-1, 1)))) {
                            considerMove(q, destTile, HungryMoveType.DEFEND_PIECE);
                        }
                        if (game.canShift(q.getType(), q.getTile(), (destTile = p.getTile().add(1, 1)))) {
                            considerMove(q, destTile, HungryMoveType.DEFEND_PIECE);
                        }
                    }
                }
            }
        }
    }

    private void check_promotePiece(Piece p, Game game) {
        if (!p.isKing()) {
            if (getSide() == Side.BLACK && p.getY() == 6) {
                if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(1, 1)))) {
                    considerMove(p, destTile, HungryMoveType.PROMOTE_PIECE);
                }
                if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(-1, 1)))) {
                    considerMove(p, destTile, HungryMoveType.PROMOTE_PIECE);
                }
            }
            if (getSide() == Side.BLACK && p.getY() == 1) {
                if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(1, -1)))) {
                    considerMove(p, destTile, HungryMoveType.PROMOTE_PIECE);
                }
                if (game.canShift(p.getType(), p.getTile(), (destTile = p.getTile().add(-1, -1)))) {
                    considerMove(p, destTile, HungryMoveType.PROMOTE_PIECE);
                }
            }
        }
    }

    private void check_takeEnemyPiece(Piece p, Game game) {
        if (game.canJump(p.getType(), p.getTile(), (destTile = p.getTile().add(2, 2)))) {
            considerJump(p, game, destTile);
        }
        if (game.canJump(p.getType(), p.getTile(), (destTile = p.getTile().add(-2, 2)))) {
            considerJump(p, game, destTile);
        }
        if (game.canJump(p.getType(), p.getTile(), (destTile = p.getTile().add(2, -2)))) {
            considerJump(p, game, destTile);
        }
        if (game.canJump(p.getType(), p.getTile(), (destTile = p.getTile().add(-2, -2)))) {
            considerJump(p, game, destTile);
        }
    }

    private void considerJump(Piece toCheck, Game game, Tile destinationTile) {
        if (!game.isTileUnderThreatFrom(getSide().oppose(), destinationTile)) {
            considerMove(toCheck, destinationTile, HungryMoveType.TAKE_PIECE_SAFELY);
        }
        else {
            considerMove(toCheck, destinationTile, HungryMoveType.TAKE_PIECE_DANGEROUSLY);
        }
    }

    private HungryAIMove checkMove(Game game, Piece piece, Tile destination) {
        if (game.canShift(piece.getType(), piece.getTile(), destination)) {
            if (!game.isTileUnderThreatFrom(getSide().oppose(), destination)) {
                return new HungryAIMove(piece, destination, HungryMoveType.SAFE_MOVE);
            }
            return new HungryAIMove(piece, destination, HungryMoveType.DANGEROUS_MOVE);
        }
        return null;
    }

    private void considerMove(Piece piece, Tile destination, HungryMoveType moveType) {
        potentialMoves[currentMoveIndex++] = new HungryAIMove(piece, destination, moveType);
    }

    private void considerMove(HungryAIMove move) {
        if (move == null) {
            return;
        }
        potentialMoves[currentMoveIndex++] = move;
    }

    @Override
    public boolean onDrawOffer(Game currentGame) {
        // If the AI is losing badly, accept the draw.
        return currentGame.countPieces(getSide()) - currentGame.countPieces(getOpposingSide()) < -5;
    }
}

class HungryAIMove extends sypan.draughts.game.move.Move {

    protected enum HungryMoveType {
        TAKE_PIECES_PROMOTE_PIECE, TAKE_PIECES_SAFELY, PROMOTE_PIECE, TAKE_PIECES_DANGEROUSLY, TAKE_PIECE_SAFELY, NEGATE_THREAT, DEFEND_PIECE, SAFE_MOVE, TAKE_PIECE_DANGEROUSLY, DANGEROUS_MOVE, DAMAGE_LIMITATION
    };

    private HungryMoveType moveType;

    private boolean promotePiece;
    private int piecesTaken;

    protected HungryAIMove(Piece piece, Tile destinationTile, HungryMoveType moveType) {
        super(piece, destinationTile);
        this.moveType = moveType;
    }

    protected HungryAIMove(Piece piece, Tile destinationTile, HungryMoveType moveType, boolean promotePiece, int piecesTaken) {
        this(piece, destinationTile, moveType);
        this.promotePiece = promotePiece;
        this.piecesTaken = piecesTaken;
    }

    protected HungryMoveType getType() {
        return moveType;
    }

    protected int getPriority() {
        return moveType.ordinal() - piecesTaken - (promotePiece ? 3 : 0);
    }

    @Override
    public String toString() {
        return super.toString() + " (" + moveType + ")";
    }
}