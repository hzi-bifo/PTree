package mst.primcompletegraph;

import java.util.List;
import java.util.Random;

import ptree.MutationManager;
import ptree.Vertex;
import dmatrix.Matrix;


/**
 * Prim optimized for complete graphs. It starts with a temporary MST that has only one node. 
 * In each step it adds one node that is not yet in the MST to the temporary MST.
 * The node that is added is the node with the lowest distance to the temporary MST.
 * The algorithm is randomized, i.e. a random number is assigned to each node; and 
 * if two nodes have the same distance to the temporary MST, the vertex with the highest number is added. 
 * Time complexity O(N) + O (1) + O(N^2) + O(N) + O(N^2) = O(N^2).
 * Space complexity O(N).
 * */
public class PrimCompleteGraph {

	/* the number of nodes in the graph */
	private int size;
	
	/* the distance between nodes, supposing that the graph is complete, distance == infinity if there is no edge */
	private Matrix distanceMatrix;
	
	/* "inMST[i]" is true if the node "i" is already in the temporary MST, false otherwise */
	private boolean inMST[]; 

	/* edge (i, shortestEdgeTo[i]) of weight distanceToMST[i] is the edge with the lowest cost that connects node i
	 * that is not yet in the MST with a node that is already in the MST (namely with node shortestEdgeTo[i]) */
	private int shortestEdgeTo[];
	private int distanceToMST[];
	
	/* array of random numbers to compare two edges of the same weight that can connect a node that is not yet in
	 * the temporary MST to two different nodes that are already in the temporary MST */
	private byte[] randArray;
	
	/* resulting MST represented as a set of edges: {(edgeV0[i], edgeV1[i]) | i in [0..edgeCount]} */
	int edgeV0[];
	int edgeV1[];
	int edgeCount;
	
	
	/**
	 * Computes MST.
	 * */
	public static void computeMST(List<Vertex> vertices, Matrix distanceMatrix, MutationManager mm){
		
		/* run MST*/
		PrimCompleteGraph prim = new PrimCompleteGraph(distanceMatrix); 
		prim.runPrim();
		
		Vertex v0;
		Vertex v1;

		/* add computed edges to the graph */
		for (int i=0; i<prim.edgeCount; i++){
			v0 = vertices.get(prim.edgeV0[i]);
			v1 = vertices.get(prim.edgeV1[i]);
			v0.setOutcomingEdge(v1);
			v1.setOutcomingEdge(v0); 
		}
		
		/* root and orient the graph */
		mst.prim.MSTPrim.root(vertices.get(0),  mm); 
	}
	 
	
	/**
	 * Constructor.
	 * */
	private PrimCompleteGraph(Matrix distanceMatrix){
		this.distanceMatrix = distanceMatrix;
		this.size = distanceMatrix.getSize();
		this.inMST = new boolean[size];
		this.distanceToMST = new int[size];
		this.shortestEdgeTo = new int[size];
		this.randArray = new byte[size];
		Random rand = new Random();
		rand.nextBytes(randArray);
		edgeV0 = new int[size-1];
		edgeV1 = new int[size-1];
		edgeCount = 0;
	}

	
	/**
	 * Update distances of nodes that are not yet in the temporary MST to the temporary MST,
	 * now considering the new added node.
	 * */
	private void updateDistances(int newNode) {
		
		int dist;
		
		for (int i = 0; i < size; i++){								
			
			if (!inMST[i]){//the node is not yet in the temporary MST
				
				dist = distanceMatrix.getDistance(newNode, i);
				
				if (dist < distanceToMST[i]){//the distance to "i" is shorter via newNode 
					
					distanceToMST[i] = dist;				 
					shortestEdgeTo[i] = newNode;					 
				
				} else {//the distance is the same but I decide based on the random numbers of respective nodes
					
					if ((distanceToMST[i] == dist) && (randArray[newNode] < randArray[shortestEdgeTo[i]])){
						shortestEdgeTo[i] = newNode;	
					}
					
				}
			}
		}
	}
	
	
	/**
	 * Run the algorithm.
	 * */
	private void runPrim(){
		
		/* initialize the distances to the temporary MST to the infinity */
		for (int i = 0; i < size; i++){
			distanceToMST[i] = Integer.MAX_VALUE;
		}
		
		/* the temporary MST contains no nodes */
		for (int i = 0; i < size; i++){
			inMST[i] = false;
		}
		
		/* add the first node to the temporary MST */
		inMST[0] = true;
		updateDistances(0);	
		
		/* count the overall cost of the tree */
		//int treeCost = 0;
		
		int skip;
		int min;
		
		/* Add the nearest node to the temporary MST in each step */
		for (int treeSize = 1; treeSize < size; treeSize++) {  
			
			/* find the node with the smallest distance to the tree among nodes that are not yet in the tree */			 
			skip = 0;
			for (int i = 0; i < size; i++){							 
				if (!inMST[i]){					             
					skip = i;
					break;
				}
			}
			
			min = skip;
			for (int i = skip+1; i < size; i++){							 
				if (!inMST[i]){					           
					if (distanceToMST[i] < distanceToMST[min]){	 
						min = i;
					} else {
						if ((distanceToMST[i] == distanceToMST[min]) && (randArray[i] < randArray[min])){
							min = i;
						}
					}
				}
			}
			
			/* add the new node */								 						 
										
			inMST[min] = true; 
			//treeCost += distanceToMST[min];						
			
			/* mark which edge was added */
			edgeV0[edgeCount] = min;
			edgeV1[edgeCount] = shortestEdgeTo[min];
			edgeCount++;
			
			/* update the distances between the nodes that are not yet in the temporary MST and the temporary MST */
			updateDistances(min); 
		}
		
		//System.out.println("cost: " + treeCost);
	}


}
