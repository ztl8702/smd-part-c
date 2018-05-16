package controller;

import java.util.Random;

import world.Car;

public class MaintainSpeedOperator extends BaseOperator {
	public static float SPEED_EPS = 0.005f;
	private Random random = new Random();
	private float target;
	public enum State {RecoveringWait, RecoveringReverse, Accelerating, Decelerating, Idle}
	private State state = State.Idle;
	
	
	public MaintainSpeedOperator(float target) {
		this.target = target;
	}
	
	private float underSpeedTime = 0;
	private float recoveringWaitTime = 0;
	private float recoveringReverseTime = 0;
	@Override
	public OperatorAction handle(float delta, Car car) {
		float currentSpeed = car.getSpeed();
		System.out.printf("[%.4f] %.6f %.6f\n", delta,currentSpeed, car.getVelocity().len());
		
		switch (this.state) {
		case Accelerating:
			if (currentSpeed < target) {
				if (currentSpeed < 0.1f) {
					underSpeedTime += delta;	
					if (underSpeedTime > 0.5) {
						System.err.println("!!!!!!!!!!!!!!!!!!STUCK!!!!!!!!!!!!!!!!!");
						changeState(State.RecoveringWait);
						break;
					}
				}
				return OperatorAction.forward();
			}
			else {
				changeState(State.Idle);
			}
			break;
		case Decelerating:
			if (currentSpeed - SPEED_EPS > target) {
				return OperatorAction.backward();
			}
			else {
				changeState(State.Idle);
			}
			break;
		case Idle:
			if (currentSpeed + SPEED_EPS < target) {
				changeState(State.Accelerating);
				return OperatorAction.forward();
			} else if (currentSpeed - SPEED_EPS-0.2f > target) {
				changeState(State.Decelerating);
				return OperatorAction.brake();
			}
			else {
				return OperatorAction.nothing();
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
			return OperatorAction.backward();
			//break;
		}
		return OperatorAction.nothing();
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
			// transition actions:
			
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
