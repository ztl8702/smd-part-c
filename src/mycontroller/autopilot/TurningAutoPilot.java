package mycontroller.autopilot;

import utilities.Coordinate;
import world.Car;

public class TurningAutoPilot extends BaseAutoPilot {

	public static final float MAINTAIN_SPEED = 2f;
	private AutoPilot maintainSpeedOpt;
	private enum State {Waiting, ReachTurningSpeed, StartTurning, FinishedTurning}
	private State state;
	private Coordinate fromTile;
	private Coordinate toTile;
	
	
	public TurningAutoPilot(Coordinate fromTile, Coordinate toTile) {
		// prototype, only support one turning type
		assert (fromTile.x+1 == toTile.x);
		assert (fromTile.y+1 == toTile.y);
		
		this.fromTile = fromTile;
		this.toTile = toTile;
		
		
		// let someone else care about the speed
		maintainSpeedOpt = new MaintainSpeedAutoPilot(MAINTAIN_SPEED);
		state = State.Waiting;
	}
	
	@Override
	public AutoPilotAction handle(float delta, Car car) {
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
			break;
		case StartTurning:
			float a = car.getAngle();
			if (90-0.05f<=a && a <= 90 +0.05f) {
				changeState(State.FinishedTurning);
			}
			break;

		}
        AutoPilotAction speedOpt = this.maintainSpeedOpt.handle(delta, car);

        switch(state){
            case Waiting:
                return AutoPilotAction.nothing();
            case ReachTurningSpeed:
                return speedOpt;
            case StartTurning:
                AutoPilotAction output = AutoPilotAction.combine(speedOpt, AutoPilotAction.turnLeft());
                // Overwrite the backward attribute:
                // We should never reverse+turn at the same time, otherwise the turning trajectory will
                // be weird.
                output.backward = false;
                return output;
            case FinishedTurning:
                return speedOpt;
            default:
                return AutoPilotAction.nothing();
        }
	}
	
	private void changeState(State newState) {
		if (this.state != newState) {
			this.state = newState;
		}
	}

	@Override
	public boolean canTakeCharge() {
		if (this.state == State.Waiting || this.state == State.FinishedTurning) {
			return false;
		}
		return true;
	}
	
	/**
     * Gets the distances ahead of the target tiles' centre line at which we need to start
     * turning.
	 */
	private double d() {
	    // This formula comes from a bit of calculus.
		return (6.0/5.0/ Math.PI) * (double)(MAINTAIN_SPEED);
	}

	@Override
	public String toString() {
		return "TurningOperator [fromTile=" + fromTile + ", toTile=" + toTile + ", state="+this.state+"]";
	}

}
