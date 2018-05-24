package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import mycontroller.common.Cell;
import utilities.Coordinate;

/**
 * PathFinder Interface
 */
public interface PathFinder {
	
	ArrayList<Coordinate> getPath(Coordinate currentPosition, float currentSpeed, float currentDirection);
	
}
