package controller;

import world.Car;

public interface Operator {
	public OperatorAction handle(float delta, Car car);

	public boolean canTakeCharge();
	
	
	
}
