/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;


import mycontroller.common.Logger;
import mycontroller.common.Util;
import mycontroller.mapmanager.MapManager;

/**
 * An AutoPilot that maintains the car speed at a given value.
 */
public class MaintainSpeedAutoPilot extends AutoPilotBase {

    /**
     * Error margin when comparing speed values
     */
	private static float SPEED_EPS = 0.005f;

    /**
     * For circuit breaker
     */
    private static float UNDERSPEED_TIMEOUT = 0.5f;

	/**
     * Target speed
     */
	private float target;

	private enum State {
		RecoveringWait, RecoveringReverse, Accelerating, Decelerating, Idle
	}

	private State state = State.Idle;

	/**
	 * Constructor
	 * 
	 * @param target The speed to maintain at.
	 */
	public MaintainSpeedAutoPilot(MapManager mapManager, float target) {
		super(mapManager);
		this.target = target;
	}

    /**
     * How long has the car been under-speed
     *
     * used for determining if the car is stuck
     */
	private float underSpeedTime = 0;
	private float recoveringWaitTime = 0;
	private float recoveringReverseTime = 0;

	@Override
	public ActuatorAction handle(float delta, SensorInfo carInfo) {
		float currentSpeed = carInfo.getSpeed();
		switch (this.state) {
		case Accelerating:
			if (currentSpeed < target) {
				if (currentSpeed < Util.STOPPED_THRESHOLD) {
					underSpeedTime += delta;
					if (underSpeedTime > UNDERSPEED_TIMEOUT) {
						// Circuit breaker mechanism: if the car does not move, we need to
						// back off for a while before pressing the pedal again.
						// This is needed due to a bug in the Simulation.
                        Logger.printWarning("MaintainSpeedAutoPilot","STUCK!!!!");
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
			Logger.printWarning("MaintainSpeedAutoPilot","RECOVERED!!");
			return ActuatorAction.backward();
		}
		return ActuatorAction.nothing();
	}

	private void changeState(State newState) {
		if (this.state != newState) {
			Logger.printInfo("MaintainSpeedAutoPilot","state change: " + this.state + " -> " + newState);
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
