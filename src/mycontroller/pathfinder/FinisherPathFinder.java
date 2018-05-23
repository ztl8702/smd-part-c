package mycontroller.pathfinder;

import mycontroller.common.Cell;
import mycontroller.common.Cell.CellType;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FinisherPathFinder implements PathFinder {
	
	@Override
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed, float currentDirection) {
		
		Node start = new Node(null, null, 0, currentPosition.x, currentPosition.y);
		Node goal= null;
		
		// get goal node
		Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if(pair.getValue() == CellType.FINISH) {
	        	Coordinate coord = (Coordinate) pair.getKey();
	        	goal =  new Node(null, null, 0, coord.x, coord.y);
	        }
//	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		
		ArrayList<Coordinate> finalPath = this.aStarSearch(start, goal);
		
		System.out.println("A Star Path found!!!!");
        for (Coordinate c : finalPath) {
            System.out.printf(c.toString());

        }
		
		return finalPath;
	}
	
	private ArrayList<Coordinate> aStarSearch(Node startNode, Node goalNode) {
		ArrayList<Coordinate> solutionPath = new ArrayList<Coordinate>();
		
		SortedCostNodeList openList = new SortedCostNodeList();
		SortedCostNodeList closedList = new SortedCostNodeList();
		
		// put starting node into list
		openList.push(startNode);
		
		// while openList is not empty
		while (openList.getListCount() > 0) {
			// get the node from openList with the lowest f
			Node currentNode = openList.pop();
			
			// we have found solution
			if (currentNode.isMatch(goalNode)) {
				goalNode.setParentNode(currentNode.getParentNode());
				break;
			}
			
			// generate successors
			ArrayList<Node> successors = currentNode.getSuccessors(currentNode); 
			
			//for each successorNode
			for (Node successorNode : successors) {
				//set the cost of it to be the cost of currentNode plus
				//the cost to get to it from the currentNode
				//--> already set while we are getting successors
				
				//try to find successorNode in the openList
				//if the existing one is as good or better
				//then discard this successorNode and continue
				int oFound = openList.indexOf(successorNode);
				
				if (oFound > 0) {
					Node existingNode = openList.nodeAt(oFound);
					if (existingNode.compareTo(currentNode) <= 0) {
						continue;
					}
				}
				
				//try to find successorNode in the closedList
				//if the existing one is as good or better
				//then discard this successorNode and continue
				int cFound = closedList.indexOf(successorNode);
				if(cFound > 0) {
					Node existingNode = closedList.nodeAt(cFound);
					if (existingNode.compareTo(currentNode) <= 0) {
						continue;
					}
				}
				
				//remove successorNode from openList and closedList
				if (oFound != -1) {
					openList.removeAt(oFound);
				}
				if (cFound != -1) {
					closedList.removeAt(cFound);
				}
				
				//Set the parent of successorNode to currentNode;
				//--> already set while we are getting successors

				//Set h to be the estimated distance to goalNode (Using heuristic function)
				//--> already set while we are getting successors
				
				// add successorNode to openList
				openList.push(successorNode);
			}
			
			// add currentNode to closedList
			closedList.push(currentNode);
		}
		
			
		//follow the parentNode from goal to start node to find solution
		Node p = goalNode;
		while(p != null) {
			solutionPath.add(new Coordinate(p.getX(), p.getY()));
			p = p.getParentNode();
		}
		
		return solutionPath;
	}


}
