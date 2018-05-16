package controller;

import utilities.Coordinate;
import world.Car;

public class TurningOperator extends BaseOperator {

	public static final float MAINTAIN_SPEED = 1.0f;
	private Operator maintainSpeedOpt;
	private enum State {Waiting, ReachTurningSpeed, StartTurning, FinishedTurning};
	private State state;
	private Coordinate fromTile;
	private Coordinate toTile;
	
	
	public TurningOperator(Coordinate fromTile, Coordinate toTile) {
		// prototype, only support one turning type
		assert (fromTile.x+1 == toTile.x);
		assert (fromTile.y+1 == toTile.y);
		
		this.fromTile = fromTile;
		this.toTile = toTile;
		
		
		// let someone else care about the speed
		maintainSpeedOpt = new MaintainSpeedOperator(MAINTAIN_SPEED);
		state = State.Waiting;
	}
	
	@Override
	public OperatorAction handle(float delta, Car car) {
		OperatorAction speedOpt = this.maintainSpeedOpt.handle(delta, car);
		Coordinate coord = new Coordinate(car.getPosition());
		System.out.printf("toTileX=%d centreX=%f d=%f beforeTurn=%f currentX=%f\n", toTile.x, this.getCentreLineX(toTile.x),d(),this.getCentreLineX(toTile.x)-d(),car.getX());
		switch (this.state) {
		case Waiting: 
			if (coord.x >= fromTile.x-2) {
				changeState(State.ReachTurningSpeed);
			}
			break;
		case ReachTurningSpeed:
			if (car.getX()>= this.getCentreLineX(toTile.x)-d()) {
				changeState(State.StartTurning);
			}
			return speedOpt;
		case StartTurning:
			float a = car.getAngle();
			if (90-0.05f<=a && a <= 90 +0.05f) {
				changeState(State.FinishedTurning);
				return speedOpt;
			}
			OperatorAction oo = OperatorAction.combine(speedOpt, OperatorAction.turnLeft());
			oo.Backward = false;
			return oo;
		default:
			return speedOpt;
		}
		return OperatorAction.nothing();
	}
	
	private void changeState(State newState) {
		if (this.state != newState) {
			this.state = newState;
		}
	}
	
	public boolean canTakeCharge() {
		if (this.state == State.Waiting || this.state == State.FinishedTurning) {
			return false;
		}
		return true;
	}
	
	// distance before the centre line
	private double d() {
		return (6.0/5.0/ Math.PI) * (double)(MAINTAIN_SPEED);
	}

	@Override
	public String toString() {
		return "TurningOperator [fromTile=" + fromTile + ", toTile=" + toTile + ", state="+this.state+"]";
	}

}
