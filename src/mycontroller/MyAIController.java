package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.math.Path;

import controller.CarController;
import mycontroller.autopilot.AutoPilotAction;
import mycontroller.common.Cell;
import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import mycontroller.pathfinder.*;
import world.Car;
import tiles.MapTile;
import utilities.Coordinate;



public class MyAIController extends CarController {
	
	public enum State { Explore, Finish, Recover }
	
	private static final boolean DEBUG = false;
	
	private boolean startedMoving;
	
	private final int DANGER_THRESHOLD = 20;
	
	private MapManager mapManager;
	private State currentState;
	private Navigator navigator;

	private Car car; //TODO: refactor and remove this

	public MyAIController(Car car) {
		super(car);
		this.car = car;

		this.startedMoving = false;
		this.currentState = State.Explore;
		
		mapManager = new MapManager();
		mapManager.initialMap(this.getMap());
		mapManager.markReachable();

		navigator = new DefaultNavigator();
		
		
//		HashMap<Coordinate, MapTile> m = getMap();
//		
//		if (DEBUG) {
//			System.out.printf("\n\n\n tiles = %d \n\n\n",m.size());
//			m.forEach((k,v) -> System.out.println("key: "+k+" value:"+v+" type"+v.getType()));
//		}
		
	}

	@Override
	public void update(float delta) {
		
		// gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		
		// update key to mapManager
		mapManager.updateKey(this.getKey());

		// update the mapManager
		mapManager.updateView(currentView);
		
		
		// have not found solution, keep exploring
		if(!startedMoving && currentState == State.Explore) {

			PathFinder wallFollower = new WallFollowingPathFinder();

			ArrayList<Coordinate> path = wallFollower.getPath(mapManager.getMap(),
					new Coordinate(this.getPosition()), null, this.getSpeed(), this.getAngle());

			navigator.loadNewPath(path);
			//RouteCompiler compiler = new DefaultRouteCompiler();

			//compiler.compile(path);
			//exit(-1);
			startedMoving = true;
			
		} else {
			AutoPilotAction action = navigator.update(delta,car);
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
		if (mapManager.foundAllKeys()) {
			
			int maxSearchDepth = 500;
			PathFinder finisher = new AStarPathFinder(maxSearchDepth);
			
			
			System.out.println("\n\n\n\n\n\n====================================\n");
			
			ArrayList<Coordinate> finalPath = new ArrayList<>();
	        ArrayList<Coordinate> subPath = null;
	        
	        Coordinate currentPosition = new Coordinate(this.getPosition());
	        // initial position before search
	        int cX = currentPosition.x;
	        int cY = currentPosition.y;
	        		
			/* loop through all the keys and set key coordinate end location */
			for( int i = this.getKey()-1; i>=1; i-- ) {
				
				Coordinate k = mapManager.findKey(i);
				if (k == null) {
					System.err.println("cant locate key position" + i);
				} else {
//					System.out.println("finding key number" + i);
				}

				subPath = finisher.getPath(mapManager.getMap(), new Coordinate(cX, cY), 
						new Coordinate(k.x, k.y), this.getSpeed(), this.getAngle());
				
				System.err.println(mapManager.printBoard());
				
				if (subPath != null) {
//					System.out.println("found path for :" + "from" + cX+cY+"to"+keyPosition.toString());
					finalPath.addAll(subPath);
//					System.out.println(finalPath.toString());
					cX = k.x;
					cY = k.y;
				}
				else {
					System.err.println("Problem finding path with astar" + "from" + cX + "," + cY + "to" + k.x + "," + k.y);
				}
			}
			// done with getting all keys, now go to finish tile
			Coordinate finalKeyPosition = mapManager.findKey(1);
			Coordinate finishTile = mapManager.getFinishTile();
			subPath = finisher.getPath(mapManager.getMap(), new Coordinate(finalKeyPosition.x, finalKeyPosition.y), 
					new Coordinate(finishTile.x, finishTile.y), this.getSpeed(), this.getAngle());
			System.err.println(mapManager.printBoard());
			
//			System.out.println("managed to get final subpath");
			if (subPath != null) {
				finalPath.addAll(subPath);
			}
			
//			System.out.println(finalPath.toString());
			
			
			// print out the result		
			System.out.println("*****ASTAR***** Path found!!!!");
			System.out.println(finalPath.toString());
//			for (Coordinate c : finalPath) {
//				System.out.printf("(%d,%d)->", c.x, c.y);
//			}

			System.out.println("\n====================================\n\n\n\n\n");

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
		
		
		
		if(mapManager.foundSolution()) {
			FinisherPathFinder finisherPathFinder = new FinisherPathFinder();
			
//			new Node(parentNode, goalNode, gCost, x, y)
//			finisherPathFinder.aStarSearch(Node startNode, Node goalNode);
		}
		
		
		if (DEBUG) System.out.printf("current unseen count: %d\n", mapManager.getUnseen().size());	
		**/
	}

}
