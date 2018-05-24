package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import mycontroller.MapManager;
import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import utilities.Coordinate;
import world.World;

public class AStarPathFinder implements PathFinder {
	

	/** The map being searched */
	private HashMap<Coordinate, Cell> map;
	/** The maximum depth of search we're willing to accept before giving up */
	private int maxSearchDistance;
	
	/** The complete set of nodes across the map */
	private Node[][] nodes;
	/** True if we allow diaganol movement */
	private boolean allowDiagMovement = false;
	/** The heuristic we're applying to determine which nodes to search first */
	private AStarHeuristic heuristic;
	
	
	
	/**
	 * Create a path finder 
	 * 
	 * @param heuristic The heuristic used to determine the search order of the map
	 * @param map The map to be searched
	 * @param maxSearchDistance The maximum depth we'll search before giving up
	 * @param allowDiagMovement True if the search should try diaganol movement
	 */
	public AStarPathFinder(HashMap<Coordinate, Cell> map, int maxSearchDistance, 
						   int initialKey, Coordinate currentPosition, 
						   float currentSpeed,float currentDirection) {
		this.heuristic = new ClosestHeuristic();
		this.map = map;
		this.maxSearchDistance = maxSearchDistance;
		
		
		//TODO need to think about it again as if this is the search we are using to explore 
		
		
		nodes = new Node[World.MAP_WIDTH][World.MAP_HEIGHT];
		for (int x=0;x<World.MAP_WIDTH;x++) {
			for (int y=0;y<World.MAP_HEIGHT;y++) {
				nodes[x][y] = new Node(x,y);
			}
		}
	}
		
	
	@Override
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, 
			float currentSpeed,float currentDirection) {
		return null;
	}

	/**
	 * Get the heuristic cost for the given location. This determines in which 
	 * order the locations are processed.
	 * 
	 * @param x The x coordinate of the tile whose cost is being determined
	 * @param y The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The heuristic cost assigned to the tile
	 */
	public float getHeuristicCost(int x, int y, int tx, int ty) {
		return heuristic.getCost(map, x, y, tx, ty);
	}
	
	/**
	 * Get the cost to move through a given location
	 * 
	 * @param sx The x coordinate of the tile whose cost is being determined
	 * @param sy The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The cost of movement through the given tile
	 */
	public float getMovementCost(int sx, int sy, int tx, int ty) {
//		return map.getCost(sx, sy, tx, ty);
		return 1;
	}
	

	public ArrayList<Coordinate> findPath(int sx, int sy, int tx, int ty) {
		ArrayList<Node> closed = new ArrayList<Node>();
		SortedList open = new SortedList();
		
		// initial state for A*. The closed group is empty. Only the starting
		// tile is in the open list and it's cost is zero, i.e. we're already there
		nodes[sx][sy].cost = 0;
		nodes[sx][sy].depth = 0;
		closed.clear();
		open.clear();
		open.add(nodes[sx][sy]);
		
		nodes[tx][ty].parent = null;
		
		// while we haven't found the goal and haven't exceeded our max search depth
		int maxDepth = 0;
		while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
			// pull out the first node in our open list, this is determined to 
			// be the most likely to be the next step based on our heuristic
			Node current = (Node) open.first();
			
			// reached goal node
			if (current == nodes[tx][ty]) {
				break;
			}
			open.remove(current);
			closed.add(current);
			
			// search through all the neighbours of the current node evaluating
			// them as next steps
			for (int x=-1;x<2;x++) {
				for (int y=-1;y<2;y++) {
					// not a neighbour, its the current tile
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					// if we're not allowing diagonal movement then only 
					// one of x or y can be set
					if (!allowDiagMovement) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					// determine the location of the neighbour and evaluate it
					int xp = x + current.x;
					int yp = y + current.y;
					System.out.printf("[%d,%d](%d,%d) ", xp,yp,x,y);
					
					if (!isWall(sx, sy, xp, yp)) {
						System.out.printf("(%d,%d is not wall!",xp,yp);//getClass();
//					if (getCellType(new Coordinate(xp, yp)) != CellType.WALL) {
//					if (true) { //isValidLocation(mover,sx,sy,xp,yp)) {
						// the cost to get to this node is cost the current plus the movement
						// cost to reach this node. Note that the heursitic value is only used
						// in the sorted open list
						float nextStepCost = current.cost + getMovementCost(current.x, current.y, xp, yp);
						Node neighbour = nodes[xp][yp];
//						map.pathFinderVisited(xp, yp);
						
						// if the new cost we've determined for this node is lower than 
						// it has been previously makes sure the node hasn't been discarded. We've
						// determined that there might have been a better path to get to
						// this node so it needs to be re-evaluated
						if (nextStepCost < neighbour.cost) {
							
							if (open.contains(neighbour)) {
								open.remove(neighbour);
							}
							if (closed.contains(neighbour)) {
								closed.remove(neighbour);
							}
						}
						
						// if the node hasn't already been processed and discarded then
						// reset it's cost to our current cost and add it as a next possible
						// step (i.e. to the open list)
						if ( (!open.contains(neighbour)) && !(closed.contains(neighbour)) ) {
							neighbour.cost = nextStepCost;
							neighbour.heuristic = getHeuristicCost(xp, yp, tx, ty);
							maxDepth = Math.max(maxDepth, neighbour.setParent(current));
							open.add(neighbour);
						}
					}
				}
			}
		}
		
		
		
		// since we've got an empty open list or we've run out of search 
		// there was no path. Just return null
		if (nodes[tx][ty].parent == null) {
			return null;
		}
		
		// At this point we've definitely found a path so we can uses the parent
		// references of the nodes to find out way from the target location back
		// to the start recording the nodes on the way.
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		Node target = nodes[tx][ty];
		while (target != nodes[sx][sy]) {
			
			path.add(0, new Coordinate(target.x, target.y));
			target = target.parent;
		}
		path.add(0, new Coordinate(target.x, target.y));
		
		// thats it, we have our path 
		return path;
	}

	
	
	
	
	public boolean isWall(int sx, int sy, int x, int y) {
		Coordinate coord = new Coordinate(x, y);
		boolean inMap = map.containsKey(coord);
//		boolean invalid = (x < 0) || (y < 0) || (x >= World.MAP_WIDTH) || (y >= World.MAP_HEIGHT);

		if (inMap && ((sx != x) || (sy != y))) {
//			invalid = map.get(new Coordinate(x, y)).type == CellType.WALL;
//		if (inMap) {
			if (map.get(coord).type == CellType.WALL) {
				return true;
			}
		}
		return false;
//		return invalid;
	}
	
