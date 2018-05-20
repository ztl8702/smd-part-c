package mycontroller.autopilot;

/**
 * The output of AutoPilot at each update cycle
 */
public class AutoPilotAction {
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

    public AutoPilotAction(boolean forward, boolean backward, boolean brake, boolean left, boolean right) {
        this.forward = forward;
        this.backward = backward;
        this.brake = brake;
        this.turnLeft = left;
        this.turnRight = right;
    }

    // Factory methods
    // These are shorthand methods for creating AutoPilotAction instances with
    // different parameters.

    /**
     * Creates an AutoPilotAction object with forward = true
     * 
     * @return
     */
    public static AutoPilotAction forward() {
        return new AutoPilotAction(true, false, false, false, false);
    }

    /**
     * Creates an AutoPilotAction object with backward = true
     * 
     * @return
     */
    public static AutoPilotAction backward() {
        return new AutoPilotAction(false, true, false, false, false);
    }

    /**
     * Creates an AutoPilotAction object with brake = true
     * 
     * @return
     */
    public static AutoPilotAction brake() {
        return new AutoPilotAction(false, false, true, false, false);
    }

    /**
     * Creates an AutoPilotAction object with turnLeft = true
     * 
     * @return
     */
    public static AutoPilotAction turnLeft() {
        return new AutoPilotAction(false, false, false, true, false);
    }

    /**
     * Creates an AutoPilotAction object with turnRight = true
     * 
     * @return
     */
    public static AutoPilotAction turnRight() {
        return new AutoPilotAction(false, false, false, false, true);
    }

    /**
     * Creates an AutoPilotAction object that represents a no-op
     * 
     * @return
     */
    public static AutoPilotAction nothing() {
        return new AutoPilotAction(false, false, false, false, false);
    }

    /**
     * Combines the outputs of two AutoPilots
     * 
     * @param a
     * @param b
     * @return
     */
    public static AutoPilotAction combine(AutoPilotAction a, AutoPilotAction b) {
        return new AutoPilotAction(a.forward || b.forward, a.backward || b.backward, a.brake || b.brake,
                a.turnLeft || b.turnLeft, a.turnRight || b.turnRight);
    }
}
