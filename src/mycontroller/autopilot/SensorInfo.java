/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.autopilot;

import controller.CarController;
import utilities.Coordinate;
import world.WorldSpatial;

/**
 * SensorInfo contains the information needed by the AutoPilot to make decisions
 *
 * It encapsulates the state of the car.
 *
 * It is immutable, once created, its fields could not be changed.
 */
public class SensorInfo {

    private final float x;
    private final float y;
    private final int tileX;
    private final int tileY;
    private final float angle;
    private final float speed;
    private final WorldSpatial.Direction orientation;


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

    /**
     * Gets the x position of car
     * @return
     */
    public float getX() {
        return this.x;
    }

    /**
     * Gets the y position of car
     * @return
     */
    public float getY() {
        return this.y;
    }

    /**
     * Gets the x position of the car in integer
     * @return
     */
    public int getTileX() {
        return this.tileX;
    }

    /**
     * Gets the y position of the car in integer
     * @return
     */
    public int getTileY() {
        return this.tileY;
    }

    /**
     * Gets the location of the car in Coordinate
     * @return
     */
    public Coordinate getCoordinate() {
        return new Coordinate(this.tileX, this.tileY);
    }

    /**
     * Gets the car's current angle
     * @return
     */
    public float getAngle() {
        return this.angle;
    }

    /**
     * Gets the car's current speed
     * @return
     */
    public float getSpeed() {
        return this.speed;
    }

    /**
     * Gets the car's current orientation
     * @return
     */
    public WorldSpatial.Direction getOrientation(){
        return this.orientation;
    }
}
