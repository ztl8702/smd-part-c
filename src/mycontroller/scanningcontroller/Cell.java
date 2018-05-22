package mycontroller.scanningcontroller;

public class Cell {
	
	public enum CellType {ROAD, WALL, START, FINISH, TRAP, UNREACHABLE}
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
	
	public static Cell newTrapCell(int key) {
		Cell newCell = new Cell();
		newCell.type = CellType.TRAP;
		newCell.key = key;
		return newCell;
	}
	
	// This method allows us to decide which node is better. 
	// We can say current node is better if this method returns negative number,
	// which means current node has lower cost than the other node being compared.
	public static int compareTo(Cell currentCell, Cell otherCell) {
//		if (currentCell.type == CellType.)
//		TODO
		return 1;
		
	}
}
