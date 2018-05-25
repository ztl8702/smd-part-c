package mycontroller.common;

import utilities.Coordinate;
import world.WorldSpatial;

import static world.WorldSpatial.Direction.*;

/**
 * Physics/Math helper methods used throughout our driving system
 *
 *
 */
public class Util {
    /**
     * Converts angle in degrees to WorldSpatial.Direction
     * @param currentAngle
     * @return
     */
    public static WorldSpatial.Direction angleToOrientation(float currentAngle) {
        if ( (-45 <= currentAngle && currentAngle <= 45) || (currentAngle>=360-45) ) {
            return EAST;
        } else if ( 45 < currentAngle && currentAngle <= 135) {
            return WorldSpatial.Direction.NORTH;
        } else if ( 135 < currentAngle && currentAngle <= 225) {
            return WorldSpatial.Direction.WEST;
        } else {
            return WorldSpatial.Direction.SOUTH;
        }
    }

    /**
     * Converts WorldSpatial.Direction to degrees
     * @param orientation
     * @return
     */
    public static float orientationToAngle(WorldSpatial.Direction orientation) {
        switch (orientation) {
            case EAST:
                return WorldSpatial.EAST_DEGREE_MIN;
            case WEST:
                return WorldSpatial.WEST_DEGREE;
            case NORTH:
                return WorldSpatial.NORTH_DEGREE;
            case SOUTH:
                return WorldSpatial.SOUTH_DEGREE;
            default:
                return WorldSpatial.EAST_DEGREE_MIN;
        }
    }


    /**
     * Infers the car's moving direction by using the current and last location
     * @param now
     * @param prev
     * @return
     */
    public static WorldSpatial.Direction inferDirection(Coordinate now, Coordinate prev) {
        int xDelta = now.x - prev.x;
        int yDelta = now.y - prev.y;
        if (xDelta == 1 && yDelta == 0) {
            // east
            return EAST;
        } else if (xDelta == -1 && yDelta == 0) {
            return WorldSpatial.Direction.WEST;
        } else if (xDelta == 0 && yDelta == 1) {
            return WorldSpatial.Direction.NORTH;
        } else if (xDelta == 0 && yDelta == -1) {
            return WorldSpatial.Direction.SOUTH;
        } else {
            // this should not happen at all.
            // but if it does there is something wrong with our path finder
            System.err.printf("[inferDirection] invalid inputs: (%d,%d)->(%d,%d)\n", prev.x,prev.y, now.x,now.y);
            System.err.printf("[inferDirection] This implies that there might be some " +
                    "bugs in the PathFinder\n", prev.x,prev.y, now.x,now.y);
            // Default to EAST
            return EAST;
        }
    }

    /**
     * Next tile in direction
     * @return
     */
    public static Coordinate getTileAhead(Coordinate startingPosition, WorldSpatial.Direction startingDirection) {
        // add the next tile in direction
        Coordinate nextLocationInDirection = null;
        switch (startingDirection) {
            case EAST:
                nextLocationInDirection = new Coordinate(startingPosition.x + 1, startingPosition.y);
                break;
            case WEST:
                nextLocationInDirection = new Coordinate(startingPosition.x - 1, startingPosition.y);
                break;
            case NORTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y + 1);
                break;
            case SOUTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y - 1);
                break;
        }
        return nextLocationInDirection;
    }

    /**
     * Next tile in reverse direction
     * @return
     */
    public static Coordinate getTileBehind(Coordinate currentPosition, WorldSpatial.Direction currentOrientation) {
        return getTileAhead(currentPosition, getOppositeOrientation(currentOrientation));
    }

    public static WorldSpatial.Direction getOppositeOrientation(WorldSpatial.Direction orientation) {
        if (orientation == null) {
            System.err.printf("[getOppositeOrientation] orientation is null!\n");
            // default to east
            return EAST;
        }
        switch (orientation) {
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            default:
                System.err.printf("[getOppositeOrientation] invalid orientation.\n");
                return EAST;
        }
    }

}


