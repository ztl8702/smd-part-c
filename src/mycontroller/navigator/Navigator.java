package mycontroller.navigator;


import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.SensorInfo;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;

public interface Navigator {
    void loadNewPath(ArrayList<Coordinate> path); // TODO: refactor so that car is not needed
    ActuatorAction update(float delta, SensorInfo carInfo);
    float getPercentageCompleted();
    void interrupt();

}
