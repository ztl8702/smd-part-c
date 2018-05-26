package mycontroller.autopilot;

import mycontroller.common.Util;
import mycontroller.mapmanager.MapManagerInterface;

/**
 * An AutoPilot that does one thing only: stops the car
 */
public class EmergencyStopAutoPilot extends AutoPilotBase {

    private AutoPilot maintainSpeedOpt = AutoPilotFactory.maintainSpeed(0);

    private float lastSpeed = Integer.MAX_VALUE;

    public EmergencyStopAutoPilot(MapManagerInterface mapManager) {
        super(mapManager);
    }

    @Override
    public ActuatorAction handle(float delta, SensorInfo carStatus) {
        lastSpeed = carStatus.getSpeed();
        return maintainSpeedOpt.handle(delta, carStatus);
    }

    @Override
    public boolean canTakeCharge() {
        return true;
    }

    @Override
    public boolean canBeSwappedOut() {
        // we can consider the car to be stopped if speed is less than 0.05
        return lastSpeed < Util.STOPPED_THRESHOLD;
    }
}
