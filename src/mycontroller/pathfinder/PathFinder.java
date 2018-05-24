package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;

import mycontroller.common.Cell;
import utilities.Coordinate;

public interface PathFinder {
	
	
	/**
	 * @param map
	 * @param currentPosition
	 * @param goalPosition
	 * @param currentSpeed
	 * @param currentDirection
	 * @return
	 */
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, Coordinate goalPosition, float currentSpeed, float currentDirection);
}
