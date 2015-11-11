package fitchcost;

import java.util.ArrayList;
import java.util.List;


/**
 * Vertex of a tree used by the Sankoff algorithm.
 * */
public class SVertex {

	/** a vertex that is represented by this SVertex */
	private SankoffAlgVertex vertex;
	
	protected byte[] seq;
	
	protected List<SVertex> children = null;
	
	/** map: (site, char) -> (cost) */
	protected int[][] cost;
	
	/** map (site) -> (min cost)  */
	protected int[] vcost;
	
	
	/**
	 * Constructor.
	 * */
	private SVertex(SankoffAlgVertex vertex){
		this.vertex = vertex;
		byte[] seqSrc = vertex.getDnaAsBytes();
		if (vertex.isLeaf()){
			seq = seqSrc;
		} else {
			seq = new byte[seqSrc.length];
		}
		cost = new int[seq.length][];
		vcost = new int[seq.length];
	}

	
	/**
	 * Create a tree that is processed by the Sankoff algorithm 
	 * from the input tree
	 * 
	 * @return root of a tree
	 * */
	public static SVertex getTree(SankoffAlgVertex root){

		if (root.isLeaf()){			
			 return new SVertex(root);
		} else {
			
			SVertex v = new SVertex(root);
			SankoffAlgVertex child;
			v.children = new ArrayList<SVertex>();
			
			for (int i=0; i<root.getChildren().size(); i++){	
				child = root.getChildren().get(i);
				v.children.add(getTree(child));
			}
			return v;
			
		}
		
	} 

	
	public void setDna(){
		vertex.setDna(this.seq);
	}
	
	
	protected boolean isLeaf(){
		return vertex.isLeaf();
	}
	
	
	public void clear(){
		vertex = null;
		seq = null;
		if (children != null){
			children.clear();
			children = null;
		}
		for (int i=0; i<vcost.length; i++){
			cost[i] = null;
		}
		cost = null;
		vcost = null;
	}
	
}
