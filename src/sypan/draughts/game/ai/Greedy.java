package sypan.draughts.game.ai;

import java.util.ArrayList;

import sypan.draughts.game.Game;
import sypan.draughts.game.GameSimulation;
import sypan.draughts.game.LogicalBoard;
import sypan.draughts.game.move.Move;
import sypan.draughts.game.piece.Piece;

/**
 * {@code Greedy} is my third AI; its name derives from the strategy it takes -
 * it will calculate and take the best move it can take at present, not planning
 * ahead or taking into account the potential future cost/impact of the move. It
 * looks only one move into the future.<p>
 *
 * It has been completely rewritten four times, most recently 06/04/2015.
 *
 * @author Carl Linley
 **/
public class Greedy extends AbstractAIType {

    @Override
    public Move calculateMove(Game game) {
        ArrayList<Move> possibleMoves = getPossibleMoves(getSide(), game);
        Move bestMove = null;

        for (Move move : possibleMoves) {
            move.setHeuristic(scoreMove(move, game));

            if (bestMove == null
             || bestMove.getHeuristicScore() < move.getHeuristicScore()) {
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int scoreMove(Move move, Game gameState) {
        int pieceID = gameState.getLogicalBoard().getPiece(move.getOrigin()).getID();

        GameSimulation simulatedGame = gameState.simulateMove(move, false);

        int menLost = gameState.countPiecesOfType(getSide().getMan()) - simulatedGame.countPiecesOfType(getSide().getMan()),
            menTaken = gameState.countPiecesOfType(getOpposingSide().getMan()) - simulatedGame.countPiecesOfType(getOpposingSide().getMan()),
            kingsLost = gameState.countPiecesOfType(getSide().getKing()) - simulatedGame.countPiecesOfType(getSide().getKing()),
            kingsTaken = gameState.countPiecesOfType(getOpposingSide().getKing()) - simulatedGame.countPiecesOfType(getOpposingSide().getKing()),
            endTileScore = 0;

        Piece p = simulatedGame.getLogicalBoard().getPiece(pieceID);

        if (p != null) {
            endTileScore = LogicalBoard.getTileValue(p.getTile());
        }
        return ((menTaken - menLost) * 4) + ((kingsLost < 0 ? -kingsLost : kingsLost) * 8) + (kingsTaken * 8) + endTileScore;
    }

    @Override
    public boolean onDrawOffer(Game currentGame) {
        // If we're losing badly, accept the draw.
        return currentGame.countPieces(getSide()) - currentGame.countPieces(getOpposingSide()) < -5;
    }
}
