package mycontroller.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import mycontroller.common.Cell;
import mycontroller.pathfinder.AStarPathFinder;
import mycontroller.pathfinder.PathFinder;
import utilities.Coordinate;
import world.World;

/**
 * CODE FOR TESTING PURPOSES
 * <p>
 * DO NOT GRADE!
 */
public class AlgorithmTester {


    public static String IN_FILE = "map.in";
    public static int startX, startY, endX, endY, width, height;
    public static HashMap<Coordinate, Cell> map;

    public static void main(String[] argv) {
        readInput();

        //System.out.printf("%d %d", startX, startY);
        int maxSearchDepth = 500;
		PathFinder finisher = new AStarPathFinder(maxSearchDepth, width, height);
		
        ArrayList<Coordinate> path = finisher.getPath(map, new Coordinate(startX, startY), 
				new Coordinate(endX, endY), 0, 0);
		
        // print out the result
        System.out.println("*****ASTAR***** Path found!!!!");

        System.out.println(path.toString());


    }

    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private static void readInput() {
        Scanner sc2;
        try {
            sc2 = new Scanner(new File(IN_FILE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        HashMap<Coordinate, Cell> result = new HashMap<>();

        width = sc2.nextInt();
        height = sc2.nextInt();
        for (int y = height - 1; y >= 0; --y) {
            for (int x = 0; x < width; ++x) {
                String token = sc2.next();
                //System.out.printf("(%d,%d) %s\n", x,y, token);
                // try parse
                Integer keyNum = tryParse(token);
                Cell newCell = null;
                Coordinate newCoord = new Coordinate(x, y);
                if (keyNum != null) {
                    newCell = Cell.newLavaCell(keyNum);
                    //System.out.printf("is key %d",keyNum);
                } else {
                    switch (token) {
                        case "W":
                            newCell = Cell.newWallCell();
                            break;
                        case "S":
                            newCell = Cell.newStartCell();
                            break;
                        case "F":
                            newCell = Cell.newFinishCell();
                            break;
                        case "R":
                            newCell = Cell.newRoadCell();
                            break;
                        case "T":
                            newCell = Cell.newLavaCell(0);
                            break;
                        case "H":
                            newCell = Cell.newHealthCell();
                            break;
                        default:
                            //System.err.println("Unknown Cell Type " + token);
                            break;
                    }
                }

                result.put(newCoord, newCell);

            }
        }

        map = result;
        startX = sc2.nextInt();
        startY = sc2.nextInt();
        endX = sc2.nextInt();
        endY = sc2.nextInt();
        sc2.close();

    }
}