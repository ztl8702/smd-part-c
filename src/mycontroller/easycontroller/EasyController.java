package mycontroller.easycontroller;

import java.util.LinkedList;
import java.util.Queue;

import controller.CarController;
import mycontroller.autopilot.MaintainSpeedAutoPilot;
import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.AutoPilotAction;
import mycontroller.autopilot.TurningAutoPilot;
import utilities.Coordinate;
import world.Car;

public class EasyController extends CarController {

	private EasyWindow controls;
	private AutoPilot opt = null;
	private Queue<AutoPilot> upcomingOpts = new LinkedList<AutoPilot>();

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
			AutoPilotAction action = opt.handle(delta, this.car);
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
				upcomingOpts.add(new TurningAutoPilot(new Coordinate(6,3), new Coordinate(7,4)));
				break;
			default:
				opt = null;
				break;
			}
	}
}
