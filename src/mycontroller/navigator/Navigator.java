package mycontroller.navigator;


import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.SensorInfo;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;

public interface Navigator {
    void loadNewPath(ArrayList<Coordinate> path);

    void loadAutoPilots(ArrayList<AutoPilot> autoPilots);
    boolean isCurrentPathCompleted();
    ActuatorAction update(float delta, SensorInfo carInfo);
    float getPercentageCompleted();
    void interrupt();

}
