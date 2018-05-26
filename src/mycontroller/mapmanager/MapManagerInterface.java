/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.mapmanager;

import mycontroller.common.Cell;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.HashMap;
import java.util.Set;

/**
 * MapManager(Interface) is the "memory system" of our driving system
 * It remembers and constructs the map as the car sees more and more 
 * tiles.
 * 
 * And it responds to queries from RouteCompiler, AutoPilot and Navigator,
 * about information on the map.
 * 
 * It is actually intended to be a singleton, but as for why we make it an
 * interface rather than static class:
 *  - We want to have flexibility of swapping out different implementations.
 *    For example, when unittesting our path finding algorithms, we can use 
 *    a "mock" implementation of MapManagerInterface that loads the map from
 *    a text file (rather than getting it through exploring the interface).
 *    That way we can test our algorithms without the simulation system.
 * 
 *  - Also, by defining an interface explicitly,  it is clearer to our team
 *    what features a MapMananger should provide. 
 */
public interface MapManagerInterface {

    /**
     * Gets the Cell given a coordinate
     * @param x
     * @param y
     * @return
     */
    Cell getCell(int x, int y);

    /**
     * Whether a tile has been seen before or not.
     * If it is not seen, it is possible to be HealthTrap / LavaTrap
     * @param x
     * @param y
     * @return
     */
    boolean hasSeenTile(int x, int y);

    /**
     * Whether a tile is reachable or not.
     *
     * Note: unreachable tiles are considered as "seen"
     * before there is no benefit to see them.
     * @param x
     * @param y
     * @return
     */
    boolean isReachable(int x, int y);


    /**
     * Whether a tile is wall or not.
     *
     * NOTE: out of boundary tiles are treated as Walls
     * @param x
     * @param y
     * @return
     */
    boolean isWall(int x, int y);
    /**
     * Gets any one of the Finish Tile
     * @return
     */
    Coordinate getFinishTile();
    
    /**
     * Get coordinate of a specified key
     * @param keyNumber
     * @return
     */
    Coordinate getKeyCoordinate(int keyNumber);

    /**
     * Get locations not yet explored
     * @return
     */
    Set<Coordinate> getUnseenLocations();
    
    /**
     * Get location of health tiles
     * @return
     */
    Set<Coordinate> getHealthTiles();

    /**
     * Whether we have found all the keys we need;
     * @param currentKey
     * @return
     */
    boolean foundAllKeys(int currentKey);

    /**
     * Initialise map.
     * Should only be called once
     *
     * @param tiles
     */
    void initialMap(HashMap<Coordinate, MapTile> tiles);
    
    /**
     * Update map
     * @param tiles
     */
    void updateView(HashMap<Coordinate, MapTile> tiles);

    /**
     * Check if coordinate is within the board
     * @param coord
     * @return
     */
    boolean isWithinBoard(Coordinate coord);
}
