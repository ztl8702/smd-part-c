/*
 * Group number: 117
 * Therrense Lua (782578), Tianlei Zheng (773109)
 */

package mycontroller.common;

import java.util.HashSet;

import utilities.Coordinate;

/**
 * Our way of identifying different regions on the map 
 * into islands 
 */

public class ColoredRegion {
	public HashSet<Coordinate> coordinates = new HashSet<>();
	
	public Cell cell;
	
	public ColoredRegion(Cell cell){
		this.cell =cell;
	}
}
