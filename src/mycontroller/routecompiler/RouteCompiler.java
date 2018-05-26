/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.routecompiler;

import mycontroller.autopilot.AutoPilot;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Queue;

/**
 * RouteCompile converts a list of coordinates (output of PathFinder) to
 * AutoPilots which carry out the path by taking turns
 * controlling the car (one AutoPilot a time)
 */
public interface RouteCompiler {

    Queue<AutoPilot> compile(ArrayList<Coordinate> path);
}
