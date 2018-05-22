package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;

import mycontroller.common.Cell;
import utilities.Coordinate;

public interface PathFinder {
	
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed, float currentDirection);
	
}
