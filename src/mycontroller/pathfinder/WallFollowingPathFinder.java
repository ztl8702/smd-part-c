package mycontroller.pathfinder;

import java.util.*;

import mycontroller.common.Cell;
import utilities.Coordinate;
import world.WorldSpatial;

public class WallFollowingPathFinder implements PathFinder {
	
	public static final List<Coordinate> ANTICLOCKWISE_DIRECTION = Arrays.asList(
			new Coordinate(0,1), //N
			new Coordinate(-1,0), //W
			new Coordinate(0,-1), //S
			new Coordinate(1,0) //E
			);

    private HashMap<Coordinate, Cell> map;
    private Coordinate startingPosition;
    private float startingSpeed;
    private WorldSpatial.Direction startingDirection;
    
    private boolean isWallLeft = false;

    @Override
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, 
			Coordinate goalPosition, float currentSpeed,float currentAngle) {
        this.map = map;
        this.startingPosition = currentPosition;
        startingSpeed = currentSpeed;

        // figure out the direction

        if (currentAngle <= 90) {
            startingDirection = WorldSpatial.Direction.EAST;
        } else if (currentAngle <= 180) {
            startingDirection = WorldSpatial.Direction.NORTH;
        } else if (currentAngle <= 270) {
            startingDirection = WorldSpatial.Direction.WEST;
        } else {
            startingDirection = WorldSpatial.Direction.SOUTH;
        }

        ArrayList<Coordinate> finalPath = new ArrayList<>();
        ArrayList<Coordinate> path1 = null;
        try {
            path1 = findPathToClosestWallBFS();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Coordinate> path2 = findPathFollowingWallDFS(path1.get(path1.size() - 1), new HashSet<>(path1));
        //path1.remove(path1.size() - 1);
        finalPath.addAll(path1);
        finalPath.addAll(path2);


//        System.out.println("Path found!!!!, path1 len= "+path1.size());
//        for (Coordinate c : finalPath) {
//            System.out.printf("(%d,%d)->", c.x, c.y);
//
//        }

        return finalPath;
    }


    private ArrayList<Coordinate> findPathToClosestWallBFS() throws Exception {


        Queue<Coordinate> queue = new LinkedList<Coordinate>();
        Set<Coordinate> visited = new HashSet<Coordinate>();
        HashMap<Coordinate, Coordinate> parent = new HashMap<>();

        queue.add(startingPosition);
        visited.add(startingPosition);
        parent.put(startingPosition, null);

        // add the next tile in direction
        Coordinate nextLocationInDirection = null;
        switch (startingDirection) {
            case EAST:
                nextLocationInDirection = new Coordinate(startingPosition.x + 1, startingPosition.y);
                break;
            case WEST:
                nextLocationInDirection = new Coordinate(startingPosition.x - 1, startingPosition.y);
                break;
            case NORTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y + 1);
                break;
            case SOUTH:
                nextLocationInDirection = new Coordinate(startingPosition.x, startingPosition.y - 1);
                break;

        }

        assert (map.get(nextLocationInDirection).type != Cell.CellType.WALL);
        queue.add(nextLocationInDirection);
        visited.add(nextLocationInDirection);
        parent.put(nextLocationInDirection, startingPosition);

        queue.remove();

        while (!queue.isEmpty()) {
            Coordinate head = queue.remove();

//            for (int xDelta = -1; xDelta <= 1; xDelta += 1) {
//                for (int yDelta = -1; yDelta <= 1; yDelta += 1) {
                	
            for (Coordinate c : ANTICLOCKWISE_DIRECTION) {
//                    if ((c.x == 0 && c.y != 0) || (c.x != 0 && c.y == 0)) {
                        Coordinate newCoord = new Coordinate(head.x + c.x, head.y + c.y);
                        if (!visited.contains(newCoord)) {
                            Cell mapTile = map.get(newCoord);
                            if (mapTile != null && mapTile.type != Cell.CellType.WALL) {
                                queue.add(newCoord);
                                visited.add(newCoord);
                                parent.put(newCoord, head);
                                if (nextToWall(newCoord)) {
                                    // found
                                    ArrayList<Coordinate> path = new ArrayList<>();

                                    Coordinate tmp = newCoord;
                                    while (tmp != null) {
                                        path.add(tmp);
                                        tmp = parent.get(tmp);
                                    }

                                    Collections.reverse(path);
                                    return path;
                                }
                            }
                        }

//                    }
//                }

            }
        }
        return null;
    }


    private Stack<Coordinate> stack;


    private boolean dfs(Coordinate currentLocation, Set<Coordinate> visited) {

        boolean isFound = false;
        // goes through W -> S -> N -> E
        for (Coordinate c : ANTICLOCKWISE_DIRECTION) {
//        for (int xDelta = -1; xDelta <= 1; xDelta += 1) {
//            for (int yDelta = -1; yDelta <= 1; yDelta += 1) {
            	// at least one of x or y is zero, avoid going diagonally            	
//                if ((xDelta == 0 && yDelta != 0) || (xDelta != 0 && yDelta == 0)) {
                    Coordinate newCoord = new Coordinate(currentLocation.x + c.x, currentLocation.y + c.y);
                    Cell mapTile = map.get(newCoord);
                    if (mapTile != null && mapTile.type != Cell.CellType.WALL && nextToWall(newCoord)) {
                        if (!visited.contains(newCoord)) {
                            isFound = true;
                            visited.add(newCoord);
                            stack.push(newCoord);
                            if (dfs(newCoord, visited)) {
                                return true;
                            }
                            stack.pop();
                            visited.remove(newCoord);
                        }
                    }
//                }
//            }
        }

        if (!isFound) {
            return true;
        }
        return false;
    }

    private ArrayList<Coordinate> findPathFollowingWallDFS(Coordinate start, Set<Coordinate> visited) {

        stack = new Stack<Coordinate>();
        boolean result = dfs(start, visited);
        assert (result);

        ArrayList<Coordinate> path = new ArrayList<>();
        while (!stack.isEmpty()) {
            path.add(stack.pop());
        }

        Collections.reverse(path);
        return path;

    }

    private boolean nextToWall(Coordinate c) {
    	
        for (int xDelta = -1; xDelta <= 1; xDelta += 1) {
            for (int yDelta = -1; yDelta <= 1; yDelta += 1) {
                if (!(xDelta == 0 && yDelta == 0))

                {
                    Coordinate newCoord = new Coordinate(c.x + xDelta, c.y + yDelta);
                    Cell mapTile = map.get(newCoord);
                    if (mapTile != null && mapTile.type == Cell.CellType.WALL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

