package controller;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import utilities.Coordinate;
import world.Car;

public class EasyController extends CarController {

	private EasyWindow controls;
	private Operator opt = null;
	private Queue<Operator> upcomingOpts = new LinkedList<Operator>();

	//private Queue<String> pendingActions = new LinkedList<String>();
	String theAction = "";
	public EasyController(Car car) {
		super(car);
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
	}

	@Override
	public void update(float delta) {
		System.out.print(upcomingOpts.size());
		while (!upcomingOpts.isEmpty() && upcomingOpts.peek().canTakeCharge()) {
			System.out.println(upcomingOpts.peek()+"taking charge");
			this.opt = upcomingOpts.remove();
		}
		
		if (opt!=null) {
			OperatorAction action = opt.handle(delta, this.car);
			if (action.Brake) {
				this.applyBrake();
			}
			if (action.Forward) {
				this.applyForwardAcceleration();			
			}
			if (action.Backward) {
				this.applyReverseAcceleration();
			}
			if (action.TurnLeft) {
				this.turnLeft(delta);
			}
			if (action.TurnRight) {
				this.turnRight(delta);
			}
			
		}
		System.out.println(opt);
		for (Operator o : this.upcomingOpts) {
			System.out.println(o);
			o.handle(delta, car);
		}
		
	}
	
	private void addOperator(String newAction) {
			switch (newAction) {
			case "speed0":
				upcomingOpts.add(new MaintainSpeedOperator(0));
				break;
			case "speed1":
				upcomingOpts.add(new MaintainSpeedOperator(1));
				break;
			case "speed2":
				upcomingOpts.add(new MaintainSpeedOperator(2));
				break;
			case "turn":
				upcomingOpts.add(new TurningOperator(new Coordinate(6,3), new Coordinate(7,4)));
				break;
			default:
				opt = null;
				break;
			}
	}
}
