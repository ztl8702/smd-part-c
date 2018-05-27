/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;
import utilities.Coordinate;
import mycontroller.mapmanager.MapManagerInterface;

/**
 * Base implementation of AutoPilot, providing some helper methods for its
 * subclasses.
 */
public abstract class AutoPilotBase implements AutoPilot {
    public static final double TILE_WIDTH = 1; // 32;

    /**
     * How far away should we shift our centre line from walls
     */
    public static final double WALL_BUFFER = 0.2;

    protected MapManagerInterface mapManager;

    public AutoPilotBase(MapManagerInterface mapManager) {
        this.mapManager = mapManager;
    }


    // Helper methods

    /**
     * Gets the x coordinate of the centre line of a Tile
     * @param tileX
     * @param tileY
     * @return
     */
    protected double getCentreLineX(int tileX, int tileY) {
        double offset = 0;
        if (mapManager.isWall(tileX+1, tileY)) {
            // wall directly on the left
            offset = -WALL_BUFFER;
        } else if (mapManager.isWall(tileX-1, tileY)) {
            // wall directly on the right
            offset = +WALL_BUFFER;
        } else if (mapManager.isWall(tileX+1, tileY+1) || mapManager.isWall(tileX+1, tileY-1)) {
            offset = -WALL_BUFFER;
        } else if (mapManager.isWall(tileX-1, tileY+1) || mapManager.isWall(tileX-1, tileY-1)) {
            offset = +WALL_BUFFER;
        }
        return TILE_WIDTH * (tileX) + offset;
    }

    /**
     * Gets the Y coordinate of the centre line of a Tile
     * @param tileX
     * @param tileY
     * @return
     */
    protected double getCentreLineY(int tileX, int tileY) {
        double offset = 0;
        if (mapManager.isWall(tileX, tileY-1)) {
            // wall directly on the south
            offset = +WALL_BUFFER;
        } else if (mapManager.isWall(tileX, tileY+1)) {
            // wall directly on the north
            offset  = -WALL_BUFFER;
        } else if (mapManager.isWall(tileX+1, tileY-1) || mapManager.isWall(tileX-1, tileY-1)){
            offset = +WALL_BUFFER;
        } else if (mapManager.isWall(tileX+1, tileY+1) || mapManager.isWall(tileX-1, tileY+1)) {
            offset = -WALL_BUFFER;
        }

        return TILE_WIDTH * (tileY) + offset;
    }

    @Override
    public boolean canBeSwappedOut() {
        return true;
    }
}
