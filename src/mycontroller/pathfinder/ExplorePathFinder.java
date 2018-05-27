package mycontroller.pathfinder;

import mycontroller.common.Cell;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Composite PathFinder that uses AStarPathFinder under the hood
 * to find a path for exploring the map
 */
public class ExplorePathFinder extends PathFinderBase {

    private static final int MAX_POINTS = 5;

    public ExplorePathFinder(MapManager mapManager) {
        super(mapManager);
    }

    /**
     * Waypoints are points of interest we want to visit on our way
     *
     * @param startPosition
     * @param startingSpeed
     * @param startingOrientation
     * @return
     */
    private Queue<Coordinate> createExploreWayPoints(Coordinate startPosition, float startingSpeed,
                                                     WorldSpatial.Direction startingOrientation) {


        boolean isColdStart = startingSpeed < 0.1;
        Queue<Coordinate> wayPoints = new LinkedList<>();

        if (isColdStart) {

            // if the car is not moving, we must move ahead first.
            Coordinate locationAhead = Util.getTileAhead(startPosition, startingOrientation);
            Cell cellAhead = mapManager.getCell(locationAhead.x, locationAhead.y);
            if (cellAhead != null && cellAhead.type != Cell.CellType.WALL) {
                wayPoints.add(Util.getTileAhead(startPosition, startingOrientation));
            } else {
                Logger.printWarning("ExplorePathFinder", "Cold start but there is no space ahead!");
            }

        }

        // find the furthest away
        ArrayList<Coordinate> unseenTiles = new ArrayList<Coordinate>(mapManager.getUnseenLocations());

        if (unseenTiles.size() > 0) {

            unseenTiles.sort((Coordinate a, Coordinate b) -> {
                int distanceA = Util.dis(a, startPosition);
                int distanceB = Util.dis(b, startPosition);
                return distanceB - distanceA ;
            });

            // simple strategy, visit the furthest one
            int added = 0;
            // but avoid those close to Lava or Wall
            for ( int i = 0; i < unseenTiles.size(); ++i) {
                if (closeToLava(unseenTiles.get(i)) || closeToWall(unseenTiles.get(i))) {
                    continue;
                } else {
                    added += 1;
                    wayPoints.add(unseenTiles.get(i));
                    if (added>=MAX_POINTS) break;
                }
            }

        }

        return wayPoints;
    }

    @Override
    public ArrayList<Coordinate> getPath(Coordinate startPosition, Coordinate goalPosition, float startingSpeed, float startingAngle) {

        return wayPointsToPath(
                new AStarPathFinder(mapManager,
                        MAX_SEARCH_DEPTH, World.MAP_WIDTH, World.MAP_HEIGHT),
                createExploreWayPoints(startPosition, startingSpeed, Util.angleToOrientation(startingAngle)),
                startPosition,
                startingSpeed, startingAngle);

    }
}
