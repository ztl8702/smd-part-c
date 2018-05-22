package mycontroller;

import java.util.ArrayList;

import utilities.Coordinate;

public class Node implements Comparable<Node> {
	
	private int g, h;
	
	public int getTotalCost() {
		return this.g + this.h;
	}
	
	private int x, y;
	
	private Node goalNode, parentNode;
	
	private int gCost;
	
	public Node(Node parentNode, Node goalNode, int gCost,int x, int y) {
		this.parentNode = parentNode;
		this.goalNode = goalNode;
		this.gCost = gCost;
		this.x=x;
		this.y=y;
		initNode();
	}
	
	private void initNode() {
		this.g = (parentNode!=null)? this.parentNode.g + gCost : gCost;
		this.h = (goalNode!=null)? (int) euclideanHeight( ) : 0;
	}
	
	private double euclideanHeight(){
		double xd = this.x - this.goalNode .x ;
		double yd = this.y - this.goalNode .y ;
		return Math.sqrt((xd*xd) + (yd*yd));
	}
	
	public boolean isMatch(Node n){
		if (n!=null)
			return (x==n.x && y==n.y);
		else
			return false;
	}
	
	public ArrayList<Node> getSuccessors(Node currentNode) {
		MapManager mapManager = new MapManager();
		ArrayList<Node> successors = new ArrayList<Node>();

		for (int xd=-1;xd<=1;xd++) {
			for (int yd=-1;yd<=1;yd++) {
				if (mapManager.isWithinBoard(new Coordinate(x+xd,y+yd))) {
					
					int cost = 1; //TODO
					
					Node n = new Node (this, this.goalNode, cost, x+xd, y+yd);
					// make sure not adding parent or current position 
					if (!n.isMatch(this.parentNode) && !n.isMatch(this))
                        successors.add(n);

				}
			}
		}
		return successors;
	}

	@Override
	public int compareTo(Node obj) {
		return this.getTotalCost() - obj.getTotalCost();
	}
	
	public Node getParentNode() {
		return this.parentNode;
	}
	
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}
}
