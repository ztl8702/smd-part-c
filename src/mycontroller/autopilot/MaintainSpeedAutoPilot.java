package mycontroller.autopilot;

import java.util.Random;

import world.Car;

/**
 * An AutoPilot that maintains the car speed at a given value.
 */
public class MaintainSpeedAutoPilot extends BaseAutoPilot {
	public static float SPEED_EPS = 0.005f;
	private Random random = new Random();
	private float target;
	public enum State {RecoveringWait, RecoveringReverse, Accelerating, Decelerating, Idle}
	private State state = State.Idle;


	/**
	 * Constructor
	 * @param target The speed to maintain at.
	 */
	public MaintainSpeedAutoPilot(float target) {
		this.target = target;
	}

	private float underSpeedTime = 0;
	private float recoveringWaitTime = 0;
	private float recoveringReverseTime = 0;

	@Override
	public AutoPilotAction handle(float delta, Car car) {
		float currentSpeed = car.getSpeed();
		System.out.printf("[%.4f] %.6f %.6f\n", delta,currentSpeed, car.getVelocity().len());

		switch (this.state) {
		case Accelerating:
			if (currentSpeed < target) {
				if (currentSpeed < 0.1f) {
					underSpeedTime += delta;
					if (underSpeedTime > 0.5) {
						// Circuit breaker mechanism: if the car does not move, we need to
						// back off for a while before pressing the pedal again.
						// This is needed due to a bug in the Simulation.
						System.err.println("!!!!!!!!!!!!!!!!!!STUCK!!!!!!!!!!!!!!!!!");
						changeState(State.RecoveringWait);
						break;
					}
				}
				return AutoPilotAction.forward();
			}
			else {
				changeState(State.Idle);
			}
			break;
		case Decelerating:
			if (currentSpeed - SPEED_EPS > target) {
				return AutoPilotAction.brake();
			}
			else {
				changeState(State.Idle);
			}
			break;
		case Idle:
			if (currentSpeed + SPEED_EPS < target) {
				changeState(State.Accelerating);
				return AutoPilotAction.forward();
			} else if (currentSpeed - SPEED_EPS-0.2f > target) {
				changeState(State.Decelerating);
				return AutoPilotAction.brake();
			}
			else {
				return AutoPilotAction.nothing();
			}
		case RecoveringWait:
			recoveringWaitTime += delta;
			if (recoveringWaitTime > 1.0f) {
				changeState(State.RecoveringReverse);
			}
			break;
		case RecoveringReverse:
			changeState(State.Idle);
			System.err.println("!!!!!!!!!!!!!!!!!!RECOVERED!!!!!!!!!!!!!!!!!");
			return AutoPilotAction.backward();
			//break;
		}
		return AutoPilotAction.nothing();
	}

	private void changeState(State newState) {
		if (this.state != newState) {
			System.out.println("[MaintinSpeedOperator] state change: "+this.state+" -> "+newState);
			this.state = newState;

			if (newState == State.Accelerating) {
				this.underSpeedTime = 0;
			}
			if (newState == State.RecoveringWait) {
				this.recoveringWaitTime = 0;
			}
		}
	}

	@Override
	public boolean canTakeCharge() {
		return true;
	}

	@Override
	public String toString() {
		return "MaintainSpeedOperator [targetSpeed=" + target + "]";
	}

}