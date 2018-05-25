package mycontroller.pathfinder;

import java.lang.reflect.Array;
import java.util.*;

import mycontroller.common.Cell;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

public class WallFollowingPathFinder extends PathFinderBase {

    public static final List<WorldSpatial.Direction> ANTICLOCKWISE_DIRECTION = Arrays.asList(
            WorldSpatial.Direction.NORTH,
            WorldSpatial.Direction.WEST,
            WorldSpatial.Direction.SOUTH,
            WorldSpatial.Direction.EAST
    );

    private Coordinate startingPosition;
    private float startingSpeed;
    private WorldSpatial.Direction startingDirection;


    public WallFollowingPathFinder(MapManagerInterface mapManager) {
        super(mapManager);
    }

    @Override
    public ArrayList<Coordinate> getPath(Coordinate currentPosition,
                                         Coordinate goalPosition, float currentSpeed, float currentAngle) {


        this.startingPosition = currentPosition;
        startingSpeed = currentSpeed;

        // figure out the direction

        startingDirection = Util.angleToOrientation(currentAngle);

        ArrayList<Coordinate> finalPath = new ArrayList<>();
        ArrayList<Coordinate> path1 = null;
        try {
            path1 = findPathToClosestWallBFS();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Coordinate> path2 = findPathFollowingWallDFS(
                path1.isEmpty() ? currentPosition : path1.get(path1.size() - 1),
                new HashSet<>(path1));
        finalPath.addAll(path1);
        finalPath.addAll(path2);


        return finalPath;
    }


    private ArrayList<Coordinate> findPathToClosestWallBFS() {


        Queue<Coordinate> queue = new LinkedList<Coordinate>();
        Set<Coordinate> visited = new HashSet<Coordinate>();
        HashMap<Coordinate, Coordinate> parent = new HashMap<>();

        queue.add(startingPosition);
        visited.add(startingPosition);
        parent.put(startingPosition, null);

        // add the next tile in direction
        Coordinate nextLocationInDirection = Util.getTileAhead(startingPosition, startingDirection);

        assert (mapManager.getCell(nextLocationInDirection.x, nextLocationInDirection.y).type != Cell.CellType.WALL);
        queue.add(nextLocationInDirection);
        visited.add(nextLocationInDirection);
        parent.put(nextLocationInDirection, startingPosition);

        queue.remove();

        while (!queue.isEmpty()) {
            Coordinate head = queue.remove();


            for (WorldSpatial.Direction d : ANTICLOCKWISE_DIRECTION) {
                Coordinate c = Util.orientationToDelta(d);
                Coordinate newCoord = new Coordinate(head.x + c.x, head.y + c.y);
                if (!visited.contains(newCoord)) {
                    Cell mapTile = mapManager.getCell(newCoord.x, newCoord.y);
                    if (mapTile != null && mapTile.type != Cell.CellType.WALL) {
                        queue.add(newCoord);
                        visited.add(newCoord);
                        parent.put(newCoord, head);
                        if (nextToWallAnySide(newCoord)) {
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
            }
        }
        Logger.printWarning(this.getClass().getName(), "BFS ended without a path");
        return new ArrayList<>();
    }


    private Stack<Coordinate> stack;


    /**
     *
     * @param currentLocation
     * @param missWallBudget
     * @param whichSide
     * @param visited
     * @return
     */
    private boolean dfs(Coordinate currentLocation,
                        int missWallBudget,
                        WorldSpatial.RelativeDirection whichSide,
                        WorldSpatial.Direction orientation,
                        Set<Coordinate> visited) {
        if (missWallBudget < 0) {
            return false;
        }
        visited.add(currentLocation);
        stack.push(currentLocation);
        boolean noSuccessor = true;

        if (orientation == null) {
            // we can choose freely which direction to go

            for (WorldSpatial.Direction d : WorldSpatial.Direction.values()) {
                Coordinate c = Util.orientationToDelta(d);
                Coordinate newCoord = new Coordinate(currentLocation.x + c.x, currentLocation.y + c.y);

                if (!isWall(newCoord.x, newCoord.y)) {
                    if (!visited.contains(newCoord)) {
                        if (nextToWallAnySide(newCoord)) {
                            noSuccessor = false;
                            if (dfs(newCoord, missWallBudget, whichSideIsWall(newCoord, d), d, visited)) {
                                return true;
                            }

                        }
                    }
                }
            }
        } else {
            // we can only turn left or right, or go straight
            Coordinate nextAhead = Util.getTileAhead(currentLocation, orientation);
            WorldSpatial.Direction myLeft =  Util.getTurnedOrientation(orientation, WorldSpatial.RelativeDirection.LEFT);
            WorldSpatial.Direction myRight =  Util.getTurnedOrientation(orientation, WorldSpatial.RelativeDirection.RIGHT);
            Coordinate leftTile = Util.getTileAhead(currentLocation, myLeft);
            Coordinate rightTile = Util.getTileAhead(currentLocation, myRight);

            if (!isWall(nextAhead.x, nextAhead.y) && !visited.contains(nextAhead)) {
                if (nextToWall(nextAhead,orientation,whichSide)) {
                    noSuccessor = false;
                    if (dfs(
                            nextAhead,
                            missWallBudget,
                            whichSide,
                            orientation,
                            visited)) {
                        return true;
                    }
                } else if (missWallBudget > 0) {
                    noSuccessor = false;
                    if (dfs(
                            nextAhead,
                            missWallBudget-1,
                            whichSide,
                            orientation,
                            visited)) {
                        return true;
                    }
                }
            }

            if (!isWall(leftTile.x, leftTile.y) && !visited.contains(leftTile)) {
                if (nextToWall(leftTile, myLeft, whichSide)) {
                    noSuccessor = false;
                    if (dfs(
                            leftTile,
                            missWallBudget,
                            whichSide,
                            myLeft,
                            visited)) {
                        return true;
                    }
                } else if (missWallBudget > 0) {
                    noSuccessor = false;
                    if (dfs(
                            leftTile,
                            missWallBudget-1,
                            whichSide,
                            myLeft,
                            visited)) {
                        return true;
                    }
                }
            }

            if (!isWall(leftTile.x, leftTile.y) && !visited.contains(rightTile)) {
                if (nextToWall(rightTile, myRight, whichSide)) {
                    noSuccessor = false;
                    if (dfs(
                            rightTile,
                            missWallBudget,
                            whichSide,
                            myRight,
                            visited)) {
                        return true;
                    }
                } else if (missWallBudget > 0) {
                    noSuccessor = false;
                    if (dfs(
                            rightTile,
                            missWallBudget-1,
                            whichSide,
                            myRight,
                            visited)) {
                        return true;
                    }
                }
            }
        }


        if (noSuccessor) {
            return true;
        }
        stack.pop();
        visited.remove(currentLocation);
        return false;
    }

    private ArrayList<Coordinate> findPathFollowingWallDFS(Coordinate start, Set<Coordinate> visited) {

        stack = new Stack<Coordinate>();
        boolean result = dfs(start, 1,null, null, visited);
        assert (result);

        ArrayList<Coordinate> path = new ArrayList<>();

        while (!stack.isEmpty()) {
            path.add(stack.pop());
        }

        Collections.reverse(path);
        path.remove(0); // remove the first one (which is the same as the end of last path)
        return path;

    }

    protected boolean isWall(int x, int y) {
        if (!mapManager.isWithinBoard(new Coordinate(x,y))) {
            return true;
        } else {
            Cell c = mapManager.getCell(x,y);
            return c.type == Cell.CellType.WALL;
        }

    }

    private WorldSpatial.RelativeDirection whichSideIsWall(Coordinate c, WorldSpatial.Direction orientation) {

        switch (orientation) {
            case EAST:
                if (isWall(c.x,c.y+1)) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (isWall(c.x,c.y-1)) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
            case WEST:
                if (isWall(c.x,c.y-1)) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (isWall(c.x,c.y+1)) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
            case NORTH:
                if (isWall(c.x-1,c.y)) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (isWall(c.x+1,c.y)) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
            case SOUTH:
                if (isWall(c.x+1,c.y)) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (isWall(c.x-1,c.y)) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
        }
        return null;
    }

    /**
     * If a particular side of me is next to wall
     * @param c
     * @param orientation
     * @param whichSide
     * @return
     */
    private boolean nextToWall(Coordinate c, WorldSpatial.Direction orientation, WorldSpatial.RelativeDirection whichSide) {
        switch (whichSide) {
            case LEFT:
                switch (orientation){
                    case EAST:
                        return isWall(c.x, c.y+1);
                    case WEST:
                        return isWall(c.x, c.y-1);
                    case NORTH:
                        return isWall(c.x-1, c.y);
                    case SOUTH:
                        return isWall(c.x+1, c.y);
                }
            case RIGHT:
                switch (orientation){
                    case EAST:
                        return isWall(c.x, c.y-1);
                    case WEST:
                        return isWall(c.x, c.y+1);
                    case NORTH:
                        return isWall(c.x+1, c.y);
                    case SOUTH:
                        return isWall(c.x-1, c.y);
                }
            default:
                return false;
        }
    }

    /**
     * If any side of me is next to a wall
     * @param c
     * @return
     */
    private boolean nextToWallAnySide(Coordinate c) {
        for (WorldSpatial.Direction d: WorldSpatial.Direction.values()) {
            for (WorldSpatial.RelativeDirection r: WorldSpatial.RelativeDirection.values()) {
                if (nextToWall(c, d, r)) {
                    return true;
                }
            }
        }
        return false;
    }
}

