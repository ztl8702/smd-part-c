/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import java.util.ArrayList;
import java.util.Collections;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;

import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

public class AStarPathFinder extends PathFinderBase {
    
    //TODO: refactor to be in Logger
    private boolean DEBUG_GET_TILE_BEHIND = false;

    /**
     * The maximum depth of search we're willing to accept before giving up
     */
    private int maxSearchDistance;

    /**
     * The complete set of nodes across the map
     */
    private Node[][] nodes;

    /**
     * heuristic based on tile type
     * the higher the value the least likely it will be searched 
     */
    private AStarHeuristic heuristicTileType = new AStarHeuristic() {
        //TODO: currently HardCoded value, maybe not the best
        @Override
        public float getCost(int x, int y, int tx, int ty) {

            // using current position, which is the current node
            Cell cell = mapManager.getCell(x, y);
            CellType cellType = null;
            if (cell != null) {
                cellType = cell.type;
            }

            
            // if node is not in close proximity to a wall
            // favour it for turning/travelling at higher speed
            if (! nextToWall(new Coordinate(x, y))) {
                if (cellType == CellType.LAVA) {
                    return 50;
                }
                return 5;
            }

            if (cellType == CellType.LAVA) {
                // disfavour lava tiles
                return 10;
            }

            if (cellType == CellType.HEALTH) {
                // favour health tiles
                return 1;
            }

            return 0;
        }
    };
    

