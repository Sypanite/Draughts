package sypan.draughts.game.ai;

import java.util.ArrayList;

import sypan.draughts.game.Game;
import sypan.draughts.game.GameSimulation;
import sypan.draughts.game.move.Move;

/**
 * {@code Minimax} uses the recursive min/max search algorithm to pick its next
 * move. It looks three moves ahead.<p>
 *
 * I don't believe this works as intended, but it works well enough. To be
 * improved if time allows.
 *
 * @author Carl Linley
 **/
public class Minimax extends AbstractAIType {

    public final static int SEARCH_DEPTH = 3;

    private MinimaxTree minimaxTree;

    @Override
    public Move calculateMove(Game game) {
        minimaxTree = new MinimaxTree(getPossibleMoves(getSide(), game));
        minimaxTree.sprout(this, game);

        return minimaxTree.getBestMove(this);
    }

    @Override
    public boolean onDrawOffer(Game currentGame) {
        // If the AI is losing badly, accept the draw.
        return currentGame.countPieces(getSide()) - currentGame.countPieces(getOpposingSide()) < -5;
    }
}

class MinimaxTree {

    private ArrayList<MinimaxNode> rootNodes;

    MinimaxTree(ArrayList<Move> possibleMoves) {
        rootNodes = new ArrayList<>();

        for (Move m : possibleMoves) {
            rootNodes.add(new MinimaxNode(m));
        }
    }

    public void sprout(Minimax m, Game g) {
        for (MinimaxNode rootNode : rootNodes) {
            rootNode.search(m, g.simulateMove(rootNode.getMove(), true));
        }
    }

    public Move getBestMove(Minimax m) {
        Move currentBestMove = null;
        int currentBestScore = 0;

        for (MinimaxNode rootNode : rootNodes) {
            int nodeScore = rootNode.minMax(m);

            if (currentBestMove == null || currentBestScore < nodeScore) {
                currentBestMove = rootNode.getMove();
                currentBestScore = nodeScore;
            }
        }
        return currentBestMove;
    }
}

final class MinimaxNode {

    private final Move thisMove;
    private final SearchType searchType;
    private final int nodeDepth;

    private ArrayList<MinimaxNode> childNodes;

    private GameSimulation gameState;

    protected MinimaxNode(MinimaxNode parentNode, Move thisMove, Minimax ai, GameSimulation currentState, SearchType searchType) {
        this.thisMove = thisMove;
        this.nodeDepth = parentNode.getDepth() + 1;
        this.searchType = searchType;

        search(ai, currentState);
    }

    protected void search(Minimax ai, GameSimulation currentState) {
        gameState = currentState;

        if (nodeDepth == Minimax.SEARCH_DEPTH) {
            return;
        }
        childNodes = new ArrayList<>();

        for (Move m : ai.getPossibleMoves(ai.getSide(), currentState)) {
            GameSimulation newState = currentState.simulateMove(m, true);
            childNodes.add(new MinimaxNode(this, m, ai, newState, searchType == SearchType.MAX ? SearchType.MIN : SearchType.MAX));
        }
    }

    protected Move getMove() {
        return thisMove;
    }

    protected MinimaxNode(Move rootMove) {
        this.thisMove = rootMove;
        this.searchType = SearchType.MAX;
        this.nodeDepth = 0;
    }

    protected int minMax(Minimax ai) {
        if (childNodes == null) {
            return gameState.assess(ai.getSide());
        }

        if (searchType == SearchType.MAX) {
            int max = 0;

            for (MinimaxNode childNode : childNodes) {
                max = Math.max(max, childNode.minMax(ai));
            }
            return max;
        }
        else {
            int min = 0;

            for (MinimaxNode childNode : childNodes) {
                min = Math.min(min, childNode.minMax(ai));
            }
            return min;
        }
    }

    private int getDepth() {
        return nodeDepth;
    }

}

enum SearchType {

    MAX, MIN;

    SearchType oppose() {
        return (this == SearchType.MAX ? SearchType.MIN : SearchType.MAX);
    }
};