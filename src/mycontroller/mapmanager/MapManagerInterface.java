package mycontroller.mapmanager;

import mycontroller.common.Cell;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.HashMap;

// TODO: We might remove this later
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
     * Gets any one of the Finish Tile
     * @return
     */
    Coordinate getFinishTile();


    Coordinate getKeyCoordinate(int keyNumber);

    /**
     * Whether we have found all the keys we need;
     * @param currentKey
     * @return
     */
    boolean foundAllKeys(int currentKey);


    void initialMap(HashMap<Coordinate, MapTile> tiles);
    void updateView(HashMap<Coordinate, MapTile> tiles);

    boolean isWithinBoard(Coordinate coord);



}
