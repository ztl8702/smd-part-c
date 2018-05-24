package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import controller.CarController;
import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.mapmanager.MapManager;
import mycontroller.mapmanager.MapManagerInterface;
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
		HashMap<Coordinate, MapTile> m = getMap();
		
		if (DEBUG) {
			System.out.printf("\n\n\n tiles = %d \n\n\n",m.size());
			m.forEach((k,v) -> System.out.println("key: "+k+" value:"+v+" type"+v.getType()));
		}
		
	}

	@Override
	public void update(float delta) {
		
		// gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		
		// update the mapManager
		//mapManager.updateView(currentView);
		
		
		// have not found solution, keep exploring
		if(!startedMoving) {
			//ExplorerPathFinder explorerPathFinder = new ExplorerPathFinder();
			PathFinder wallFollower = new WallFollowingPathFinder();
			System.out.println("\n\n\n\n\n\n====================================\n");

			ArrayList<Coordinate> path = wallFollower.getPath(
					new Coordinate(this.getPosition()),this.getSpeed(),this.getAngle());
			System.out.println("\n====================================\n\n\n\n\n");

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
