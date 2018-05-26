/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.routecompiler;

import utilities.Coordinate;
import world.WorldSpatial;

/**
 * Base class (abstract) for RouteCompliler implmentations
 */
public abstract class RouteCompilerBase implements RouteCompiler {
    // Some internal data structures
    
    /**
     * A semantic type of action for the car
     */
    protected enum ActionType {
        GoStraight, TurnLeft, TurnRight
    };

    /**
     * Intermediate presentation of the Car's actions
     * 
     * This is used inside RouteCompiler before the path (list of coordinates) is converted
     * to AutoPilots. An analogy is the IL of JVM.
     * 
     * 
     * Notes to markers:
     * Yes, we use all public fields, because we intend to use this like a struct in C,
     * just a flat structure for data.
     *  
     * This type is protected and only used internally in RouteCompilers, so we don't run the risk
     * of unwanted modifications.
     */
    protected class Action {
        public ActionType type;
        public Coordinate start;
        public Coordinate finish;
        public WorldSpatial.Direction goStraightDirection;
        public float speedLimit;
    }
}
