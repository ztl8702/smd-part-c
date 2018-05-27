/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;

/**
 * A simple AutoPilot that reverse the car a little bit.
 *
 * It is used when the car hit the wall unexpectedly or
 * when PathFinder cannot find a path.
 */
public class ReverseAutoPilot extends AutoPilotBase {

    private static final float REVERSE_DISTANCE = 0.7f;
    private enum State {Idle, Reversing, Stopping, Finished}
    private State state;

    /**
     * Starting location
     */
    private float startX, startY;


    public ReverseAutoPilot(MapManager mapManager) {
        super(mapManager);
        state = State.Idle;
    }


    @Override
    public ActuatorAction handle(float delta, SensorInfo carStatus) {

        switch (state) {
            case Idle:
                // records the starting location
                // so that we can calculate distance travelled later
                startX = carStatus.getX();
                startY = carStatus.getY();
                state = State.Reversing;
                break;
            case Reversing:
                double distanceTravelled = Util.eDis(carStatus.getX(), carStatus.getY(), startX, startY);
                if (distanceTravelled > REVERSE_DISTANCE) {
                    // ok the car has moved enough distance,
                    // and should be able to move again.

                    // this AutoPilot's job is done
                    state = State.Stopping;
                }
                break;
            case Stopping:
                if (carStatus.getSpeed()< Util.STOPPED_THRESHOLD){
                    state = State.Finished;
                }
                break;
        }


        switch (state) {
            case Reversing:
                return ActuatorAction.backward();
            case Stopping:
                return ActuatorAction.brake();
            default:
                return ActuatorAction.nothing();
        }
    }

    @Override
    public boolean canTakeCharge() {
        if (state != State.Finished) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeSwappedOut() {
        if (state == State.Reversing || state == State.Stopping) {
            return false;
        } else {
            return true;
        }
    }

}
