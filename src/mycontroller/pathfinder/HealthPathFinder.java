package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;

public class HealthPathFinder implements PathFinder {
	
	private MapManagerInterface mapManager;
	
	public HealthPathFinder(MapManagerInterface mapManager) {
		this.mapManager = mapManager;
	}
	
		

	@Override
	public ArrayList<Coordinate> getPath(Coordinate currentPosition, Coordinate goalPosition, float currentSpeed,
			float currentDirection) {
		Set<Coordinate> healthTiles = mapManager.getHealthTiles();
		
		for (Coordinate h: healthTiles) {
			
		}
		
		
		return null;
	}
//1. Recovery mode dijkstra to nearest health tile. Stay until 100. Make hashmap of health tiles in mapmanager
}
