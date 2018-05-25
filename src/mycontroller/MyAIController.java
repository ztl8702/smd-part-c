package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import mycontroller.autopilot.AutoPilotFactory;

import controller.CarController;

import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.common.Logger;
import mycontroller.common.Cell.CellType;
import mycontroller.mapmanager.MapManager;
import mycontroller.mapmanager.MapManagerInterface;

import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import mycontroller.pathfinder.*;
import world.Car;
import world.World;
import tiles.MapTile;
import utilities.Coordinate;


public class MyAIController extends CarController {
    public enum Goal {
        Explore,
        Finish,
        Recover
    }

    public enum State {
        ExecutingExploringPath,
        ExecutingFinishPath,
        ExecutingRecoverPath,
        WaitingToRegainHealth,
        Idle
    }

    private static final boolean DEBUG = true;


    private MapManagerInterface mapManager;

    /**
     * State is what we are doing
     */
    private State currentState;
    /**
     * Goal is what we what to do
     */
    private Goal currentGoal;

    private Navigator navigator;

    public MyAIController(Car car) {
        super(car);

        this.currentState = State.Idle;
        this.currentGoal = Goal.Explore;

        mapManager = new MapManager();
        mapManager.initialMap(this.getMap());
        navigator = new DefaultNavigator();
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
                        if (DEBUG) System.err.println("i came in here");
                        ArrayList<Coordinate> path = getHealthPath();
                        if (DEBUG) System.err.println("found path");
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

        /**
         // car hit a wall
         //TODO maybe make a method to check ifCollided
         if(getSpeed() == 0 && startedMoving) {
         ReverseMode reverseMode = new ReverseMode();
         }

         // health is lower than danger threshold
         if(getHealth() <= DANGER_THRESHOLD) {
         RecoveryPathFinder recoveryPathFinder = new RecoveryPathFinder();
         }
         if (DEBUG) System.out.printf("current unseen count: %d\n", mapManager.getUnseen().size());
         **/
    }

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
     * @return
     */
    private ArrayList<Coordinate> getAStarPath() {
        return new FinisherPathFinder(mapManager, this.getKey()).getPath(
                new Coordinate(this.getPosition()),
                null, // finisher doesn't care about goalPosition, it will figure out by itself
                    this.getSpeed(),
                    this.getAngle()
                );
    }

    private ArrayList<Coordinate> getHealthPath() {
        return new HealthPathFinder(mapManager).getPath(
                new Coordinate(this.getPosition()),
                null, // we don't have a goal position here, it is up to HealthPathFinder to figure out
                this.getSpeed(),
                this.getAngle()
        );

    }

    private void changeGoal(Goal newGoal) {
        if (newGoal != currentGoal) {
            Logger.printDebug("MyAIController", "Change Goal: " + currentGoal + "->" + newGoal);
            currentGoal = newGoal;
        }
    }

    private void changeState(State newState) {
        if (newState != currentState) {
            Logger.printDebug("MyAIController", "Change State: " + currentState + "->" + newState);
            currentState = newState;
        }
    }

    /**
     * Whether the car is on a HealthTile
     *
     * @return
     */
    private boolean onHealthTile() {
        Coordinate now = new Coordinate(this.getPosition());
        return (mapManager.getCell(now.x, now.y)).type == CellType.HEALTH;
    }

    private boolean isLowHealth() {
        return this.getHealth() <= 60;
    }

    private boolean isEnoughHealth() {
        return this.getHealth() >= 100; // avoid rounding error
    }


}