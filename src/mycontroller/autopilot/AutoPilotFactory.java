package mycontroller.autopilot;

import mycontroller.mapmanager.MapManagerInterface;
import utilities.Coordinate;
import world.WorldSpatial;

public class AutoPilotFactory {

    private static MapManagerInterface mapManager;

    public static void initialise(MapManagerInterface mapManager) {
        AutoPilotFactory.mapManager = mapManager;
    }

    public static AutoPilot forwardTo (Coordinate start, Coordinate finish, float speed) {
        return new ForwardToAutoPilot(mapManager, start, finish, speed);
    }


    public static AutoPilot turn(Coordinate start, Coordinate end, WorldSpatial.RelativeDirection whichWayToTurn){
        return new TurningAutoPilot(mapManager, start, end, whichWayToTurn);
    }
}

