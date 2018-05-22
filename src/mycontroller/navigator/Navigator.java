package mycontroller.navigator;


import mycontroller.autopilot.AutoPilotAction;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;

public interface Navigator {
    void loadNewPath(ArrayList<Coordinate> path); // TODO: refactor so that car is not needed
    AutoPilotAction update(float delta,Car car);
    float getPercentageCompleted();
    void interrupt();

}
