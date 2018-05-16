package controller;

import world.Car;

public abstract class BaseOperator implements Operator {
	public static final double TILE_WIDTH  = 1;//32;
	//HELPERS
	protected double getCentreLineX(int tileX) {
		return TILE_WIDTH * (tileX);
	}
	
	public double getCentreLineY(int tileY) {
		return TILE_WIDTH * (tileY);
	}

}
