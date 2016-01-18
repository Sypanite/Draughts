package sypan.draughts.client;

import sypan.draughts.client.gui.AbstractGUIState;
import sypan.draughts.client.gui.state.MainMenuState;
import sypan.draughts.game.Game;
import sypan.draughts.game.player.Side;

import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;

/**
 * This class subclasses {@link ChaseCamera}.<p>
 *
 * It prevents the handling of mouse input - I control the camera in the code, and the player would interfere.
 *
 * @author Carl Linley
 **/
public class DraughtsCamera extends ChaseCamera {

    private enum CameraStatus {
        DESTROYED, IDLE, MOVING, ROAMING
    };

    private final float CAMERA_SPEED = 0.25f;
    private final int CAMERA_SLEEP_MS = 3, CAMERA_SLEEP_MS_FAST = 1;

    private final Client client;
    
    private CameraStatus cameraStatus;

    protected DraughtsCamera(Client client) {
        super(client.getCamera(), client.getGraphicalBoard().getBoardNode(), client.getInputManager());
        this.client = client;

        client.getFlyByCamera().setEnabled(false);
        setHideCursorOnRotate(false);
        setInvertVerticalAxis(false);
        setDefaultDistance(25);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
    }

    /**
     * Causes the camera to rotate slowly. This is used as a background for the
     * main menu {@link AbstractGUIState}.
     *
     * @param client - the game client.
     * @see MainMenuState
     **/
    protected void idleRoam(Client client) {
        cameraStatus = CameraStatus.ROAMING;

        final float ROAM_SPEED = CAMERA_SPEED / 50;

        setDefaultVerticalRotation(50 * FastMath.DEG_TO_RAD);

        client.getExecutor().submit(() -> {
            float currentHorizontal = getHorizontalRotation();
            
            while (cameraStatus == CameraStatus.ROAMING) {
                currentHorizontal += ROAM_SPEED;
                
                setDefaultHorizontalRotation(currentHorizontal * FastMath.DEG_TO_RAD);
                Thread.sleep(CAMERA_SLEEP_MS);
            }
            return null;
        });
    }

    /**
     * Rotates the camera to the specified side.
     *
     * @param game - the current game.
     * @param side - the side to rotate to, either {@code Side.BLACK] or {@code Side.WHITE}.
     *
     * @throws InterruptedException if the thread is interrupted.
     *
     */
    public void moveToSide(Game game, Side side) throws InterruptedException {
        cameraStatus = CameraStatus.MOVING;

        client.getExecutor().submit(() -> {
            float currentHorizontal = getHorizontalRotation() * FastMath.RAD_TO_DEG,
                  currentVertical = getVerticalRotation() * FastMath.RAD_TO_DEG,
                  targetHorizontal = (side.isPlaying() ? (game.getCurrentTurn() == Side.BLACK ? 90 : -90) : -180),
                  verticalAxis = (client.getConfig().overheadCamera() ? 90 : 50);
            
            long sleepTime = (client.getConfig().rotateQuickly() ? CAMERA_SLEEP_MS_FAST : CAMERA_SLEEP_MS);
            
            while (currentHorizontal != targetHorizontal || currentVertical != verticalAxis) {
                if ((game.getCurrentTurn() == Side.WHITE ? targetHorizontal - currentHorizontal > 0.25f : targetHorizontal - currentHorizontal < 0.25f)) {
                    currentHorizontal = targetHorizontal;
                }
                else {
                    currentHorizontal += (currentHorizontal < targetHorizontal ? CAMERA_SPEED : -CAMERA_SPEED);
                }
                
                if (verticalAxis - currentVertical < 0.25f) {
                    currentVertical = verticalAxis;
                }
                else {
                    currentVertical += (currentVertical < verticalAxis ? rotationSpeed : -rotationSpeed);
                }
                
                setDefaultHorizontalRotation(currentHorizontal * FastMath.DEG_TO_RAD);
                setDefaultVerticalRotation(currentVertical * FastMath.DEG_TO_RAD);
                Thread.sleep(sleepTime);
            }
            cameraStatus = CameraStatus.IDLE;
            return null;
        });
    }

    public boolean isMoving() {
        return cameraStatus == CameraStatus.MOVING;
    }

    public void destroy() {
        cameraStatus = CameraStatus.DESTROYED;
    }
}
