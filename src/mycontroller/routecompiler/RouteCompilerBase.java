/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.routecompiler;

import utilities.Coordinate;
import world.WorldSpatial;

/**
 * TODO: for Radium to comment
 */
public abstract class RouteCompilerBase implements RouteCompiler {
	// TODO: remove this note
	// Note: some dirty ad-hoc data structures, meant for internal use, don't need to be reflected in UML (i guess)
    protected enum ActionType {
        GoStraight, TurnLeft, TurnRight
    };

    // TODO: remove this note
    /* Note: use all public field because we intend to use this like a struct in C, 
     * this type is protected and only used internally in RouteCompilers, so we are too lazy to make getter and setters
     */
    protected class Action {
        public ActionType type;
        public Coordinate start;
        public Coordinate finish;
        public WorldSpatial.Direction goStraightDirection;
        public float speedLimit;
    }
}
