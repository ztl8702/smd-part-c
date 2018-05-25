package mycontroller.pathfinder;

import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.World;

import java.util.*;

/**
 * A wrapper around AStarPathFinder to find the path to the closest
 * health tile.
 */
public class HealthPathFinder extends PathFinderBase {

    private static int ASTAR_SEARCH_DEPTH = 500;

    public HealthPathFinder(MapManagerInterface mapManager) {
        super(mapManager);
    }

    @Override
    public ArrayList<Coordinate> getPath(Coordinate startingPosition,
                                         Coordinate goalPosition,
                                         float startingSpeed,
                                         float startingAngle) {
        boolean isColdStart = startingSpeed < 0.1;
        PathFinder finisher = new AStarPathFinder(mapManager, ASTAR_SEARCH_DEPTH, World.MAP_WIDTH, World.MAP_HEIGHT);

        Set<Coordinate> healthTiles = mapManager.getHealthTiles();
        ArrayList<ArrayList<Coordinate>> healthPaths = new ArrayList<>(healthTiles.size());

        Coordinate currentPosition = startingPosition;
        if (isColdStart) {
            currentPosition = Util.getTileAhead(startingPosition, Util.angleToOrientation(startingAngle));
        }
        // initial position before search
        int cX = currentPosition.x;
        int cY = currentPosition.y;
        float lastAngle = startingAngle;

        for (Coordinate h : healthTiles) {
            Logger.printDebug("HealthPathFinder", h.toString());
            // update distance from current location to h
            ArrayList<Coordinate> path = finisher.getPath(new Coordinate(cX, cY), new Coordinate(h.x, h.y), startingSpeed, lastAngle);

            if (path != null) {
                if (isColdStart) {
                    path.add(0, Util.cloneCoordinate(startingPosition));
                }
                healthPaths.add(path);
            }
        }

        Collections.sort(healthPaths, (ArrayList a1, ArrayList a2) -> a1.size() - a2.size()); // smallest to biggest

        Logger.printDebug("HealthPathFinder", healthPaths.toString());

        if (!healthPaths.isEmpty()) {
            return healthPaths.get(0);
        }
        return null;
    }

}
