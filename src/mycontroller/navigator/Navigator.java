package mycontroller.navigator;


import mycontroller.autopilot.ActuatorAction;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;

public interface Navigator {
    void loadNewPath(ArrayList<Coordinate> path); // TODO: refactor so that car is not needed
    ActuatorAction update(float delta, Car car);
    float getPercentageCompleted();
    void interrupt();

}
