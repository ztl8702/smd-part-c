package mycontroller.autopilot;

import utilities.Coordinate;
import world.Car;

import javax.sound.midi.Track;
import java.security.InvalidParameterException;

public class ForwardToAutoPilot extends BaseAutoPilot {
    private static float CRUISING_SPEED = 5.0f;
    private static float DECELERATION = 1.0f;

    public enum TrackingAxis {X, Y}
    public enum State {Idle, On, Finished}

    private TrackingAxis trackingAxis;
    private float fromPos, toPos;
    private State state;
    private float targetSpeed;


    public ForwardToAutoPilot(Coordinate from, Coordinate to, float targetSpeed ) {

        if (from.x == to.x) {
            // tracking y
            trackingAxis = TrackingAxis.Y;
            fromPos = from.y;
            toPos = to.y;
        } else if (from.y == to.y) {
            trackingAxis = TrackingAxis.X;
            fromPos = from.x;
            toPos = to.x;
        } else {
            throw new InvalidParameterException();
        }
        state = State.Idle;
        this.targetSpeed = targetSpeed;
    }

    @Override
    public AutoPilotAction handle(float delta, Car car) {
        switch (state) {
            case Idle:
                if ((trackingAxis==TrackingAxis.X && (int)car.getX() == (int)fromPos)
                        || (trackingAxis==TrackingAxis.Y && (int)car.getY() == (int)fromPos)) {
                    changeState(State.On);
                }
                break;
            case On:
                if ((trackingAxis == TrackingAxis.X && !(inRange(car.getX(), fromPos,toPos)))
                        || (trackingAxis == TrackingAxis.Y && !(inRange(car.getY(), fromPos,toPos)))) {
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
                double speedLimit = getSpeedLimit(d-delta*car.getSpeed()-0.03, targetSpeed);
                System.out.printf("speedLimit=%.5f\n", speedLimit);
                AutoPilot ap = new MaintainSpeedAutoPilot((float)speedLimit);
                return ap.handle(delta,car);
            case Finished:
                if (Math.abs(car.getSpeed() - targetSpeed) < 0.1f) {
                    return AutoPilotAction.nothing();
                } else {
                    AutoPilot ap2 = new MaintainSpeedAutoPilot(targetSpeed);
                    return ap2.handle(delta,car);
                }

            default:
                return AutoPilotAction.nothing();
        }
    }

    private boolean inRange(double x, double a, double b) {
        double upper = Math.max(a,b);
        double lower = Math.min(a,b);
        return lower <= x && x<= upper;

    }

    @Override
    public String toString() {
        return "ForwardToAutoPilot{" +
                "fromPos=" + fromPos +
                ", toPos=" + toPos +
                ", state=" + state +
                ", targetSpeed=" + targetSpeed +
                '}';
    }

    @Override
    public boolean canTakeCharge() {
        switch (state){
            case Idle:
                return false;
            case On:
                return true;
            default:
                return false;
        }
    }
    private void changeState(State newState) {
        if (newState !=state) {
            state = newState;
        }
    }
    private double getDistanceToTarget(double x, double y) {
        switch (this.trackingAxis){
            case X:
                return Math.abs(x - toPos);
            case Y:
                return Math.abs(y-toPos);
        }
        return 0;
    }

    private double getStoppingDistance(double speedFrom, double speedTo) {
        return (speedFrom*speedFrom - speedTo*speedTo)/(2.0*DECELERATION);
    }

    private double getSpeedLimit(double distanceFromTarget, double speedTarget) {
        distanceFromTarget = Math.max(0.0, distanceFromTarget);
        return Math.min(CRUISING_SPEED, Math.sqrt(2.0*DECELERATION*distanceFromTarget + speedTarget*speedTarget));
    }
}
