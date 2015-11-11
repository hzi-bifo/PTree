package mst.prim;

import java.util.Comparator;

import ptree.Vertex;
//import ptree.branching.MutationManagerBranching;


/**
 * Represents an edge with short weight used in 
 * {@link MSTPrim#computeMST(java.util.List, Matrix, MutationManagerBranching, MemoryManager)} method.
 * */
public class SEdge implements Comparable<SEdge> {
	
	protected Vertex v0;
	protected Vertex v1;
	protected short weight;
	
	
	@SuppressWarnings("unused")
	private SEdge(){
	}
	
	
	/**
	 * Creates an edge (v0,v1):weight.
	 * */
	public SEdge(Vertex v0, Vertex v1, short weight){
		this.v0 = v0;
		this.v1 = v1;
		this.weight = weight;
	}
	
	
	/**
	 * Get edges` comparator.
	 * */
	public static Comparator<SEdge> getWeightComparator(){
		
		return new Comparator<SEdge>(){
			
			@Override
			public int compare(SEdge arg0, SEdge arg1) {
				
				if (arg0.weight < arg1.weight){
					return -1; 
				} else {
					if (arg0.weight > arg1.weight){
						return 1;
					} else {
						return 0;
					}
				}
			}		
		};
	}

	
	/**
	 * Compare weight of two edges.
	 * */
	@Override
	public int compareTo(SEdge arg0) {
		short weight = arg0.weight;
		if (this.weight < weight){
			return -1; 
		} else {
			if (this.weight > weight){
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	
	public Vertex getV0(){
		return v0;
	}
	
	
	public Vertex getV1(){
		return v1;
	}
	
	public short getWeight(){
		return weight;
	}
}