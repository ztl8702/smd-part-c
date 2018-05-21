package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import utilities.Coordinate;

public interface PathFinder {
	
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed, float currentDirection);
	
}
