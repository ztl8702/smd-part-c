/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import mycontroller.autopilot.AutoPilotFactory;

import world.Car;
import tiles.MapTile;
import utilities.Coordinate;
import controller.CarController;

import mycontroller.autopilot.AutoPilotFactory;
import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.common.Logger;
import mycontroller.common.Cell.CellType;
import mycontroller.mapmanager.MapManager;
import mycontroller.mapmanager.MapManagerInterface;
import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import mycontroller.pathfinder.*;

//TODO: VERY BIG TODO, can't hard code the value for when to switch to recovery mode, DISCUSS with Radium

/**
 * Our main controller class that keeps track of different states of the game
 * and delegates work to other classes to find path and execute car movement
 */
public class MyAIController extends CarController {

    private enum Goal { 
        Explore,    // explore the map (to see more tiles and find key locations)
        Finish,     // finish the game (to pick up keys and navigate to Finish tile)
        Recover     // to regain health
    }
    
    private  enum State { 
        ExecutingExploringPath, // on the way to explore the map
        ExecutingFinishPath,    // on the way to a key or to the Finish tile
        ExecutingRecoverPath,   // on the way to a health tile
        WaitingToRegainHealth,  // staying on a health tile
        Idle
    }

    private State currentState;
    private Goal currentGoal;
    private MapManagerInterface mapManager;
    private Navigator navigator;
    
    /**
     * MyAIController constructor that initialises 
     * the map for updating and
     * autopilot
     * @param car
     */
    public MyAIController(Car car) {
        super(car);

        this.currentState = State.Idle;
        this.currentGoal = Goal.Explore;

        mapManager = new MapManager();
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
                if (isLowHealth()) { // low health warning
                    changeGoal(Goal.Recover);
                }
                break;
            case Finish:
                if (isLowHealth()) { // low health warning
                    changeGoal(Goal.Recover);
                }
                break;
            case Recover:
                if (isEnoughHealth()) {
                    // health recovered, go back to ....
                    if (mapManager.foundAllKeys(this.getKey())) {
                        changeGoal(Goal.Finish);
                    } else {
                        changeGoal(Goal.Explore);
                    }
                }
                break;
        }

        // Should we interrupt the Navigator
        if ((currentState == State.ExecutingExploringPath || currentState == State.ExecutingFinishPath)
                && currentGoal == Goal.Recover) {
            // health is low, we need to stop
            navigator.requestInterrupt();
        }
        if (currentState == State.ExecutingExploringPath && currentGoal == Goal.Finish) {
            // don't need to explore anymore
            navigator.requestInterrupt();
        }

        // State change
        switch (currentState) {
            case Idle:
                switch (currentGoal) {
                    case Explore: {
                        PathFinder wallFollower = new WallFollowingPathFinder(mapManager);
                        ArrayList<Coordinate> path = wallFollower.getPath(
                                new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());
                        navigator.loadNewPath(path);
                        changeState(State.ExecutingExploringPath);
                    }
                    break;
                    case Finish: {
                        ArrayList<Coordinate> path = getAStarPath();
                        navigator.loadNewPath(path);
                        changeState(State.ExecutingFinishPath);
                    }
                    break;
                    case Recover: {
                        Logger.printDebug("MyAIController", "before getHealthPath");
                        ArrayList<Coordinate> path = getHealthPath();
                        Logger.printDebug("MyAIController", "after getHealthPath");
                        navigator.loadNewPath(path);
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
            // have not found solution, keep exploring

        }

        //TODO: make new ReverseMode Interrupt when car collide with wall
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
        return this.getHealth() <= 60;
    }

    /**
     * Check if car has regain enough health
     * 
     * @return
     */
    private boolean isEnoughHealth() {
        return this.getHealth() >= 99; // avoid rounding error
    }
}