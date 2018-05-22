package mycontroller.pathfinder;

import mycontroller.pathfinder.Node;
import mycontroller.pathfinder.NodeComparator;

import java.util.ArrayList;
import java.util.Collections;

public class SortedCostNodeList {
	ArrayList<Node> list;
	NodeComparator nodeComparator;
	
	public SortedCostNodeList() {
		list = new ArrayList<Node>();
		nodeComparator = new NodeComparator();
	}
	
	public Node nodeAt(int i){
		return list.get(i);
	}

	public Node removeAt(int i){
		return list.remove(i);
	}

	public int indexOf(Node n){
		return list.indexOf(n);
	}

	public void push(Node n){
		list.add(n);
		Collections.sort(list, nodeComparator);
	}

	public Node pop(){
		return list.remove(0);
	}
	
	public int getListCount() {
		return list.size();
	}
}
