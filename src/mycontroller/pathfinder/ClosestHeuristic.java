package mycontroller.pathfinder;

import java.util.HashMap;

import mycontroller.common.Cell;
import utilities.Coordinate;

public class ClosestHeuristic implements AStarHeuristic {

	@Override
	public float getCost(HashMap<Coordinate, Cell> map, int x, int y, int tx, int ty) {
		/* based on Euclidean Distance */
		// float dx = tx - x;
		// float dy = ty - y;
		// float result = (float) (Math.sqrt((dx*dx)+(dy*dy)));

		/* based on Manhattan Distance */
		float dx = Math.abs(tx - x);
		float dy = Math.abs(ty - y);
		float result = (dx + dy);
		
		return result;
	}


}