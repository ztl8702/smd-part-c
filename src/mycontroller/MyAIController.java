package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import mycontroller.autopilot.AutoPilotFactory;
import org.apache.logging.log4j.core.util.SystemNanoClock;

import controller.CarController;

import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
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
		
		
		// have not found solution, keep exploring
		if(!startedMoving && currentState == State.Explore) {

			PathFinder wallFollower = new WallFollowingPathFinder(mapManager);

			ArrayList<Coordinate> path = wallFollower.getPath(
					new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());

			navigator.loadNewPath(path);
			//RouteCompiler compiler = new DefaultRouteCompiler();

			//compiler.compile(path);
			//exit(-1);
			startedMoving = true;
			
		} else {
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
		}
		
		
		// once all keys have been found
		if (mapManager.foundAllKeys(this.getKey())) {
			
			int maxSearchDepth = 500;
			PathFinder finisher = new AStarPathFinder(mapManager, maxSearchDepth, World.MAP_WIDTH, World.MAP_HEIGHT);
			
			ArrayList<Coordinate> finalPath = new ArrayList<>();
	        ArrayList<Coordinate> subPath = null;
	        
	        Coordinate currentPosition = new Coordinate(this.getPosition());
	        // initial position before search
	        int cX = currentPosition.x;
	        int cY = currentPosition.y;
	        		
			// loop through all the keys and set key coordinate end location
			for( int i = this.getKey()-1; i>=1; i-- ) {
				
				Coordinate k = mapManager.getKeyCoordinate(i);

				subPath = finisher.getPath(new Coordinate(cX, cY), 
						new Coordinate(k.x, k.y), this.getSpeed(), this.getAngle());
				
				if (subPath != null) {
					finalPath.addAll(subPath);
					subPath = null;

					cX = k.x;
					cY = k.y;
				}
				else {
					System.err.println("Problem finding path with astar" + "from" + cX + "," + cY + "to" + k.x + "," + k.y);
				}
			}
			
			
 			// done with getting all keys, now go to finish tile
			Coordinate finalKeyPosition = mapManager.getKeyCoordinate(1);
			Coordinate finishTile = mapManager.getFinishTile();
			
			
			if (finalKeyPosition == null) {			

				subPath = finisher.getPath(new Coordinate(this.getPosition()), 
					new Coordinate(finishTile.x, finishTile.y), this.getSpeed(), this.getAngle());

			} else {
				subPath = finisher.getPath(new Coordinate(finalKeyPosition.x, finalKeyPosition.y), 
					new Coordinate(finishTile.x, finishTile.y), this.getSpeed(), this.getAngle());

			}
			
			
			
			
			if (subPath != null) {
				finalPath.addAll(subPath);

			}
			// print out the result		
			System.err.println("************************ASTAR***************** Path found!!!!");
			System.err.println(finalPath.toString());
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

}
