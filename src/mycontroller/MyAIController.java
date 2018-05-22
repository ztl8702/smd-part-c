package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import controller.CarController;
import mycontroller.pathfinder.*;
import mycontroller.routecompiler.DefaultRouteCompiler;
import mycontroller.routecompiler.RouteCompiler;
import world.Car;
import tiles.MapTile;
import utilities.Coordinate;

import static java.lang.System.exit;


public class MyAIController extends CarController {
	
	public enum State { Explore, Finish, Recover }
	
	private static final boolean DEBUG = false;
	
	private boolean startedMoving;
	
	private final int DANGER_THRESHOLD = 20;
	
	private MapManager mapManager;
	private State currentState;
	
	public MyAIController(Car car) {
		super(car);
		
		this.startedMoving = false;
		
		this.currentState = State.Explore;
		
		mapManager = new MapManager();
		mapManager.initialMap(this.getMap());
		mapManager.markReachable();
		
		
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
		mapManager.updateView(currentView);
		
		
		// have not found solution, keep exploring
		if(this.currentState == State.Explore) {
			//ExplorerPathFinder explorerPathFinder = new ExplorerPathFinder();
			PathFinder wallFollower = new WallFollowingPathFinder();
			System.out.println("\n\n\n\n\n\n====================================\n");

			ArrayList<Coordinate> path = wallFollower.getPath(mapManager.getMap(),
					new Coordinate(this.getPosition()),this.getSpeed(),this.getAngle());
			System.out.println("\n====================================\n\n\n\n\n");

			RouteCompiler compiler = new DefaultRouteCompiler();

			compiler.compile(path);
			exit(-1);
			startedMoving = true;
			
		}
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
		
	}

}
