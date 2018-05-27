/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;

import java.util.*;

/**
 * Revised implementation of AStarPathFinder
 *
 * If it works better, rename it to AStarPathFinder and delele the original one
 */
public class AStarPathFinder2 extends PathFinderBase {

    //TODO: refactor to be in Logger
    private boolean DEBUG_GET_TILE_BEHIND = false;

    /**
     * The maximum depth of search we're willing to accept before giving up
     */
    private int maxSearchDistance;

    /**
     * The complete set of nodes across the map
     */
    private Node[][][] nodes;

    private int width;
    private int height;

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
    public AStarPathFinder2(MapManagerInterface mapManager, int maxSearchDistance, int width, int height) {
        super(mapManager);
        this.maxSearchDistance = maxSearchDistance;
        this.width = width;
        this.height = height;

    }

    private int directionToInt(Direction d){
        switch (d) {
            case EAST:
                return 0;
            case NORTH:
                return 1;
            case WEST:
                return 2;
            case SOUTH:
                return 3;
            default:
                return -1;
        }
    }

    private Direction intToDirection(int t) {
        switch (t) {
            case 0:
                return Direction.EAST;
            case 1:
                return Direction.NORTH;
            case 2:
                return Direction.WEST;
            case 3:
                return Direction.SOUTH;
            default:
                return null;
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

    private void initialiseNodes(Coordinate goalPosition) {
        nodes = new Node[width][height][4];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (Direction d: Direction.values()) {
                    Node thisNode = nodes[x][y][directionToInt(d)] = new Node(x, y, d);
                    thisNode.depth = 0;
                    thisNode.cost = 1e+8f;
                    // pre-calculate heuristics
                    thisNode.heuristic = getHeuristicCost(thisNode, goalPosition);
                }
            }
        }
    }

    @Override
    public ArrayList<Coordinate> getPath(Coordinate startPosition,
                                         Coordinate goalPosition,
                                         float currentSpeed,
                                         float currentDirection) {

        initialiseNodes(goalPosition);
        int sx = startPosition.x;
        int sy = startPosition.y;
        int tx = goalPosition.x;
        int ty = goalPosition.y;

        Direction startingDirection = Util.angleToOrientation(currentDirection);

        Set<Node> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>(new NodeComparator());

        Node startNode = nodes[sx][sy][directionToInt(startingDirection)];
        startNode.setParent(null);
        startNode.cost = 0;
        startNode.depth = 0;
        startNode.heuristic = getHeuristicCost(startNode, goalPosition);
        // initial state for A*. The closed group is empty. Only the starting
        // tile is in the open list and it's cost is zero, i.e. we're already there

        open.add(startNode);

        // while we haven't found the goal and haven't exceeded our max search depth
        int maxDepth = 0;
        while ((maxDepth < maxSearchDistance) && !open.isEmpty()) {
            // pull out the first node in our open list, this is determined to
            // be the most likely to be the next step based on our heuristic
            Node current = open.poll();

            // reached goal node
            if (isGoal(current, tx, ty)) {
                // produce output
                // At this point we've definitely found a path so we can uses the parent
                // references of the nodes to find out way from the target location back
                // to the start recording the nodes on the way.
                ArrayList<Coordinate> path = new ArrayList<Coordinate>();
                Node now = current;
                while (now != null) {

                    path.add(new Coordinate(now.x, now.y));
                    now = now.parent;
                }

                Collections.reverse(path);
                // that's it, we have our path
                return path;
            }

            closed.add(current);

            // update neighbours

            // find out neighbours
            Direction leftDirection = Util.getTurnedOrientation(current.direction,
                    WorldSpatial.RelativeDirection.LEFT);
            Direction rightDirection = Util.getTurnedOrientation(current.direction,
                    WorldSpatial.RelativeDirection.RIGHT);
            Coordinate leftCoord = Util.getTileAhead(new Coordinate(current.x, current.y), leftDirection);
            Coordinate rightCoord = Util.getTileAhead(new Coordinate(current.x, current.y), rightDirection);
            Coordinate aheadCoord = Util.getTileAhead(new Coordinate(current.x, current.y), current.direction);
            int[] neighbourX = {
                    leftCoord.x,
                rightCoord.x,
                aheadCoord.x
            };

            int[] neighbourY = {
                    leftCoord.y,
                    rightCoord.y,
                    aheadCoord.y
            };

            Direction[] neighbourDirection = {
                    leftDirection,
                    rightDirection,
                    current.direction
            };

            for (int i = 0; i < 3; ++i) {
                // search through all the neighbours of the current node evaluating
                // them as next steps
                Coordinate newCoord = new Coordinate(neighbourX[i], neighbourY[i]);
                Node newNode = nodes[neighbourX[i]][neighbourY[i]][directionToInt(neighbourDirection[i])];
                if (mapManager.isWithinBoard(newCoord)
                    && !mapManager.isWall(neighbourX[i], neighbourY[i])) {
                    if (!closed.contains(newNode) && !open.contains(newNode)) {
                        float newCost = current.cost + getMovementCost(current, newNode);
                        if (newCost < newNode.cost) {
                            // if the new cost we've determined for this node is lower than
                            // it has been previously makes sure the node hasn't been discarded. We've
                            // determined that there might have been a better path to get to
                            // this node so it needs to be re-evaluated
                            newNode.parent = current;
                            newNode.cost = newCost;
                            newNode.depth = current.depth+1;
                            maxDepth = Math.max(maxDepth, newNode.depth);
                        }
                        open.add(newNode);

                    }
                }
            }

        }

        // since we've got an empty open list or we've run out of search
        // there was no path. Just return null

        return null;
    }

