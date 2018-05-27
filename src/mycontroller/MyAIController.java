/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller;

import java.util.*;

import mycontroller.autopilot.*;

import mycontroller.mapmanager.MapManager;
import world.Car;
import tiles.MapTile;
import utilities.Coordinate;
import controller.CarController;

import mycontroller.autopilot.AutoPilotFactory;
import mycontroller.common.Logger;
import mycontroller.common.Cell.CellType;
import mycontroller.mapmanager.DefaultMapManager;
import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import mycontroller.pathfinder.*;


/**
 * Our main controller class that keeps track of different states of the game
 * and delegates work to other classes to find path and execute car movement
 */
public class MyAIController extends CarController {

    private static final float ENOUGH_HEALTH = 100-1; // -1 to avoid rounding errors
    private static final float LOW_HEALTH = 60;

    private enum Goal { 
        Explore,    // explore the map (to see more tiles and find key locations)
        Finish,     // finish the game (to pick up keys and navigate to Finish tile)
        Recover,    // to regain health
        UnStuck     // to reverse when stuck or hitting the wall
    }
    
    private enum State {
        ExecutingExploringPath, // on the way to explore the map
        ExecutingFinishPath,    // on the way to a key or to the Finish tile
        ExecutingRecoverPath,   // on the way to a health tile
        WaitingToRegainHealth,  // staying on a health tile
        Reversing,              // reversing to recover from wall-hitting or stucking
        Idle
    }

    private State currentState;
    private Goal currentGoal;
    private MapManager mapManager;
    private Navigator navigator;

    private static final double STUCK_THRESHOLD = 1.0; // 1.0 seconds;
    private double stuckTimeCounter = 0.0;
    
    public MyAIController(Car car) {
        super(car);

        this.currentState = State.Idle;
        this.currentGoal = Goal.Explore;

        mapManager = new DefaultMapManager();
        mapManager.initialMap(this.getMap());
        navigator = new DefaultNavigator(mapManager);
        AutoPilotFactory.initialise(mapManager);

    }

    @Override
    public void update(float delta) {

        // gets what the car can see
        HashMap<Coordinate, MapTile> currentView = getView();

        // update the mapManager
        mapManager.updateView(currentView);
        navigatorUpdate(delta);

        // First, figure the goal
        switch (currentGoal) {
            case Explore:
                if (mapManager.foundAllKeys(this.getKey())) {
                    changeGoal(Goal.Finish);
                }
                if (isLowHealth() && seenHealthTile()) { // low health warning
                    // can only change to Recover mode
                    // if we know where is the HealthTile
                    // otherwise we can only wait to die
                    changeGoal(Goal.Recover);
                }
                break;
            case Finish:
                if (isLowHealth() && seenHealthTile()) { // low health warning
                    changeGoal(Goal.Recover);
                }
                break;
            case Recover:
                if (isEnoughHealth()) {
                    // health recovered, go back to either Finish or Explore
                    if (mapManager.foundAllKeys(this.getKey())) {
                        changeGoal(Goal.Finish);
                    } else {
                        changeGoal(Goal.Explore);
                    }
                }
                break;
            case UnStuck:
                if (!isStuck() && currentState == State.Idle) {
                    // unstucked, go back to either Finish or Explore
                    if (mapManager.foundAllKeys(this.getKey())) {
                        changeGoal(Goal.Finish);
                    } else {
                        changeGoal(Goal.Explore);
                    }
                }
        }

        // Should we interrupt the Navigator?
        if ((currentState == State.ExecutingExploringPath || currentState == State.ExecutingFinishPath)
                && currentGoal == Goal.Recover) {
            // health is low, we need to stop
            navigator.requestInterrupt();
        }
        if (currentState == State.ExecutingExploringPath && currentGoal == Goal.Finish) {
            // don't need to explore anymore
            navigator.requestInterrupt();
        }

        // Secondly, process State change
        switch (currentState) {
            case Idle:
                switch (currentGoal) {
                    case Explore: {
                        //PathFinder exploreFinder = new WallFollowingPathFinder(mapManager);
                        PathFinder exploreFinder = new ExplorePathFinder(mapManager);

                        ArrayList<Coordinate> path = exploreFinder.getPath(
                                new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());
                        navigator.loadNewPath(path, false);
                        changeState(State.ExecutingExploringPath);
                    }
                    break;
                    case Finish: {
                        ArrayList<Coordinate> path = getAStarPath();
                        navigator.loadNewPath(path, false);
                        changeState(State.ExecutingFinishPath);
                    }
                    break;
                    case Recover: {
                        Logger.printDebug("MyAIController", "before getHealthPath");
                        ArrayList<Coordinate> path = getHealthPath();
                        Logger.printDebug("MyAIController", "after getHealthPath");
                        navigator.loadNewPath(path, true);
                        changeState(State.ExecutingRecoverPath);
                    }
                    break;
                }
            case ExecutingExploringPath:
                if (navigator.isIdle()) {
                    changeState(State.Idle);
                }
                break;
            case ExecutingFinishPath:
                if (navigator.isIdle()) {
                    changeState(State.Idle);
                }
                break;
            case ExecutingRecoverPath:
                if (navigator.isIdle() && onHealthTile()) {
                    changeState(State.WaitingToRegainHealth);
                }
                break;
            case WaitingToRegainHealth:
                if (isEnoughHealth()) {
                    changeState(State.Idle);
                }
                break;
            case Reversing:
                if (navigator.isIdle()){
                    changeState(State.Idle);
                }
            // have not found solution, keep exploring

        }

        if (currentState == State.Idle
                || currentState == State.ExecutingExploringPath
                || currentState == State.ExecutingFinishPath) {
            if (isStopped()) {
                stuckTimeCounter += delta;
            } else {
                stuckTimeCounter = 0;
            }

        }

        if (isStuck()) {
            stuckTimeCounter = 0;
            changeGoal(Goal.UnStuck);
            if (currentState!=State.Reversing) {
                navigator.forceInterrupt();
                navigator.loadAutoPilots(new ArrayList<>(Arrays.asList(
                        new ReverseAutoPilot(mapManager)
                )));
                changeState(State.Reversing);
            }
        }
    }

