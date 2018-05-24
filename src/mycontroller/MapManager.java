package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import tiles.MapTile;
import tiles.LavaTrap;
import utilities.Coordinate;
import world.World;



public class MapManager {
	
	
	private int currentKey;
	
	//TODO check if all keys have been found and update as well
	
	private HashMap<Coordinate, Cell> map = new HashMap<Coordinate, Cell>();
	
	
	private int xStart = 0;
	private int xEnd = World.MAP_WIDTH - 1;
	private int yStart = 0;
	private int yEnd = World.MAP_HEIGHT - 1;
	private static Coordinate[] DIRECTIONS = {
			new Coordinate(-1,0),
			new Coordinate(+1,0),
			new Coordinate(0,-1),
			new Coordinate(0,+1)
	};
	
	private HashSet<Coordinate> unseen = new HashSet<Coordinate>();


	private HashSet<Coordinate> reachable = new HashSet<Coordinate>();
	private HashMap<Integer, Coordinate> keys = new HashMap<Integer, Coordinate>();
	
//	private int colourCount = 0;
//	private HashMap<Coordinate, Integer> colour = new HashMap<>();
//	private HashMap<Integer ,ColoredRegion> areas = new HashMap<>();
	
//	private MapWindow mapWindow;
//	private ColoursWindow cWindow;
	
	public MapManager() {
//		mapWindow = new MapWindow();
//		cWindow = new ColoursWindow();
	}

	public HashMap<Coordinate,Cell> getMap() {
		return this.map;
	}
	
	public HashMap<Integer,Coordinate> getKeys() {
		return this.keys;
	}

	public HashSet<Coordinate> getUnseen() {
		return this.unseen; 
	}
	
	
	
	public Coordinate getFinishTile() {
		Iterator<Map.Entry<Coordinate, Cell>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Coordinate, Cell> pair = (Map.Entry<Coordinate, Cell>)it.next();
	        
	        // get the location of "finish" tile
	        if (pair != null && ((Cell)pair.getValue()).type == CellType.FINISH) {
	        	return (Coordinate) pair.getKey();
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }      
	    return null;
	}
	
	public Coordinate findKey(int key) {
		if (keys.get(key) != null) {
			return keys.get(key);
		}
		return null;
	}
	// check if we have found all the keys	
	public boolean foundAllKeys() {
		
		for (int i=1; i<currentKey; i++) {
			if (findKey(i) == null) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	
	public void updateKey(int currentKey) {
		this.currentKey = currentKey;
	}
	
	
	
	// TODO: maybe issue here, as ur mutating ???
	public void initialMap(HashMap<Coordinate, MapTile> tiles) {
		tiles.forEach((coord,tile) -> {
			switch (tile.getType()) {
			case ROAD:
				this.map.put(coord, Cell.newRoadCell());
				this.unseen.add(coord);
				break;
			case WALL:
				this.map.put(coord, Cell.newWallCell());
				// no need to see
				break;
			case START:
				this.map.put(coord, Cell.newStartCell());
				break;
			case FINISH:
				this.map.put(coord, Cell.newFinishCell());
				break;
			default:
				System.err.println("Unexpected cell type in initial map.");
				break;
			}
		});
		markReachable();
	}


	
	public void updateView(HashMap<Coordinate, MapTile> tiles) {
		boolean hasUpdate = false; // update window frame
		for(Map.Entry<Coordinate, MapTile> entry : tiles.entrySet()) {
			Coordinate coord = entry.getKey();
			MapTile tile = entry.getValue();
			{
				if (this.unseen.contains(coord)) {
					hasUpdate = true;
					this.unseen.remove(coord);
					switch (tile.getType()) {
					case TRAP:
						this.unseen.remove(coord);
						if (tile instanceof LavaTrap) {
							LavaTrap t = (LavaTrap)tile;
							this.map.put(coord, Cell.newLavaCell(t.getKey()));
							this.keys.put(t.getKey(), coord);
						} else {
							this.map.put(coord, Cell.newHealthCell());						
						}
						break;
					case ROAD:
						// do nothing
						break;
					default:
						
						break;
					}
				}
			}
		}

		if (hasUpdate) {
//			this.printBoard();
//			this.printColours(getConnectedComponents());
		}
	}
	
	public String printBoard() {
		String output  ="";
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				Cell cell = this.map.get(new Coordinate(x,y));
				if (cell == null) {
					return null;
				}
				boolean isUnseen = this.unseen.contains(new Coordinate(x,y));
				boolean isReachable = this.reachable.contains(new Coordinate(x,y));
				
				String str = "S";
				switch (cell.type) {
				case START:
					str = "S";
					break;
				case FINISH:
					str = "F";
					break;
				case HEALTH:
					str = "H";
					break;
				case LAVA:
					if (cell.key!=0) {
						str = new Integer(cell.key).toString();
					} else {
						str = "T";
					}
					break;
				case ROAD:
					if (!isUnseen) {
						if (!isReachable) {
							str = "X";
						
						} else {
							str = " ";
						}
					} else {
						str = "?";
					}
					break;
				case WALL:
					str = "W";
					break;
				default:
					break;
				}
				output +=(str+" ");
			}
			output+="\n";
		}
		
		return output;
		//output+=String.format"\b"
//		this.mapWindow.setText(output);
	}
	
//	public void printColours(ColoredRegion[] regions) {
//		Color colors[] = new Color[] {
//			Color.BLACK,
//			Color.RED,
//			Color.GREEN,
//			Color.BLUE,
//			Color.YELLOW,
//			Color.PINK,
//			Color.GRAY,
//			Color.ORANGE,
//			Color.MAGENTA
//		};
//		
//		int[][] colorMap = new int[xEnd+1][yEnd+1];
//		//colorMap.
//		for (int x = xStart; x<=xEnd; ++x) {
//			for (int y = yStart; y<=yEnd; ++y) {
//				colorMap[x][y]=0;
//			}
//		}
//		int i = 0;
//		for (ColoredRegion r:regions) {
//			++i;
//			for (Coordinate c: r.coordinates) {
//				colorMap[c.x][c.y] = i;
//			}
//		}
//
//		cWindow.clear();
//		for (int y = yEnd; y>=yStart; --y) {
//			for (int x = xStart; x<=xEnd; ++x) {
//				int theC = colorMap[x][y];
//				String tt = String.format("%d ", theC);
//				if (theC>9) {
//					cWindow.appendText("+ ", colors[theC% colors.length]);
//					
//				} else {
//					cWindow.appendText(tt, colors[theC% colors.length]);
//				}
//			}
//			cWindow.appendText("\n", Color.BLACK);
//		}
//	}
	
