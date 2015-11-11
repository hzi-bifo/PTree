package ptree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;


/**
 * To test the tree whether it really corresponds to the phylogenetic tree.
 * */
public class PhyloTreeTest {

	private Log log;
	
	private Map<String,byte[]> map;
	private List<String> list;
	
	private boolean checkOK;
	
	
	/**
	 * Initialize the test object with input vertices.
	 * */
	public PhyloTreeTest(List<Vertex> vertices){
		
		log = LogFactory.getLog(PhyloTreeTest.class);
		
		/* map: vertex_name -> dna */
		map = new HashMap<String,byte[]>(vertices.size());
		
		/* list of vertices` names */
		list = new ArrayList<String>(vertices.size());
		
		Vertex vertex;
		byte[] bytes;
		
		for (int i=0; i<vertices.size(); i++){
			
			vertex = vertices.get(i);
			bytes = new byte[vertex.getDna().getBytes().length];
			
			for (int j=0; j<bytes.length; j++){
				bytes[j] = vertex.getDna().getBytes()[j];
			}
			
			map.put(new String(vertex.getName()), bytes);
			list.add(new String(vertex.getName()));
		}
	}
	
	
	/**
	 * Check the tree structure.
	 * Verify whether the tree contains all vertices as the input set and whether this vertices
	 * have the same DNA as vertices from the input set. Check also all parent-child relations whether edges` mutations 
	 * corresponds to the DNAs of vertices.
	 * */
	public boolean checkTree(Vertex root, Configuration config){
		
		checkOK = true;
		
		long beginTime = System.currentTimeMillis();
		
		try {
	
			/* does it contains all sequences? */
			
			/* collect all originals */
			Map<String,byte[]> mapTree = new HashMap<String,byte[]>(map.size());
			collectOriginals(root, mapTree);
			
			/* compare whether vertices in a tree correspond to the input vertices */
			String name;
			byte[] dnaInput;
			byte[] dnaTree;
			for (int i=0; i<list.size(); i++){
				name = list.get(i);
				dnaInput = map.get(name);
				dnaTree = mapTree.get(name);
				if (dnaTree == null){
					log.error("There is missing a vertex in a tree: " + name);
					checkOK = false;
					continue;
				}
				if (dnaInput.length != dnaTree.length){
					log.error("The dna of a vertex has a different length: " + name);
					checkOK = false;
					continue;
				}
				for (int j=0; j<dnaInput.length; j++){
					if (dnaInput[j] != dnaTree[j]){
						log.error("Different site in a dna: " + (char)dnaInput[j] + " -> " + (char)dnaTree[j] + " " + name);
						checkOK = false;
					}
				}
			}
			
			/* check whether the edges` costs correspond */
			checkEdgesMutations(root, config.getGapChar(), config.getCountGapAsChange());
			mapTree.clear();
			
		} catch (Exception e){
			log.error("Exception in checkTree",e);
			checkOK = false;
		}
		
		if (checkOK){
			//log.info("Output for " + root.getName() + " (first vertex name) successful, check time: " 
			//		+ (System.currentTimeMillis() - beginTime) + "ms");
		} else {
			log.error("Output for " + root.getName() + " (first vertex name) FAILED, check time: " 
					+ (System.currentTimeMillis() - beginTime) + "ms");
		}
		
		return checkOK;
	}
	
	
	/**
	 * Check whether edges` mutations corresponds.
	 * */
	private void checkEdgesMutations(Vertex vertex, byte gapChar, boolean countGapAsChange){
		
		if (vertex.getIncomingEdge() != null){
			
			/* check mutations */
			Vertex parent = vertex.getIncomingEdge();
			List<Mutation> mutations = vertex.getMutations().getMutations();
			byte[] parentDna = parent.getDna().getBytes();
			byte[] vertexDna = vertex.getDna().getBytes();
			if (parentDna.length != vertexDna.length){
				log.error("Parent has a different length of DNA.");
				checkOK = false;
			}
			
			int mutationIndex = 0;
			Mutation mutation;
			
			for (int i=0; i<parentDna.length; i++){
				if (parentDna[i] != vertexDna[i]){
					
					if ((!countGapAsChange)&&((parentDna[i] == gapChar)||(vertexDna[i] == gapChar))){
						continue;//skip gaps
					}
					
					mutation = mutations.get(mutationIndex);
					mutationIndex++;
					if (((mutation.getPosition()-1) != i)||(mutation.getFromChar() != parentDna[i])||
							(mutation.getToChar() != vertexDna[i])){
						log.error("Wrong mutation " + vertex.getName() + " " + (char)parentDna[i] + " " + (i+1) + " " + (char)vertexDna[i]);
						checkOK = false;
					}
				}
			}
			
			for (int i=0; i<mutations.size(); i++){
				mutation = mutations.get(i);
				if ((parentDna[mutation.getPosition()-1] != mutation.getFromChar())||
						(vertexDna[mutation.getPosition()-1] != mutation.getToChar())){
					log.error("Wrong mutation record " + vertex.getName() + " " + (mutation.getPosition()-1) + " ");
					checkOK = false;
				}
			}
			
		}
		
		for (int i=0; i<vertex.getOutcomingEdges().size(); i++){
			checkEdgesMutations(vertex.getOutcomingEdges().get(i), gapChar, countGapAsChange);
		}
	}
	
	
	private void collectOriginals(Vertex vertex, Map<String,byte[]> outputMap){
		
		if (vertex.isLeaf()){
			if (vertex.isOriginal()){
				outputMap.put(vertex.getName(), vertex.getDna().getBytes());
			} else {
				log.error("Discovered a leaf that is not an original vertex.");
				checkOK = false;
			}
		} else {
			for (int i=0; i<vertex.getOutcomingEdges().size(); i++){
				collectOriginals(vertex.getOutcomingEdges().get(i),outputMap);
			}
		}
	}

	
	public void clear(){
		if (map != null){
			map.clear();
			list.clear();
		}
	}
	
}
