package mst.prim;

import java.util.Comparator;

import ptree.Vertex;
//import ptree.branching.MutationManagerBranching;



/**
 * Represents an edge with float weight used in 
 * {@link MSTPrim#computeMST(java.util.List, DMatrix, MutationManagerBranching, MemoryManager)} method.
 * */
public class FEdge implements Comparable<FEdge> {

	protected Vertex v0;
	protected Vertex v1;
	protected float weight;
	
	
	@SuppressWarnings("unused")
	private FEdge(){
	}
	
	
	/**
	 * Creates an edge (v0,v1):weight.
	 * */
	protected FEdge(Vertex v0, Vertex v1, float weight){
		this.v0 = v0;
		this.v1 = v1;
		this.weight = weight;
	}
	
	
	/**
	 * Get edges` comparator.
	 * */
	public static Comparator<FEdge> getWeightComparator(){
		
		return new Comparator<FEdge>(){
			
			@Override
			public int compare(FEdge arg0, FEdge arg1) {
				return (arg0.weight < arg1.weight)?(-1):1;
			}		
		};
	}

	
	/**
	 * Compare weight of two edges.
	 * */
	@Override
	public int compareTo(FEdge arg0) {
		if (this.weight < arg0.weight){
			return -1; 
		} else {
			return 1;
		}
	}

}
