package mycontroller.autopilot;


import mycontroller.mapmanager.MapManagerInterface;

/**
 * An AutoPilot that maintains the car speed at a given value.
 */
public class MaintainSpeedAutoPilot extends BaseAutoPilot {
	public static float SPEED_EPS = 0.005f;
	private float target;

	public enum State {
		RecoveringWait, RecoveringReverse, Accelerating, Decelerating, Idle
	}

	private State state = State.Idle;

	/**
	 * Constructor
	 * 
	 * @param target The speed to maintain at.
	 */
	public MaintainSpeedAutoPilot(MapManagerInterface mapManager, float target) {
		super(mapManager);
		this.target = target;
	}

	private float underSpeedTime = 0;
	private float recoveringWaitTime = 0;
	private float recoveringReverseTime = 0;

	@Override
	public ActuatorAction handle(float delta, SensorInfo car) {
		float currentSpeed = car.getSpeed();
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
				return ActuatorAction.forward();
			} else {
				changeState(State.Idle);
			}
			break;
		case Decelerating:
			if (currentSpeed - SPEED_EPS > target) {
				return ActuatorAction.brake();
			} else {
				changeState(State.Idle);
			}
			break;
		case Idle:
			if (currentSpeed + SPEED_EPS < target) {
				changeState(State.Accelerating);
				return ActuatorAction.forward();
			} else if (currentSpeed - SPEED_EPS - 0.2f > target) {
				changeState(State.Decelerating);
				return ActuatorAction.brake();
			} else {
				return ActuatorAction.nothing();
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
			return ActuatorAction.backward();
		// break;
		}
		return ActuatorAction.nothing();
	}

	private void changeState(State newState) {
		if (this.state != newState) {
			if (DEBUG_AUTOPILOT) System.out.println("[MaintinSpeedOperator] state change: " + this.state + " -> " + newState);
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
