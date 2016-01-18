package sypan.draughts.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import sypan.draughts.client.Client;
import sypan.draughts.client.gui.state.GameReplayState;
import sypan.draughts.game.move.Move;
import sypan.draughts.game.move.MoveType;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.PieceType;
import sypan.draughts.game.piece.Tile;
import sypan.draughts.game.player.PlayerType;
import sypan.draughts.game.player.Side;
import sypan.utility.Logger;

/**
 * {@code GameReplay} is a subclass of {@code Game} created specifically to
 * support the replaying of past games.
 *
 * @author Carl Linley
 *
 */
public class GameReplay extends Game {

    private final ArrayList<Move> moveList;
    private GameEnd gameEnd;

    private int currentMoveIndex;
    private Move currentMove;

    private boolean autoReplaying;

    public GameReplay(String filePath, Client client) {
        super(client, PlayerType.SPECTATOR, PlayerType.SPECTATOR);

        client.setShowHover(false);
        moveList = new ArrayList<>();

        if (!loadGame(filePath)) {
            super.endGame(-1, null);
            Logger.logInfo("Error loading game - save must be missing or corrupted.");
        }
    }

    @Override
    public void playMove(Piece toMove, Tile destinationTile) {
        movePiece(null, toMove, destinationTile);
    }

    @Override
    public void saveGame(int endCode, Side victor) {
    }

    @Override
    protected GameReplayState getGUIState() {
        if (getClient().getGUI().getState() instanceof GameReplayState) {
            return (GameReplayState) getClient().getGUI().getState();
        }
        return null;
    }

    @Override
    protected boolean movePiece(MoveType moveType, Piece p, Tile destinationTile) {
        getClient().enqueue(() -> {
            if (p.getTile().distance(destinationTile) == 1) {
                getLogicalBoard().movePiece(p.getTile(), destinationTile);
            }
            else {
                getLogicalBoard().jumpPiece(p.getTile(), destinationTile);
            }
            checkPromotion(p);
            return null;
        });
        return true;
    }

    private void autoReplay() {
        getGUIState().toggleAutoReplay(true);

        (new Thread(() -> {
            while (!gameEnded() && autoReplaying) {
                nextMove();
                
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                }
            }
            
            if (getGUIState() != null) {
                getGUIState().toggleAutoReplay(false);
            }
        }, "Auto Replay")).start();
    }

    protected Move getNext() {
        return moveList.get(currentMoveIndex + 1);
    }

    private boolean loadGame(String filePath) {
        BufferedReader reader = null;
        String readLine;
        String[] move;

        try {
            reader = new BufferedReader(new FileReader(new File(filePath)));
            super.getClient().getGraphicalBoard().clearPieces();

            while (!(readLine = reader.readLine()).startsWith("ENDGAME")) {
                move = readLine.split(", ");

                if (Integer.parseInt(move[0]) == 1) {
                    // We're parsing an initial move.
                    super.createPiece(-1, move[2].equals("W") ? PieceType.MAN_WHITE : PieceType.MAN_BLACK, Tile.parseTile(move[3]));
                }
                else {
                    // We're parsing a player-made move.
                    moveList.add(new Move(Tile.parseTile(move[3]), Tile.parseTile(move[4])));
                }
            }
            /* End of the game */
            String[] endLine = readLine.split(", ");
            gameEnd = new GameEnd(Integer.parseInt(endLine[1]), (endLine[2].equals("B") ? Side.BLACK : Side.WHITE));

            /* Close reader */
            Logger.logInfo("Successfully loaded previous game '" + filePath + "'.");
            reader.close();
            return true;
        }
        catch (IOException | NullPointerException e) {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e1) {
            }
            return false;
        }
    }

    public void nextMove() {
        if (getClient().isPieceMoving()) {
            return;
        }

        if (currentMoveIndex < moveList.size()) {
            currentMove = moveList.get(currentMoveIndex);
            playMove(getLogicalBoard().getPiece(currentMove.getOrigin()), currentMove.getDestination());
            currentMoveIndex++;
        }
        else {
            super.endGame(gameEnd.getCode(), gameEnd.getVictor());
        }
    }

    public void toggleAutoplay() {
        autoReplaying = !autoReplaying;

        if (autoReplaying) {
            autoReplay();

        }
    }
}

class GameEnd {

    private final int endCode;
    private final Side victor;

    protected GameEnd(int endCode, Side victor) {
        this.endCode = endCode;
        this.victor = victor;
    }

    protected Side getVictor() {
        return victor;
    }

    protected int getCode() {
        return endCode;
    }
}