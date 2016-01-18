package sypan.draughts.game.player;

import java.util.concurrent.Callable;

import sypan.draughts.client.Client;
import sypan.draughts.game.Game;
import sypan.draughts.game.ai.Apathy;
import sypan.draughts.game.ai.DraughtsAI;
import sypan.draughts.game.ai.Greedy;
import sypan.draughts.game.ai.Hungry;
import sypan.draughts.game.ai.Minimax;
import sypan.draughts.game.move.Move;
import sypan.utility.Logger;

/**
 * {@code AIPlayer} is a subclass of {@code Player} created to handle artificially intelligent players.
 * 
 * @see Player
 * @see PlayerType
 * @see DraughtsAI
 * @see Apathy
 * @see Greedy
 * @see Hungry
 * @author Carl Linley
 **/
public class AIPlayer extends Player {

    private final static int TURN_WAIT_TIME_MS = 1000;

    private final DraughtsAI currentAI;
    private Move nextMove;

    public AIPlayer(PlayerType aiType, Side mySide) {
        super(aiType, mySide);

        currentAI = createAIClass();   
        initAI();
    }

    private void initAI() {
        currentAI.initialiseType(this);
        setName(getAIName());
    }

    /**
     * Returns this AI player's name. Originally, this was done using:
     * <pre>   {@code currentAI.getClass().getSimpleName()}</pre> Of course, doing
     * it this way is not obfuscation safe; as I have a liking for obfuscating
     * my work, this method was born!
     *
     * @return this AI's 'name'.
     **/
    private String getAIName() {
        if (currentAI instanceof Apathy) {
            return "Apathy";
        }
        if (currentAI instanceof Greedy) {
            return "Greedy";
        }
        if (currentAI instanceof Hungry) {
            return "Hungry";
        }
        if (currentAI instanceof Minimax) {
            return "Minimax";
        }
        return null;
    }

    /**
     * Calculates a move for the AI to play and sends said move to <i>game</i>.
     *
     * @param currentGame - the current game.
     * @param client - the client.
     **/
    public void playMove(Game currentGame, Client client) {
        long start = System.currentTimeMillis();

        currentAI.updateMovablePieces(currentGame);

        nextMove = currentAI.calculateMove(currentGame);
        long calculationTime = (System.currentTimeMillis() - start);

        Logger.logInfo("AI move: " + nextMove);

        client.getExecutor().submit(() -> {
            if (calculationTime < TURN_WAIT_TIME_MS) {
                Thread.sleep(TURN_WAIT_TIME_MS - calculationTime);
            }
            currentGame.playMove(nextMove.getPiece(), nextMove.getDestination());
            return null;
        });
    }

    public boolean onDrawOffer(Game currentGame) {
        currentAI.updateMovablePieces(currentGame);

        return currentAI.onDrawOffer(currentGame);
    }

    /**
     * @return an instance of this AI player's style of play.
     **/
    private DraughtsAI createAIClass() {
        switch (getType()) {
            case APATHY:
                return new Apathy();
            case GREEDY:
                return new Greedy();
            case HUNGRY:
                return new Hungry();
            case MINMAX:
                return new Minimax();

            default:
                return null;
        }
    }

    /**
     * @deprecated Use "createAIClass()" instead. This was to make the project
     * obfuscation-safe by ensuring direct class references are used as opposed
     * to dynamically built ones.
     *
     * @return an instance of this AI player's style of play.
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     **/
    @Deprecated
    @SuppressWarnings("unused")
    private DraughtsAI getAIClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String enumerationName = getType().toString().toLowerCase().replaceAll("_", "").replaceAll("_", " ");

        char buf[] = enumerationName.toCharArray();

        boolean endMarker = true;

        for (int i = 0; i < buf.length; i++) {
            char c = buf[i];

            if (endMarker && c >= 'a' && c <= 'z') {
                buf[i] -= 0x20;
                endMarker = false;
            }
        }
        return (DraughtsAI) Class.forName("sypan.draughts.game.ai." + new String(buf, 0, buf.length)).newInstance();
    }
}