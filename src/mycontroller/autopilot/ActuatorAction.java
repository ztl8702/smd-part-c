/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

/**
 * The output of AutoPilot
 *
 * This is a mediator between AutoPilot, Navigator and Controller,
 * so that AutoPilot is not manipulating the car controls directly.
 */
public class ActuatorAction {
    /**
     * Should the forward acceleration be applied.
     */
    public boolean forward;
    /**
     * Should the reverse acceleration be applied.
     */
    public boolean backward;
    /**
     * Should the brake be applied;
     */
    public boolean brake;
    /**
     * Should the vehicle turn left.
     */
    public boolean turnLeft;
    /**
     * Should the vehicle turn right.
     */
    public boolean turnRight;

    public ActuatorAction(boolean forward, boolean backward, boolean brake, boolean left, boolean right) {
        this.forward = forward;
        this.backward = backward;
        this.brake = brake;
        this.turnLeft = left;
        this.turnRight = right;
    }

    // Factory methods:
    // These are shorthand methods for creating ActuatorAction instances with
    // different parameters.

    /**
     * Creates an ActuatorAction object with forward = true
     * 
     * @return
     */
    public static ActuatorAction forward() {
        return new ActuatorAction(true, false, false, false, false);
    }

    /**
     * Creates an ActuatorAction object with backward = true
     * 
     * @return
     */
    public static ActuatorAction backward() {
        return new ActuatorAction(false, true, false, false, false);
    }

    /**
     * Creates an ActuatorAction object with brake = true
     * 
     * @return
     */
    public static ActuatorAction brake() {
        return new ActuatorAction(false, false, true, false, false);
    }

    /**
     * Creates an ActuatorAction object with turnLeft = true
     * 
     * @return
     */
    public static ActuatorAction turnLeft() {
        return new ActuatorAction(false, false, false, true, false);
    }

    /**
     * Creates an ActuatorAction object with turnRight = true
     * 
     * @return
     */
    public static ActuatorAction turnRight() {
        return new ActuatorAction(false, false, false, false, true);
    }

    /**
     * Creates an ActuatorAction object that represents a no-op
     * 
     * @return
     */
    public static ActuatorAction nothing() {
        return new ActuatorAction(false, false, false, false, false);
    }

    /**
     * Combines the outputs of two AutoPilots
     * 
     * @param a
     * @param b
     * @return
     */
    public static ActuatorAction combine(ActuatorAction a, ActuatorAction b) {
        return new ActuatorAction(a.forward || b.forward, a.backward || b.backward, a.brake || b.brake,
                a.turnLeft || b.turnLeft, a.turnRight || b.turnRight);
    }
}