    private boolean isStuck(){
        return this.stuckTimeCounter > STUCK_THRESHOLD;
    }

    private boolean isStopped() {
        return this.getSpeed() < 0.1;
    }

    /**
     * Do we know any HealthTile locations?
     * @return
     */
    private boolean seenHealthTile() {
        return mapManager.getHealthTiles().size() > 0;
    }

    /**
     * Ask the navigator to decide what action to do next.
     * (Navigator will then delegate to AutoPilot) 
     * 
     * @see DefaultNavigator
     * @see AutoPilot
     * 
     * @param delta
     */
    private void navigatorUpdate(float delta) {
        ActuatorAction action = navigator.update(delta, SensorInfo.fromController(this));
        if (action.brake) {
            this.applyBrake();
        }
        if (action.forward) {
            this.applyForwardAcceleration();
        }
        if (action.backward) {
            this.applyReverseAcceleration();
        }
        if (action.turnLeft) {
            this.turnLeft(delta);
        }
        if (action.turnRight) {
            this.turnRight(delta);
        }
    }

   /**
     * Gets a path to find all keys and to the finish tile,
     * by calling A* path finding algorithm
     *
     * @return ArrayList<Coordinate> path to goal 
     */
    private ArrayList<Coordinate> getAStarPath() {
        return new FinisherPathFinder(mapManager, this.getKey()).getPath(
                new Coordinate(this.getPosition()),
                null, // finisher doesn't care about goalPosition, it will figure out by itself
                    this.getSpeed(),
                    this.getAngle()
                );
    }

    /**
     * Finds the best path to a nearest health tile,
     * using A* search
     * 
     * @return ArrayList<Coordinate> path to goal
     */
    private ArrayList<Coordinate> getHealthPath() {
        return new HealthPathFinder(mapManager).getPath(
                new Coordinate(this.getPosition()),
                null, // we don't have a goal position here, it is up to HealthPathFinder to figure out
                this.getSpeed(),
                this.getAngle()
        );

    }

    /**
     * Change the currentGoal
     * 
     * @param newGoal
     */
    private void changeGoal(Goal newGoal) {
        if (newGoal != currentGoal) {
            Logger.printDebug("MyAIController", "Change Goal: " + currentGoal + "->" + newGoal);
            currentGoal = newGoal;
        }
    }

    /**
     * Change the currentState
     * 
     * @param newState
     */
    private void changeState(State newState) {
        if (newState != currentState) {
            Logger.printDebug("MyAIController", "Change State: " + currentState + "->" + newState);
            currentState = newState;
        }
    }

    /**
     * Check if car is on a HealthTile
     *
     * @return
     */
    private boolean onHealthTile() {
        Coordinate now = new Coordinate(this.getPosition());
        return (mapManager.getCell(now.x, now.y)).type == CellType.HEALTH;
    }

    /**
     * Check if car is low on health
     * 
     * @return
     */
    private boolean isLowHealth() {
        return this.getHealth() <= LOW_HEALTH;
    }

    /**
     * Check if car has regain enough health
     * 
     * @return
     */
    private boolean isEnoughHealth() {
        return this.getHealth() >= ENOUGH_HEALTH; // avoid rounding error
    }
}