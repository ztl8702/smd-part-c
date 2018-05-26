package mycontroller.routecompiler;

import mycontroller.autopilot.AutoPilot;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Optimising compiler that uses different speed values at
 * different turning locations,
 * in order to increase the overall speed.
 *
 *
 */
public class SpeedOptimisedRouteCompiler implements RouteCompiler {

    @Override
    public Queue<AutoPilot> compile(ArrayList<Coordinate> path) {
        return null;
    }
}
