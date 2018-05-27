/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.pathfinder;

import mycontroller.mapmanager.MapManager;

/**
 * This abstract base class implements some common functionality shared by
 * PathFinder implementations
 */
public abstract class PathFinderBase implements PathFinder {

    // TODO: move common code between different path finders here

    protected MapManager mapManager;
    protected static int MAX_SEARCH_DEPTH = 500;

    public PathFinderBase(MapManager mapManager) {
        this.mapManager = mapManager;
    }
}
