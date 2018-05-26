/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.common;

import utilities.Coordinate;
import world.WorldSpatial;

import static world.WorldSpatial.Direction.*;
import java.util.Arrays;
import java.util.List;

/**
 * Physics / Math helper methods and constants
 * used throughout our driving system
 */
public class Util {
    /**
     * Four directions, in anti-clockwise order
     */
	public static final List<Coordinate> ANTICLOCKWISE_DIRECTION = Arrays.asList(
            new Coordinate(0, 1),  //N
            new Coordinate(-1, 0), //W
            new Coordinate(0, -1), //S
            new Coordinate(1, 0)   //E
    );    

    /**
     * Max speed when cruising
     */
    public static float MAX_CRUISING_SPEED = 5.0f;
    
    /**
     * Our (estimated) deceleration due to braking. The lower the value, the earlier the car starts braking,
     * but the risk of overruning will also be lower.
     */
    public static float DECELERATION = 1.99f;
    
    /**
     * Our (estimated) aceleration due to applyForwardAcceleration. The lower the value, the earlier the car 
     * starts accelerating, but the risk of overruning will also be lower.
     */
    public static float ACCELERATION = 1.99f;

    /**
     * Converts angle in degrees to WorldSpatial.Direction
     *
     * @param currentAngle
     * @return
     */
    public static WorldSpatial.Direction angleToOrientation(float currentAngle) {
        if ((-45 <= currentAngle && currentAngle <= 45) || (currentAngle >= 360 - 45)) {
            return EAST;
        } else if (45 < currentAngle && currentAngle <= 135) {
            return NORTH;
        } else if (135 < currentAngle && currentAngle <= 225) {
            return WEST;
        } else {
            return SOUTH;
        }
    }

    /**
     * Converts WorldSpatial.Direction to angle degrees
     *
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
     * Converts WorldSpatial.Direction to delta change in (x,y). For example, EAST => (+1,0).
     * 
     * @param orientation
     * @return
     */
    public static Coordinate orientationToDelta(WorldSpatial.Direction orientation) {
        switch (orientation){
            case NORTH:
                return new Coordinate(0, 1);
            case WEST:
                return new Coordinate(-1, 0);
            case SOUTH:
                return new Coordinate(0, -1);
            case EAST:
                return new Coordinate(1, 0);
        }
        warn("orientationToDelta", "invalid argument");
        return new Coordinate(0,0);
    }

    /**
     * Infers the car's moving direction by using the current and last location
     *
     * @param now
     * @param prev
     * @return
     */
    public static WorldSpatial.Direction inferDirection(Coordinate now, Coordinate prev) {
        int xDelta = now.x - prev.x;
        int yDelta = now.y - prev.y;
        if (xDelta == 1 && yDelta == 0) {
            return EAST;
        } else if (xDelta == -1 && yDelta == 0) {
            return WEST;
        } else if (xDelta == 0 && yDelta == 1) {
            return NORTH;
        } else if (xDelta == 0 && yDelta == -1) {
            return SOUTH;
        } else {
            // this should not happen at all.
            // but if it does there is something wrong with our path finder
            warn("inferDirection",
                    String.format("invalid inputs: (%d,%d)->(%d,%d)", prev.x, prev.y, now.x, now.y));
            warn("inferDirection",
                    String.format("This implies that there might be some " +
                            "bugs in the PathFinder", prev.x, prev.y, now.x, now.y)
            );
            // Default to EAST
            return EAST;
        }
    }

    /**
     * Get the coordinate of the tile in infront of the car
     * 
     * @param startingPosition
     * @param startingDirection
     * @return
     */
    public static Coordinate getTileAhead(Coordinate startingPosition, WorldSpatial.Direction startingDirection) {
        return getTileAheadNth(startingPosition, startingDirection, 1);
    }

    /**
     * Next nth-tile in direction
     *
     * @return
     */
    public static Coordinate getTileAheadNth(Coordinate startingPosition, WorldSpatial.Direction startingDirection, int n) {
        // add the next tile in direction
        Coordinate nextLocationInDirection = null;
        switch (startingDirection) {
            case EAST:
                nextLocationInDirection = new Coordinate(startingPosition.x + n, startingPosition.y);
                break;
            case WEST:
                nextLocationInDirection = new Coordinate(startingPosition.x - n, startingPosition.y);
                break;
            case NORTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y + n);
                break;
            case SOUTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y - n);
                break;
        }
        return nextLocationInDirection;
    }

    /**
     * Get the coordinate of the tile behind the car
     * 
     * @param currentPosition
     * @param currentOrientation
     * @return
     */
    public static Coordinate getTileBehind(Coordinate currentPosition, WorldSpatial.Direction currentOrientation) {
        return getTileAhead(currentPosition, getOppositeOrientation(currentOrientation));
    }

    /**
     * Get the opposite orientation of where the car is currently facing
     * 
     * @param orientation
     * @return
     */
    public static WorldSpatial.Direction getOppositeOrientation(WorldSpatial.Direction orientation) {
        if (orientation == null) {
            warn("getOppositeOrientation", "orientation is null!");
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
                warn("getOppositeOrientation", "invalid orientation.");
                return EAST;
        }
    }

    /**
     * Get the orientation the car is facing after turning
     * 
     * @param original
     * @param turningMode
     * @return
     */
    public static WorldSpatial.Direction getTurnedOrientation(WorldSpatial.Direction original, WorldSpatial.RelativeDirection turningMode) {
        switch (turningMode) {
            case LEFT:
                switch (original){
                    case EAST:
                        return NORTH;
                    case NORTH:
                        return WEST;
                    case WEST:
                        return SOUTH;
                    case SOUTH:
                        return EAST;
                }
            case RIGHT:
                switch (original) {
                    case EAST:
                        return SOUTH;
                    case SOUTH:
                        return WEST;
                    case WEST:
                        return NORTH;
                    case NORTH:
                        return EAST;
                }
        }

        // Defensive coding: We should not reach here. If we do, there is something wrong elsewhere in the code.
        warn("getTurnedOrientation", "Invalid arguments!");
        return null;

    }

    /**
     * Return a copy of the coordinate
     * 
     * @param a the coordinate
     * @return
     */
    public static Coordinate cloneCoordinate(Coordinate a) {
        return new Coordinate(a.x, a.y);
    }

    /**
     * Gets the distance required for speed change. (deccelerating)
     * @param speedFrom
     * @param speedTo
     * @return
     */
    public static double getStoppingDistance(double speedFrom, double speedTo) {
        double result = (speedFrom * speedFrom - speedTo * speedTo) / (2.0 * (double)DECELERATION);
        if (result < 0) {
            warn("getStoppingDistance", "Negative distance! Check calling function for bugs.");
        }
        return result;
    }

    /**
     * Gets the distance required for speed change. (accelerating)
     * @param speedFrom
     * @param speedTo
     * @return
     */
    public static double getAccelerateDistance(double speedFrom, double speedTo) {
        double result = (speedTo * speedTo - speedFrom * speedFrom) / (2.0 * (double)ACCELERATION);
        if (result < 0) {
            warn("getAccelerateDistance", "Negative distance! Check calling function for bugs.");
        }
        return result;
    }

    /**
     * Manhattan distance
     * @return
     */
    public static int dis(Coordinate a, Coordinate b) {
        return (Math.abs(a.x-b.x)+Math.abs(a.y-b.y));
    }


    /**
     * Print warning message (for warnings internal to Util only)
     *
     * @param methodName
     * @param message
     */
    private static void warn(String methodName, String message) {
        Logger.printWarning("Util." + methodName, message);
    }

}
