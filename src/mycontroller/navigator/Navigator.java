/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.navigator;

import mycontroller.autopilot.ActuatorAction;
import mycontroller.autopilot.AutoPilot;
import mycontroller.autopilot.SensorInfo;
import utilities.Coordinate;

import java.util.ArrayList;

public interface Navigator {
	
    /**
     * Load the new path
     * 
     * @param path
     */
    void loadNewPath(ArrayList<Coordinate> path);

    /**
     * Load the new autoPilots
     * 
     * @param autoPilots
     */
    void loadAutoPilots(ArrayList<AutoPilot> autoPilots);
    
    /**
     * Check if car is idle
     * 
     * @return
     */
    boolean isIdle();
    
    /**
     * Update the AutoPilot with new actions
     * 
     * @param delta
     * @param carInfo
     * @return
     */
    ActuatorAction update(float delta, SensorInfo carInfo);
    
    /**
     * Get how much of the current path has been executed
     * 
     * @return
     */
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
