package mycontroller.autopilot;

import mycontroller.common.Logger;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.RelativeDirection;
import world.WorldSpatial.Direction;

import java.security.InvalidParameterException;

public class TurningAutoPilot extends AutoPilotBase {

    // TODO: Handle situation where the MAINTAIN_SPEED cannot be reached before
    // turning
    /**
     * Max turning speed when turning followed by moving straight
     *
     * 1 2 3 4 5
     *         6
     *         7
     *         8 9 10
     */
    public static final double MAX_TURNING_SPEED = 2.0;
    /**
     * Max turning speed when turning followed by turning
     *
     * L-turn
     * 1 2 3
     *     4 5 6
     *
     * or
     *
     * width-2 U-turn
     * 1 2 3
     * 6 5 4
     *
     */
    public static final double MAX_TURNING_SPEED_U_TURN = 0.7;

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

    public TurningAutoPilot(MapManagerInterface mapManager,
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

        // let some other AutoPilot care about the speed
        maintainSpeedOpt = new MaintainSpeedAutoPilot(mapManager, (float)turningSpeed);
        state = State.Waiting;
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo car) {
        Coordinate coord = new Coordinate(car.getTileX(), car.getTileY());

        Logger.printInfo("TurningAutoPilot",
                String.format("toTileX=%d centreX=%f d=%f beforeTurn=%f currentX=%f\n", toTile.x,
                this.getCentreLineX(toTile.x,toTile.y), d(), this.getCentreLineX(toTile.x, toTile.y) - d(), car.getX()
                )
        );

        switch (this.state) {
            case Waiting:
                if (reachedBufferArea(coord, car.getOrientation())) {
                    changeState(State.ReachTurningSpeed);
                }
                break;
            case ReachTurningSpeed:
                if (reachedTurningPoint(car.getX(), car.getY())) {
                    changeState(State.StartTurning);
                }
                break;
            case StartTurning:
                float a = car.getAngle();
                if (reachedTargetAngle(a)) {
                    changeState(State.FinishedTurning);
                }
                break;

        }
        ActuatorAction speedOpt = this.maintainSpeedOpt.handle(delta, car);

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
     * Has the car reached the "buffer area", where we need to start adjusting the speed.
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
     * Has the car reached the position where we need to start applying turnLeft/turnRight
     *
     * @param x
     * @param y
     * @return
     */
    private boolean reachedTurningPoint(float x, float y) {
        double offset = 0.001;
        switch (fromDirection) {
            case WEST:
                return (double)x <= this.getCentreLineX(toTile.x, toTile.y) + d() - offset;
            case EAST:
                return (double)x >= this.getCentreLineX(toTile.x, toTile.y) - d() + offset;
            case NORTH:
                return (double)y >= this.getCentreLineY(toTile.x, toTile.y) - d() + offset;
            case SOUTH:
                return (double)y <= this.getCentreLineY(toTile.x, toTile.y) + d() - offset;
            default:
                return false;
        }

    }

    /**
     * Has the car turned to the desired orientation?
     *
     * @param a
     * @return
     */
    private boolean reachedTargetAngle(float a) {
        switch (toDirection) {
            case NORTH:
                return WorldSpatial.NORTH_DEGREE - 0.05f <= a && a <= WorldSpatial.NORTH_DEGREE + 0.05f;
            case SOUTH:
                return WorldSpatial.SOUTH_DEGREE - 0.05f <= a && a <= WorldSpatial.SOUTH_DEGREE + 0.05f;
            case WEST:
                return WorldSpatial.WEST_DEGREE - 0.05f <= a && a <= WorldSpatial.WEST_DEGREE + 0.05f;
            case EAST:
                return (WorldSpatial.EAST_DEGREE_MAX - 0.05f <= a && a <= WorldSpatial.EAST_DEGREE_MAX + 0.05f)
                        || (WorldSpatial.EAST_DEGREE_MIN - 0.05f <= a && a <= WorldSpatial.EAST_DEGREE_MIN + 0.05f);
            default:
                return false;
        }
    }

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
    private double d() {
        // This formula comes from a bit of calculus.
        return (6.0 / 5.0 / Math.PI) * (turningSpeed);
    }

    @Override
    public String toString() {
        return "TurningOperator [fromTile=" + fromTile + ", toTile=" + toTile + ", state=" + this.state + " turn " + fromDirection + "->" + toDirection + "]";
    }

}
