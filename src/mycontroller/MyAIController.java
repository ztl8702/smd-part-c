package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import mycontroller.autopilot.AutoPilotFactory;

import controller.CarController;

import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.common.Util;
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
import world.WorldSpatial;


public class MyAIController extends CarController {
    public enum State {Explore, Finish, Recover}

    private static final boolean DEBUG = false;

    private boolean finishExploring = false;
    private boolean executingExploringPath = false;


    private MapManagerInterface mapManager;
    private State currentState;
    private Navigator navigator;

    public MyAIController(Car car) {
        super(car);
        this.currentState = State.Explore;

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
        
        if (mapManager.foundAllKeys(this.getKey())) {
        	currentState = State.Finish;
        	finishExploring = true;
        }
        
        //TODO: move to Navigator
//		if (this.getHealth() <= 40) { // low health warning
//			currentState = State.Recover;
//		}
//		if (currentState == State.Recover && this.getHealth() == 100) { // recovered to full health
//			
//			Coordinate currentPosition = new Coordinate(this.getPosition());
//	        // initial position before search
//	        int cX = currentPosition.x;
//	        int cY = currentPosition.y;
//	        
//	        // check if still on health tile
//	        if (mapManager.getCell(cX, cY).type == CellType.HEALTH) {
//	        	if (finishExploring) {
//	        		currentState = State.Finish;
//	        	} else {
//	        		currentState = State.Explore;
//	        	}
//	        }
//		}
        

        if (!navigator.isIdle()) {
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

            if (executingExploringPath && mapManager.foundAllKeys(this.getKey())) {
                // no need to keep exploring now
                navigator.requestInterrupt();
			}
			// else if ( health is low ) {
			//  navigator.requestInterrupt();	
			//}
            
        } else {
        	
            // have not found solution, keep exploring
            if (currentState == State.Explore) {

                PathFinder wallFollower = new WallFollowingPathFinder(mapManager);

                ArrayList<Coordinate> path = wallFollower.getPath(
                        new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());

                navigator.loadNewPath(path);
                //RouteCompiler compiler = new DefaultRouteCompiler();
                executingExploringPath = true;
                //compiler.compile(path);
                //exit(-1);

            } 
            // found all keys, can now get remaining keys
 			else if (currentState == State.Finish) {
                executingExploringPath = false;
 				// once all keys have been found
 				ArrayList<Coordinate> path = getAStarPath();
 				navigator.loadNewPath(path); 				
 			}
 			// in recovery mode
 			else if (currentState == State.Recover) {
                executingExploringPath = false;
 				ArrayList<Coordinate> path = getHealthPath();
 				navigator.loadNewPath(path);
 			}
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

    /**
     * Way points are locations we need to visit
     *
     * @return
     */
    private Queue<Coordinate> createKeyWayPoints() {

        boolean isColdStart = this.getSpeed() < 0.1;

        Queue<Coordinate> wayPoints = new LinkedList<>();

        if (isColdStart) {
            // if the car is not moving, we must move ahead first.
            wayPoints.add(Util.getTileAhead(new Coordinate(this.getPosition()), this.getOrientation()));
        }

        // loop through all the keys and set key coordinate end location
        for (int i = this.getKey() - 1; i >= 1; i--) {
            Coordinate nextKeyToFind = mapManager.getKeyCoordinate(i);
            wayPoints.add(new Coordinate(nextKeyToFind.x, nextKeyToFind.y));
        }
        // finally add our finalTile

        Coordinate finishTile = mapManager.getFinishTile();
        wayPoints.add(new Coordinate(finishTile.x, finishTile.y));
        return wayPoints;
    }
    
    private Queue<Coordinate> createHealthWayPoints() {

        boolean isColdStart = this.getSpeed() < 0.1;

        Queue<Coordinate> wayPoints = new LinkedList<>();

        if (isColdStart) {
            // if the car is not moving, we must move ahead first.
            wayPoints.add(Util.getTileAhead(new Coordinate(this.getPosition()), this.getOrientation()));
        }
        
    	
    	
    	Set<Coordinate> healthTiles = mapManager.getHealthTiles();
		for (Coordinate h: healthTiles) {
			wayPoints.add(new Coordinate(h.x, h.y));
			// update distance from current location to h
		}
		
		// using distance, go to shortest
        

        // loop through all the keys and set key coordinate end location
        for (int i = this.getKey() - 1; i >= 1; i--) {
            Coordinate nextKeyToFind = mapManager.getKeyCoordinate(i);
            wayPoints.add(new Coordinate(nextKeyToFind.x, nextKeyToFind.y));
        }
        // finally add our finalTile

        Coordinate finishTile = mapManager.getFinishTile();
        wayPoints.add(new Coordinate(finishTile.x, finishTile.y));
        return wayPoints;
    }
    
    
    private ArrayList<Coordinate> getHealthPath() {
    	
    	int maxSearchDepth = 500;
        PathFinder finisher = new AStarPathFinder(mapManager, maxSearchDepth, World.MAP_WIDTH, World.MAP_HEIGHT);


//         <coordinate of health tile>, <path to health tile from current location>
//        HashMap<Coordinate, ArrayList<Coordinate>> healthPathMap = new HashMap<>();
        
        Set<Coordinate> healthTiles = mapManager.getHealthTiles();
        ArrayList<ArrayList<Coordinate>> healthPaths = new ArrayList<ArrayList<Coordinate>>(healthTiles.size());

        Coordinate currentPosition = new Coordinate(this.getPosition());
        // initial position before search
        int cX = currentPosition.x;
        int cY = currentPosition.y;
        float lastAngle = this.getAngle();
        
		for (Coordinate h: healthTiles) {
			// update distance from current location to h
			ArrayList<Coordinate> path = finisher.getPath(new Coordinate(cX, cY), new Coordinate(h.x, h.y), this.getSpeed(), lastAngle);
//			healthPaths.put(h, path);
			healthPaths.add(path);
		}
		
//		// using distance, go to shortest
//		Collections.sort(healthPaths);
//		
//		for (ArrayList<Coordinate> p : healthPaths.values()) {
//			p.size()
//		}
		
		
		return null;
    }

    /**
     * Gets a path to find all keys and to the finish tile,
     * by calling A* path finding algorithm
     *
     * @return
     */
    private ArrayList<Coordinate> getAStarPath() {

        int maxSearchDepth = 500;
        PathFinder finisher = new AStarPathFinder(mapManager, maxSearchDepth, World.MAP_WIDTH, World.MAP_HEIGHT);

        ArrayList<Coordinate> finalPath = new ArrayList<>();

        Queue<Coordinate> wayPoints = createKeyWayPoints();

        Coordinate currentPosition = new Coordinate(this.getPosition());
        // initial position before search
        int cX = currentPosition.x;
        int cY = currentPosition.y;
        float lastAngle = this.getAngle();

        // visit way points one by one
        while (!wayPoints.isEmpty()) {
            Coordinate nextWayPoint = wayPoints.remove();
            int goalX = nextWayPoint.x;
            int goalY = nextWayPoint.y;
            if (!(goalX == cX && goalY == cY)) {
                ArrayList<Coordinate> subPath = finisher.getPath(new Coordinate(cX, cY),
                        new Coordinate(goalX, goalY), this.getSpeed(), lastAngle);

                if (subPath != null) {
                    // gets the ending direction
                    WorldSpatial.Direction endingOrientation = Util.inferDirection(new Coordinate(goalX, goalY),
                            subPath.get(subPath.size() - 2));
                    lastAngle = Util.orientationToAngle(endingOrientation);


                    if (!finalPath.isEmpty()) {
                        // remove first coordinate to avoid repetition
                        subPath.remove(0);
                    }
                    finalPath.addAll(subPath);
                    cX = goalX;
                    cY = goalY;
                }
            }
        }
		return finalPath;		
	}
}