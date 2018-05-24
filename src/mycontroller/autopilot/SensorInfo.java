package mycontroller.autopilot;

import controller.CarController;
import utilities.Coordinate;
import world.WorldSpatial;

/**
 * SensorInfo contains the information needed by the AutoPilot to make decisions
 *
 * It encapsulates the state of the car.
 *
 * It is immutable, once created, its fields should not be changed.
 */
public class SensorInfo {

    private float x;
    private float y;
    private int tileX;
    private int tileY;
    private float angle;
    private float speed;
    private WorldSpatial.Direction orientation;


    public SensorInfo (float x, float y, int tileX, int tileY, float angle, float speed, WorldSpatial.Direction orientation) {
        this.x = x;
        this.y = y;
        this.tileX = tileX;
        this.tileY = tileY;
        this.angle = angle;
        this.speed = speed;
        this.orientation = orientation;
    }

    // Factory method
    public static SensorInfo fromController(CarController car) {
        Coordinate coord = new Coordinate( car.getPosition());
        return new SensorInfo(car.getX(), car.getY(), coord.x, coord.y, car.getAngle(), car.getSpeed(), car.getOrientation());
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getTileX() {
        return this.tileX;
    }

    public int getTileY() {
        return this.tileY;
    }

    public Coordinate getCoordinate() {
        return new Coordinate(this.tileX, this.tileY);
    }

    public float getAngle() {
        return this.angle;
    }

    public float getSpeed() {
        return this.speed;
    }

    public WorldSpatial.Direction getOrientation(){
        return this.orientation;
    }
}
