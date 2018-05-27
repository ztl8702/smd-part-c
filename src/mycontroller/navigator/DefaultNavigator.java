/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.navigator;

import mycontroller.autopilot.*;
import mycontroller.common.Cell;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;
import mycontroller.routecompiler.DefaultRouteCompiler;
import mycontroller.routecompiler.RouteCompiler;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Default (naive) implementation of the Navigator.
 *
 * (We planned to have different implementations, but didn't have the time.)
 *
 * More information is in the interface.
 * @see Navigator
 */
public class DefaultNavigator implements Navigator {

    private enum State {Idle, Navigating, AttemptingToStop}

    private boolean interruptRequested = false;
    private State state = State.Idle;

    private AutoPilot opt = null;
    private Queue<AutoPilot> upcomingOpts = new LinkedList<AutoPilot>();
    private MapManager mapManager;

    public DefaultNavigator(MapManager mapManager) {
        this.mapManager = mapManager;
    }


    @Override
    public void loadNewPath(ArrayList<Coordinate> path, boolean forceStop) {
        switch (state) {
            case Idle:
                if (path != null && !path.isEmpty()) {
                    this.opt = null;
                    RouteCompiler compiler = new DefaultRouteCompiler();
                    this.upcomingOpts = compiler.compile(path);
                    if (forceStop) {
                        upcomingOpts.add(AutoPilotFactory.stop());
                    }
                    this.changeState(State.Navigating);
                }
                break;
            case Navigating:
                Logger.printWarning("DefaultNavigator", "Attempting to loadNewPath while navigating!");
                break;
            case AttemptingToStop:
                Logger.printWarning("DefaultNavigator", "Attempting to loadNewPath while stopping!");
        }
    }

    @Override
    public void loadAutoPilots(ArrayList<AutoPilot> autoPilots) {
        this.opt = null;
        this.upcomingOpts = new LinkedList<>();
        for (AutoPilot a : autoPilots) {
            this.upcomingOpts.add(a);
        }
    }

    /**
     * Use the auto pilot to handle current update
     *
     * @param delta
     * @param car
     * @return
     */
    private ActuatorAction autoPilotHandle(float delta, SensorInfo car) {
        while (!upcomingOpts.isEmpty() && upcomingOpts.peek().canTakeCharge()
                && (this.opt == null || this.opt.canBeSwappedOut())) {
            info(upcomingOpts.peek() + "taking charge");
            this.opt = upcomingOpts.remove();
        }

        info(String.format("delta=%.6f (%.6f, %.6f)\n", delta, car.getX(), car.getY()));
        ActuatorAction action = ActuatorAction.nothing();

        if (opt != null) {
            action = opt.handle(delta, car);
        }
        info(String.format("Current AutoPilot: %s", opt));

        // look ahead by two
        if (!this.upcomingOpts.isEmpty()) {
            this.upcomingOpts.peek().handle(delta, car);

            if (upcomingOpts.size() >= 2) {
                ((LinkedList<AutoPilot>) upcomingOpts).get(1).handle(delta, car);
            }

        }

        return action;
    }

    @Override
    public ActuatorAction update(float delta, SensorInfo carInfo) {

        switch (state) {
            case Navigating:
                ActuatorAction action = autoPilotHandle(delta, carInfo);
                if (interruptRequested) {
                    if (canCurrentAutoPilotBeStopped()) {
                        Coordinate nextCell = Util.getTileAhead(carInfo.getCoordinate(), carInfo.getOrientation());
                        Coordinate nextnextCell = nextCell == null ? null : Util.getTileAhead(nextCell, carInfo.getOrientation());
                        int distanceToWall = wallAhead(carInfo.getTileX(), carInfo.getTileY(), carInfo.getOrientation()) - 1;
                        double stoppingDistance = Util.getStoppingDistance(carInfo.getSpeed(), 0);
                        if ((double) distanceToWall >= stoppingDistance && // make sure there is enough stopping distance
                                !lavaTilesAhead(carInfo.getTileX(),   // and there is no Lava on our "stopping zone"
                                        carInfo.getTileY(),
                                        carInfo.getOrientation(),
                                        (int) Math.ceil(distanceToWall))
                                ) {

                            ArrayList<AutoPilot> stopping = new ArrayList<>();

                            stopping.add(new EmergencyStopAutoPilot(mapManager));
                            this.loadAutoPilots(stopping);
                            changeState(State.AttemptingToStop);
                            interruptRequested = false;
                        }
                    }
                }
                if (hasNoMoreAutoPilots()) {
                    changeState(State.Idle);
                }
                return action;
            case AttemptingToStop:
                if (carInfo.getSpeed() < 0.1 && canCurrentAutoPilotBeStopped()) {
                    changeState(State.Idle);
                }
                return autoPilotHandle(delta, carInfo);
            default:
                return ActuatorAction.nothing();
        }
    }

    /**
     * How many tiles away from the wall ahead?
     *
     * @param x
     * @param y
     * @param direction
     * @return
     */
    private int wallAhead(int x, int y, WorldSpatial.Direction direction) {
        for (int i = 1; ; i++) {
            Coordinate ahead = Util.getTileAheadNth(new Coordinate(x, y), direction, i);
            if (mapManager.isWall(ahead.x, ahead.y)) {
                return i;
            }
        }
    }

    /**
     * Is there any lava tiles ahead? We don't want to interrupt on a lava tile.
     *
     * @param x
     * @param y
     * @param direction
     * @return
     */
    private boolean lavaTilesAhead(int x, int y, WorldSpatial.Direction direction, int distance) {
        for (int i = 0; i <= distance; ++i) {
            Coordinate location = Util.getTileAheadNth(new Coordinate(x, y), direction, distance);
            if (mapManager.isWithinBoard(location)) {
                if (mapManager.getCell(location.x, location.y).type == Cell.CellType.LAVA) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    @Override
    public float getPercentageCompleted() {
        return 0;
    }

    @Override
    public void requestInterrupt() {
        switch (state) {
            case Idle:
                // nothing to do, already stopped
                break;
            case Navigating:
                interruptRequested = true;
                break;
            case AttemptingToStop:
                // already trying to stop
                break;
        }
    }

    @Override
    public void forceInterrupt() {
        changeState(State.Idle);
        interruptRequested = false;
        opt = null;
        upcomingOpts = new LinkedList<>();
        changeState(State.Navigating);
    }

    @Override
    public boolean isIdle() {
        return this.state == State.Idle;
    }

    private boolean hasNoMoreAutoPilots() {
        if (this.upcomingOpts.isEmpty() && (this.opt == null || this.opt.canBeSwappedOut())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean canCurrentAutoPilotBeStopped() {
        if (this.opt == null || this.opt.canBeSwappedOut()) {
            return true;
        } else {
            return false;
        }
    }

    private void changeState(State newState) {
        if (state != newState) {
            info("Changing state " + state + "-> " + newState);
            state = newState;
        }
    }

    private void info(String message) {
        Logger.printDebug("DefaultNavigator", message);
    }
}
