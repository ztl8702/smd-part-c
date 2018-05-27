/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;
import utilities.Coordinate;

import java.security.InvalidParameterException;

/**
 * An composite AutoPilot that knows how to go from tile A to tile B by going straight forward.
 *
 * Under the hood, it uses MainSpeedAutoPilot to control the speed,
 * and ReCentreAutoPilot to make sure the car travels on the centre line and avoids walls
 */
public class ForwardToAutoPilot extends AutoPilotBase {

    /**
     * How much deviate from the centre line should we start to recentre the car.
     *
     * Should be set to slightly higher than the WALL_BUFFER, but less than 2*WALL_BUFFER
     */
    private static double RECENTER_EPS = 0.22;

    /**
     * Are we moving horizontally or vertically?
     */
    public enum TrackingAxis {
        X, Y
    }

    private enum State {
        Idle, On, Recentering, Finished
    }

    private TrackingAxis trackingAxis;
    private float fromPos, toPos, otherAxis;
    private State state;
    private float targetSpeed;

    private AutoPilot recentringAutoPilot = null;
    private AutoPilot mainTainSpeedAutoPilot = null;

    public ForwardToAutoPilot(MapManager mapManager, Coordinate from, Coordinate to, float targetSpeed) {
        super(mapManager);
        // Either x or y position of the tiles must be identical,
        // i.e. we only allow moving horizontally or vertically.
        if (from.x == to.x) {
            // Moving along y axis
            trackingAxis = TrackingAxis.Y;
            fromPos = from.y;
            toPos = to.y;
            otherAxis = from.x;
        } else if (from.y == to.y) {
            // Moving along x axis
            trackingAxis = TrackingAxis.X;
            fromPos = from.x;
            toPos = to.x;
            otherAxis = from.y;
        } else {
            throw new InvalidParameterException();
        }

        // offset the end point a little
        // to let the next AutoPilot take over earlier
        float offset = Math.signum(toPos - fromPos) * 0.1f;
        toPos -= offset;

        state = State.Idle;
        this.targetSpeed = targetSpeed;
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo car) {
        Coordinate coord = new Coordinate(car.getTileX(), car.getTileY());
        switch (state) {
            case Idle:
                if (
                        (trackingAxis == TrackingAxis.X
                                && (inRange(car.getX(), fromPos, toPos)
                                || coord.x == Math.round(fromPos)
                        )
                                && coord.y == Math.round(otherAxis))
                                ||
                                (trackingAxis == TrackingAxis.Y
                                        && (inRange(car.getY(), fromPos, toPos)
                                        || coord.y == Math.round(fromPos)
                                )
                                        && coord.x == Math.round(otherAxis))) {
                    // if we are in between fromPos and toPos,
                    // and that our location on the other axis is correct

                    // This means this AutoPilot can start performing its duty!

                    //if (car.getOrientation() == theOrientation){
                    changeState(State.On);
                    //}
                }
                break;
            case On:
                if ((trackingAxis == TrackingAxis.X
                        && !(inRange(car.getX(), fromPos, toPos)
                        || coord.x == Math.round(fromPos)))
                        || (trackingAxis == TrackingAxis.Y
                        && !(inRange(car.getY(), fromPos, toPos)
                        || coord.y == Math.round(fromPos)))
                        ) {
                    // if we have travelled past the toPos (target position),
                    // then this AutoPilot can retire
                    changeState(State.Finished);
                }

                // Check if we should change to ReCentring State
                if (car.getSpeed() > 1.0) {
                    Coordinate nextCell = Util.getTileAhead(car.getCoordinate(), car.getOrientation());
                    if (nextCell == null) {
                        nextCell = car.getCoordinate();
                    }
                    if (trackingAxis == TrackingAxis.X) {
                        double newCentreLineY = getCentreLineY(nextCell.x, nextCell.y);
                        if (!mapManager.isWall(nextCell.x, nextCell.y)
                                && Math.abs(car.getY() - newCentreLineY) > RECENTER_EPS) {
                            // if we have diverted from centre line (Y)
                            changeState(State.Recentering);
                            Logger.printWarning("==============", "ReCentre AP Takes over");
                            mainTainSpeedAutoPilot = AutoPilotFactory.maintainSpeed(car.getSpeed());
                            recentringAutoPilot = AutoPilotFactory.recentre(ReCentreAutoPilot.CentringAxis.Y,
                                    (float) newCentreLineY);
                        }
                    } else if (trackingAxis == TrackingAxis.Y) {
                        double newCentreLineX = getCentreLineX(nextCell.x, nextCell.y);
                        if (!mapManager.isWall(nextCell.x, nextCell.y)
                                && Math.abs(car.getX() - newCentreLineX) > RECENTER_EPS) {
                            // if we have diverted from centre line (X)
                            changeState(State.Recentering);
                            Logger.printWarning("==============", "ReCentre AP Takes over");
                            mainTainSpeedAutoPilot = AutoPilotFactory.maintainSpeed(car.getSpeed());
                            recentringAutoPilot = AutoPilotFactory.recentre(ReCentreAutoPilot.CentringAxis.X,
                                    (float) newCentreLineX);
                        }
                    }
                }


                break;
            case Recentering:
                if (this.recentringAutoPilot.canBeSwappedOut()) {
                    // if the recentering is done
                    changeState(State.On);
                    Logger.printWarning("==============", "ReCentre AP Thrown out!!!!");
                    recentringAutoPilot = null;
                }
            case Finished:
                break;
        }

        // after calculating state change
        // produce the output (ActuatorAction)
        switch (state) {
            case Idle:
                return ActuatorAction.nothing();
            case On:
                double d = getDistanceToTarget(car.getX(), car.getY());
                double speedLimit = getSpeedLimit(d - delta * car.getSpeed() - 0.03, targetSpeed);
                Logger.printInfo("ForwardToAutoPilot", String.format("speedLimit=%.5f\n", speedLimit));
                mainTainSpeedAutoPilot = AutoPilotFactory.maintainSpeed((float) speedLimit);
                return mainTainSpeedAutoPilot.handle(delta, car);
            case Recentering:
                ActuatorAction speedOps = mainTainSpeedAutoPilot.handle(delta, car);
                speedOps.backward = false;
                Logger.printInfo("ForwardToAutoPilot", String.format("recentre %s\n", recentringAutoPilot));
                return ActuatorAction.combine(speedOps, recentringAutoPilot.handle(delta, car));
            case Finished:
                if (Math.abs(car.getSpeed() - targetSpeed) < 0.1f) {
                    return ActuatorAction.nothing();
                } else {
                    mainTainSpeedAutoPilot = AutoPilotFactory.maintainSpeed(targetSpeed);
                    return mainTainSpeedAutoPilot.handle(delta, car);
                }

            default:
                return ActuatorAction.nothing();
        }
    }

