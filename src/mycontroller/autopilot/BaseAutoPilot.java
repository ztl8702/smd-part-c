package mycontroller.autopilot;

import mycontroller.autopilot.AutoPilot;

/**
 * Base implementation of AutoPilot, providing some helper methods for its
 * subclasses.
 */
public abstract class BaseAutoPilot implements AutoPilot {
    public static final double TILE_WIDTH = 1; // 32;

    // Helper methods
    protected double getCentreLineX(int tileX) {
        return TILE_WIDTH * (tileX);
    }

    protected double getCentreLineY(int tileY) {
        return TILE_WIDTH * (tileY);
    }

    @Override
    public boolean canBeSwappedOut() {
        return true;
    }
}
