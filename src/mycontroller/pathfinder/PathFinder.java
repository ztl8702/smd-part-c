package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;

import mycontroller.common.Cell;
import utilities.Coordinate;

public interface PathFinder {
	
	/**
	 * @param map
	 * @param currentPosition
	 * @param currentSpeed
	 * @param currentDirection
	 * @return
	 */
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed, float currentDirection);
	
	
//	public ArrayList<Coordinate> getPath(int sx, int sy, int tx, int ty);
}
