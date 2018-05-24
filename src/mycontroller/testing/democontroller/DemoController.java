package mycontroller.testing.democontroller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import controller.CarController;
import mycontroller.autopilot.*;
import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import sun.management.Sensor;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

/**
 * DemoController is used for testing autopilots
 */
public class DemoController extends CarController {

	private Navigator navigator;


	public DemoController(Car car) {
		super(car);
		navigator =  new
				DefaultNavigator();
		ArrayList<AutoPilot> upcomingOpts = new ArrayList<>();
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(2,3), new Coordinate(4,3), 0));
        upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(4,3), new Coordinate(6,3), 2));
        upcomingOpts.add(new TurningAutoPilot(new Coordinate(6,3), new Coordinate(7,4), WorldSpatial.RelativeDirection.LEFT));

		upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(7,3), new Coordinate(7,7), 5));
		upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(7,6), new Coordinate(7,7), 5));
		upcomingOpts.add(new ReCentreAutoPilot(ReCentreAutoPilot.CentringAxis.X, 7.4f));
		upcomingOpts.add(new ForwardToAutoPilot(new Coordinate(7,7), new Coordinate(7,12), 0));

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

        navigator.loadAutoPilots(upcomingOpts);
	}

	@Override
	public void update(float delta) {

		System.out.printf("delta=%.6f (%.6f, %.6f)\n", delta,this.getX(),this.getY());
		SensorInfo carInfo = SensorInfo.fromController(this);

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

}
