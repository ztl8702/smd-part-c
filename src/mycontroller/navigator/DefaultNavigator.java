package mycontroller.navigator;

import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.AutoPilotAction;
import mycontroller.routecompiler.DefaultRouteCompiler;
import mycontroller.routecompiler.RouteCompiler;
import utilities.Coordinate;
import world.Car;

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
    public AutoPilotAction update(float delta, Car car) {
        while (!upcomingOpts.isEmpty() && upcomingOpts.peek().canTakeCharge() && (this.opt ==null || this.opt.canBeSwappedOut()) ) {
            System.out.println(upcomingOpts.peek()+"taking charge");
            this.opt = upcomingOpts.remove();
        }
        System.out.printf("delta=%.6f (%.6f, %.6f)\n", delta,car.getX(),car.getY());
        AutoPilotAction action = AutoPilotAction.nothing();
        if (opt!=null) {
            action = opt.handle(delta, car);
        }
        System.out.println(opt);
        for (AutoPilot o : this.upcomingOpts) {
            System.out.println(o);
            o.handle(delta, car);
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
}
