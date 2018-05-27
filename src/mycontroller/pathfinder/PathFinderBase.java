/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import mycontroller.common.Cell;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.Queue;

/**
 * This abstract base class implements some common functionality shared by
 * PathFinder implementations
 */
public abstract class PathFinderBase implements PathFinder {

    protected MapManager mapManager;
    protected static int MAX_SEARCH_DEPTH = 500;

    public PathFinderBase(MapManager mapManager) {
        this.mapManager = mapManager;
    }


    /**
     * Calls a backend (another PathFinder) to construct path from wayPoints
     * @param backend
     * @param wayPoints
     * @param startPosition
     * @param startingSpeed
     * @param startingAngle
     * @return
     */
    protected ArrayList<Coordinate> wayPointsToPath(PathFinder backend, Queue<Coordinate> wayPoints, Coordinate startPosition,
                                                    float startingSpeed, float startingAngle) {
        ArrayList<Coordinate> finalPath = new ArrayList<>();

        // initial position before search
        int cX = startPosition.x;
        int cY = startPosition.y;
        float lastAngle = startingAngle;

        // visit way points one by one
        while (!wayPoints.isEmpty()) {
            Coordinate nextWayPoint = wayPoints.remove();
            int goalX = nextWayPoint.x;
            int goalY = nextWayPoint.y;
            if (!(goalX == cX && goalY == cY)) {
                ArrayList<Coordinate> subPath = backend.getPath(
                        new Coordinate(cX, cY),
                        new Coordinate(goalX, goalY),
                        startingSpeed, // can change this to optimise
                        lastAngle);

                if (subPath != null) {

                    // gets the ending direction
                    WorldSpatial.Direction endingOrientation =
                            Util.inferDirection(new Coordinate(goalX, goalY),
                                    subPath.get(subPath.size() - 2));

                    lastAngle = Util.orientationToAngle(endingOrientation);


                    if (!finalPath.isEmpty()) {
                        // remove first coordinate to avoid repetition
                        subPath.remove(0);
                    }
                    finalPath.addAll(subPath);
                    cX = goalX;
                    cY = goalY;
                }
            }
        }
        return finalPath;
    }

    /**
     * Whether a location is close to Lava
     * @param coord
     * @return
     */
    protected boolean closeToLava(Coordinate coord) {
        for (Coordinate delta: Util.DIRECTIONS_EIGHT) {
            Coordinate newCoord = new Coordinate(coord.x+delta.x , coord.y+delta.y);
            if (mapManager.isWithinBoard(newCoord)) {
                if (mapManager.getCell(newCoord.x, newCoord.y).type == Cell.CellType.LAVA){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a location is close to wall
     * @param coord
     * @return
     */
    protected boolean closeToWall(Coordinate coord) {

        for (Coordinate delta: Util.DIRECTIONS_EIGHT) {
            Coordinate newCoord = new Coordinate(coord.x+delta.x , coord.y+delta.y);
            if (mapManager.isWall(newCoord.x, newCoord.y)) {
                return true;
            }
        }
        return false;
    }

}
