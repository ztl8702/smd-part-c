package mycontroller.navigator;


import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.SensorInfo;
import utilities.Coordinate;

import java.util.ArrayList;

public interface Navigator {
    void loadNewPath(ArrayList<Coordinate> path);

    void loadAutoPilots(ArrayList<AutoPilot> autoPilots);
    
    boolean isIdle();
    ActuatorAction update(float delta, SensorInfo carInfo);
    
    float getPercentageCompleted();

    /**
     * Request Navigator to stop navigating on the current path
     *
     * Note: this does not guarantee stopping immediately,
     * Navigator might be in some critical state, like turning, which
     * cannot be interrupted until completed.
     *
     * Always use isIdle() to check;
     */
    void requestInterrupt();

}
