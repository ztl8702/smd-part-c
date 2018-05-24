package mycontroller.common;

public class Cell {
	
	public enum CellType {ROAD, WALL, START, FINISH, LAVA, HEALTH, UNREACHABLE}
	public int key;
	public CellType type;
	
	public String getCellTypeHash() {
		return String.format("(%s,%d)",this.type.toString(),this.key);
	}
	
	public static Cell newRoadCell() {
		Cell newCell = new Cell();
		newCell.type = CellType.ROAD;
		newCell.key = 0;
		return newCell;
	}
	
	public static Cell newWallCell() {
		Cell newCell = new Cell();
		newCell.type = CellType.WALL;
		newCell.key = 0;
		return newCell;
	}
	
	public static Cell newStartCell() {
		Cell newCell = new Cell();
		newCell.type = CellType.START;
		newCell.key = 0;
		return newCell;
	}
	
	public static Cell newFinishCell() {
		Cell newCell = new Cell();
		newCell.type = CellType.FINISH;
		newCell.key = 0;
		return newCell;
	}
	
	public static Cell newLavaCell(int key) {
		Cell newCell = new Cell();
		newCell.type = CellType.LAVA;
		newCell.key = key;
		return newCell;
	}
	
	public static Cell newHealthCell() {
		Cell newCell = new Cell();
		newCell.type = CellType.HEALTH;
		newCell.key = 0;
		return newCell; 
	}
}