	public boolean isWithinBoard(Coordinate coord) {
		return (xStart <= coord.x && coord.x <= xEnd) && (yStart <= coord.y && coord.y <= yEnd);
	}
	

	private HashSet<Coordinate> dfs_visited;
	

	
	private void markReachable() {
		dfs_visited = new HashSet<>();
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				if (map.get(new Coordinate(x,y)).type == Cell.CellType.START) {
					dfsConnectedArea(new Coordinate(x,y));
					break;
				}
			}
		}
		
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				if (!reachable.contains(new Coordinate(x,y))) {
					unseen.remove(new Coordinate(x,y));
				}
			}
		}
		
		
	}
	
	private void dfsConnectedArea (Coordinate currentLocation) {
		if (dfs_visited.contains(currentLocation)) {
			return;
		}
		dfs_visited.add(currentLocation);
		reachable.add(currentLocation);
		
		for (Coordinate direction : DIRECTIONS) {
			int newX = currentLocation.x + direction.x;
			int newY = currentLocation.y + direction.y;
			Coordinate newCoord = new Coordinate(newX,newY);
			
			if (this.isWithinBoard(newCoord)) {
				if (this.map.get(newCoord).type!=Cell.CellType.WALL) {					
					dfsConnectedArea(newCoord);
				}
			}
		}
	}
//	
//	public ColoredRegion[] getConnectedComponents() {
//		int colouredCount = 0;
//		dfs_visited = new HashSet<Coordinate>();
//		ArrayList<ColoredRegion> regions = new ArrayList<ColoredRegion>();
//		for (int y = yEnd; y>=yStart; --y) {
//			for (int x = xStart; x<=xEnd; ++x) {
//				Coordinate coord = new Coordinate(x,y);
//				if (reachable.contains(coord) && !dfs_visited.contains(coord) && !(map.get(coord).type == Cell.CellType.WALL)) {
//					++colouredCount;
//					ColoredRegion newRegion = new ColoredRegion(map.get(coord));
//					dfsColour(newRegion, coord, colouredCount);
//					regions.add(newRegion);
//				}
//			}
//		
//		}
//		return regions.toArray(new ColoredRegion[regions.size()]);
//	}
//	
//	private void dfsColour(ColoredRegion region, Coordinate currentLocation, int colour) {
//		if (dfs_visited.contains(currentLocation)) {
//			return;
//		}
//		dfs_visited.add(currentLocation);
//		region.coordinates.add(currentLocation);
//		
//		
//		for (Coordinate direction : DIRECTIONS) {
//			int newX = currentLocation.x + direction.x;
//			int newY = currentLocation.y + direction.y;
//			Coordinate newCoord = new Coordinate(newX,newY);
//			
//			if (this.isWithinBoard(newCoord)) {
//				if (this.map.get(newCoord).getCellTypeHash().equals(this.map.get(currentLocation).getCellTypeHash())
//						&& !this.unseen.contains(newCoord)) {					
//					dfsColour(region, newCoord, colour);
//				}
//			}
//		}
//	}
}
