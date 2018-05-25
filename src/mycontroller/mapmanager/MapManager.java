package mycontroller.mapmanager;

import java.util.*;


import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import mycontroller.common.Logger;
import tiles.MapTile;
import tiles.LavaTrap;
import utilities.Coordinate;
import world.World;



public class MapManager implements MapManagerInterface {


    private HashMap<Coordinate, Cell> map = new HashMap<>();


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

    private Set<Coordinate> unseen = new HashSet<>();


    private Set<Coordinate> reachable = new HashSet<>();
    private Map<Integer, Coordinate> keys = new HashMap<>();



    public MapManager() {

    }


    @Override
    public Cell getCell(int x, int y) {
    	if (isWithinBoard(new Coordinate(x, y))) {
    		return this.map.get(new Coordinate(x, y));
    	}
    	
        return null;
    }

    @Override
    public boolean hasSeenTile(int x, int y) {
        return ! this.unseen.contains(new Coordinate(x,y));
    }

    @Override
    public boolean isReachable(int x, int y) {
        return this.reachable.contains(new Coordinate(x,y));
    }

    @Override
    public Coordinate getFinishTile() {
    	for (Coordinate c : map.keySet()) {
    		if (map.get(c).type == CellType.FINISH) {
    			return c;
    		}
    	}
        return null;
    }

    @Override
    public Coordinate getKeyCoordinate(int keyNumber) {
    	if (this.keys.get(keyNumber) == null) {
    		return null;
    	}
        return this.keys.get(keyNumber);
    }

    @Override
    public Set<Coordinate> getUnseenLocations() {
        return null;
    }


    @Override
    public boolean foundAllKeys(int currentKey) {
        for (int i=1; i<currentKey; i++) {
            if (this.getKeyCoordinate(i) == null) {
                return false;
            }
        }
        return true;
    }

    public void initialMap(HashMap<Coordinate, MapTile> tiles) {
        tiles.forEach((coord,tile) -> {
            switch (tile.getType()) {
                case ROAD:
                    this.map.put(new Coordinate(coord.x, coord.y), Cell.newRoadCell());
                    this.unseen.add(new Coordinate(coord.x, coord.y));
                    break;
                case WALL:
                    this.map.put(new Coordinate(coord.x, coord.y), Cell.newWallCell());
                    // no need to see
                    break;
                case START:
                    this.map.put(new Coordinate(coord.x, coord.y), Cell.newStartCell());
                    break;
                case FINISH:
                    this.map.put(new Coordinate(coord.x, coord.y), Cell.newFinishCell());
                    break;
                default:
                    Logger.printWarning(this.getClass().getName(),"Unexpected cell type in initial map.");
                    break;
            }
        });
        markReachable();
    }


    @Override
    public void updateView(HashMap<Coordinate, MapTile> tiles) {
        for(Map.Entry<Coordinate, MapTile> entry : tiles.entrySet()) {
            Coordinate coord = entry.getKey();
            MapTile tile = entry.getValue();
            {
                if (this.unseen.contains(coord)) {
                    this.unseen.remove(coord);
                    switch (tile.getType()) {
                        case TRAP:
                            this.unseen.remove(coord);
                            if (tile instanceof LavaTrap) {
                                LavaTrap t = (LavaTrap)tile;
                                this.map.put(new Coordinate(coord.x, coord.y), Cell.newLavaCell(t.getKey()));
                                this.keys.put(t.getKey(), new Coordinate(coord.x, coord.y));
                            } else {
                                this.map.put(new Coordinate(coord.x, coord.y), Cell.newHealthCell());
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

    }

    /**
     * Debug function
     * @return
     */
    public String printBoard() {
        String output  ="";
        for (int y = yEnd; y>=yStart; --y) {
            for (int x = xStart; x<=xEnd; ++x) {
                Cell cell = this.getCell(x,y);
                if (cell == null) {
                    return "";
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
    }


    @Override
    public boolean isWithinBoard(Coordinate coord) {
        return (xStart <= coord.x && coord.x <= xEnd) && (yStart <= coord.y && coord.y <= yEnd);
    }


    private HashSet<Coordinate> dfs_visited;

    private void markReachable() {
        dfs_visited = new HashSet<>();
        for (int y = yEnd; y>=yStart; --y) {
            for (int x = xStart; x<=xEnd; ++x) {
                if (this.getCell(x,y).type == Cell.CellType.START) {
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
                if (this.getCell(newCoord.x, newCoord.y).type!=Cell.CellType.WALL) {
                    dfsConnectedArea(newCoord);
                }
            }
        }
    }
}
