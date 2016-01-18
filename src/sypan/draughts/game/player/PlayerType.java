package sypan.draughts.game.player;

/**
 * {@code PlayerType} is an enumerated type containing all possible types of players. This includes
 * a human player as well as every type of artificially intelligent player.<p>
 * 
 * @author Carl Linley
 **/
public enum PlayerType {

    HUMAN,      // The player is not a bot.

    APATHY,     // Picks a psuedo-random move.
    GREEDY,     // Picks the optimal move it can find at present.
    HUNGRY,     // Sketchy version of GREEDY - picks an adequate move most of the time.
    MINMAX,     // An AI player that uses the min/max algorithm.

    SPECTATOR;	//Nobody is playing - each 'player' follows a set script.

    public String getFlag() {
        return (this == HUMAN ? "HU" : "AI");
    }
};