//	public CellType getCellType(Coordinate coord) {
//		if (map.containsKey(coord)) {
//			return map.get(coord).type;
//		}
//		System.out.println("no such coordinate in map: getCellType(Coordinate)");
//		
//		return null;
//	}
	
	
	
	
	
	
	
	
	
	
	/*****************************************************************************************/
	/*************************        SortedList & Node class       **************************/
	/*****************************************************************************************/
	
	
	/**
	 * A simple sorted list
	 */
	private class SortedList {
		/** The list of elements */
		private ArrayList list = new ArrayList();
		
		/**
		 * Retrieve the first element from the list
		 *  
		 * @return The first element from the list
		 */
		public Object first() {
			return list.get(0);
		}
		
		/**
		 * Empty the list
		 */
		public void clear() {
			list.clear();
		}
		
		/**
		 * Add an element to the list - causes sorting
		 * 
		 * @param o The element to add
		 */
		public void add(Object o) {
			list.add(o);
			Collections.sort(list);
		}
		
		/**
		 * Remove an element from the list
		 * 
		 * @param o The element to remove
		 */
		public void remove(Object o) {
			list.remove(o);
		}
	
		/**
		 * Get the number of elements in the list
		 * 
		 * @return The number of element in the list
 		 */
		public int size() {
			return list.size();
		}
		
		/**
		 * Check if an element is in the list
		 * 
		 * @param o The element to search for
		 * @return True if the element is in the list
		 */
		public boolean contains(Object o) {
			return list.contains(o);
		}
	}
	
	/**
	 * A single node in the search graph
	 */
	private class Node implements Comparable {
		/** The x coordinate of the node */
		private int x;
		/** The y coordinate of the node */
		private int y;
		/** The path cost for this node */
		private float cost;
		/** The parent of this node, how we reached it in the search */
		private Node parent;
		/** The heuristic cost of this node */
		private float heuristic;
		/** The search depth of this node */
		private int depth;
		
		/**
		 * Create a new node
		 * 
		 * @param x The x coordinate of the node
		 * @param y The y coordinate of the node
		 */
		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Set the parent of this node
		 * 
		 * @param parent The parent node which lead us to this node
		 * @return The depth we have no reached in searching
		 */
		public int setParent(Node parent) {
			depth = parent.depth + 1;
			this.parent = parent;
			
			return depth;
		}
		
		/**
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(Object other) {
			Node o = (Node) other;
			
			float f = heuristic + cost;
			float of = o.heuristic + o.cost;
			
			if (f < of) {
				return -1;
			} else if (f > of) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	

}























