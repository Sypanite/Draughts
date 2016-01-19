package sypan.draughts.game;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sypan.draughts.client.Client;
import sypan.draughts.client.effect.PromotionEffect;
import sypan.draughts.client.gui.StateType;
import sypan.draughts.client.gui.state.GameState;
import sypan.draughts.client.manager.ModelManager.ModelType;
import sypan.draughts.client.manager.SoundManager.SoundType;

import sypan.draughts.game.move.*;
import sypan.draughts.game.piece.*;
import sypan.draughts.game.player.*;

import sypan.utility.Logger;
import sypan.utility.Utility;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

/**
 * {@code Game} handles the actual game-play of Draughts.
 *
 * @author Carl Linley
 **/
public class Game {

    private Client client;

    private LogicalBoard logicalBoard;
    private Side currentTurn, gameWinner;
    private Player[] gamePlayer;
    private ArrayList<Move> moveHistory;

    private boolean changingTurn, drawOffered, gameEnded, jumpedPiece, movePlayed;

    /**
     * Used by {@link GameSimulation} only.
     *
     * @param logicalBoard - the logical board.
     **/
    protected Game(LogicalBoard logicalBoard) {
        this.logicalBoard = logicalBoard;
    }

    public Game(Client client, PlayerType playerBlack, PlayerType playerWhite) {
        this.client = client;
        logicalBoard = new LogicalBoard(client.getGraphicalBoard());

        gamePlayer = new Player[2];
        moveHistory = new ArrayList<>();

        createPlayer(playerBlack);
        createPlayer(playerWhite);

        initialisePieces();
        currentTurn = Side.BLACK;

        if (getPlayer(Side.BLACK).isHuman()) {
            try {
                moveCameraTo(Side.BLACK);
                client.setShowHover(true);
            }
            catch (InterruptedException e) {
                client.getDraughtsCamera().setDefaultHorizontalRotation(90 * FastMath.DEG_TO_RAD);
            }
        }
        else if (getPlayer(Side.WHITE).isHuman()) {
            try {
                moveCameraTo(Side.WHITE);
            }
            catch (InterruptedException e) {
                client.getDraughtsCamera().setDefaultHorizontalRotation(-90 * FastMath.DEG_TO_RAD);
            }
        }
        else {
            try {
                moveCameraTo(Side.SPECTATOR);
            }
            catch (InterruptedException e) {
                client.getDraughtsCamera().setDefaultHorizontalRotation(0);
            }
            client.getDraughtsCamera().setDefaultVerticalRotation(50 * FastMath.DEG_TO_RAD);
        }
        Logger.logInfo(gamePlayer[0].getName() + " v.s " + gamePlayer[1].getName());
        start();
    }

    private void start() {
        if (getPlayer(Side.BLACK).isAI()) {
            getAIPlayer(Side.BLACK).playMove(this, client);
        }
    }

    public GameSimulation simulateMove(Move move, boolean justThisMove) {
        return new GameSimulation(this, move.getPiece(), move.getDestination(), justThisMove);
    }

    private void createPlayer(PlayerType playerType) {
        int playerIndex = (gamePlayer[0] == null ? 0 : 1);
        Side side = Side.values()[playerIndex];

        if (playerType == PlayerType.SPECTATOR) {
            gamePlayer[playerIndex] = new Spectator();
        }
        else if (playerType == PlayerType.HUMAN) {
            gamePlayer[playerIndex] = new HumanPlayer(side);
        }
        else {
            gamePlayer[playerIndex] = new AIPlayer(playerType, side);
        }
    }

    private void initialisePieces() {
        client.getGraphicalBoard().clearPieces();
        int pieceID = 0;

        for (int i = 0; i != 3; i++) {
            for (int j = 1; j < 9; j += 2) {
                createPiece(pieceID++, PieceType.MAN_WHITE, new Tile((i != 1 ? j : j - 1), i));
            }
        }
        for (int i = 7; i != 4; i--) {
            for (int j = 1; j < 9; j += 2) {
                createPiece(pieceID++, PieceType.MAN_BLACK, new Tile((i == 6 ? j : j - 1), i));
            }
        }
    }

