package mst.prim;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;



/**
 * Manages allocation and deallocation of the arrays and priority queues that are big and allocated very often.
 * Get methods have a parameter size that denotes the recommended size of the object.
 * Methods free are used to free the object.
 * */
public class MemoryManager {

	private List<PriorityQueue<SEdge>> edgePriorityQueues;
	private List<List<SEdge>> edgeArray;
	
	private List<PriorityQueue<FEdge>> dedgePriorityQueues;
	private List<List<FEdge>> dedgeArray;
	
	
	/**
	 * Constructor.
	 * */
	public MemoryManager(){
		edgePriorityQueues = new ArrayList<PriorityQueue<SEdge>>(10);
		edgeArray = new ArrayList<List<SEdge>>(10);
		dedgePriorityQueues = new ArrayList<PriorityQueue<FEdge>>(10);
		dedgeArray = new ArrayList<List<FEdge>>(10);
	}
	
	
	public synchronized void clear(){
		
		for (int i=0; i<edgePriorityQueues.size(); i++){
			edgePriorityQueues.get(i).clear();
		}
		edgePriorityQueues.clear();
		
		for (int i=0; i<edgeArray.size(); i++){
			edgeArray.get(i).clear();
		}
		edgeArray.clear();
		
		for (int i=0; i<dedgePriorityQueues.size(); i++){
			dedgePriorityQueues.get(i).clear();
		}
		dedgePriorityQueues.clear();
		
		for (int i=0; i<dedgeArray.size(); i++){
			dedgeArray.get(i).clear();
		}
		dedgeArray.clear();
		
	}
	
	
	//------------------------
	
	
	public synchronized PriorityQueue<SEdge> getEdgePriorityQueue(int size){
		if (edgePriorityQueues.isEmpty()){
			//System.out.println("created new priority queue: " + size);
			return new PriorityQueue<SEdge>(size); 
		} else {
			return edgePriorityQueues.remove(edgePriorityQueues.size()-1);	
		}
	}
	
	
	public synchronized void freeEdgePriorityQueue(PriorityQueue<SEdge> queue){
		queue.clear();
		edgePriorityQueues.add(queue);
	}
	
	
	public synchronized List<SEdge> getEdgeArray(int size){
		if (edgeArray.isEmpty()){
			//System.out.println("created new array: " + size);
			return new ArrayList<SEdge>(size); 
		} else {
			return edgeArray.remove(edgeArray.size()-1);	
		}
	}
	
	
	public synchronized void freeEdgeArray(List<SEdge> array){
		array.clear();
		edgeArray.add(array);
	}
	
	
	//------------------------
	
	
	public synchronized PriorityQueue<FEdge> getDEdgePriorityQueue(int size){
		if (dedgePriorityQueues.isEmpty()){
			//System.out.println("created new priority queue: " + size);
			return new PriorityQueue<FEdge>(size); 
		} else {
			return dedgePriorityQueues.remove(dedgePriorityQueues.size()-1);	
		}
	}
	
	
	public synchronized void freeDEdgePriorityQueue(PriorityQueue<FEdge> queue){
		queue.clear();
		dedgePriorityQueues.add(queue);
	}


	public synchronized List<FEdge> getDEdgeArray(int size){
		if (dedgeArray.isEmpty()){
			//System.out.println("created new array: " + size);
			return new ArrayList<FEdge>(size); 
		} else {
			return dedgeArray.remove(dedgeArray.size()-1);	
		}
	}

	
	public synchronized void freeDEdgeArray(List<FEdge> array){
		array.clear();
		dedgeArray.add(array);
	}
	
}
