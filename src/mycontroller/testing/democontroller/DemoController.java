package mycontroller.testing.democontroller;

import java.util.LinkedList;
import java.util.Queue;

import controller.CarController;
import mycontroller.autopilot.*;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

/**
 * DemoController is used for testing autopilots
 */
public class DemoController extends CarController {

	private EasyWindow controls;
	private AutoPilot opt = null;
	private Queue<AutoPilot> upcomingOpts = new LinkedList<AutoPilot>();

	//private Queue<String> pendingActions = new LinkedList<String>();
	String theAction = "";

	private Car car;

	public DemoController(Car car) {
		super(car);
		this.car = car; // TODO: this is a hack, refactor later

		controls = new EasyWindow();
		controls.onSetSpeed1 = () ->{
			System.out.println("\n\n\nSpeed1\n\n\n");
			//pendingActions.add("speed1"); 
			addOperator("speed1");
		};
		controls.onSetSpeed2 = () ->{
			addOperator("speed2");
		};
		controls.onSetSpeed0 = () ->{
			addOperator("speed0");
		};
		controls.onSetTurn = () ->{
			addOperator("turn");
		};
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(2,3), new Coordinate(4,3), 0));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(4,3), new Coordinate(6,3), 2));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(6,3), new Coordinate(7,4), WorldSpatial.RelativeDirection.LEFT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(7,3), new Coordinate(7,12), 0));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(7,12), new Coordinate(6,13), WorldSpatial.RelativeDirection.LEFT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(6,13), new Coordinate(2,13), 0));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(2,13), new Coordinate(1,14), WorldSpatial.RelativeDirection.RIGHT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(1,14), new Coordinate(1,17), 0));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(1,17), new Coordinate(2,18), WorldSpatial.RelativeDirection.RIGHT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(2,18), new Coordinate(8,18), 0));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(9,18), new Coordinate(10,17), WorldSpatial.RelativeDirection.RIGHT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(10,17), new Coordinate(10,15), 1));
        // U Turn
		// currently U-turn only works at speed 1
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(10,15), new Coordinate(11,14), WorldSpatial.RelativeDirection.LEFT));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(10,14), new Coordinate(11,15), WorldSpatial.RelativeDirection.LEFT));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(11,15), new Coordinate(11,17), 0));
	}

	@Override
	public void update(float delta) {
		System.out.print(upcomingOpts.size());
		while (!upcomingOpts.isEmpty() && upcomingOpts.peek().canTakeCharge() && (this.opt ==null || this.opt.canBeSwappedOut()) ) {
			System.out.println(upcomingOpts.peek()+"taking charge");
			this.opt = upcomingOpts.remove();
		}
		System.out.printf("delta=%.6f (%.6f, %.6f)\n", delta,car.getX(),car.getY());
		if (opt!=null) {
			ActuatorAction action = opt.handle(delta, this.car);
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
		System.out.println(opt);
		for (AutoPilot o : this.upcomingOpts) {
			System.out.println(o);
			o.handle(delta, car);
		}
		
	}
	
	private void addOperator(String newAction) {
			switch (newAction) {
			case "speed0":
				upcomingOpts.add(new MaintainSpeedAutoPilot(0));
				break;
			case "speed1":
				upcomingOpts.add(new MaintainSpeedAutoPilot(1));
				break;
			case "speed2":
				upcomingOpts.add(new MaintainSpeedAutoPilot(2));
				break;
			case "turn":
				upcomingOpts.add(new TurningAutoPilot(new Coordinate(6,3), new Coordinate(7,4),WorldSpatial.RelativeDirection.LEFT));
				break;
			default:
				opt = null;
				break;
			}
	}
}
