/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import mycontroller.common.Cell;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * FinisherPathFinder
 *
 * A composite (wrapper) PathFinder that uses A* under-the-hood
 * to find a path towards the finish tile, while collecting all
 * the keys.
 */
public class FinisherPathFinder extends PathFinderBase {

    /**
     * The key id that the car currently owns
     */
    private int currentKey = 0;

    public FinisherPathFinder(MapManagerInterface mapManager, int currentKey) {
        super(mapManager);
        this.currentKey = currentKey;

    }
    
    @Override
    public ArrayList<Coordinate> getPath(Coordinate startPosition, 
                                         Coordinate goalPosition, 
                                         float startingSpeed, 
                                         float startingAngle) {
        
        return doGetPath(startPosition, startingSpeed, startingAngle);
    }

    /**
     * Waypoints are locations we need to visit on our way to the finish tile
     *
     * @return
     */
    private Queue<Coordinate> createKeyWayPoints(Coordinate startingPosition, float startingSpeed,
                                                 WorldSpatial.Direction startingOrientation) {
        boolean isColdStart = startingSpeed < 0.1;
        Queue<Coordinate> wayPoints = new LinkedList<>();

        if (isColdStart) {

            // if the car is not moving, we must move ahead first.
            Coordinate locationAhead = Util.getTileAhead(startingPosition, startingOrientation);
            Cell cellAhead = mapManager.getCell(locationAhead.x, locationAhead.y);
            if (cellAhead !=null && cellAhead.type != Cell.CellType.WALL) {
                wayPoints.add(Util.getTileAhead(startingPosition, startingOrientation));
            } else {
                Logger.printWarning("FinisherPathFinder","Cold start but there is no space ahead!");
            }

        }

        // loop through all the keys and set key coordinate end location
        for (int i = this.currentKey - 1; i >= 1; i--) {
            Coordinate nextKeyToFind = mapManager.getKeyCoordinate(i);
            wayPoints.add(new Coordinate(nextKeyToFind.x, nextKeyToFind.y));
        }
        // finally add our finalTile

        Coordinate finishTile = mapManager.getFinishTile();
        wayPoints.add(new Coordinate(finishTile.x, finishTile.y));
        return wayPoints;
    }

    /**
     * Get starting and ending position and calling the getPath method to get final path
     *  
     * @param startPosition
     * @param startingSpeed
     * @param startingAngle
     * @return
     */
    private ArrayList<Coordinate> doGetPath(Coordinate startPosition, 
            float startingSpeed, float startingAngle) {

        PathFinder finisher = new AStarPathFinder2(mapManager,
                MAX_SEARCH_DEPTH, World.MAP_WIDTH, World.MAP_HEIGHT);

        ArrayList<Coordinate> finalPath = new ArrayList<>();

        Queue<Coordinate> wayPoints = createKeyWayPoints(startPosition,
                startingSpeed,
                Util.angleToOrientation(startingAngle));

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
                ArrayList<Coordinate> subPath = finisher.getPath(
                        new Coordinate(cX, cY),
                        new Coordinate(goalX, goalY),
                        startingSpeed, //TODO: change this
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
}
