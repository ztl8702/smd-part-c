package mycontroller.routecompiler;

import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.AutoPilotFactory;
import mycontroller.autopilot.ForwardToAutoPilot;
import mycontroller.autopilot.TurningAutoPilot;
import mycontroller.common.Logger;
import mycontroller.common.Util;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Naive RouteCompiler
 * <p>
 * Does the basic stuff, assumes speed is always 1.
 * <p>
 * Optimisations can be added later.
 */
public class DefaultRouteCompiler extends RouteCompilerBase {



    @Override
    public Queue<AutoPilot> compile(ArrayList<Coordinate> path) {
        LinkedList<AutoPilot> output = new LinkedList<>();

        Action lastAction = null;
        ArrayList<Action> actionList = new ArrayList<>();
        for (int i = 0; i < path.size(); ++i) {
            Coordinate thisCoord = path.get(i);
            Coordinate lastCoord = i - 1 >= 0 ? path.get(i - 1) : null;

            if (lastCoord == null) {
                // this is the first coordinate
                lastAction = new Action();
                lastAction.type = ActionType.GoStraight;
                lastAction.start = cloneCoordinate(thisCoord);
                lastAction.finish = cloneCoordinate(thisCoord);
                lastAction.goStraightDirection = null;
                // we don't know the direction yet
            } else {
                if (lastAction.type == ActionType.GoStraight) {
                    if (lastAction.goStraightDirection == null) {
                        // now we know the direction
                        WorldSpatial.Direction drt = Util.inferDirection(thisCoord, lastAction.finish);

                        lastAction.finish = cloneCoordinate(thisCoord);
                        lastAction.goStraightDirection = drt;

                    } else {
                        // we already have a direction, now check if we are on the same direction
                        WorldSpatial.Direction drt = Util.inferDirection(thisCoord, lastAction.finish);

                        if (drt == lastAction.goStraightDirection) {
                            // good, just append the new coordinate to the last action
                            lastAction.finish = cloneCoordinate(thisCoord);
                        } else {
                            // ah! we need to turn

                            // figure out which way to turn
                            WorldSpatial.RelativeDirection turningMode = whichWayToTurn(drt, lastAction.goStraightDirection);

                            Coordinate lastActionFinishBeforeBackOff = cloneCoordinate(lastAction.finish);
                            // lastAction back off by one tile
                            switch (lastAction.goStraightDirection) {
                                case EAST:
                                    lastAction.finish.x -= 1;
                                    break;
                                case WEST:
                                    lastAction.finish.x += 1;
                                    break;
                                case NORTH:
                                    lastAction.finish.y -= 1;
                                    break;
                                case SOUTH:
                                    lastAction.finish.y += 1;
                                    break;
                            }

                            actionList.add(lastAction);

                            lastAction = new Action();
                            if (turningMode == WorldSpatial.RelativeDirection.LEFT) {
                                lastAction.type = ActionType.TurnLeft;
                            } else {
                                lastAction.type = ActionType.TurnRight;
                            }

                            lastAction.start = cloneCoordinate(actionList.get(actionList.size() - 1).finish);
                            lastAction.finish = cloneCoordinate(thisCoord);

                            actionList.add(lastAction);
                            lastAction = new Action();
                            lastAction.start = cloneCoordinate(lastActionFinishBeforeBackOff);
                            lastAction.finish = cloneCoordinate(thisCoord);
                            lastAction.type = ActionType.GoStraight;
                            lastAction.goStraightDirection = Util.inferDirection(lastAction.finish, lastAction.start);
                        }

                    }
                } else {
                    // this should not happen
                } // if lastAction.type

            } // if lastCoord == null


        } // for

        if (lastAction != null) {
            actionList.add(lastAction);
            lastAction = null;
        }

        // filter out any no-ops ( going straight actions that starts and ends on the same cell;
        actionList.removeIf(action -> (action.type == ActionType.GoStraight && action.start.x == action.finish.x && action.start.y == action.finish.y));

        // process speed limits
        for (int i = 0; i < actionList.size(); ++i) {
            Action currentAction = actionList.get(i);
            Action prevAction = i-1 >=0 ? actionList.get(i-1) : null;
            Action nextAction = i+1 < actionList.size() ? actionList.get(i+1) : null;
            Action nextnextAction = i+2 < actionList.size() ? actionList.get(i+2) : null;

            if (i == actionList.size() -1) {
                // last action => must stop
                currentAction.speedLimit = 0f;

            } else if (currentAction.type == ActionType.GoStraight) {
                if ((nextAction.type == ActionType.TurnLeft || nextAction.type == ActionType.TurnRight)||
                        (nextnextAction == null || nextnextAction.type == ActionType.TurnLeft || nextnextAction.type == ActionType.TurnRight)) {
                    // following by consecutive turnings
                    currentAction.speedLimit = (float)TurningAutoPilot.MAX_TURNING_SPEED_U_TURN;
                } else if (nextAction.type == ActionType.TurnLeft || nextAction.type == ActionType.TurnRight) {
                    currentAction.speedLimit = (float)TurningAutoPilot.MAX_TURNING_SPEED;
                } else {
                    currentAction.speedLimit = (float)Util.MAX_CRUISING_SPEED;
                }
            } else if (currentAction.type == ActionType.TurnLeft || currentAction.type == ActionType.TurnRight) {
                if ((nextAction!=null && (nextAction.type == ActionType.TurnLeft || nextAction.type==ActionType.TurnRight)) ||
                        (prevAction!=null && (prevAction.type == ActionType.TurnLeft || prevAction.type==ActionType.TurnRight))) {
                    // part of a series of consecutive turnings
                    currentAction.speedLimit = (float) TurningAutoPilot.MAX_TURNING_SPEED_U_TURN;

                } else {
                    currentAction.speedLimit = (float) TurningAutoPilot.MAX_TURNING_SPEED;
                }
            }
            else {
                // this should not happen
                // if it does, warn the programmer

                Logger.printWarning("DefaultRouteCompiler", "Something wrong in speed calculation!");
            }
        }

        // convert to autopilots

        for (Action a : actionList
                ) {

            switch (a.type) {
                case GoStraight:
                    if (actionList.indexOf(a) == actionList.size() - 1) {
                        // last action => stop
                        output.add(AutoPilotFactory.forwardTo(a.start, a.finish, 0f));
                    } else {
                        output.add(AutoPilotFactory.forwardTo(a.start, a.finish, a.speedLimit));
                    }
                    break;
                case TurnLeft:
                    output.add(AutoPilotFactory.turn(a.start, a.finish, WorldSpatial.RelativeDirection.LEFT, a.speedLimit));
                    break;
                case TurnRight:
                    output.add(AutoPilotFactory.turn(a.start, a.finish, WorldSpatial.RelativeDirection.RIGHT, a.speedLimit));
                    break;
            }
        }

        printOutput(output);

        return output;
    }

