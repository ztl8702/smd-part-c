package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import utilities.Coordinate;
import world.WorldSpatial;

public class WallFollowingPathFinder implements PathFinder {

	private HashMap<Coordinate, Cell> map;
	private Coordinate startingPosition;
	private float startingSpeed;
	private WorldSpatial.Direction startingDirection;
	
	@Override
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed,
			float currentDirection) {
		
		this.map = map;
		this.startingPosition
		return null;
	}
	
	
	private ArrayList<Coordinate> findPathToClosestWallBFS() {
		
	}
}
