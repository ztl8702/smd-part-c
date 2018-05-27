/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.WorldSpatial;

/**
 * Pure fabrication that creates AutoPilot instances
 *
 * Because we need to inject mapManager into each AutoPilot
 */
public class AutoPilotFactory {

    private static MapManagerInterface mapManager;

    /**
     * Saves the reference to MapManager, so that it can be injected later
     * @param mapManager
     */
    public static void initialise(MapManagerInterface mapManager) {
        AutoPilotFactory.mapManager = mapManager;
    }

    /**
     * Creates a ForwardToAutoPilot
     * @param start
     * @param finish
     * @param speed
     * @return
     */
    public static AutoPilot forwardTo(Coordinate start, Coordinate finish, float speed) {
        return new ForwardToAutoPilot(mapManager, start, finish, speed);
    }

    /**
     * Creates a TurningAutoPilot
     * @param start
     * @param end
     * @param whichWayToTurn
     * @param speed
     * @return
     */
    public static AutoPilot turn(Coordinate start, Coordinate end, WorldSpatial.RelativeDirection whichWayToTurn,
            float speed) {
        return new TurningAutoPilot(mapManager, start, end, whichWayToTurn, speed);
    }

    /**
     * Creates a MaintainSpeedAutoPilot
     * @param speed
     * @return
     */
    public static AutoPilot maintainSpeed(float speed) {
        return new MaintainSpeedAutoPilot(mapManager, speed);
    }

    /**
     * Creates an EmergencyStopAutoPilot
     * @return
     */
    public static AutoPilot stop() {
        return new EmergencyStopAutoPilot(mapManager);
    }

    /**
     * Creates a RecentreAutoPilot
     */

    public static AutoPilot recentre(ReCentreAutoPilot.CentringAxis axis, float newCentreLine) {
        return new ReCentreAutoPilot(mapManager, axis, newCentreLine);
    }
}
