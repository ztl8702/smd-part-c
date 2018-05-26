/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import java.util.*;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
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
    public ArrayList<Coordinate> getPath(Coordinate startPosition,
                                         Coordinate goalPosition,
                                         float currentSpeed,
                                         float currentAngle) {

        this.startingPosition = startPosition;
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

        WorldSpatial.Direction wallFollowingStartDirection;
        if (path1.isEmpty() || path1.size() < 2) {
            wallFollowingStartDirection = startingDirection;
        } else {
            Coordinate lastTile = path1.get(path1.size()-1);
            Coordinate secondLastTile = path1.get(path1.size()-2);
            WorldSpatial.Direction lastDirection = Util.inferDirection(lastTile, secondLastTile);
            if (nextToWall(lastTile, lastDirection, WorldSpatial.RelativeDirection.LEFT) ||
                    nextToWall(lastTile,lastDirection,WorldSpatial.RelativeDirection.RIGHT)) {
                wallFollowingStartDirection = lastDirection;
            }
            else {
                wallFollowingStartDirection = Util.getTurnedOrientation(lastDirection,WorldSpatial.RelativeDirection.RIGHT);
            }

        }
        ArrayList<Coordinate> path2 = findPathFollowingWall(
                path1.isEmpty() ? startPosition : path1.get(path1.size() - 1),
                wallFollowingStartDirection,
                new HashSet<>(path1));
        finalPath.addAll(path1);
        finalPath.addAll(path2);

        return finalPath;
    }


    /**
     * Find a path to closest wall to follow
     * 
     * @return
     */
    private ArrayList<Coordinate> findPathToClosestWallBFS() {


        Queue<Coordinate> queue = new LinkedList<Coordinate>();
        Set<Coordinate> visited = new HashSet<Coordinate>();
        HashMap<Coordinate, Coordinate> parent = new HashMap<>();

        queue.add(startingPosition);
        visited.add(startingPosition);
        parent.put(startingPosition, null);

        // add the next tile in direction
        Coordinate nextLocationInDirection = Util.getTileAhead(startingPosition, startingDirection);

        assert (mapManager.getCell(nextLocationInDirection.x, nextLocationInDirection.y).type != CellType.WALL);
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
                    if (mapTile != null && mapTile.type != CellType.WALL) {
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


    /**
     * Find a path using wall following algorithm, after already followed a wall
     * 
     * @param start
     * @param startingDirection
     * @param visited
     * @return
     */
    private ArrayList<Coordinate> findPathFollowingWall(Coordinate start, WorldSpatial.Direction startingDirection, Set<Coordinate> visited) {

        WorldSpatial.RelativeDirection whichSideFollowing = whichSideIsWall(start, startingDirection);
        WorldSpatial.RelativeDirection turnWhenLoseWall = whichSideFollowing;
        WorldSpatial.RelativeDirection turnWhenHitWall = (whichSideFollowing == WorldSpatial.RelativeDirection.LEFT)?WorldSpatial.RelativeDirection.RIGHT : WorldSpatial.RelativeDirection.LEFT;

        ArrayList<Coordinate> path = new ArrayList<>();
        Coordinate currentCell = Util.cloneCoordinate(start);
        WorldSpatial.Direction currentDirection = startingDirection;
        if (!(nextToWall(currentCell, currentDirection, whichSideFollowing)))
            throw new AssertionError();

        while (true) {
            Coordinate tileAhead = Util.getTileAhead(currentCell,currentDirection);
            if (nextToWall(tileAhead, currentDirection, whichSideFollowing)
                    && !(isWall(tileAhead.x, tileAhead.y) || isNarrowRoad(tileAhead.x, tileAhead.y, currentDirection))) {
                currentCell = tileAhead;

            } else if (isWall(tileAhead.x, tileAhead.y)|| isNarrowRoad(tileAhead.x, tileAhead.y, currentDirection)) {
                // hit wall
                currentDirection = Util.getTurnedOrientation(currentDirection, turnWhenHitWall);
                //Coordinate tileRight = Util.getTileAhead(currentCell, currentDirection);
                continue;
                //currentCell = tileRight;
            } else {
                // miss wall
                currentDirection = Util.getTurnedOrientation(currentDirection, turnWhenLoseWall);
                Coordinate tileLeft = Util.getTileAhead(tileAhead, currentDirection);
                path.add(Util.cloneCoordinate(tileAhead));
                visited.add(Util.cloneCoordinate(tileAhead));
                currentCell = tileLeft;
            }
            path.add(Util.cloneCoordinate(currentCell));
            if (visited.contains(currentCell)) {
                break;
            } else {
                visited.add(Util.cloneCoordinate(currentCell));
            }
        }
        return path;

    }
    
    // TODO: move to PathFinderBase
    protected boolean isWall(int x, int y) {
        if (!mapManager.isWithinBoard(new Coordinate(x,y))) {
            return true;
        } else {
            Cell c = mapManager.getCell(x,y);
            return c.type == CellType.WALL;
        }

    }

    /**
     * Check to see if the road is narrow (eg. 1 tile wide) //TODO: is this correct?
     * 
     * @param x
     * @param y
     * @param movingDirection
     * @return
     */
    protected boolean isNarrowRoad(int x, int y, WorldSpatial.Direction movingDirection) {
        Coordinate left = Util.getTileAhead(new Coordinate(x,y), Util.getTurnedOrientation(movingDirection,WorldSpatial.RelativeDirection.LEFT));
        Coordinate right = Util.getTileAhead(new Coordinate(x,y), Util.getTurnedOrientation(movingDirection,WorldSpatial.RelativeDirection.RIGHT));
        return (isWall(left.x, left.y) && isWall(right.x, right.y));
    }

    
    /**
     * Get the side of the car where there is a wall
     * 
     * @param c
     * @param orientation
     * @return
     */
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
     * If a particular side of the car is next to wall
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
     * If any side of the car is next to a wall
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

