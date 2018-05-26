/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.WorldSpatial;

public class AutoPilotFactory {

    private static MapManagerInterface mapManager;

    public static void initialise(MapManagerInterface mapManager) {
        AutoPilotFactory.mapManager = mapManager;
    }

    public static AutoPilot forwardTo(Coordinate start, Coordinate finish, float speed) {
        return new ForwardToAutoPilot(mapManager, start, finish, speed);
    }

    public static AutoPilot turn(Coordinate start, Coordinate end, WorldSpatial.RelativeDirection whichWayToTurn,
            float speed) {
        return new TurningAutoPilot(mapManager, start, end, whichWayToTurn, speed);
    }

    public static AutoPilot maintainSpeed(float speed) {
        return new MaintainSpeedAutoPilot(mapManager, speed);
    }

    public static AutoPilot stop() {
        return new EmergencyStopAutoPilot(mapManager);
    }
}
