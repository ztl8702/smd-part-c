package mycontroller.autopilot;

import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;
import world.WorldSpatial.RelativeDirection;
import world.WorldSpatial.Direction;

import java.security.InvalidParameterException;

public class TurningAutoPilot extends BaseAutoPilot {

    // TODO: Handle situation where the MAINTAIN_SPEED cannot be reached before
    // turning
    public static final float MAINTAIN_SPEED = 2f;

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
    // TODO: Check if WorldSpatial.Direction and WorldSpatial.RelativeDirection is
    // in the interface that we can use
    private Direction fromDirection;
    private Direction toDirection;
    private RelativeDirection turningMode;

    public TurningAutoPilot(Coordinate fromTile, Coordinate toTile, RelativeDirection turningMode) {
        // prototype, only support one turning type
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

        // let someone else care about the speed
        maintainSpeedOpt = new MaintainSpeedAutoPilot(MAINTAIN_SPEED);
        state = State.Waiting;
    }

    @Override
    public AutoPilotAction handle(float delta, Car car) {
        Coordinate coord = new Coordinate(car.getPosition());
        System.out.printf("toTileX=%d centreX=%f d=%f beforeTurn=%f currentX=%f\n", toTile.x,
                this.getCentreLineX(toTile.x), d(), this.getCentreLineX(toTile.x) - d(), car.getX());

        switch (this.state) {
        case Waiting:
            if (reachedBufferArea(coord)) {
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
        AutoPilotAction speedOpt = this.maintainSpeedOpt.handle(delta, car);

        switch (state) {
        case Waiting:
            return AutoPilotAction.nothing();
        case ReachTurningSpeed:
            return speedOpt;
        case StartTurning:
            AutoPilotAction output;
            if (turningMode == RelativeDirection.LEFT) {
                output = AutoPilotAction.combine(speedOpt, AutoPilotAction.turnLeft());
            } else {
                output = AutoPilotAction.combine(speedOpt, AutoPilotAction.turnRight());
            }
            // Overwrite the backward attribute:
            // We should never reverse+turn at the same time, otherwise the turning
            // trajectory will
            // be weird.
            output.backward = false;
            return output;
        case FinishedTurning:
            return speedOpt;
        default:
            return AutoPilotAction.nothing();
        }
    }

    private boolean reachedBufferArea(Coordinate coord) {
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

    private boolean reachedTurningPoint(float x, float y) {
        switch (fromDirection) {
        case WEST:
            return x <= this.getCentreLineX(toTile.x) + d();
        case EAST:
            return x >= this.getCentreLineX(toTile.x) - d();
        case NORTH:
            return y >= this.getCentreLineY(toTile.y) - d();
        case SOUTH:
            return y <= this.getCentreLineY(toTile.y) + d();
        default:
            return false;
        }

    }

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
        if (this.state == State.StartTurning) {
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
        return (6.0 / 5.0 / Math.PI) * (double) (MAINTAIN_SPEED);
    }

    @Override
    public String toString() {
        return "TurningOperator [fromTile=" + fromTile + ", toTile=" + toTile + ", state=" + this.state + "]";
    }

}
