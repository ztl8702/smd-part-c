package mycontroller.navigator;

import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import mycontroller.common.Logger;
import mycontroller.routecompiler.DefaultRouteCompiler;
import mycontroller.routecompiler.RouteCompiler;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class DefaultNavigator implements Navigator {
    private AutoPilot opt = null;
    private Queue<AutoPilot> upcomingOpts = new LinkedList<AutoPilot>();
    @Override
    public void loadNewPath(ArrayList<Coordinate> path) {
        this.opt = null;
        RouteCompiler  compiler = new DefaultRouteCompiler();
        this.upcomingOpts = compiler.compile(path);
    }

    @Override
    public void loadAutoPilots(ArrayList<AutoPilot> autoPilots) {
        this.opt = null;
        this.upcomingOpts = new LinkedList<>();
        for (AutoPilot a : autoPilots){
            this.upcomingOpts.add(a);
        }
    }


    @Override
    public ActuatorAction update(float delta, SensorInfo car) {
        while (!upcomingOpts.isEmpty() && upcomingOpts.peek().canTakeCharge() && (this.opt ==null || this.opt.canBeSwappedOut()) ) {
            info(upcomingOpts.peek()+"taking charge");
            this.opt = upcomingOpts.remove();
        }

        info(String.format("delta=%.6f (%.6f, %.6f)\n", delta,car.getX(),car.getY()));
        ActuatorAction action = ActuatorAction.nothing();

        if (opt!=null) {
            action = opt.handle(delta, car);
        }
        info(String.format("Current AutoPilot: %s", opt));

        // only look ahead by one
        if (!this.upcomingOpts.isEmpty()){
            this.upcomingOpts.peek().handle(delta,car);
        }

        return action;
    }

    @Override
    public float getPercentageCompleted() {
        return 0;
    }

    @Override
    public void interrupt() {

    }

	@Override
	public boolean isCurrentPathCompleted() {
		if (this.upcomingOpts.isEmpty() && (this.opt == null || this.opt.canBeSwappedOut())) {
			return true;
		} else {
			return false;
		}
	}

	private void info(String message) {
        Logger.printInfo("DefaultNavigator", message);
    }
}
