package controller;

public class OperatorAction {
	public boolean Forward;
	public boolean Backward;
	public boolean Brake;
	public boolean TurnLeft;
	public boolean TurnRight;
	
	public OperatorAction(boolean forward, boolean backward, boolean brake, boolean left, boolean right) {
		this.Forward = forward;
		this.Backward = backward;
		this.Brake = brake;
		this.TurnLeft = left;
		this.TurnRight = right;
	}
	
	public static OperatorAction forward() {
		return new OperatorAction(true, false, false, false, false);
	}
	
	public static OperatorAction backward() {
		return new OperatorAction(false, true, false, false, false);
	}

	public static OperatorAction brake() {
		return new OperatorAction(false, false, true, false, false);
	}


	public static OperatorAction turnLeft() {
		return new OperatorAction(false, false, false, true, false);
	}
	
	public static OperatorAction turnRight() {
		return new OperatorAction(false, false, false, false, true);
	}
	
	public static OperatorAction nothing() {
		return new OperatorAction(false, false, false, false, false);
	}

	public static OperatorAction combine(OperatorAction a, OperatorAction b) {
		return new OperatorAction(a.Forward || b.Forward,
				a.Backward||b.Backward,
				a.Brake || b.Brake,
				a.TurnLeft || b.TurnLeft,
				a.TurnRight || b.TurnRight);
	}
}


