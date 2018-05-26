

/** TODO: POTENTIALL TO REMOVE **/


/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.testing.democontroller;

import java.util.ArrayList;

import controller.CarController;
import mycontroller.autopilot.*;
import mycontroller.mapmanager.MapManager;
import mycontroller.mapmanager.MapManagerInterface;
import mycontroller.navigator.DefaultNavigator;
import mycontroller.navigator.Navigator;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

/**
 * DemoController is used for testing autopilots
 */
public class DemoController extends CarController {
    
    private MapManagerInterface mapManager = new MapManager();
    private Navigator navigator = new DefaultNavigator(mapManager);


    public DemoController(Car car) {
        super(car);
        mapManager.initialMap(this.getMap());
        AutoPilotFactory.initialise(mapManager);

        ArrayList<AutoPilot> upcomingOpts = new ArrayList<>();
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(2, 3), new Coordinate(4, 3), 0));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(4, 3), new Coordinate(6, 3), 2));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(6, 3), new Coordinate(7, 4), WorldSpatial.RelativeDirection.LEFT,2));

        //upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(7, 3), new Coordinate(7, 12), 0));
        //upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(7, 6), new Coordinate(7, 7), 5));
        //upcomingOpts.add(new ReCentreAutoPilot(mapManager, ReCentreAutoPilot.CentringAxis.X, 7.4f));
       // upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(7, 7), new Coordinate(7, 12), 0));

        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(7, 4), new Coordinate(7, 12), 0));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(7, 12), new Coordinate(6, 13), WorldSpatial.RelativeDirection.LEFT,2));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(6, 13), new Coordinate(2, 13), 0));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(2, 13), new Coordinate(1, 14), WorldSpatial.RelativeDirection.RIGHT,2));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(1, 14), new Coordinate(1, 17), 0));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(1, 17), new Coordinate(2, 18), WorldSpatial.RelativeDirection.RIGHT,2));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(2, 18), new Coordinate(8, 18), 0));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(9, 18), new Coordinate(10, 17), WorldSpatial.RelativeDirection.RIGHT,2));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(10, 17), new Coordinate(10, 15), 1));
        // U Turn
        // currently U-turn only works at speed 1
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(10, 15), new Coordinate(11, 14), WorldSpatial.RelativeDirection.LEFT,2));
        upcomingOpts.add(new TurningAutoPilot(mapManager, new Coordinate(10, 14), new Coordinate(11, 15), WorldSpatial.RelativeDirection.LEFT,2));
        upcomingOpts.add(new ForwardToAutoPilot(mapManager, new Coordinate(11, 15), new Coordinate(11, 17), 0));

        navigator.loadAutoPilots(upcomingOpts);
    }

    @Override
    public void update(float delta) {

        System.out.printf("delta=%.6f (%.6f, %.6f)\n", delta, this.getX(), this.getY());
        mapManager.updateView(this.getView());
        SensorInfo carInfo = SensorInfo.fromController(this);

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
    }

}