    private boolean isGoal(Node node, int tx, int ty)
    {
        return node.x == tx && node.y == ty;
    }

    /**
     * Get the heuristic cost for the given location.
     * @param neighour
     * @param goal
     * @return
     */
    private float getHeuristicCost(Node neighour, Coordinate goal) {
        // heuristics should always be lower than the actual cost
        return Util.dis(new Coordinate(neighour.x, neighour.y), new Coordinate(goal.x, goal.y));
    }


    /**
     * Get the cost to move through a given location. g(n).
     * @param current
     * @param neighbour
     * @return
     */
    private float getMovementCost(Node current, Node neighbour) {
        Cell cell = mapManager.getCell(neighbour.x, neighbour.y);

        float cost = 1.0f;

        if (cell.type == CellType.LAVA) {
            cost *= 10;
        }

        if (current.direction!=neighbour.direction) {
            cost *= 5;
        }

        return cost;
    }


    /**
     * A single node in the search graph
     */
    private class Node {

        // These are the state
        /**
         * The x coordinate of the node
         */
        private int x;
        /**
         * The y coordinate of the node
         */
        private int y;
        /**
         * The current direction of this node
         */
        private Direction direction;


        // These are for reconstructing the path:
        /**
         * The path cost for this node. i.e. g(n)
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
         * Create a new node
         *
         * @param x The x coordinate of the node
         * @param y The y coordinate of the node
         */
        public Node(int x, int y, Direction direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }

        /**
         * Set the parent of this node
         *
         * @param parent The parent node which lead us to this node
         * @return The depth we have not reached in searching
         */
        public void setParent(Node parent) {
            this.parent = parent;
        }

        public float getEstimatedCost() {
            return heuristic + cost;
        }

        @Override
        public int hashCode() {
            return String.format("(%d,%d,%s)", x,y,direction).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            Node node = (Node) obj;

            return (node.x == x && node.y == y && node.direction == direction);
        }
    }

    private class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            if (o1.getEstimatedCost() < o2.getEstimatedCost()) {
                return -1;
            } else if (o1.getEstimatedCost() > o2.getEstimatedCost()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}