    protected void createPiece(int pieceID, PieceType pieceType, Tile originTile) {
        Spatial pieceModel = client.getModelStore().getModel(ModelType.valueOf("PIECE_" + pieceType.toString().split("_")[0]));

        getLogicalBoard().createPiece(new Piece(pieceID, pieceType, pieceModel), originTile);
    }

    protected boolean movePiece(MoveType moveType, Piece p, Tile destinationTile) {
        Tile originTile = p.getTile().clone();

        if (moveType == MoveType.VALID_SHIFT) {
            getLogicalBoard().movePiece(p.getTile(), destinationTile);
        }
        else if (moveType == MoveType.VALID_JUMP) {
            getLogicalBoard().jumpPiece(p.getTile(), destinationTile);
        }
        else {
            return false;
        }
        moveHistory.add(new Move(p.getSide(), originTile, destinationTile));
        return true;
    }

    protected MoveType checkMove(Piece toMove, Tile destinationTile) {
        if (destinationTile.equals(toMove.getTile())
         || destinationTile.outOfBounds()
         || getLogicalBoard().pieceOccupies(destinationTile)) {
            return MoveType.INVALID;
        }

        TurnType turnType = getCurrentTurnType();

        if (toMove.getTile().distance(destinationTile) == 1) {// Is Player trying to make a standard move?
            if (canShift(toMove.getType(), toMove.getTile(), destinationTile)) {// Yes - is the move valid?
                if (turnType == TurnType.FREE) {
                    return MoveType.VALID_SHIFT;
                }
                return MoveType.INVALID_SHIFT;// We can take a piece, therefore a shift is not valid.
            }
        }
        else {
            if (canJump(toMove.getType(), toMove.getTile(), destinationTile)) {
                return MoveType.VALID_JUMP;
            }
        }
        return MoveType.INVALID;
    }

    private boolean canJump(Piece toMove, Tile destTile) {
        return canJump(toMove.getType(), toMove.getTile(), destTile);
    }

    private int countPossibleJumps(Piece toMove) {
        int jumps = 0;
        Tile destTile = new Tile();

        for (int i = 0; i != 4; i++) {
            destTile.set(toMove.getTile());

            if (i == 0) {
                destTile.addLocal(2, 2);
            }
            if (i == 1) {
                destTile.addLocal(-2, 2);
            }
            if (i == 2) {
                destTile.addLocal(2, -2);
            }
            if (i == 3) {
                destTile.addLocal(-2, -2);
            }

            if (canJump(toMove, destTile)) {
                jumps ++;
            }
        }
        return jumps;
    }

