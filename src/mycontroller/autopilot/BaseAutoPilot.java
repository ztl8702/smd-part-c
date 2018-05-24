package mycontroller.autopilot;

import mycontroller.common.Cell;
import mycontroller.mapmanager.MapManagerInterface;

/**
 * Base implementation of AutoPilot, providing some helper methods for its
 * subclasses.
 */
public abstract class BaseAutoPilot implements AutoPilot {
    public static final double TILE_WIDTH = 1; // 32;
    
    public static boolean DEBUG_AUTOPILOT = false;

    public static final double WALL_BUFFER = 0.2;

    protected MapManagerInterface mapManager;

    public BaseAutoPilot(MapManagerInterface mapManager) {
        this.mapManager = mapManager;
    }

    // Helper methods
    protected double getCentreLineX(int tileX, int tileY) {
        double offset = 0;
        if (mapManager.getCell(tileX+1, tileY).type == Cell.CellType.WALL ||
        mapManager.getCell(tileX+1, tileY+1).type == Cell.CellType.WALL ||
        mapManager.getCell(tileX+1, tileY-1).type == Cell.CellType.WALL){
            offset = -WALL_BUFFER;
        } else if (mapManager.getCell(tileX-1, tileY).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX-1, tileY+1).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX-1, tileY-1).type == Cell.CellType.WALL) {
            offset  = WALL_BUFFER;
        }

        return TILE_WIDTH * (tileX) + offset;
    }

    protected double getCentreLineY(int tileX, int tileY) {
        double offset = 0;
        if (mapManager.getCell(tileX, tileY-1).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX+1, tileY-1).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX-1, tileY-1).type == Cell.CellType.WALL){
            offset = +WALL_BUFFER;
        } else if (mapManager.getCell(tileX, tileY+1).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX+1, tileY+1).type == Cell.CellType.WALL ||
                mapManager.getCell(tileX-1, tileY+1).type == Cell.CellType.WALL) {
            offset  = -WALL_BUFFER;
        }

        return TILE_WIDTH * (tileY) + offset;
    }

    @Override
    public boolean canBeSwappedOut() {
        return true;
    }
}
