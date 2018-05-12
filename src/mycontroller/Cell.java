package mycontroller;

public class Cell {
	public enum CellType {ROAD, WALL, START, FINISH, TRAP, UNREACHABLE}
	public int key;
	//boolean unseen = true;
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
	
	public static Cell newTrapCell(int key) {
		Cell newCell = new Cell();
		newCell.type = CellType.TRAP;
		newCell.key = key;
		return newCell;
	}
	
}
