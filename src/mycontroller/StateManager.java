package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import tiles.MapTile;
import tiles.TrapTile;
import tiles.LavaTrap;
import utilities.Coordinate;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;

public class StateManager {
	public HashMap<Coordinate, Cell> map = new HashMap<Coordinate, Cell>();
	public int xStart = Integer.MAX_VALUE;
	public int xEnd = Integer.MIN_VALUE;
	public int yStart = Integer.MAX_VALUE;
	public int yEnd = Integer.MIN_VALUE;
	public static Coordinate[] DIRECTIONS = {
			new Coordinate(-1,0),
			new Coordinate(+1,0),
			new Coordinate(0,-1),
			new Coordinate(0,+1)
	};
	
	public HashSet<Coordinate> unseen = new HashSet<Coordinate>();
	public HashSet<Coordinate> reachable = new HashSet<Coordinate>();
	public HashMap<Integer, Coordinate> keys = new HashMap<Integer, Coordinate>();
	
	private int colourCount = 0;
	public HashMap<Coordinate, Integer> colour = new HashMap<>();
	public HashMap<Integer ,ColoredRegion> areas = new HashMap<>();
	
	private MapWindow mapWindow;
	private ColoursWindow cWindow;
	public StateManager() {
		mapWindow = new MapWindow();
		cWindow = new ColoursWindow();
		
		
	}
	
	public void initialMap(HashMap<Coordinate, MapTile> tiles) {
		tiles.forEach((coord,tile) -> {
			xStart = Math.min(xStart, coord.x);
			xEnd = Math.max(xEnd, coord.x);
			yStart = Math.min(yStart, coord.y);
			yEnd = Math.max(yEnd, coord.y);
			
			this.colour.put(coord, 0);
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
	}


	
	public void updateView(HashMap<Coordinate, MapTile> tiles) {
		boolean hasUpdate = false;
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
							this.map.put(coord, Cell.newTrapCell(t.getKey()));
							this.keys.put(t.getKey(), coord);
						} else {
							this.map.put(coord, Cell.newTrapCell(0));						
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
			this.printBoard();
			this.printColours(getConnectedComponents());
		}
	}
	
	public void printBoard() {
		String output  ="";
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				Cell cell = this.map.get(new Coordinate(x,y));
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
				case TRAP:
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
		//output+=String.format"\b"
		this.mapWindow.setText(output);
	}
	
	public void printColours(ColoredRegion[] regions) {
		Color colors[] = new Color[] {
			Color.BLACK,
			Color.RED,
			Color.GREEN,
			Color.BLUE,
			Color.YELLOW,
			Color.PINK,
			Color.GRAY,
			Color.ORANGE,
			Color.MAGENTA
		};
		
		int[][] colorMap = new int[xEnd+1][yEnd+1];
		//colorMap.
		for (int x = xStart; x<=xEnd; ++x) {
			for (int y = yStart; y<=yEnd; ++y) {
				colorMap[x][y]=0;
			}
		}
		int i = 0;
		for (ColoredRegion r:regions) {
			++i;
			for (Coordinate c: r.coordinates) {
				colorMap[c.x][c.y] = i;
			}
		}

		cWindow.clear();
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				int theC = colorMap[x][y];
				String tt = String.format("%d ", theC);
				if (theC>9) {
					cWindow.appendText("+ ", colors[theC% colors.length]);
					
				} else {
					cWindow.appendText(tt, colors[theC% colors.length]);
				}
			}
			cWindow.appendText("\n", Color.BLACK);
		}
	}
	
	private boolean isWithinBoard(Coordinate coord) {
		return (xStart <= coord.x && coord.x <= xEnd) && (yStart <= coord.y && coord.y <= yEnd);
	}
	

	private HashSet<Coordinate> dfs_visited;
	

	
	public void markReachable() {
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
	
	public ColoredRegion[] getConnectedComponents() {
		int colouredCount = 0;
		dfs_visited = new HashSet<Coordinate>();
		ArrayList<ColoredRegion> regions = new ArrayList<ColoredRegion>();
		for (int y = yEnd; y>=yStart; --y) {
			for (int x = xStart; x<=xEnd; ++x) {
				Coordinate coord = new Coordinate(x,y);
				if (reachable.contains(coord) && !dfs_visited.contains(coord) && !(map.get(coord).type == Cell.CellType.WALL)) {
					++colouredCount;
					ColoredRegion newRegion = new ColoredRegion(map.get(coord));
					dfsColour(newRegion, coord, colouredCount);
					regions.add(newRegion);
				}
			}
		
		}
		return regions.toArray(new ColoredRegion[regions.size()]);
	}
	
	private void dfsColour(ColoredRegion region, Coordinate currentLocation, int colour) {
		if (dfs_visited.contains(currentLocation)) {
			return;
		}
		dfs_visited.add(currentLocation);
		region.coordinates.add(currentLocation);
		
		
		for (Coordinate direction : DIRECTIONS) {
			int newX = currentLocation.x + direction.x;
			int newY = currentLocation.y + direction.y;
			Coordinate newCoord = new Coordinate(newX,newY);
			
			if (this.isWithinBoard(newCoord)) {
				if (this.map.get(newCoord).getCellTypeHash().equals(this.map.get(currentLocation).getCellTypeHash())
						&& !this.unseen.contains(newCoord)) {					
					dfsColour(region, newCoord, colour);
				}
			}
		}
	}
}
