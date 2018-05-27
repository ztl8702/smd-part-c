/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.RelativeDirection;
import world.WorldSpatial.Direction;

import java.security.InvalidParameterException;

public class TurningAutoPilot extends AutoPilotBase {

    private static final double TURNING_OVERRUN_DISTANCE = 0.001;
    private static final float ANGLE_EPS = 0.05f;

    private enum TurningType {
        EastToNorth, EastToSouth
    }

    private AutoPilot maintainSpeedOpt;

    private enum State {
        Waiting, ReachTurningSpeed, StartTurning, FinishedTurning
    }

    private State state;
    private Coordinate fromTile;
    private Coordinate toTile;
    private Direction fromDirection;
    private Direction toDirection;
    private RelativeDirection turningMode;


    private double turningSpeed;

    public TurningAutoPilot(MapManager mapManager,
                            Coordinate fromTile,
                            Coordinate toTile,
                            RelativeDirection turningMode,
                            double turningSpeed) {
        super(mapManager);
        this.turningSpeed = turningSpeed;

        // figure out which direction we are turning from and which direction we are turning to
        if (fromTile.x + 1 == toTile.x && fromTile.y + 1 == toTile.y && turningMode == RelativeDirection.LEFT) {
            fromDirection = Direction.EAST;
            toDirection = Direction.NORTH;
        } else if (fromTile.x + 1 == toTile.x && fromTile.y + 1 == toTile.y && turningMode == RelativeDirection.RIGHT) {
            fromDirection = Direction.NORTH;
            toDirection = Direction.EAST;
        } else if (fromTile.x + 1 == toTile.x && fromTile.y - 1 == toTile.y && turningMode == RelativeDirection.LEFT) {
            fromDirection = Direction.SOUTH;
            toDirection = Direction.EAST;
        } else if (fromTile.x + 1 == toTile.x && fromTile.y - 1 == toTile.y && turningMode == RelativeDirection.RIGHT) {
            fromDirection = Direction.EAST;
            toDirection = Direction.SOUTH;
        } else if (fromTile.x - 1 == toTile.x && fromTile.y - 1 == toTile.y && turningMode == RelativeDirection.LEFT) {
            fromDirection = Direction.WEST;
            toDirection = Direction.SOUTH;
        } else if (fromTile.x - 1 == toTile.x && fromTile.y - 1 == toTile.y && turningMode == RelativeDirection.RIGHT) {
            fromDirection = Direction.SOUTH;
            toDirection = Direction.WEST;
        } else if (fromTile.x - 1 == toTile.x && fromTile.y + 1 == toTile.y && turningMode == RelativeDirection.LEFT) {
            fromDirection = Direction.NORTH;
            toDirection = Direction.WEST;
        } else if (fromTile.x - 1 == toTile.x && fromTile.y + 1 == toTile.y && turningMode == RelativeDirection.RIGHT) {
            fromDirection = Direction.WEST;
            toDirection = Direction.NORTH;
        } else {
            throw new InvalidParameterException();
        }

        this.fromTile = fromTile;
        this.toTile = toTile;
        this.turningMode = turningMode;

        // let some other AutoPilot carInfoe about the speed
        maintainSpeedOpt = AutoPilotFactory.maintainSpeed((float)turningSpeed);
        state = State.Waiting;
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo carInfo) {
        Coordinate coord = new Coordinate(carInfo.getTileX(), carInfo.getTileY());

        Logger.printInfo("TurningAutoPilot",
                String.format("toTileX=%d centreX=%f d=%f beforeTurn=%f currentX=%f\n", toTile.x,
                this.getCentreLineX(toTile.x,toTile.y),
                        d(turningSpeed),
                        this.getCentreLineX(toTile.x, toTile.y) - d(turningSpeed), carInfo.getX()
                )
        );

        switch (this.state) {
            case Waiting:
                if (reachedBufferArea(coord, carInfo.getOrientation())) {
                    changeState(State.ReachTurningSpeed);
                } 
                break;
            case ReachTurningSpeed:
                // Handle situation where the turningSpeed cannot be reached
                if (Math.abs(carInfo.getSpeed() - turningSpeed)>0.01 && carInfo.getSpeed()>Util.MAX_TURNING_SPEED_U_TURN-0.01) {

                    double distanceToReachSpeed = carInfo.getSpeed() > turningSpeed ?
                            Util.getStoppingDistance(carInfo.getSpeed(), turningSpeed)
                            : Util.getAccelerateDistance(carInfo.getSpeed(), turningSpeed);
                    double distanceToTurn = d(turningSpeed);
                    double availableDistance =  distanceFromTarget(carInfo.getX(), carInfo.getY());
                    if (distanceToReachSpeed+distanceToTurn > availableDistance) {
                        Logger.printWarning("TurningAutoPilot", "I cannot reduce speed to turningSpeed!");
                        // if we can still do it using current speed?
                        if (d(carInfo.getSpeed())- TURNING_OVERRUN_DISTANCE >= availableDistance) {
                            Logger.printWarning("TurningAutoPilot",
                                    String.format("But we can still turn!, changing turingSpeed to %.5f", carInfo.getSpeed()));

                        } else {
                            Logger.printWarning("TurningAutoPilot","PANIC!!!!!");
                        }
                        turningSpeed = carInfo.getSpeed();
                        maintainSpeedOpt = AutoPilotFactory.maintainSpeed(carInfo.getSpeed());
                    }
                }
                
                if (reachedTurningPoint(carInfo.getX(), carInfo.getY(), Math.min(carInfo.getSpeed(),turningSpeed))) {
                    changeState(State.StartTurning);
                }
                break;
            case StartTurning:
                float a = carInfo.getAngle();
                if (reachedTargetAngle(a)) {
                    changeState(State.FinishedTurning);
                }
                break;

        }
        ActuatorAction speedOpt = this.maintainSpeedOpt.handle(delta, carInfo);

        switch (state) {
            case Waiting:
                return ActuatorAction.nothing();
            case ReachTurningSpeed:
                return speedOpt;
            case StartTurning:
                ActuatorAction output;
                if (turningMode == RelativeDirection.LEFT) {
                    output = ActuatorAction.combine(speedOpt, ActuatorAction.turnLeft());
                } else {
                    output = ActuatorAction.combine(speedOpt, ActuatorAction.turnRight());
                }
                // Overwrite the backward attribute:
                // We should never reverse+turn at the same time, otherwise the turning
                // trajectory will be weird.
                output.backward = false;
                return output;
            case FinishedTurning:
                return speedOpt;
            default:
                return ActuatorAction.nothing();
        }
    }

