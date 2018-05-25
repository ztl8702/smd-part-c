package mycontroller.pathfinder;

import java.util.ArrayList;
import utilities.Coordinate;

/**
 * PathFinder Interface
 *
 * Algorithms that can find paths from A to B on a grid
 *
 * PathFinders are not concerned with game physics.
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
	ArrayList<Coordinate> getPath(Coordinate startPosition, Coordinate goalPosition, float startingSpeed, float startingAngle);
}
