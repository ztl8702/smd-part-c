package tiles;

import com.badlogic.gdx.math.Vector2;

import world.Car;

public class MudTrap extends TrapTile {
	
	public static final float SLOWDOWN_FACTOR = 0.6f;
	
	public String getTrap() { return "mud"; }
	
	public void applyTo(Car car, float delta) {
		Vector2 currentSpeed = car.getVelocity();
		float xReduction = currentSpeed.x*SLOWDOWN_FACTOR*delta;
		float yReduction = currentSpeed.y*SLOWDOWN_FACTOR*delta;
		car.setVelocity(currentSpeed.x-xReduction,currentSpeed.y-yReduction);
	}
	
	public boolean canAccelerate() {
		return false;
	}
	
	public boolean canTurn() {
		return true;
	}
}
