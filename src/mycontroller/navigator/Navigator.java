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
     * Check if Navigator is in an Idle state. 
     * 
     * Navigator will 
     * only accept new paths if it is in an Idle state.
     * 
     * @return
     */
    boolean isIdle();
    
    /**
     * This is the game update cycle function. MyAIController uses
     * this to delegate the decision making on each update cycle 
     * to Navigator.
     * 
     * Navigator is provided with information about the car,
     * and it returns an ActuatorAction as a result. 
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