   private WorldSpatial.RelativeDirection whichWayToTurn(WorldSpatial.Direction now, WorldSpatial.Direction prev) {
        switch (prev) {
            case EAST:
                if (now == WorldSpatial.Direction.NORTH) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (now == WorldSpatial.Direction.SOUTH) { // be explicit in "else" condition,
                    // so that if there is a bug lurking elsewhere,
                    // we can find it.
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
                break;
            case WEST:
                if (now == WorldSpatial.Direction.NORTH) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                } else if (now == WorldSpatial.Direction.SOUTH) {
                    return WorldSpatial.RelativeDirection.LEFT;
                }
                break;
            case NORTH:
                if (now == WorldSpatial.Direction.WEST) {
                    return WorldSpatial.RelativeDirection.LEFT;
                } else if (now == WorldSpatial.Direction.EAST) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                }
                break;
            case SOUTH:
                if (now == WorldSpatial.Direction.WEST) {
                    return WorldSpatial.RelativeDirection.RIGHT;
                } else if (now == WorldSpatial.Direction.EAST) {
                    return WorldSpatial.RelativeDirection.LEFT;
                }
                break;
        }
        return null;

    }

    /**
     * For debug
     */
    private static void printOutput(Queue<AutoPilot> autoPilots) {
        System.out.println("\n\n\n===================================\n");

        System.out.printf("We have %d AutoPilots in total: \n", autoPilots.size());

        AutoPilot tmp[] = autoPilots.toArray(new AutoPilot[autoPilots.size()]);

        for (int i = 0; i < tmp.length; ++i) {
            System.out.printf("\t%d:\t%s\n", i, tmp[i]);
        }

        System.out.println("\n===================================\n\n\n");

    }

    private Coordinate cloneCoordinate(Coordinate a) {
        return new Coordinate(a.x, a.y);
    }
}
