/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import java.util.ArrayList;
import utilities.Coordinate;

/**
 * PathFinder(s) are the "brain" of our driving system.
 *
 * These are lgorithms that can find paths from A to B on a grid.
 *
 * PathFinders output a path as a list of coordinates.
 * They are not concerned with game physics and low level car actuators.
 * 
 * It is up to RouteCompiler and AutoPilots to understand and carry out
 * the path decided by PathFinder.
 */
public interface PathFinder {
    /**
     * @param startPosition The position to start the path from
     * @param goalPosition The position to end the path at
     * @param startingSpeed The speed of the car at the start of the path
     *                      If the speed is zero (< 0.1), e.g. the Car is starting
     *                      from stand still,
     *                      the output path must start from moving forward,
     *                      before the car cannot start turning with zero speed.
     * @param startingAngle The angle the car is facing (either in real or fictionally)
     *                      at the startingPosition
     * @return if there is no possible path, the PathFinder **must** return null
     */
    ArrayList<Coordinate> getPath(Coordinate startPosition, 
                                  Coordinate goalPosition, 
                                  float startingSpeed, 
                                  float startingAngle);
}
