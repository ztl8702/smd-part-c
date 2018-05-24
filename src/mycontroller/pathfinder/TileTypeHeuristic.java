package mycontroller.pathfinder;

import java.util.HashMap;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import utilities.Coordinate;

public class TileTypeHeuristic implements AStarHeuristic {

	@Override
	public float getCost(HashMap<Coordinate, Cell> map, int x, int y, int tx, int ty) {
		// using current position
		// check if its a lava tile
		// or a health tile
		
		CellType cellType = map.get(new Coordinate(x, y)).type;
		
		if (cellType == CellType.LAVA) {
			//TODO: currently HardCoded value, maybe not the best
			// the higher the value the least likely it will be searched
			return 5;
		}
		
		if (cellType == CellType.HEALTH) {
			return -1;
		}
		
		return 0;
	}

}