    // TODO: move to PathFinderBase, same code as in WallFollowingPathFinder
    // TODO: same code as in WallFollowingPathFinder
    private boolean nextToWall(Coordinate c) {
        for (int xDelta = -1; xDelta <= 1; xDelta += 1) {
            for (int yDelta = -1; yDelta <= 1; yDelta += 1) {
                if (!(xDelta == 0 && yDelta == 0)) {
                    Coordinate newCoord = new Coordinate(c.x + xDelta, c.y + yDelta);
                    Cell mapTile = mapManager.getCell(newCoord.x, newCoord.y);
                    if (mapTile != null && mapTile.type == CellType.WALL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * heuristic based on distance
     * favour shorter distances 
     */
    private AStarHeuristic heuristicClosest = new AStarHeuristic() {

        @Override
        public float getCost(int x, int y, int tx, int ty) {
            float dx = Math.abs(tx - x);
            float dy = Math.abs(ty - y);
            float result = (dx + dy);

            return result;
        }
    };

    /**
     * AStarPathFinder constructor that initialises the search space for A* search
     * 
     * @param mapManager
     * @param maxSearchDistance
     * @param width
     * @param height
     */
    public AStarPathFinder(MapManager mapManager, int maxSearchDistance, int width, int height) {
        super(mapManager);
        this.maxSearchDistance = maxSearchDistance;

        nodes = new Node[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new Node(x, y);
            }
        }
    }

    /**
     * Print log string
     *
     * @param message
     */
    private void info(String message) {
        Logger.printInfo("AStarPathFinder", message);
    }

    @Override
    public ArrayList<Coordinate> getPath(Coordinate startPosition,
                                         Coordinate goalPosition, 
                                         float currentSpeed, 
                                         float currentDirection) {

        int sx = startPosition.x;
        int sy = startPosition.y;
        int tx = goalPosition.x;
        int ty = goalPosition.y;


        ArrayList<Node> closed = new ArrayList<Node>();
        SortedList open = new SortedList();

        // initial state for A*. The closed group is empty. Only the starting
        // tile is in the open list and it's cost is zero, i.e. we're already there
        nodes[sx][sy].cost = 0;
        nodes[sx][sy].depth = 0;

        nodes[sx][sy].direction = currentDirection;
        closed.clear();
        open.clear();
        open.add(nodes[sx][sy]);

        // TODO: this can cause index out of bound if the map is not bounded by walls
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
            for (Coordinate d : Util.ANTICLOCKWISE_DIRECTION) {
                // determine the location of the neighbour and evaluate it
                int xp = d.x + current.x;
                int yp = d.y + current.y;


                // if we are exploring from original position
                // need to avoid providing a solution that involves reversing the car
                if (startPosition.x == current.x && startPosition.y == current.y) {
                    if (DEBUG_GET_TILE_BEHIND) {
                        info("maxdepth is " + maxDepth);
                        info(String.format("%d\n%d\n", current.x, current.y));
                    }
                   
                    Coordinate tileBehind = getTileBehind(current.direction, current.x, current.y);

                    if (DEBUG_GET_TILE_BEHIND) {
                        info(String.format("tileBehind is %s \n(%d,%d)",
                                tileBehind.toString(), xp, yp));
                    }
                    if (!(tileBehind.x == xp && tileBehind.y == yp)) {
                        updateNodes(xp, yp, tx, ty, current, closed, open, maxDepth);
                    }
                } else {
                    updateNodes(xp, yp, tx, ty, current, closed, open, maxDepth);
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

    private void updateNodes(int xp, int yp, int tx, int ty,
                           Node current, ArrayList<Node> closed, SortedList open, int maxDepth) {
        if (!mapManager.isWall(xp, yp) && mapManager.isWithinBoard(new Coordinate(xp,yp))) {
            // the cost to get to this node is cost the current plus the movement
            // cost to reach this node. Note that the heursitic value is only used
            // in the sorted open list
            float nextStepCost = current.cost + getMovementCost(current.x, current.y, xp, yp);
            Node neighbour = nodes[xp][yp];
//				map.pathFinderVisited(xp, yp);
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
            if ((!open.contains(neighbour)) && !(closed.contains(neighbour))) {
                neighbour.cost = nextStepCost;
                neighbour.heuristic = getHeuristicCost(xp, yp, tx, ty);
                neighbour.direction = getDirection(current.x, current.y, xp, yp); // getDirection(xp, yp, tx, ty);
                maxDepth = Math.max(maxDepth, neighbour.setParent(current));
                open.add(neighbour);
            }
        }
    }


    /**
     * Get the coordinate of the tile behind current location
     * 
     * @param currentAngle
     * @param x
     * @param y
     * @return
     */
    private Coordinate getTileBehind(float currentAngle, int x, int y) {
        WorldSpatial.Direction currentOrientation = Util.angleToOrientation(currentAngle);
        return Util.getTileBehind(new Coordinate(x, y), currentOrientation);
    }

    /**
     * Get the current direction based on previous step
     *
     * @param xp from point x
     * @param yp from point y
     * @param tx to point x
     * @param ty to point y
     * @return
     */
    private float getDirection(int xp, int yp, int tx, int ty) {
        Direction direction = Util.inferDirection(new Coordinate(tx, ty), new Coordinate(xp, yp));
        return Util.orientationToAngle(direction);
    }

    /**
     * Get the heuristic cost for the given location. This determines in which
     * order the locations are processed.
     *
     * @param x  The x coordinate of the tile whose cost is being determined
     * @param y  The y coordiante of the tile whose cost is being determined
     * @param tx The x coordinate of the target location
     * @param ty The y coordinate of the target location
     * @return The heuristic cost assigned to the tile
     */
    private float getHeuristicCost(int x, int y, int tx, int ty) {
        float h1 = heuristicClosest.getCost(x, y, tx, ty);
        float h2 = heuristicTileType.getCost(x, y, tx, ty);

        float heuristics = h1 + h2;
        return heuristics;
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
    //TODO: maybe remove this altogether since each time cost is always equal to ONE
    private float getMovementCost(int sx, int sy, int tx, int ty) {
        return 1;
    }


    /*****************************************************************************************/
    /*************************        SortedList & Node class       **************************/
    /*****************************************************************************************/


    /**
     * A simple sorted list
     */
    private class SortedList {
        /**
         * The list of elements
         */
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
        /**
         * The x coordinate of the node
         */
        private int x;
        /**
         * The y coordinate of the node
         */
        private int y;
        /**
         * The path cost for this node
         */
        private float cost;
        /**
         * The parent of this node, how we reached it in the search
         */
        private Node parent;
        /**
         * The heuristic cost of this node
         */
        private float heuristic;
        /**
         * The search depth of this node
         */
        private int depth;
        /**
         * The current direction of this node
         */
        private float direction;

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
         * @return The depth we have not reached in searching
         */
        public int setParent(Node parent) {
            depth = parent.depth + 1;
            this.parent = parent;

            return depth;
        }

        /**
         * Used to compare nodes based on cost
         * 
         * @param other the object to be compared to
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
