package mycontroller.pathfinder;

import mycontroller.mapmanager.MapManagerInterface;

/**
 * This abstract base class implements some common functionality shared by
 * PathFinder implementations
 */
public abstract class PathFinderBase implements PathFinder {

    // TODO: move common code between different path finders here

    protected MapManagerInterface mapManager;

    public PathFinderBase(MapManagerInterface mapManager) {
        this.mapManager = mapManager;
    }
}
