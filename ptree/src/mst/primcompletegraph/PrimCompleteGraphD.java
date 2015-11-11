package mst.primcompletegraph;

import java.util.List;
import java.util.Random;

import ptree.MutationManager;
import ptree.Vertex;
import dmatrix.DMatrix;


/** See: PrimCompleteGraph */
public class PrimCompleteGraphD {// !!!

	private int size;
	
	private DMatrix distanceMatrix;
	
	private boolean inMST[]; 

	private int shortestEdgeTo[];
	private float distanceToMST[];// !!!
	
	private byte[] randArray;
	
	int edgeV0[];
	int edgeV1[];
	int edgeCount;
	
	
	/**
	 * Computes MST.
	 * */
	public static void computeMST(List<Vertex> vertices, DMatrix distanceMatrix, MutationManager mm){// !!!
		
		PrimCompleteGraphD prim = new PrimCompleteGraphD(distanceMatrix); 
		prim.runPrim();
		
		Vertex v0;
		Vertex v1;

		for (int i=0; i<prim.edgeCount; i++){
			v0 = vertices.get(prim.edgeV0[i]);
			v1 = vertices.get(prim.edgeV1[i]);
			v0.setOutcomingEdge(v1);
			v1.setOutcomingEdge(v0); 
		}
		
		mst.prim.MSTPrim.root(vertices.get(0),  mm); 
	}
	 
	
	/**
	 * Constructor.
	 * */
	private PrimCompleteGraphD(DMatrix distanceMatrix){
		this.distanceMatrix = distanceMatrix;
		this.size = distanceMatrix.getSize();
		this.inMST = new boolean[size];
		this.distanceToMST = new float[size];// !!!
		this.shortestEdgeTo = new int[size];
		this.randArray = new byte[size];
		Random rand = new Random();
		rand.nextBytes(randArray);
		edgeV0 = new int[size-1];
		edgeV1 = new int[size-1];
		edgeCount = 0;
	}

	
	
	private void updateDistances(int newNode) {
		
		float dist;// !!!
		
		for (int i = 0; i < size; i++){								
			
			if (!inMST[i]){
				
				dist = distanceMatrix.getDistance(newNode, i);
				
				if (dist < distanceToMST[i]){
					
					distanceToMST[i] = dist;				 
					shortestEdgeTo[i] = newNode;					 
				
				} else {
					// !!!
					if ((Math.abs(distanceToMST[i] - dist) < 0.001) && (randArray[newNode] < randArray[shortestEdgeTo[i]])){
						shortestEdgeTo[i] = newNode;	
					}
					
				}
			}
		}
	}
	
	
	private void runPrim(){
		
		for (int i = 0; i < size; i++){
			distanceToMST[i] = Float.MAX_VALUE; // !!!
		}
		
		for (int i = 0; i < size; i++){
			inMST[i] = false;
		}
		
		inMST[0] = true;
		updateDistances(0);	
		
		int skip;
		int min;
		
		for (int treeSize = 1; treeSize < size; treeSize++) {  
					 
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
					} else {// !!!
						if ((Math.abs(distanceToMST[i] - distanceToMST[min]) < 0.001) 
								&& (randArray[i] < randArray[min])){
							min = i;
						}
					}
				}
			}
						
			inMST[min] = true; 			
			
			edgeV0[edgeCount] = min;
			edgeV1[edgeCount] = shortestEdgeTo[min];
			edgeCount++;
			
			updateDistances(min); 
		}
		
	}


}
