package mycontroller.pathfinder;

import java.util.ArrayList;
import utilities.Coordinate;

/**
 * PathFinder Interface
 */
public interface PathFinder {
	
	
	/**
	 * @param currentPosition
	 * @param goalPosition
	 * @param currentSpeed
	 * @param currentDirection
	 * @return
	 */
	public ArrayList<Coordinate> getPath(Coordinate currentPosition, Coordinate goalPosition, float currentSpeed, float currentDirection);
}
