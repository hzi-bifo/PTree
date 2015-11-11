package ptree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nj.NJVertex;

import common.Configuration;

import dmatrix.Matrix;
import fitchcost.SankoffAlg;

public class NJTree {

	private static Log log = LogFactory.getLog(NJTree.class);
	
	/**
	 * Get the NJ tree.
	 * 
	 * @param originals a list of all original vertices
	 * @param config configuration
	 * 
	 * @return NJ tree
	 * */
	public static Tree getNJTree(List<Vertex> originals, DNAPreprocessor dPrep, Configuration config){
		
		/* compute distance matrix */
		Matrix dMatrix = new Matrix(originals, dPrep, config);
		dMatrix.computeDistanceMatrix(null, null, null);
		
		/* compute NJ tree */
		NJVertex root = dMatrix.getNJTree(config);
		
		/* map: vertex_id -> vertex */
		Map<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(originals.size());
		for (int i=0; i<originals.size(); i++){
			idToVertex.put(i, originals.get(i));
		}
	
		/* transform NJ tree to vertices (Vertex) */
		int seqLen = originals.get(0).getDna().getBytes().length;
		Vertex rootVertex = toNJTree(root, idToVertex, seqLen);
		
		/* reconstruct the intermediates according to the Sankoff algorithm */
		byte ignoreChar = (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar());
		SankoffAlg sankoffAlg = new SankoffAlg(null, ignoreChar, rootVertex);
		
		/* compute fitch cost and reconstruct the intermediates */
		sankoffAlg.assignAndSetSequences();
		
		/* add mutations */
		PTreeMethods.updateMutations(rootVertex, config);
		
		/* collect intermediate vertices */
		List<Vertex> intermediateVertices = new ArrayList<Vertex>();
		collectIntermediateVertices(intermediateVertices, rootVertex);

		/* set the status of the intermediates */
		PTreeMethods.setStatus(intermediateVertices, new ArrayList<Vertex>(0), originals);
		
		for (int i=0; i<intermediateVertices.size(); i++){
			if (intermediateVertices.get(i).equals(rootVertex)){
				intermediateVertices.remove(i);
				break;
			}
		}
		
		List<Vertex> allVertices = new ArrayList<Vertex>(originals.size() + intermediateVertices.size() + 1);
		allVertices.add(rootVertex);
		allVertices.addAll(originals);
		allVertices.addAll(intermediateVertices);
		
		/* create "Tree" */
		return new Tree(allVertices, config, null, null, dMatrix, new GapHandler(config, null));
	}
	
	
	/**
	 * Transforms the NJ tree whose vertices are objects of class NJVertex to the tree whose vertices 
	 * are of class Vertex.
	 * 
	 * @param root the root of the NJ tree
	 * @param idToVertex mapping for the original vertices (0,1,2,3, .. -> vertex)
	 * @param seqLen the length of a DNA sequence  
	 * */
	private static Vertex toNJTree(NJVertex root, Map<Integer, Vertex> idToVertex, int seqLen){
		if (root.isLeaf()){
			Vertex rootVertex = idToVertex.get(root.getName());
			if (rootVertex == null){
				log.error("No mapping for a vertex number: " + root.getName());
				System.exit(-1);
			}
			return rootVertex;
		} else {
			Vertex rootVertex = new Vertex(-1, new Dna(new byte[seqLen]));
			Vertex child;
			for (NJVertex v : root.getChildren()){
				child = toNJTree(v, idToVertex, seqLen);
				child.setIncomingEdge(rootVertex);
				rootVertex.setOutcomingEdge(child);
			}
			return rootVertex;
		}
	}
	
	
	/**
	 * Collect all vertices in the tree.
	 * */
	private static void collectIntermediateVertices(List<Vertex> intermediateVertices, Vertex root){
		if (!root.isLeaf()){
			intermediateVertices.add(root);
			for (Vertex v : root.getOutcomingEdges()){
				collectIntermediateVertices(intermediateVertices, v);
			}
		}
	}
	
}