    /**
     * Whether x is in range [a,b] or [b,a]
     *
     * @param x
     * @param a
     * @param b
     * @return
     */
    private boolean inRange(double x, double a, double b) {
        double upper = Math.max(a, b);
        double lower = Math.min(a, b);
        return lower <= x && x <= upper;
    }

    @Override
    public String toString() {
        return "ForwardToAutoPilot{" + "fromPos=" + fromPos + ", toPos="
                + toPos + ", state=" + state + ", targetSpeed="
                + targetSpeed + '}';
    }

    @Override
    public boolean canTakeCharge() {
        switch (state) {
            case Idle:
                return false;
            case On:
            case Recentering:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean canBeSwappedOut() {
        if (state == State.Recentering || state == State.On) {
            return false;
        } else {
            return true;
        }
    }

    private void changeState(State newState) {
        if (newState != state) {
            state = newState;
        }
    }

    /**
     * How far are we from the target position.
     */
    private double getDistanceToTarget(double x, double y) {
        switch (this.trackingAxis) {
            case X:
                return Math.abs(x - toPos);
            case Y:
                return Math.abs(y - toPos);
        }
        return 0;
    }

    /**
     * Gets what the current speed should be, given that we are some distance away
     * from our target position, and that we need to have a given target speed when
     * reaching the target position.
     */
    private double getSpeedLimit(double distanceFromTarget, double speedTarget) {
        distanceFromTarget = Math.max(0.0, distanceFromTarget);
        return Math.min(
                Util.MAX_CRUISING_SPEED,
                Math.sqrt(2.0 * Util.DECELERATION * distanceFromTarget + speedTarget * speedTarget)
        );
    }
}
