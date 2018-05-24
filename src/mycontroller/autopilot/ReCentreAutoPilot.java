package mycontroller.autopilot;

import world.Car;
import world.World;
import world.WorldSpatial;

/**
 * Recentres a moving car to a certain position along the X or Y axis.
 */
public class ReCentreAutoPilot extends BaseAutoPilot {
    public enum CentringAxis {
        X, Y
    }

    public enum State {
        Idle, TurningA, GoingStraight, TurningBack, Finished
    }

    private static double MAX_TURNING_ANGLE = 45;
    private static double TURNING_EPS = 0.05;

    private CentringAxis axis;
    private State state;
    private float target;
    private WorldSpatial.Direction orientation;
    private float originalAngle;
    private WorldSpatial.RelativeDirection turningMode;

    private SensorInfo lastInfo = null;

    public ReCentreAutoPilot(CentringAxis axis, float target) {
        this.axis = axis;
        state = State.Idle;
        this.target = target;
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo car) {
        lastInfo = car;
        double x = car.getX();
        double y = car.getY();
        double angle = car.getAngle();

        switch (state) {
            case Idle:
                if (canStartTurning(car)) {
                    orientation = car.getOrientation();
                    originalAngle = car.getAngle();

                    turningMode = getTurningMode(orientation, getPosOnCentringAxis(car));
                    changeState(State.TurningA);

                }

                break;
            case TurningA:
                if (shouldTurnBack(car)) {
                    changeState(State.TurningBack);
                } else if (angleDifference(originalAngle, car.getAngle()) >= MAX_TURNING_ANGLE) {
                    changeState(State.GoingStraight);
                }
                break;
            case GoingStraight:
                if (shouldTurnBack(car)) {
                    changeState(State.TurningBack);
                }
                break;
            case TurningBack:
                if (angleDifference(originalAngle, car.getAngle()) <= TURNING_EPS) {
                    changeState(State.Finished);
                }
                break;

        }

        // actions
        ActuatorAction actions = ActuatorAction.nothing();
        switch (state) {
            case Idle:
                break;
            case TurningA:
                if (this.turningMode == WorldSpatial.RelativeDirection.LEFT) {
                    actions = ActuatorAction.turnLeft();
                } else {
                    actions = ActuatorAction.turnRight();
                }
                break;
            case TurningBack:
                if (this.turningMode == WorldSpatial.RelativeDirection.LEFT) {
                    actions = ActuatorAction.turnRight();
                } else {
                    actions = ActuatorAction.turnLeft();
                }
                break;

        }
        return actions;
    }


    private boolean canStartTurning(SensorInfo car) {
        return true;
    }
    private double angleDifference(double a, double b) {
        double diff = Math.abs(a-b);
        if (diff > 180) diff = 180.0 -diff;
        return diff;
    }


    private WorldSpatial.RelativeDirection getTurningMode(WorldSpatial.Direction orientation, float posOnAxis) {
        if (this.axis == CentringAxis.X) {
            if (orientation == WorldSpatial.Direction.NORTH) {
                return (target > posOnAxis) ? WorldSpatial.RelativeDirection.RIGHT : WorldSpatial.RelativeDirection.LEFT;
            } else if (orientation == WorldSpatial.Direction.SOUTH) {
                return (target > posOnAxis) ? WorldSpatial.RelativeDirection.LEFT : WorldSpatial.RelativeDirection.RIGHT;
            }
        } else {
            if (orientation == WorldSpatial.Direction.EAST) {
                return (target > posOnAxis) ? WorldSpatial.RelativeDirection.LEFT : WorldSpatial.RelativeDirection.RIGHT;
            } else if (orientation == WorldSpatial.Direction.WEST) {
                return (target > posOnAxis) ? WorldSpatial.RelativeDirection.RIGHT : WorldSpatial.RelativeDirection.LEFT;
            }
        }

        return null;    // invalid configuration, should not turn
    }

    private float getPosOnCentringAxis(SensorInfo carInfo) {
        switch (this.axis) {
            case X:
                return carInfo.getX();

            case Y:
                return carInfo.getY();
        }
        return 0;
    }

    private double getDistanceForTurningBack(double speed, double thetaRad) {
        // integrate sin(5*pi/6*x) from 0 to a/(5*pi/6)

        return -6.0/5.0/Math.PI * (Math.cos(thetaRad)-1.0) * speed;
    }

    private boolean shouldTurnBack(SensorInfo carInfo) {

        double distanceFromTarget = Math.abs(getPosOnCentringAxis(carInfo) - target);

        double angleDiffRad = angleDifference(originalAngle, carInfo.getAngle())* Math.PI/180.0;
        double distanceNeededForTurningBack = getDistanceForTurningBack(carInfo.getSpeed(),angleDiffRad);

        return distanceNeededForTurningBack >= distanceFromTarget; // +DISTANCE MARGIN
    }

    @Override
    public boolean canBeSwappedOut() {
        if (state == State.TurningBack || state == State.TurningA || state == State.GoingStraight) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean canTakeCharge() {
        if (state == State.TurningBack || state == State.TurningA || state == State.GoingStraight) {
            return true;
        } else {
            return false;
        }

    }

    private void changeState(State newState) {
        if (newState != state) {
            state = newState;
        }
    }

    @Override
    public String toString() {
        return "ReCentreAutoPilot{" +
                "axis=" + axis +
                ", state=" + state +
                ", ["+ (lastInfo!=null ? getPosOnCentringAxis(lastInfo) : "?") +"->"+ target +
                "], TurningMode= " +turningMode+"}";
    }

    /**
     * Gets the distance from the target centre link at which we need to start
     * turing back.
     *
     * @param theta turning angle (delta from the target orientation) in radians
     * @return
     */
    private double d(double theta) {
        // Some calculus
        // integrate sin(a-a*6/5/pi*x) from 0 to a*6/5/pi

        return 5.0 * Math.PI * (Math.cos(theta - (36.0 * theta * theta) / (25.0 * Math.PI * Math.PI)) - Math.cos(theta))
                / (6.0 * theta);
    }
}