    /**
     * Has the carInfo reached the "buffer area", where we need to start adjusting the speed.
     *
     * @param coord
     * @return
     */
    private boolean reachedBufferArea(Coordinate coord, Direction currentOrientation) {
        if (fromDirection != currentOrientation) return false;
        switch (fromDirection) {
            case WEST:
                return coord.x <= fromTile.x + 2 && coord.y == fromTile.y;
            case EAST:
                return coord.x >= fromTile.x - 2 && coord.y == fromTile.y;
            case NORTH:
                return coord.y >= fromTile.y - 2 && coord.x == fromTile.x;
            case SOUTH:
                return coord.y <= fromTile.y + 2 && coord.x == fromTile.x;
            default:
                return false;
        }
    }

    /**
     * Has the carInfo reached the position where we need to start applying turnLeft/turnRight
     *
     * @param x
     * @param y
     * @return
     */
    private boolean reachedTurningPoint(float x, float y, double currentSpeed) {
        return distanceFromTarget(x,y) <=  d(currentSpeed) - TURNING_OVERRUN_DISTANCE;
    }

    /**
     * How far away are we from the position when we should stop turning at
     * @param x
     * @param y
     * @return
     */
    private double distanceFromTarget(double x, double y) {
        switch (fromDirection) {
            case WEST:
                return x - this.getCentreLineX(toTile.x, toTile.y);
            case EAST:
                return this.getCentreLineX(toTile.x, toTile.y) - x;
            case NORTH:
                return this.getCentreLineY(toTile.x, toTile.y) - y;
            case SOUTH:
                return y - this.getCentreLineY(toTile.x, toTile.y);
            default:
                Logger.printWarning("TurningAutoPilot", "something is wrong");
                return 0;
        }
    }

    /**
     * Has the carInfo turned to the desired orientation?
     *
     * @param a
     * @return
     */
    private boolean reachedTargetAngle(float a) {
        switch (toDirection) {
            case NORTH:
                return WorldSpatial.NORTH_DEGREE - ANGLE_EPS <= a && a <= WorldSpatial.NORTH_DEGREE + ANGLE_EPS;
            case SOUTH:
                return WorldSpatial.SOUTH_DEGREE - ANGLE_EPS <= a && a <= WorldSpatial.SOUTH_DEGREE + ANGLE_EPS;
            case WEST:
                return WorldSpatial.WEST_DEGREE - ANGLE_EPS <= a && a <= WorldSpatial.WEST_DEGREE + ANGLE_EPS;
            case EAST:
                return (WorldSpatial.EAST_DEGREE_MAX - ANGLE_EPS <= a
                        && a <= WorldSpatial.EAST_DEGREE_MAX + ANGLE_EPS)
                        || (WorldSpatial.EAST_DEGREE_MIN - ANGLE_EPS <= a
                        && a <= WorldSpatial.EAST_DEGREE_MIN + ANGLE_EPS);
            default:
                return false;
        }
    }

    /**
     * Shorthand function for changing the current state
     * @param newState
     */
    private void changeState(State newState) {
        if (this.state != newState) {
            this.state = newState;
        }
    }

    @Override
    public boolean canTakeCharge() {
        if (this.state == State.Waiting || this.state == State.FinishedTurning) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeSwappedOut() {
        if (this.state == State.StartTurning || this.state == State.ReachTurningSpeed) {
            return false;
        }
        return true;
    }

    /**
     * Gets the distances ahead of the target tiles' centre line at which we need to
     * start turning.
     */
    private double d(double speed) {
        // This formula comes from a bit of calculus.
        return (6.0 / 5.0 / Math.PI) * (speed);
    }

    @Override
    public String toString() {
        return "TurningOperator [fromTile=" + fromTile + ", toTile=" + toTile + ", state=" + this.state + " turn " + fromDirection + "->" + toDirection + "]";
    }

}
