package mycontroller.autopilot;

import world.Car;

/**
 * Recentres a moving car to a certain position along the X or Y axis.
 */
public class ReCentreAutoPilot extends BaseAutoPilot {
    public enum CentringAxis {
        X, Y
    }

    public enum State {
        Idle, Turning, GoingStraight, TurningBack, Finished
    }

    private CentringAxis axis;
    private State state;
    private float target;

    public ReCentreAutoPilot(CentringAxis axis, float target) {
        this.axis = axis;
        state = State.Idle;
        this.target = target;
    }

    @Override
    public AutoPilotAction handle(float delta, Car car) {
        double x = car.getX();
        double y = car.getY();
        double angle = car.getAngle();

        switch (state) {
        case Idle:
            // Figure out the targets
            break;
        case Turning:
            break;
        }
        return AutoPilotAction.nothing();
    }

    @Override
    public boolean canTakeCharge() {
        return true;
    }

    private void changeState(State newState) {
        if (newState != state) {
            state = newState;
        }
    }

    /**
     * Gets the distance from the target centre link at which we need to start
     * turing back.
     *
     * @param theta turning angle (delta from the target orientation) in radians
     * @return
     */
    private double d(double theta) {
        // Some calculus
        // integrate sin(a-a*6/5/pi*x) from 0 to a*6/5/pi

        return 5.0 * Math.PI * (Math.cos(theta - (36.0 * theta * theta) / (25.0 * Math.PI * Math.PI)) - Math.cos(theta))
                / (6.0 * theta);
    }
}
