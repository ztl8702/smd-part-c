package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import mycontroller.pathfinder.PathFinder;
import utilities.Coordinate;
import world.World;

public class AStarPathFinder implements PathFinder {
	
	private int initialKey;
	
	
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
		
		this.initialKey = initialKey;
		
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
		
		ArrayList<Coordinate> finalPath = new ArrayList<>();
        ArrayList<Coordinate> subPath = null;
        
        // initial position before search
        int cX = currentPosition.x;
        int cY = currentPosition.y;
        		
		/* loop through all the keys and set key coordinate end location */
		for( int i = initialKey-1; i>=1; i-- ) {
			
			Coordinate keyPosition = getKeyPosition(i);
			
			subPath = findPath(cX, cY, keyPosition.x, keyPosition.y);
			if (!subPath.isEmpty()) {
				finalPath.addAll(subPath);
				
				cX = keyPosition.x;
				cY = keyPosition.y;
			}
			else {
				System.err.println("Problem finding path with astar" + "from" + cX + "," + cY + "to" + keyPosition.x + "," + keyPosition.y);
			}
		}
		// done with getting all keys, now go to finish tile
		subPath = findPath(getKeyPosition(1).x, getKeyPosition(1).y, getPosition(CellType.FINISH).x, getPosition(CellType.FINISH).y);
		if (!subPath.isEmpty()) {
			finalPath.addAll(subPath);
		}
		
		
		// print out the result		
		System.out.println("*****ASTAR***** Path found!!!!");
		for (Coordinate c : finalPath) {
			System.out.printf("(%d,%d)->", c.x, c.y);
		}
		
		return finalPath;
	}
	
	private Coordinate getPosition(CellType cellType) {
		Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        // get the location of "finish" tile
	        if (pair != null && ((Cell)pair.getValue()).type == cellType) {
	        	return (Coordinate) pair.getKey();
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }      
	    return null;
	}
	
	private CellType getCellType(int x, int y) {
		Coordinate coord = new Coordinate(x, y);
		
		return ((Cell)map.get(coord)).type;
	}
	
	private Coordinate getKeyPosition(int key) {
		Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        // this is the current location of the specified key
	        if (pair != null && ((Cell)pair.getValue()).key == key) {
	        	return (Coordinate) pair.getKey();
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }
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
		
//		Set<Coordinate> visited = new HashSet<Coordinate>();
		
		/* The set of nodes that have been searched through */
		ArrayList closed = new ArrayList();
		/* The set of nodes that we do not yet consider fully searched */
		SortedList open = new SortedList();
		
		// easy first check, if the destination is blocked, we can't get there
		if (getCellType(tx, ty) == CellType.UNREACHABLE) {
			return null;
		}
		
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
					
					if (true) { //isValidLocation(mover,sx,sy,xp,yp)) {
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























