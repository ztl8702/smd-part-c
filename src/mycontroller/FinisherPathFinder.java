package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class FinisherPathFinder implements PathFinder {
	
	
	public FinisherPathFinder() {
		
	}
	
	private ArrayList<Node> aStarSearch(Node startNode, Node goalNode) {
		ArrayList<Node> solutionPath = new ArrayList<Node>();
		
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
			solutionPath.add(p);
			p = p.getParentNode();
		}
		
		return solutionPath;
	}

	@Override
	public ArrayList<Coordinate> getPath(HashMap<Coordinate, Cell> map, Coordinate currentPosition, float currentSpeed, float currentDirection) {
		return null;
	}
}
