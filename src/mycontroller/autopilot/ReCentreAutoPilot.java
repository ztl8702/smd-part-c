/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.common.Logger;
import mycontroller.mapmanager.MapManagerInterface;
import world.WorldSpatial;

/**
 * An AutoPilot that recentres a moving car to a certain position along the X or Y axis.
 * <p>
 * It works by first turn left/right a bit, move straight a bit, and then turning back.
 */
public class ReCentreAutoPilot extends AutoPilotBase {
    public enum CentringAxis {
        X, Y
    }

    public enum State {
        Idle, TurningA, GoingStraight, TurningBack, Finished
    }

    /**
     * Maximum speed when recentring begins
     */
    public static double MAX_SWITCH_LANE_SPEED = 2.0f;

    private static double MAX_TURNING_ANGLE = 30;
    private static double TURNING_EPS = 0.05; // angle
    private static double DISTANCE_MARGIN = 0.01;

    private CentringAxis axis;
    private State state;
    private float target;
    private WorldSpatial.Direction orientation;
    private float originalAngle;
    private WorldSpatial.RelativeDirection turningMode;

    private SensorInfo lastInfo = null;

    public ReCentreAutoPilot(MapManagerInterface mapManager, CentringAxis axis, float target) {
        super(mapManager);
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

        if (car.getSpeed() > (float) MAX_SWITCH_LANE_SPEED) {
            // oops, this might be an issue
            // warn the programmer

            Logger.printWarning("ReCentreAutoPilot",
                    String.format("Overspeed, current speed=%.6f, limit=%.6f", car.getSpeed(), MAX_SWITCH_LANE_SPEED));

        }

        switch (state) {
            case Idle:
                if (canStartTurning(car)) {
                    orientation = car.getOrientation();
                    originalAngle = car.getAngle();

                    turningMode = getTurningMode(orientation, getPosOnCentringAxis(car));
                    if (turningMode != null) changeState(State.TurningA);

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

    /**
     * Calculates the absolute difference between two angles
     *
     * @param a
     * @param b
     * @return
     */
    private double angleDifference(double a, double b) {
        double diff = Math.abs(a - b);
        if (diff > 180) diff = 180.0 - diff;
        return diff;
    }


    /**
     * Infer which way we are turning
     *
     * @param orientation
     * @param posOnAxis
     * @return
     */
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

    /**
     * Gets the position of the car along the axis of moving direction
     *
     * @param carInfo
     * @return
     */
    private float getPosOnCentringAxis(SensorInfo carInfo) {
        switch (this.axis) {
            case X:
                return carInfo.getX();

            case Y:
                return carInfo.getY();
        }
        return 0;
    }

    /**
     * Estimates the distance we need to travel during "turning back"
     *
     * @param speed
     * @param thetaRad current angle difference in radians
     * @return
     */
    private double getDistanceForTurningBack(double speed, double thetaRad) {
        // integrate sin(5*pi/6*x) from 0 to a/(5*pi/6)

        // these number are results of calculus, doesn't make much sense to
        // make them static fields.

        return -6.0 / 5.0 / Math.PI * (Math.cos(thetaRad) - 1.0) * speed;
    }

    /**
     * Should we start turning back now?
     * @param carInfo
     * @return
     */
    private boolean shouldTurnBack(SensorInfo carInfo) {

        double distanceFromTarget = Math.abs(getPosOnCentringAxis(carInfo) - target);

        double angleDiffRad = angleDifference(originalAngle, carInfo.getAngle()) * Math.PI / 180.0;
        double distanceNeededForTurningBack = getDistanceForTurningBack(carInfo.getSpeed(), angleDiffRad);

        return distanceNeededForTurningBack >= distanceFromTarget - DISTANCE_MARGIN;
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
                ", [" + (lastInfo != null ? getPosOnCentringAxis(lastInfo) : "?") + "->" + target +
                "], TurningMode= " + turningMode + "}";
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
