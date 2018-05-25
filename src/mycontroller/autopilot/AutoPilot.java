package mycontroller.autopilot;


/**
 * AutoPilot, like the ones in aviation, are not fully auto, but rather
 * semi-auto.
 *
 * Its responsibility is to abstract away the lower-level steering of the car,
 * and provide a high-level interface for controlling the vehicle.
 */
public interface AutoPilot {
    /**
     * This is called on each update cycle. The AutoPilot looks at the environment
     * (via the Car object), and then make a decision about what car controls to use
     * at that cycle.
     * that encapsulates the information needed for decision-making.
     *
     * @see ActuatorAction
     * @param delta Seconds passed since last update
     * @param carStatus
     * @return
     */
    ActuatorAction handle(float delta, SensorInfo carStatus);

    /**
     * Lets the caller know whether this AutoPilot can take over the control. Some
     * types of AutoPilot (such as TurningAutoPilot) only takes over control at
     * certain tiles.
     *
     * @return true if it is ready to take over control.
     */
    public boolean canTakeCharge();

    /**
     * Lets the caller know whether this AutoPilot can be swapped out.
     * 
     * Sometimes, AutoPilots are in certain crtical states (like in the process of making a turn),
     * and should not be interrupted. Otherwise, the car will end up in an unrecoverable state (like
     * halfway in the turning trajectory).
     * 
     * @return true if it can be swapped out.
     */
    public boolean canBeSwappedOut();

}
