package mycontroller.autopilot;

import utilities.Coordinate;
import world.Car;

import java.security.InvalidParameterException;

/**
 * An AutoPilot that knows how to go from tile A to tile B going straight forward.
 * 
 */
public class ForwardToAutoPilot extends BaseAutoPilot {
    private static float CRUISING_SPEED = 5.0f;
    /**
     * Our (estimated) de-celeration due to braking. The lower the value, the earlier the car starts braking,
     * but the risk of overruning will also be lower.
     */
    private static float DECELERATION = 1.0f;

    public enum TrackingAxis {
        X, Y
    }

    public enum State {
        Idle, On, Finished
    }

    private TrackingAxis trackingAxis;
    private float fromPos, toPos, otherAxis;
    private State state;
    private float targetSpeed;

    public ForwardToAutoPilot(Coordinate from, Coordinate to, float targetSpeed) {
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
    public AutoPilotAction handle(float delta, Car car) {
        Coordinate coord = new Coordinate(car.getPosition());
        switch (state) {
        case Idle:
            if ((trackingAxis == TrackingAxis.X && inRange(car.getX(), fromPos, toPos)
                    && coord.y == Math.round(otherAxis))
                    || (trackingAxis == TrackingAxis.Y && inRange(car.getY(), fromPos, toPos)
                            && coord.x == Math.round(otherAxis))) {
                changeState(State.On);
            }
            break;
        case On:
            if ((trackingAxis == TrackingAxis.X && !(inRange(car.getX(), fromPos, toPos)))
                    || (trackingAxis == TrackingAxis.Y && !(inRange(car.getY(), fromPos, toPos)))) {
                changeState(State.Finished);
            }
            break;
        case Finished:
            break;
        }

        switch (state) {
        case Idle:
            return AutoPilotAction.nothing();
        case On:
            double d = getDistanceToTarget(car.getX(), car.getY());
            double speedLimit = getSpeedLimit(d - delta * car.getSpeed() - 0.03, targetSpeed);
            System.out.printf("speedLimit=%.5f\n", speedLimit);
            AutoPilot ap = new MaintainSpeedAutoPilot((float) speedLimit);
            return ap.handle(delta, car);
        case Finished:
            if (Math.abs(car.getSpeed() - targetSpeed) < 0.1f) {
                return AutoPilotAction.nothing();
            } else {
                AutoPilot ap2 = new MaintainSpeedAutoPilot(targetSpeed);
                return ap2.handle(delta, car);
            }

        default:
            return AutoPilotAction.nothing();
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
            return true;
        default:
            return false;
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
