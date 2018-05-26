package mycontroller.autopilot;

import mycontroller.mapmanager.MapManagerInterface;

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
        return lastSpeed < 0.05;
    }
}
