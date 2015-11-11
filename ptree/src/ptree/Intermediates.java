package ptree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tests.Stat;

import mst.MST;
import mst.prim.MemoryManager;

import common.Configuration;
import common.TimeStamp;
import common.Trace;
import dmatrix.Matrix;


/**
 * To infer intermediates in MST version.
 * */
public class Intermediates {

	public static final int STRATEGY_NONE = 0;
	public static final int STRATEGY_RANDOM = 1;
	public static final int STRATEGY_BIG_SETS = 2;
	public static final int STRATEGY_BIGGEST_COST_DECREASE = 3;
	
	private int strategy;
	private double coefficient;
	private double threshold;
	private double thresholdLocalTopology;
	private int minIntAtNode;
	private boolean intFilterViaLocalTopology;
	private long intermediatesEnterLocalTopology;
	private long intermediatesPassedLocalTopology; 

	private Configuration config;
	private MutationManager mm; 
	private MemoryManager mem;
	
	private Random random;
	
	/* Statistics computed during the inferring process */
	private long intermediateInferred;
	private long intermediateChosen;
	
	private Log log;
	
	protected Intermediates(Configuration config, MemoryManager mem){
		log = LogFactory.getLog(Intermediates.class);
		this.strategy = config.getIntStrategy();
		this.coefficient = config.getIntStrategyCoefficient();
		this.threshold = config.getIntStrategyThreshold();
		this.thresholdLocalTopology = config.getIntFilterViaLocalTopologyThreshold();
		this.minIntAtNode = config.getIntStrategyMinIntAtNode();
		this.intFilterViaLocalTopology = config.getIntFilterViaLocalTopology();
		this.config = config;
		this.mm = new MutationManager(config, true);
		this.mem = mem;
		if (this.strategy == STRATEGY_RANDOM){
			random = new Random();
		} 
	}
	
	
	/**
	 * Search for repeated mutations and infer intermediate vertices.
	 *
	 * @param root the root of a tree
	 * @param the number of vertices of a tree
	 * 
	 * @return new intermediates
	 * */
	public List<Vertex> getIntermediatesFromRepeatedMutations(Vertex root, int treeSize){
		
		List<Vertex> intermediates = new ArrayList<Vertex>();
	
		this.intermediateInferred = 0;
		this.intermediateChosen = 0;
		this.intermediatesEnterLocalTopology = 0;
		this.intermediatesPassedLocalTopology = 0;
		
		dfsSearchForRepeatedMutations(root, intermediates);
		
		if (((this.strategy == STRATEGY_RANDOM) || (this.strategy == STRATEGY_BIG_SETS) 
				|| (this.strategy == STRATEGY_BIGGEST_COST_DECREASE)) && (this.intermediateInferred > 0)){
			double rate = ((double)this.intermediateChosen/(double)this.intermediateInferred);
			Trace.print("Intermediates taken: " + (rate*100) +"%");
			if (rate >= this.threshold){
				this.strategy = STRATEGY_NONE;
			}
		}
		
		if ((this.intFilterViaLocalTopology) && (this.intermediatesEnterLocalTopology > 0)){
				double rate = ((double)this.intermediatesPassedLocalTopology/(double)this.intermediatesEnterLocalTopology);
				Trace.print("Intermediates filtered via local topology: " + (rate*100) +"%");
				Trace.print("Tree size:  " + treeSize);
				if (this.intermediatesEnterLocalTopology < (((double)treeSize)*(this.thresholdLocalTopology))){
					this.intFilterViaLocalTopology = false;
				}
		}
		
		return intermediates;
	}
	
	
	/**
	 * Traverse a tree and for each internal node search for repeated mutations pattern.
	 * 
	 * @param vertex current vertex in the DFS
	 * @param intermediates input and output list of intermediate vertices 
	 * 
	 * */
	private void dfsSearchForRepeatedMutations(Vertex vertex, List<Vertex> intermediates){
		
		if (!vertex.isLeaf()){
			
			/* process this internal node */
			 
			searchRepeatedMutationsInInternalNode(vertex, intermediates);
			
			List<Vertex> children = vertex.getOutcomingEdges();
			
			/* process children of this internal node */
			for (int i=0; i< children.size(); i++){
				dfsSearchForRepeatedMutations(children.get(i), intermediates);
			}
		}
	}
	
	
	/**
	 * Infer keys (sets of mutations that repeat on at least two edges) for strategy 0, 1, or 2.
	 * */
	private static Mutations[] inferKeysStrategy012(Vertex vertex, List<Vertex> children){
		
		/* set of mutations */
		Set<Mutations> set = new HashSet<Mutations>();
		
		Mutations m;
		
		/* search intersection among children */
		for (int i=0; i<children.size(); i++){
			
			for (int j=i+1; j<children.size(); j++){
				
				/* get intersection of two children */
				m = Mutations.getIntersection(children.get(i).getMutations(), children.get(j).getMutations());
				
				/* there is an intersection */
				if (m != null){
					set.add(m);
				}
			}
		}
		
		Vertex parent = vertex.getIncomingEdge();
		
		if (parent != null){
			
			/* get inverse mutations	(vertex -> parent) */
			Mutations invMutation = Mutations.getInverseMutations(vertex.getMutations());
			
			/* search intersection: parent of vertex with children of vertex */
			for (int i=0; i<children.size(); i++){
				
				m = Mutations.getIntersection(children.get(i).getMutations(), invMutation);
				
				if (m != null){
					set.add(m);
				}	
			}
		}

		if (set.isEmpty()){ /* we haven`t found any repeated mutation pattern */
			return null;
		} else {
			return set.toArray(new Mutations[0]);
		}
	}
	
	
	/**
	 * Infer keys (sets of mutations that repeat on at least two edges) for strategy 3.
	 * */
	private static Mutations[] inferKeysStrategy3(Vertex vertex, List<Vertex> children){
		
		/* set of mutations (map: mutation -> id of vertices) */
		Map<Mutations, Set<Integer>> map = new HashMap<Mutations, Set<Integer>>();
		
		Mutations m;
		//short occurrence;
		Set<Integer> set;
		
		/* search intersection among children */
		for (int i=0; i<children.size(); i++){
			
			for (int j=i+1; j<children.size(); j++){
				
				/* get intersection of two children */
				m = Mutations.getIntersection(children.get(i).getMutations(), children.get(j).getMutations());
				
				/* there is an intersection */
				if (m != null){
					if (map.containsKey(m)){
						set = map.get(m);
						//System.out.println("MORE OCCURENCES" + occurrence);
					} else {
						set = new HashSet<Integer>(2);
						map.put(m, set);
					}
					
					set.add(children.get(i).getIntId());
					set.add(children.get(j).getIntId());
				}
			}
		}
		
		Vertex parent = vertex.getIncomingEdge();
		
		if (parent != null){
			
			/* get inverse mutations	(vertex -> parent) */
			Mutations invMutation = Mutations.getInverseMutations(vertex.getMutations());
			
			/* search intersection: parent of vertex with children of vertex */
			for (int i=0; i<children.size(); i++){
				
				m = Mutations.getIntersection(children.get(i).getMutations(), invMutation);
				
				if (m != null){
					if (map.containsKey(m)){
						set = map.get(m);
						//System.out.println("MORE OCCURENCES" + occurrence);
					} else {
						set = new HashSet<Integer>(2);
						map.put(m, set);
					}
					
					set.add(children.get(i).getIntId());
					set.add(parent.getIntId());
				}
			}
		}

		if (map.isEmpty()){ /* we haven`t found any repeated mutation pattern */
			return null;
		} else {
			short occurrence;
			Mutations keys[] = map.keySet().toArray(new Mutations[0]);
			for (int i=0; i<keys.length; i++){
				occurrence = (short)(map.get(keys[i]).size());
				keys[i].setOccurrence(occurrence);
			}
			return keys;
		}
	}
	
	
	/**
	 * Infer new intermediates for a given internal node.
	 * Strategy
	 * 
	 * @param vertex internal node
	 * @param intermediates input and output list of intermediate vertices 
	 * 
	 * */
	private void searchRepeatedMutationsInInternalNode(Vertex vertex, List<Vertex> intermediates){
		
		List<Vertex> children = vertex.getOutcomingEdges();
		
		Mutations keys[] = null;
		switch (this.strategy){
		case STRATEGY_NONE:
		case STRATEGY_RANDOM: 
		case STRATEGY_BIG_SETS:
			keys = inferKeysStrategy012(vertex, children);
			break;
		case STRATEGY_BIGGEST_COST_DECREASE:
			keys = inferKeysStrategy3(vertex, children); 
			break;	
		}
		
		if (keys == null){
			return;
		}

		int keyLength = keys.length;
		if (this.strategy == STRATEGY_RANDOM 
				|| this.strategy == STRATEGY_BIG_SETS 
				|| this.strategy == STRATEGY_BIGGEST_COST_DECREASE){

			/* compute key length and sort it, take only what is needed in the for loop */
			keyLength = Math.max(Math.min(this.minIntAtNode, keys.length), 
					Math.min((int)((double)vertex.getDegree()*this.coefficient), keyLength));
			
			this.intermediateInferred += keys.length;
			this.intermediateChosen += keyLength;

			/* sort the array according to the decreasing size of the mutation sets */
			List<Mutations> mList = new ArrayList<Mutations>(keys.length);
			for (int i=0; i<keys.length; i++){
				mList.add(keys[i]);
			}
			
			switch (this.strategy){
			case STRATEGY_RANDOM: 
				chooseAtRandom(mList, keyLength);
				break;
			case STRATEGY_BIG_SETS:
			case STRATEGY_BIGGEST_COST_DECREASE:
				Collections.sort(mList);
				break;	
			}
			
			//for (int i=0; i<keyLength; i++){
			//	keys[i] = mList.get(i);
			//}
		
			//for (int i=0; i<mList.size(); i++){
			//	System.out.print(" (" + mList.get(i).getMutationCount() + ", " + mList.get(i).getCostDecrease() + ") ");
			//}
			//System.out.println();
			//for (int i=0; i<keyLength; i++){
			//	System.out.print(" (" + keys[i].getMutationCount() + ", " + keys[i].getCostDecrease() + ") ");
			//}
			//System.out.println(); 
			//System.out.println("---------------------------"); 
		}
		
		/* filter intermediates according to the local topology 
		 * considering: parent, vertex, children of the vertex
		 * */
		if (this.intFilterViaLocalTopology){
			this.intermediatesEnterLocalTopology += keyLength;
			List<Mutations> mutInt = filterIntermediatesViaLocalTopology(vertex, keys, keyLength);
			keyLength = mutInt.size();
			for (int i=0; i<keyLength; i++){
				keys[i] = mutInt.get(i);
			}
			this.intermediatesPassedLocalTopology += keyLength;
		}
		
		Dna dna;
		Dna secDna;
		Vertex intermediate;
		
		//System.out.println("inf: " + keys.length);
		
		/* For each mutation set infer a new intermediate */
		for (int i=0; i< keyLength; i++){
			
			/* get DNA */
			dna = vertex.getDna().clone();
			dna.applyMutations(keys[i]);
			
			/* add new intermediate vertex */
			intermediate = new Vertex(-1,dna);
			
			/* set the secondary dna (corresponds to the DNA without subsampling) */
			if (vertex.getSecDna() != null){
				secDna = vertex.getSecDna().clone();
				secDna.applyMutations(keys[i]);
				intermediate.setSecDna(secDna);
			}
			
			intermediates.add(intermediate);
			
			//System.out.println("inf int: " + intermediate.getName() + " " + keys[i] + " from " + vertex.getName());
		}
	}

	
	/**
	 * Choose at random "count" elements from the list.
	 * */
	private void chooseAtRandom(List<Mutations> list, int count){
		 
		if (list.size() <= count){
			return;
		}
		
		if (list.size() < (count*2)){
			int rem = list.size() - count;
			for (int i=0; i<rem; i++){
				list.remove(random.nextInt(list.size()));
			}
			
		} else {
		
			List<Mutations> newList = new ArrayList<Mutations>(count);
			for (int i=0; i<count; i++){
				newList.add(list.remove(random.nextInt(list.size())));
			}	
			list.clear();
			list.addAll(newList);
			newList.clear();
		}
	}
	
	
	/**
	 * Filter intermediates according to the local topology considering: the vertex,
	 * parent, and children of the vertex.
	 * 
	 * @return list of mutations sets that correspond to the selected intermediates
	 * */
	private List<Mutations> filterIntermediatesViaLocalTopology(Vertex vertex, Mutations[] keys, int keyLength){
		
		/* get sites in the sequences that are relevant (contained in mutations) */
		Set<Integer> relevantSites = new HashSet<Integer>();
		List<Mutation> mutationList = new ArrayList<Mutation>();
		Mutations mutations;

		/* mutation set in the "vertex" */
		mutations = vertex.getMutations();
		if (mutations != null){
			mutationList.addAll(mutations.getMutations());
		}
		
		/* mutation set in children */
		for (int i=0; i<vertex.getOutcomingEdges().size(); i++){
			mutations = vertex.getOutcomingEdges().get(i).getMutations();
			if (mutations != null){
				mutationList.addAll(mutations.getMutations());
			}
		}
		 
		/* fill in the set of the relevant sites */
		for (int i=0; i<mutationList.size(); i++){
			relevantSites.add(mutationList.get(i).getPosition()-1);
		}
		mutationList.clear();
		
		/* mapping from the original sequences (big arrays) 
		 * to the sequences that contain only relevant sites (small arrays)
		 * 
		 * the diffArrayBig[index] contains index of the same site in the small array (or -1)
		 * the diffArraySmall[index] contains the index of the same site in the big array
		 * */
		short[] diffArrayBig = new short[vertex.getDna().getBytes().length];
		short[] diffArraySmall = new short[relevantSites.size()];
		short idxSmallArray = 0;
		for (int i=0; i<diffArrayBig.length; i++){
			if (relevantSites.contains(i)){
				diffArrayBig[i] = idxSmallArray;
				diffArraySmall[idxSmallArray] = (short)i;
				idxSmallArray++;
			} else {
				diffArrayBig[i] = -1;
			}
		}
		relevantSites.clear();
		
		/* set positions, according to the "small array" (relevant sites) 
		 * in all mutations sets that correspond to the new intermediates 
		 * */
		List<Mutation> mutationListSrc;
		List<Mutation> mutationListDst;
		Mutation mutation;
		Mutations[] keys2 = new Mutations[keyLength];
		
		for (int i=0; i<keyLength; i++){
			mutationListSrc = keys[i].getMutations();
			mutationListDst = new ArrayList<Mutation>(mutationListSrc.size());
			for (int j=0; j<mutationListSrc.size(); j++){
				mutation = mutationListSrc.get(j);
				mutationListDst.add(new Mutation(mutation,diffArrayBig[mutation.getPosition()-1]+1));				
			}
			keys2[i] = new Mutations(mutationListDst);
		}
		
		/* vertices that create the local topology */
		List<Vertex> localTopologyTree = new ArrayList<Vertex>(keyLength + vertex.getOutcomingEdges().size() + 2);
		
		/* vertices that will be considered as originals, i.e. parent, vertex, children */
		List<Vertex> originalVertices = new ArrayList<Vertex>(vertex.getOutcomingEdges().size() + 2);
		
		/* add parent */
		Vertex parent = vertex.getIncomingEdge();
		if (parent != null){
			originalVertices.add(parent);
		}
		
		/* add vertex */
		originalVertices.add(vertex);
		
		/* add children */
		for (int i=0; i<vertex.getOutcomingEdges().size(); i++){
			originalVertices.add(vertex.getOutcomingEdges().get(i));
		}
		
		/* create the copy of the original vertices with updated sequences (consider only relevant sites) */
		Vertex v;
		byte[] seqBig;
		byte[] seqSmall;
		
		for (int i=0; i<originalVertices.size(); i++){
			v = originalVertices.get(i);
			seqBig = v.getDnaAsBytes();
			seqSmall = new byte[diffArraySmall.length];
			for (int j=0; j<diffArraySmall.length; j++){
				seqSmall[j] = seqBig[diffArraySmall[j]];
			}
			localTopologyTree.add(new Vertex(v, seqSmall, true));//original vertex, new sequence, true ~ vertex is original 
		}
		
		/* the copy of the "vertex" with the updated sequence
		 * (the sequence of the intermediates will be derived from this sequence) 
		 * */
		Vertex vertexBase;
		if (parent != null){
			vertexBase = localTopologyTree.get(1);
		} else {
			vertexBase = localTopologyTree.get(0);
		}
		
		/* add candidate intermediates with the relevant sequences */
		
		/* mapping: intermediate vertex id -> index of the mutations set in the "keys" and "keys2" array */
		Map<Integer,Integer> mapIntToKey = new HashMap<Integer, Integer>(keyLength);
		
		List<Vertex> newIntermediates = new ArrayList<Vertex>(keyLength);
		Vertex intermediate;
		Dna dna;
		
		/* For each mutation set infer a new intermediate (considering only relevant sites) */
		for (int i=0; i< keys2.length; i++){
			
			/* get sequence */
			dna = vertexBase.getDna().clone();
			dna.applyMutations(keys2[i]);
			
			/* add new intermediate vertex */
			intermediate = new Vertex(-1,dna);
			mapIntToKey.put(intermediate.getIntId(), i);
			newIntermediates.add(intermediate);	
		}
		
		/* run the usual algorithm from the PTree to decide which intermediates are the best */
		
		PTreeMethods.setStatusIgnoringSampling(newIntermediates, localTopologyTree, null);
		localTopologyTree.addAll(newIntermediates);
		newIntermediates.clear();
		
		/* remove duplicate intermediate vertices (do not remove originals) */
		PTreeMethods.removeDuplicateVertices11NoOriginals(localTopologyTree);
		
		if (localTopologyTree.size() >= Short.MAX_VALUE){
			log.error("The number of candidate intermediates is too high, choose some strategy to reduce it!");
		}
		
		//Matrix dMatrix = new Matrix(localTopologyTree, this.config);
		Matrix dMatrix = new Matrix(localTopologyTree, new DNAPreprocessor(0), this.config);
		
		//dMatrix.computeDistanceMatrix();
		dMatrix.computeDistanceMatrix(null, null, null);
		
		List<Vertex> removedIntermediates = null;
		
		for (int loop=0;;loop++){
			
			TimeStamp ts = TimeStamp.getTimeStamp();
			
			if (loop == 0){
				/* compute MST, root the graph, add mutations */
				mst.MST.computeMST(localTopologyTree, dMatrix, this.mm, this.mem, this.config);
				Stat.computeMSTStandardFinished(ts);
				//Trace.print("MST compute (i) (" + localTopologyTree.size() + ")", ts);
			} else {
				if (MST.getUseOptimization(config, localTopologyTree.size(), removedIntermediates.size())){
					
					mst.MST.repairMST(localTopologyTree, removedIntermediates, dMatrix, this.mm, 
							this.mem, this.config);
					Stat.computeMSTRepairFinished(ts);
					//Trace.print("MST repair (i) (" + localTopologyTree.size() + ", " + removedIntermediates.size() + ")", ts);
					//Tree tempT1 = new Tree(localTopologyTree, this.config, null, dMatrix);
					//int costT1 = Tree.getCostConsideringTheFirstDNA(localTopologyTree, this.config);
				} else {
					PTreeMethods.deforestation(localTopologyTree);
					mst.MST.computeMST(localTopologyTree, dMatrix, this.mm, this.mem, this.config);
					Stat.computeMSTStandardFinished(ts);
					//Trace.print("MST compute (i) (" + localTopologyTree.size() + ", " + removedIntermediates.size() + ")", ts);
					//Tree tempT2 = new Tree(localTopologyTree, this.config, null, dMatrix);
					//int costT2 = Tree.getCostConsideringTheFirstDNA(localTopologyTree, this.config);
				}

				//if (costT1 != costT2){
				//	System.exit(-1);
				//}

			}
			
			/* remove intermediates with degree 1 and node status 0 */ 
			PTreeMethods.removeIntermediatesDegree1NodeStatus0(localTopologyTree);
			
			/* remove all inferred intermediates with degree 2 and node status 0 */
			removedIntermediates = PTreeMethods.removeIntermediatesDegree2Status0(localTopologyTree);
			if (removedIntermediates == null){
		
				/* no intermediate vertex has been removed */
				break;
			}
			
			/* restore the distance matrix (some intermediate vertices were deleted) */
			//dMatrix.restoreDistanceMatrixOld();
			dMatrix.restoreDistanceMatrix();
			/*if (!Matrix.equal(dMatrix, dMatrix2)){
				System.err.println("Matrices are not equal - considering the local topology");
				System.exit(-1);
			}*/
			
			/* destroy a tree that was constructed last time */
			//PTreeMethods.deforestation(localTopologyTree);	//was not commented !!!
		}
		
		dMatrix.clear();
		
		/* the end of the usual algorithm from the PTree*/
		
		/* mutations */
		List<Mutations> resultIntermediateMutations = 
			new ArrayList<Mutations>(localTopologyTree.size() - originalVertices.size());
		
		/* get the intermediates that remained */
		for (int i=0; i<localTopologyTree.size(); i++){
			if (!localTopologyTree.get(i).isOriginal()){
				resultIntermediateMutations.add(keys[mapIntToKey.get(localTopologyTree.get(i).getIntId())]);
			}
		}
		
		localTopologyTree.clear();
		originalVertices.clear();
		mapIntToKey.clear();
		
		return resultIntermediateMutations;
	}
	
	
}
