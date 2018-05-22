package mycontroller.routecompiler;

import mycontroller.autopilot.AutoPilot;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Queue;

/**
 * RouteCompile converts a list of coordinates (output of PathFinder) to
 * a bunch of AutoPilots who are going to carry out the path by taking turns
 * controlling the car (one AutoPilot a time)
 */
public interface RouteCompiler {

    Queue<AutoPilot> compile(ArrayList<Coordinate> path);
}
