package mycontroller.autopilot;

import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;

import java.security.InvalidParameterException;

/**
 * An AutoPilot that knows how to go from tile A to tile B going straight forward.
 * 
 */
public class ForwardToAutoPilot extends AutoPilotBase {
    private static float CRUISING_SPEED = 5.0f;
    /**
     * Our (estimated) deceleration due to braking. The lower the value, the earlier the car starts braking,
     * but the risk of overruning will also be lower.
     */
    private static float DECELERATION = 1.0f;

    private static double RECENTER_EPS = 0.02;

    public enum TrackingAxis {
        X, Y
    }

    public enum State {
        Idle, On, Recentering, Finished
    }

    private TrackingAxis trackingAxis;
    private float fromPos, toPos, otherAxis;
    private State state;
    private float targetSpeed;
    private AutoPilot recentringAutoPilot = null;
    private AutoPilot mainTainSpeedAutoPilot = null;

    public ForwardToAutoPilot(MapManagerInterface mapManager, Coordinate from, Coordinate to, float targetSpeed) {
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
        state = State.Idle;
        this.targetSpeed = targetSpeed;
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo car) {
        Coordinate coord = new Coordinate(car.getTileX(), car.getTileY());
        switch (state) {
        case Idle:
            if ((trackingAxis == TrackingAxis.X && inRange(car.getX(), fromPos, toPos)
                    && coord.y == Math.round(otherAxis))
                    || (trackingAxis == TrackingAxis.Y && inRange(car.getY(), fromPos, toPos)
                            && coord.x == Math.round(otherAxis))) {
                //if (car.getOrientation() == theOrientation){
                   changeState(State.On);
                //}
            }
            break;
        case On:
            if ((trackingAxis == TrackingAxis.X && !(inRange(car.getX(), fromPos, toPos)))
                    || (trackingAxis == TrackingAxis.Y && !(inRange(car.getY(), fromPos, toPos)))) {
                changeState(State.Finished);
            }

            if (car.getSpeed() > 1.0) {
                if (trackingAxis == TrackingAxis.X) {
                    double newCentreLineY = getCentreLineY(car.getTileX(), car.getTileY());
                    if (Math.abs(car.getY() - newCentreLineY ) > RECENTER_EPS) {
                        changeState(State.Recentering);
                        mainTainSpeedAutoPilot = new MaintainSpeedAutoPilot(mapManager, (float) car.getSpeed());
                        recentringAutoPilot = new ReCentreAutoPilot(mapManager, ReCentreAutoPilot.CentringAxis.Y, (float)newCentreLineY);
                    }
                } else if (trackingAxis == TrackingAxis.Y) {
                    double newCentreLineX = getCentreLineX(car.getTileX(), car.getTileY());
                    if (Math.abs(car.getX() - newCentreLineX) > RECENTER_EPS ){
                        changeState(State.Recentering);
                        mainTainSpeedAutoPilot = new MaintainSpeedAutoPilot(mapManager, (float) car.getSpeed());
                        recentringAutoPilot = new ReCentreAutoPilot(mapManager, ReCentreAutoPilot.CentringAxis.X, (float)newCentreLineX);
                    }
                }
            }


            break;
        case Recentering:
            if (this.recentringAutoPilot.canBeSwappedOut()) {
                changeState(State.On);
                recentringAutoPilot = null;
            }
        case Finished:
            break;
        }



        switch (state) {
        case Idle:
            return ActuatorAction.nothing();
        case On:
            double d = getDistanceToTarget(car.getX(), car.getY());
            double speedLimit = getSpeedLimit(d - delta * car.getSpeed() - 0.03, targetSpeed);
            if (DEBUG_AUTOPILOT) System.out.printf("speedLimit=%.5f\n", speedLimit);
            mainTainSpeedAutoPilot= new MaintainSpeedAutoPilot(mapManager, (float) speedLimit);
            return mainTainSpeedAutoPilot.handle(delta, car);
        case Recentering:
            ActuatorAction speedOps = mainTainSpeedAutoPilot.handle(delta, car);
            speedOps.backward = false;
            System.out.printf("\t%s\n", recentringAutoPilot);
            return ActuatorAction.combine(speedOps, recentringAutoPilot.handle(delta,car));
        case Finished:
            if (Math.abs(car.getSpeed() - targetSpeed) < 0.1f) {
                return ActuatorAction.nothing();
            } else {
                mainTainSpeedAutoPilot = new MaintainSpeedAutoPilot(mapManager, targetSpeed);
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
        return "ForwardToAutoPilot{" + "fromPos=" + fromPos + ", toPos=" + toPos + ", state=" + state + ", targetSpeed="
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
        if (state == State.Recentering) {
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

    private double getStoppingDistance(double speedFrom, double speedTo) {
        return (speedFrom * speedFrom - speedTo * speedTo) / (2.0 * DECELERATION);
    }

    /**
     * Gets what the current speed should be, given that we are some distance away
     * from our target position, and that we need to have a given target speed when
     * reaching the target position.
     */
    private double getSpeedLimit(double distanceFromTarget, double speedTarget) {
        distanceFromTarget = Math.max(0.0, distanceFromTarget);
        return Math.min(CRUISING_SPEED, Math.sqrt(2.0 * DECELERATION * distanceFromTarget + speedTarget * speedTarget));
    }
}
