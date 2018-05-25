package mycontroller.autopilot;

import mycontroller.common.Cell;
import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;

/**
 * Base implementation of AutoPilot, providing some helper methods for its
 * subclasses.
 */
public abstract class AutoPilotBase implements AutoPilot {
    public static final double TILE_WIDTH = 1; // 32;
    
    public static boolean DEBUG_AUTOPILOT = false;

    public static final double WALL_BUFFER = 0.2;

    protected MapManagerInterface mapManager;

    public AutoPilotBase(MapManagerInterface mapManager) {
        this.mapManager = mapManager;
    }


    private boolean isWall(int x, int y) {
        if (!mapManager.isWithinBoard(new Coordinate(x,y))) {
            return false;
        } else {
            Cell c = mapManager.getCell(x,y);
            return c.type == Cell.CellType.WALL;
        }

    }
    // Helper methods
    protected double getCentreLineX(int tileX, int tileY) {
        double offset = 0;
        if (isWall(tileX+1, tileY) || isWall(tileX+1, tileY+1) || isWall(tileX+1, tileY-1)){
            offset = -WALL_BUFFER;
        } else if (isWall(tileX-1, tileY) || isWall(tileX-1, tileY+1) || isWall(tileX-1, tileY-1)) {
            offset  = WALL_BUFFER;
        }

        return TILE_WIDTH * (tileX) + offset;
    }

    protected double getCentreLineY(int tileX, int tileY) {
        double offset = 0;
        if (isWall(tileX, tileY-1) || isWall(tileX+1, tileY-1) || isWall(tileX-1, tileY-1)){
            offset = +WALL_BUFFER;
        } else if (isWall(tileX, tileY+1) || isWall(tileX+1, tileY+1) || isWall(tileX-1, tileY+1)) {
            offset  = -WALL_BUFFER;
        }

        return TILE_WIDTH * (tileY) + offset;
    }

    @Override
    public boolean canBeSwappedOut() {
        return true;
    }
}