    public void playMove(Piece toMove, Tile destinationTile) {
        if (movePlayed || destinationTile.equals(toMove.getTile())) {
            return;
        }

        MoveType moveType = checkMove(toMove, destinationTile);

        if (moveType.isValid()) {
            movePlayed = true;
            movePiece(moveType, toMove, destinationTile);
            client.setShowHover(false);

            jumpedPiece = (moveType == MoveType.VALID_JUMP);

            if (jumpedPiece) {
                final Tile destTile = new Tile();

                client.getExecutor().submit(() -> {
                    int piecesTaken = 1;
                    boolean resetTurn = false;
                    
                    while (jumpedPiece) {
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                        }

                        client.enqueue(() -> {
                            int possibleJumps = countPossibleJumps(toMove);

                            if (possibleJumps == 0) {
                                jumpedPiece = false;
                            }
                            else if (possibleJumps == 1) {
                                for (int i = 0; i != 4; i++) {
                                    destTile.set(toMove.getTile());

                                    if (i == 0) {
                                        destTile.addLocal(2, 2);
                                    }
                                    if (i == 1) {
                                        destTile.addLocal(-2, 2);
                                    }
                                    if (i == 2) {
                                        destTile.addLocal(2, -2);
                                    }
                                    if (i == 3) {
                                        destTile.addLocal(-2, -2);
                                    }

                                    if (canJump(toMove, destTile)) {
                                        break;
                                    }
                                }
                                movePiece(MoveType.VALID_JUMP, toMove, destTile);
                            }
                            else {
                                destTile.set(-1, -1);
                                jumpedPiece = false;
                            }
                            return null;
                        });

                        if (!destTile.equals(-1, -1) && jumpedPiece) {
                            notifyJump(piecesTaken);
                            piecesTaken++;
                        }
                    }

                    client.enqueue(() -> {
                        if (!destTile.equals(-1, -1)) {
                            endTurn(toMove);
                        }
                        else { // Choice
                            Logger.logInfo("Setting choice.");
                            movePlayed = false;
                            lockPieces(currentTurn);

                            if (getPlayer(currentTurn).isHuman()) {
                                client.setShowHover(true);
                            }
                            else {
                                getAIPlayer(currentTurn).playMove(this, client);
                            }
                        }
                        return null;
                    });
                });
            }
            else {
                endTurn(toMove);
            }
        }
        else {
            notify("Invalid move!" + (moveType == MoveType.INVALID_SHIFT ? " You must take an enemy piece." : ""), ColorRGBA.Red, 1000);
            client.getSoundManager().playSound(SoundType.INVALID_MOVE, null, false);
        }
    }

    public boolean canShift(PieceType pieceType, Tile originTile, Tile destTile) {
        if (destTile.outOfBounds() || getLogicalBoard().pieceOccupies(destTile)) {
            return false;
        }
        switch (pieceType) {
            case MAN_WHITE:
                return (destTile.equals(originTile.getX() + 1, originTile.getY() + 1) || destTile.equals(originTile.getX() - 1, originTile.getY() + 1));

            case MAN_BLACK:
                return (destTile.equals(originTile.getX() + 1, originTile.getY() - 1) || destTile.equals(originTile.getX() - 1, originTile.getY() - 1));

            case KING_BLACK:
            case KING_WHITE:
                return canShift(PieceType.MAN_BLACK, originTile, destTile) || canShift(PieceType.MAN_WHITE, originTile, destTile);
        }
        return false;
    }

    public boolean canJump(PieceType pieceType, Tile originTile, Tile destTile) {
        if (destTile.outOfBounds() || getLogicalBoard().pieceOccupies(destTile)) {
            return false;
        }
        switch (pieceType) {
            case KING_BLACK:
            case KING_WHITE:

            case MAN_BLACK:
                // NOT sexy code - TODO cleanup
                if ((destTile.equals(originTile.getX() + 2, originTile.getY() - 2)
                        && (getLogicalBoard().pieceOccupies(destTile.subtract(1, -1))
                        && getLogicalBoard().getPiece(destTile.subtract(1, -1)).belongsTo(pieceType.getSide().oppose()))
                 || (destTile.equals(originTile.getX() - 2, originTile.getY() - 2)
                        && (getLogicalBoard().pieceOccupies(destTile.subtract(-1, -1))
                        && getLogicalBoard().getPiece(destTile.subtract(-1, -1)).belongsTo(pieceType.getSide().oppose()))))) {
                    return true;
                }
                else if (pieceType == PieceType.MAN_BLACK) {
                    return false;
                }

            case MAN_WHITE:
                if ((destTile.equals(originTile.getX() + 2, originTile.getY() + 2)
                        && (getLogicalBoard().pieceOccupies(destTile.subtract(1, 1))
                        && getLogicalBoard().getPiece(destTile.subtract(1, 1)).belongsTo(pieceType.getSide().oppose()))
                    || (destTile.equals(originTile.getX() - 2, originTile.getY() + 2)
                        && (getLogicalBoard().pieceOccupies(destTile.subtract(-1, 1))
                        && getLogicalBoard().getPiece(destTile.subtract(-1, 1)).belongsTo(pieceType.getSide().oppose()))))) {
                    return true;
                }
                else if (pieceType == PieceType.MAN_WHITE) {
                    return false;
                }
                break;
        }
        return false;
    }

    protected void notifyJump(int piecesTaken) {
        client.enqueue(() -> {
            switch (piecesTaken) {
                case 2:
                    notify("Double jump!", ColorRGBA.Cyan, 500);
                break;
                    
                case 3:
                    notify("Triple jump!", ColorRGBA.Blue, 500);
                break;
                    
                case 4:
                    notify("Quadruple jump!", ColorRGBA.Magenta, 500); // I've never seen this
                break;
                    
                case 5:
                    notify("Quintuple jump!", ColorRGBA.Pink, 500); // Or this
                break;
                    
                default:
                break;
            }
            return null;
        });
    }

    protected void saveGame(int endCode, Side victor) {
        String fileName = Utility.getDate(true) + " - " + Utility.getTime(true).substring(0, 5) + " - " + gamePlayer[0].getName() + " v " + gamePlayer[1].getName();
        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(new File("history/" + fileName + ".csv")));

            /*
             * Save initial piece set-up - this is constant
             */
            for (int i = 7; i != 4; i--) {
                for (int j = 1; j < 9; j += 2) {
                    writer.write("1, " + gamePlayer[1].getType().getFlag() + ", " + "B, " + (i == 6 ? j : j - 1) + "," + i);
                    writer.newLine();
                }
            }
            for (int i = 0; i != 3; i++) {
                for (int j = 1; j < 9; j += 2) {
                    writer.write("1, " + gamePlayer[0].getType().getFlag() + ", " + "W, " + (i != 1 ? j : j - 1) + "," + i);
                    writer.newLine();
                }
            }

            /* Save move history */
            for (Move m : moveHistory) {
                writer.write("0, " +                                                            // 0, 1 - Move type - 0 for player, 1 for initial
                             getPlayer(m.getSide()).getType().getFlag() + ", " +                // AI, HU - Player type - 'AI' for AI, 'HU' for human
                             m.getSide().getID() + ", " +                                       // B, W - Side - 'B' for black, 'W' for white
                             m.getOrigin().getX() + "," + m.getOrigin().getY() + ", " +         // n, n - Origin tile coordinates
                             m.getDestination().getX() + "," + m.getDestination().getY() + ""); // n, n - Destination tile coordinates
                writer.newLine();
            }
            writer.write("ENDGAME, " + endCode + ", " + victor.getID());

            /* Close writer */
            writer.close();
            Logger.logInfo("Saved game. ('history/" + fileName + ".csv')");
        }
        catch (IOException e) {
            Logger.logSevere("Failed to save game: " + e + " - " + e.getMessage());
        }
    }

    /**
     * @param toCheck - the piece we're checking for promotion.
     * 
     * @return true if Piece <i>toCheck</i> is being promoted - this value, if
     * true, is used to stop the turn ending prematurely.
     **/
    protected boolean checkPromotion(Piece toCheck) {
        if (!toCheck.isKing()) {
            if (toCheck.isBlack() && toCheck.getTile().getY() == 0) {
                promotePiece(toCheck);
                return true;
            }
            else if (toCheck.isWhite() && toCheck.getTile().getY() == 7) {
                promotePiece(toCheck);
                return true;
            }
        }
        return false;
    }

    public void promotePiece(Piece toPromote) {
        if (toPromote == null) {// Squashes a super-rare bug
            return;
        }

        if (client.getConfig().useEffects()) {
            client.getEffectManager().createEffect(new PromotionEffect(client, toPromote), toPromote.getTile().getWorldLocation().subtract(0, 1, 0));
        }
        else {
            applyPromotion(toPromote);
        }
    }

    /**
     * Returns an array of a varying size containing every movable piece
     * currently on the board belonging to the specified side.
     *
     * @param forSide - the side whose pieces to retrieve.
     * @return an array containing every movable piece currently on the board
     * belonging to the specified side.
     **/
    public Piece[] getMovablePieces(Side forSide) {
        Piece[] myPieces = new Piece[countMovablePieces(forSide)];
        int currentIndex = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(forSide)
                 && !client.getGraphicalBoard().isPieceLocked(p)
                 && (canMovePiece(p) || canJumpPiece(p))) {
                    myPieces[currentIndex++] = p;
                }
            }
        }
        return myPieces;
    }

    /**
     * Returns an array of a varying size containing every piece currently on
     * the board belonging to the specified side.
     *
     * @param forSide - the side whose pieces to retrieve.
     *
     * @return an array containing every piece currently on the board belonging
     * to the specified side.
     **/
    public Piece[] getAllPieces(Side forSide) {
        Piece[] myPieces = new Piece[countPieces(forSide)];
        int currentIndex = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(forSide)) {
                    myPieces[currentIndex++] = p;
                }
            }
        }
        return myPieces;
    }

    public TurnType getCurrentTurnType() {
        if (drawOffered) {
            return TurnType.RESPOND_DRAW;// You must reply to a draw offer.
        }

        int canTakeCount = 0;

        for (Piece p : getMovablePieces(currentTurn)) {
            if (p != null && canJumpPiece(p)) {
                canTakeCount++;
            }
        }
        if (canTakeCount == 0) {
            return TurnType.FREE;// You can move unconditionally.
        }
        if (canTakeCount == 1) {
            return TurnType.TAKE_ENFORCED;// I'm playing this move for you.
        }
        else {
            return TurnType.TAKE_CHOICE;// You can move conditionally.
        }
    }

    protected void endTurn(Piece pieceMoved) {
        if (!canMove(currentTurn.oppose())) {
            endGame(countPieces(currentTurn.oppose()) > 0 ? 1 : 2, currentTurn);
        }
        else {
            if (checkPromotion(pieceMoved)) {
                client.getExecutor().submit(() -> {
                    Thread.sleep(3000); // Wait for the promotion performance to finish.
                    
                    client.enqueue(() -> {
                        changeTurn();
                        return null;
                    });
                    return null;
                });
            }
            else {
                changeTurn();
            }
        }
    }

    protected void changeTurn() {
        changingTurn = true;

        client.getExecutor().submit(() -> {
            while (client.isPieceMoving()) {
                Thread.sleep(100);
            }
            setTurn(currentTurn.oppose());

            TurnType turnType = getCurrentTurnType();

            if (getPlayer(currentTurn).isHuman()) {
                moveCameraTo(currentTurn);
                
                while (client.getDraughtsCamera().isMoving()) {
                    Thread.sleep(100);
                }
                if (turnType != TurnType.RESPOND_DRAW) {
                    client.setShowHover(true);
                    
                    if (turnType == TurnType.TAKE_ENFORCED) {
                        enforceJump();
                    }
                    else if (turnType == TurnType.TAKE_CHOICE) {
                        lockPieces(currentTurn);
                    }
                }
                else {
                    client.setShowHover(false);
                    client.getGUI().setState(StateType.SUBSTATE_RESPOND_DRAW);
                }
            }
            else {
                changingTurn = movePlayed = false;
                
                if (getCurrentTurnType() != TurnType.TAKE_ENFORCED) {
                    if (turnType == TurnType.TAKE_CHOICE) {
                        lockPieces(currentTurn);
                    }
                    getAIPlayer(currentTurn).playMove(this, client);
                }
                else {
                    enforceJump();
                }
            }
            return null;
        });
    }

    private void lockPieces(Side forSide) {
        client.enqueue(() -> {
            for (Piece p : getAllPieces(forSide)) {
                if (!canJumpPiece(p)) {
                    client.getGraphicalBoard().lockPiece(p);
                }
            }
            return null;
        });
    }

    /**
     * Handles an enforced jump.
     **/
    protected void enforceJump() {
        client.enqueue(() -> {
            client.setShowHover(false);
            
            for (Piece p : getMovablePieces(currentTurn)) {
                if (p != null && canJumpPiece(p)) {
                    movePlayed = false;
                    playMove(p, Utility.getJumpDestination(Game.this, p, p.getTile()));
                    notify("Jump enforced!", ColorRGBA.White, 3000);
                    changingTurn = false;
                }
            }
            return null;
        });
    }

    private void moveCameraTo(Side side) throws InterruptedException {
        client.getDraughtsCamera().moveToSide(this, side);
        changingTurn = movePlayed = false;
    }

    public int countPiecesOfType(PieceType pieceType) {
        int pieceCount = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.getType() == pieceType) {
                    pieceCount++;
                }
            }
        }
        return pieceCount;
    }

    public int countPieces(Side side) {
        int pieceCount = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(side)) {
                    pieceCount++;
                }
            }
        }
        return pieceCount;
    }

    public int countMovablePieces(Side side) {
        int pieceCount = 0;

        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(side) && canMovePiece(p)) {
                    pieceCount++;
                }
            }
        }
        return pieceCount;
    }

    protected void endGame(int endCode, Side victor) {
        gameEnded = true;
        gameWinner = victor;

        client.setShowHover(false);

        /*
         * Blow every remaining piece up, just for fun!
         */
        client.getExecutor().submit(() -> {
            if (endCode != -1) {// Erroneous ending
                for (Piece[] o : getLogicalBoard().getPieces()) {
                    for (Piece p : o) {
                        if (p != null) {
                            client.enqueue(() -> {
                                getLogicalBoard().removePiece(p.getTile());
                                return null;
                            });
                            Thread.sleep(100);
                        }
                    }
                }
                Thread.sleep(1000);
                client.stopMusic();
                client.getSoundManager().playSound(SoundType.END_GAME, null, false);
                notifyEndGame(endCode, victor);
                
                // Wait five seconds, then return to main menu
                Thread.sleep(5000);
            }
            
            client.enqueue(() -> {
                client.setCurrentGame(null);
                client.displayMainMenu();
                return null;
            });
            return null;
        });
        if (this instanceof GameReplay == false) {// It's been a very long time since I've written '== false'...
            saveGame(endCode, victor);
        }
    }

    public boolean gameEnded() {
        return gameEnded;
    }

    private void notifyEndGame(int endCode, Side victor) {
        String winner = victor.getName(), loser = victor.oppose().getName();

        switch (endCode) {
            case 1:// Cannot move
                notify(loser + " cannot move - " + winner + " wins!", ColorRGBA.Green, 10000);
            break;

            case 2:// Run out of pieces
                notify(loser + " has no pieces left - " + winner + " wins!", ColorRGBA.Green, 10000);
            break;

            case 3:// Draw
                notify(winner + " accepted " + loser + "'s draw request - it's a draw!", ColorRGBA.Yellow, 10000);
            break;

            case 4:// Forfeit
                notify(loser + " forfeited - " + winner + " wins!", ColorRGBA.Yellow, 10000);
            break;
        }
    }

    /**
     * Used as a win condition.
     *
     * @param side - the side we're checking.
     * @return true if parameter <i>side</i> can move any piece.
     **/
    protected boolean canMove(Side side) {
        return countMovablePieces(side) != 0;
    }

    public boolean canMovePiece(Piece p) {
        return canShift(p.getType(), p.getTile(), p.getTile().add(1, 1))
            || canShift(p.getType(), p.getTile(), p.getTile().add(-1, 1))
            || canShift(p.getType(), p.getTile(), p.getTile().add(1, -1))
            || canShift(p.getType(), p.getTile(), p.getTile().add(-1, -1))
            || canJumpPiece(p);
    }

    public boolean canJumpPiece(Piece p) {
        return Utility.getJumpDestination(this, p, p.getTile()) != null;
    }

    public void notify(String notification, ColorRGBA notificationColour, int displayTimeMS) {
        client.enqueue(() -> {
            GameState gameState = getGUIState();
            
            if (gameState != null) {
                gameState.notify(notification, notificationColour, displayTimeMS);
            }
            return null;
        });
    }

    protected GameState getGUIState() {
        if (client.getGUI().getState() instanceof GameState) {
            return (GameState) client.getGUI().getState();
        }
        return null;
    }

    protected void setTurn(Side side) {
        currentTurn = side;
        client.getGraphicalBoard().resetLockedPieces();
    }

    public boolean pieceUnderThreat(Piece toCheck) {
        return isTileUnderThreatFrom(toCheck.getSide().oppose(), toCheck.getTile());
    }

    public boolean isTileUnderThreatFrom(Side side, Tile toCheck) {
        return tileUnderThreatFrom(side, toCheck) != null;
    }

    /**
     * Checks if the specified tile is under threat from a piece belonging to
     * the specified side.
     *
     * @param side - the side we are checking for.
     * @param toCheck - the tile we are checking.
     * @return the piece threatening <b>toCheck</b>, or {@code null} if the tile
     * is not under threat.
     **/
    public Piece tileUnderThreatFrom(Side side, Tile toCheck) {
        for (Piece[] o : getLogicalBoard().getPieces()) {
            for (Piece p : o) {
                if (p != null && p.belongsTo(side)) {
                    if (canShift(p.getType(), p.getTile(), toCheck)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public Side getCurrentTurn() {
        return currentTurn;
    }

    public final Player getPlayer(Side side) {
        return gamePlayer[side.ordinal()];
    }

    private AIPlayer getAIPlayer(Side side) {
        return (AIPlayer) getPlayer(side);
    }

    public boolean changingTurn() {
        return changingTurn;
    }

    public boolean movePlayed() {
        return movePlayed;
    }

    protected void setLogicalBoard(LogicalBoard logicalBoard) {
        this.logicalBoard = logicalBoard;
    }

    public LogicalBoard getLogicalBoard() {
        return logicalBoard;
    }

    public boolean isReplay() {
        return this instanceof GameReplay;
    }

    public boolean includesHuman() {
        return getPlayer(Side.WHITE).isHuman() || getPlayer(Side.BLACK).isHuman();
    }

    public void offerDraw() {
        notify(currentTurn.getName() + " offered a draw!", ColorRGBA.Magenta, 3000);
        drawOffered = true;

        if (getPlayer(currentTurn.oppose()).isHuman()) {
            changeTurn();
        }
        else {
            AIPlayer aiPlayer = (AIPlayer) getPlayer(currentTurn.oppose());
            respondDraw(aiPlayer.onDrawOffer(this));
        }
    }

    public void respondDraw(boolean acceptedOffer) {
        drawOffered = false;

        if (acceptedOffer) {
            endGame(3, currentTurn);
        }
        else {
            notify(currentTurn.getName() + " declined the draw offer.", ColorRGBA.Magenta, 3000);

            if (getPlayer(currentTurn.oppose()).isHuman()) {
                changeTurn();
            }
        }
    }

    public void forfeitGame() {
        endGame(4, currentTurn.oppose());
    }

    /**
     * Applies the actual promotion - notifies the player(s), changes the piece,
     * and plays the sound.
     *
     * @param toPromote - the piece to receive the promotion.
     **/
    public void applyPromotion(Piece toPromote) {
        client.enqueue(() -> {
            Game.this.notify("The piece has been promoted!", ColorRGBA.White, 3000);
            toPromote.setModel(client.getModelStore().getModel(ModelType.PIECE_KING));
            client.getSoundManager().playSound(SoundType.PROMOTE_PIECE, toPromote.getModel().getLocalTranslation(), false);
            return null;
        });
        toPromote.setType(toPromote.isBlack() ? PieceType.KING_BLACK : PieceType.KING_WHITE);
    }

    protected void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    /**
     * @return the side that won the game, or {@code null} if the game is still
     * going or it was a draw.
     **/
    public Side getWinner() {
        return gameWinner;
    }
}