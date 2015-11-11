package ptree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mst.prim.SEdge;

import common.Configuration;
import dmatrix.Matrix;
import fitchcost.SankoffAlg;
import fitchcost.SankoffAlgVertex;


/**
 * Represents a tree in terms of its cost, original vertices and intermediate (inferred) vertices.
 * */
public class Tree {

	private Log log;
	
	private List<Vertex> allVertices; 
	private List<Vertex> originalVertices;
	private List<Vertex> intermediateVertices;
	private List<Vertex> intermediateVerticesStatus0;
	
	private Matrix distanceMatrix;
	private GapHandler gapHandler;
	private Sampling sampling;
	private MutationManager mutationManager;
	
	boolean countGapAsChange;
	byte gapChar;
	
	private final int cost;
	
	
	/*private final int treeCost;//for testing
	private final int partialFitchCost;
	private final int realFitchCost;
	
	public int getTreeCost(){
		return treeCost;
	}
	
	public int getPartialFitchCost(){
		return partialFitchCost;
	}
	
	public int getRealFitchCost(){
		return realFitchCost;
	}*/
	
	
	/**
	 * Constructor.
	 * 
	 * @param sampling a sampling or null
	 * @param distanceMatrix
	 * */
	public Tree(List<Vertex> allVertices, Configuration config, List<Vertex> modifiedNonTreeVertices, 
			Sampling sampling, Matrix distanceMatrix, GapHandler gapHandler){
		
		log = LogFactory.getLog(Tree.class);
		
		this.allVertices = new ArrayList<Vertex>(allVertices);
		this.sampling = sampling;
		this.distanceMatrix = distanceMatrix;
		this.gapHandler = gapHandler;
		
		this.countGapAsChange = config.getCountGapAsChange();
		this.gapChar = config.getGapChar();
		this.mutationManager = new MutationManager(config, false);
		
		switch (config.getTreeComparisonMethod()){
		case Configuration.TREE_COMPARISON_CURRENT_COST: {
			this.cost = getCost(allVertices,config);
			//this.treeCost = this.cost;//test
		} break;
		case Configuration.TREE_COMPARISON_CURRENT_FITCH_COST: {
			byte ignoreChar = (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar());
			SankoffAlg sankoffAlg = new SankoffAlg(null, ignoreChar, new SankoffAlgUnmaskedDna(allVertices.get(0)));
			this.cost = sankoffAlg.getCost();
			//this.partialFitchCost = sankoffAlg.getCost();//test
		} break;
		case Configuration.TREE_COMPARISON_CURRENT_FITCH_COST_ORIGINALS_LEAFS: {
			/* move originals to leafs */
			List<Vertex> addedVertices = PTreeMethods.moveOriginalsToLeaves(allVertices, false);
			/* compute fitch cost */
			byte ignoreChar = (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar());
			SankoffAlg sankoffAlg = new SankoffAlg(null, ignoreChar, new SankoffAlgUnmaskedDna(allVertices.get(0)));
			this.cost = sankoffAlg.getCost();
			//this.realFitchCost = sankoffAlg.getCost();//test
			/* delete originals that were moved to leafs */
			Vertex parent;
			for (Vertex v : addedVertices){
				parent = v.getIncomingEdge();
				parent.removeOutcomingEdge(v);
			}
			//System.out.println("COSTS: " + this.treeCost + " " + this.partialFitchCost + " " + this.realFitchCost);
		} break;
		default:
			log.error("Wrong configuration for the tree comparison, default (0) used.");
			this.cost = getCost(allVertices,config);
		}
		
		this.originalVertices = new ArrayList<Vertex>();
		this.intermediateVertices = new ArrayList<Vertex>();
		this.intermediateVerticesStatus0 = new ArrayList<Vertex>();
		
		for (Vertex vertex : allVertices){
			
			if (vertex.isOriginal()){
				originalVertices.add(vertex);
			} else {
				intermediateVertices.add(vertex);
				if (vertex.getStatus() == Vertex.STATUS_ZERO){
					intermediateVerticesStatus0.add(vertex);
				}
			}
		}
		
		if ((gapHandler.getIsActive()) && (distanceMatrix != null)){
			
			/* restore gaps */
			gapHandler.restoreGaps(allVertices, modifiedNonTreeVertices, false);//restore all gaps !!!
			
			//TEST
			/*Matrix dMatrixTemp4 = new Matrix(allVertices, config); 
			dMatrixTemp4.computeDistanceMatrix(null, null, null);
			if (!Matrix.equal(dMatrixTemp4, distanceMatrix)){          
				System.err.println("Matrices are not equal! (4)");       
				System.exit(-1);                              
			} */
		}
	}
	
	
	
	
	private class SankoffAlgUnmaskedDna implements SankoffAlgVertex {

		private Vertex vertex; 
		
		public SankoffAlgUnmaskedDna(Vertex vertex){
			this.vertex = vertex;
		}
		
		@Override
		public List<SankoffAlgVertex> getChildren() {
			List<SankoffAlgVertex> list = new ArrayList<SankoffAlgVertex>();
			for (Vertex v : vertex.getOutcomingEdges()){
				list.add(new SankoffAlgUnmaskedDna(v));
			}
			return list;
		}

