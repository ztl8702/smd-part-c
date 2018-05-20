package mycontroller.autopilot;

import world.Car;

/**
 * AutoPilot, like the ones in aviation, are not fully auto, but rather semi-auto.
 *
 * Its responsibility is to abstract away the lower-level steering of the car,
 * and provide a high-level interface for controlling the vehicle.
 */
public interface AutoPilot {
    /**
     * This is called on each update cycle. The AutoPilot looks at the environment (via the Car object), and then
     * make a decision about what car controls to use at that cycle.
     * TODO: Consider replacing the `car` parameter with an object that encapsulates the information
     * needed for decision-making.
     *
     * @see AutoPilotAction
     * @param delta Seconds passed since last update
     * @param car
     * @return
     */
	public AutoPilotAction handle(float delta, Car car);

    /**
     * Lets the caller know whether this AutoPilot can take over the control.
     * Some types of AutoPilot (such as TurningAutoPilot) only takes over control at certain tiles.
     *
     * @return true if it is ready to take over control.
     */
	public boolean canTakeCharge();

	public boolean canBeSwappedOut();
	
	
}