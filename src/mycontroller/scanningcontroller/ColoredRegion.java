package mycontroller.scanningcontroller;

import java.util.HashSet;

import utilities.Coordinate;

public class ColoredRegion {
	public HashSet<Coordinate> coordinates = new HashSet<>();
	
	public Cell cell;
	
	public ColoredRegion(Cell cell){
		this.cell =cell;
	}
}