		@Override
		public byte[] getDnaAsBytes() {
			if (vertex.getSecDna() != null){
				return vertex.getSecDna().getBytes();
			} else {
				return vertex.getDna().getBytes();
			}
		}

		@Override
		public boolean isLeaf() {
			return vertex.isLeaf();
		}

		@Override
		public void setDna(byte[] dnaAsBytes) {
			System.err.println("Calls unimplemented method: ptree/Tree/SankoffAlgUnmaskedDna/setDna");
		}
	}
	
	
	
	public void checkDistanceMatrix(Configuration config, DNAPreprocessor dPrep){
		
		Matrix dMatrixCorrect = new Matrix(allVertices,dPrep, config);
		dMatrixCorrect.computeDistanceMatrix(null, null, null);
		if (!Matrix.equal(distanceMatrix, dMatrixCorrect)){
			System.err.println("Matrices are not equal - checkDistanceMatrix");
			System.exit(-1);
		}
	}
	
	
	public void restoreGapSubstitutions(){
		if (gapHandler.getIsActive()){
			gapHandler.restoreGapSubstitution(allVertices);
		}
	}
	
	public GapHandler getGapHandler(){
		return this.gapHandler;
	}

	
	/**
	 * Get the distance between two vertices ignoring the current sampling.
	 * */
	public Short getDistanceIgnoringSamplingTry(Vertex v0, Vertex v1){
		
		Short distance = distanceMatrix.getDistanceTry(v0, v1);
		if (distance == null){
			return null;
		}
		
		if (sampling == null){
			return distance;
		}
		
		List<Integer> positions = sampling.getSampling();
		
		byte[] dna0;
		byte[] dna1;
		
		if (v0.getSecDna() != null){
			dna0 = v0.getSecDna().getBytes();
		} else {
			dna0 = v0.getDna().getBytes();
		}
		
		if (v1.getSecDna() != null){
			dna1 = v1.getSecDna().getBytes();
		} else {
			dna1 = v1.getDna().getBytes();
		}
		
		for (int index : positions){
			
			if (mutationManager.countAsChange(dna0[index], dna1[index])){
				distance++;
			}
			
			/*if (dna0[index] != dna1[index]){
				if (countGapAsChange){
					distance++;
				} else {
					if (dna0[index] != gapChar && dna1[index] != gapChar){
						distance++;
					}
				}	
			}*/
		}
		
		return distance;
	}
	
	
	protected List<Vertex> getOriginalVertices(){
		return originalVertices;
	}
	
	public List<Vertex> getOriginalVerticesCopyList(){
		List<Vertex> list = new ArrayList<Vertex>(originalVertices.size());
		list.addAll(this.originalVertices);
		return list;
	}
	
	
	protected List<Vertex> getIntermediateVertices(){
		return intermediateVertices;
	}
	
	
	protected List<Vertex> getIntermediateVerticesStatus0(){
		return intermediateVerticesStatus0;
	}
	
	
	public int getCost(){
		return cost;
	}
	
	
	/**
	 * Computes a cost of a tree that is given as a list of vertices.
	 * */
	private static int getCost(List<Vertex> vertices, Configuration config){
		
		/* collect edges */
		List<SEdge> edges = new ArrayList<SEdge>(vertices.size()-1);
		Tree.collectEdges(edges, vertices.get(0));
		MutationManager mc = new MutationManager(config,false);
		
		/* compute cost */
		int tcost = 0;
		
		if (!edges.isEmpty()){
			
			if (edges.get(0).getV0().getSecDna() != null && (edges.get(0).getV1().getSecDna() != null)){
				for (SEdge edge : edges){
					if ((edge.getV0().getSecDna() != null) && (edge.getV1().getSecDna() != null)){
						tcost+= mc.getMutationsCount(edge.getV0().getSecDna(), edge.getV1().getSecDna());
					} else {
						tcost+= edge.getWeight();
					}
				}
			} else {
				for (SEdge edge : edges){
					tcost+= edge.getWeight();
				}
			}
		}
		
		return tcost;
	}
	
	
	/** 
	 * Helper method that collects all edges in a tree.
	 * */
	public static void collectEdges(List<SEdge> edges, Vertex root){
		
		List<Vertex> outcomingEdges = root.getOutcomingEdges();
		Mutations mutations = null;
		short weight = 0;
		
		for (Vertex child : outcomingEdges){
			
			mutations = child.getMutations();
			if ((mutations != null) && (mutations.getMutations() != null)){
				weight = (short)mutations.getMutations().size();
			} else {
				weight = 0;
			}
			edges.add(new SEdge(root, child, weight));
			collectEdges(edges, child);
		}
	}
	
	
	public List<Vertex> getAllVertices(){
		return allVertices;
	}
	
	/**
	 * Computes a cost of a tree that is given as a list of vertices.
	 * */
	/*private static int getCostConsideringTheFirstDNA(List<Vertex> vertices, Configuration config){
		
		// collect edges  
		List<SEdge> edges = new ArrayList<SEdge>(vertices.size()-1);
		Tree.collectEdges(edges, vertices.get(0));
		
		// compute cost  
		int tcost = 0;
		for (int i=0; i<edges.size(); i++){
			tcost+= edges.get(i).getWeight();
		}
		
		return tcost;
	}*/

}
