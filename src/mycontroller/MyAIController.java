package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import mycontroller.autopilot.AutoPilotFactory;

import controller.CarController;

import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.common.Util;
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
	
	public enum State { Explore, Finish, Recover }
	
	private static final boolean DEBUG = false;
	
	private boolean startedMoving;


	
	
	private MapManagerInterface mapManager;
	private State currentState;
	private Navigator navigator;

	public MyAIController(Car car) {
		super(car);


		this.startedMoving = false;
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
		if (mapManager.foundAllKeys(this.getKey())) currentState = State.Finish;
			
		if (!navigator.isCurrentPathCompleted()) {
			ActuatorAction action = navigator.update(delta,SensorInfo.fromController(this));
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
		} else {
			// have not found solution, keep exploring
			if(currentState == State.Explore) {

				PathFinder wallFollower = new WallFollowingPathFinder(mapManager);

				ArrayList<Coordinate> path = wallFollower.getPath(
						new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());

				navigator.loadNewPath(path);
				//RouteCompiler compiler = new DefaultRouteCompiler();

				//compiler.compile(path);
				//exit(-1);
				
			} else {
				// once all keys have been found
				if (mapManager.foundAllKeys(this.getKey())) {
					ArrayList<Coordinate> path = getAStarPath();
					navigator.loadNewPath(path);
//					// print out the result
//					System.err.println("************************ASTAR***************** Path found!!!!");
//					System.err.println(finalPath.toString());
				}
				
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
     * @return
     */
	private Queue<Coordinate> createWayPoints() {

	    boolean isColdStart = this.getSpeed() <0.1;

        Queue<Coordinate> wayPoints = new LinkedList<>();

        if (isColdStart) {
            // if the car is not moving, we must move ahead first.
            wayPoints.add(Util.getTileAhead(new Coordinate(this.getPosition()), this.getOrientation()));
        }

        // loop through all the keys and set key coordinate end location
        for( int i = this.getKey()-1; i>=1; i-- ) {
            Coordinate nextKeyToFind = mapManager.getKeyCoordinate(i);
            wayPoints.add(new Coordinate(nextKeyToFind.x, nextKeyToFind.y));
        }
        // finally add our finalTile

        Coordinate finishTile = mapManager.getFinishTile();
        wayPoints.add(new Coordinate(finishTile.x, finishTile.y));
        return wayPoints;
    }

    /**
     * Gets a path to find all keys and to the finish tile,
     * by calling A* path finding algorithm
     * @return
     */
	private ArrayList<Coordinate> getAStarPath() {

        int maxSearchDepth = 500;
        PathFinder finisher = new AStarPathFinder(mapManager, maxSearchDepth, World.MAP_WIDTH, World.MAP_HEIGHT);

        ArrayList<Coordinate> finalPath = new ArrayList<>();

        Queue<Coordinate> wayPoints = createWayPoints();

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
            if ( !( goalX == cX && goalY == cY) ) {
                ArrayList<Coordinate> subPath = finisher.getPath(new Coordinate(cX, cY),
                        new Coordinate(goalX, goalY), this.getSpeed(), lastAngle);

                if (subPath != null) {
                    // gets the ending direction
                    WorldSpatial.Direction endingOrientation = Util.inferDirection(new Coordinate(goalX, goalY),
                            subPath.get(subPath.size()-2));
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
