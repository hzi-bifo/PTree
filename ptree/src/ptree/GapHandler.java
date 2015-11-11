package ptree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Configuration;
import dmatrix.Matrix;
import fitchcost.SankoffAlg;
import fitchcost.SankoffAlgVertex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Gaps in the internal nodes of a tree are generally not allowed. 
 * Therefore they are canceled after an MST is build and restored for the next sampling loop.
 * It is for the settings: do not count gap as changes!!!
 * To use this class, the settings of the Mutation manager must be: Don`t buffer mutations !!!
 * All changes in the sequences are reflected in the distance matrix.
 * */
public class GapHandler {

	/* whether the gap handler is active or not */
	private boolean gapHandlerActive;
	
	private Log log;
	private byte gapChar;
	private byte ignoreChar;
	private Configuration config;
	private Matrix distanceMatrix;
	
	/* map: vertex -> list of canceled gaps (indices) */
	private Map<Vertex,List<Integer>> mapVertexToPosList;
	
	/* a vertex is in the set if at least one its gap was replaced by a character */
	private Set<Vertex> setOfGapVertices;
	
	/* data structures to store substitutions after restoreGaps */
	private List<Vertex> verticesList = null;
	private List<List<Integer>> positionList = null;
	private List<List<Byte>> substitutionList = null;

	
	
	/**
	 * Constructor.
	 * */
	public GapHandler(Configuration config, Matrix distanceMatrix){
		if (config.getCountGapAsChange()){
			this.gapHandlerActive = false;
		} else {
			this.gapHandlerActive = true;
			this.config = config;
			this.gapChar = config.getGapChar();
			this.ignoreChar = config.getMaskChar();
			this.distanceMatrix = distanceMatrix;
			this.mapVertexToPosList = new HashMap<Vertex,List<Integer>>();
			this.setOfGapVertices = new HashSet<Vertex>();
		}
		log = LogFactory.getLog(GapHandler.class);
	}		
	
	
	public boolean getIsActive(){
		return this.gapHandlerActive;
	}
	
	
	/**
	 * After this method returns: internal nodes will not contain gaps, originals that
	 * are leafs will contain gaps (as they contained gaps as input sequences) if set.
	 * An intermediate vertex can contain a gap at position "p" if all its children contain a gap 
	 * at position "p" (note: all children, not only direct children).
	 * 
	 * @param vertices vertices that represents a tree where the first vertex is the root of the tree
	 * @param restoreLeafs whether the leafs will be really restored to gaps
	 * */
	public void cancelGapsInInternals(List<Vertex> vertices, boolean restoreLeafs){
		
		if (!this.gapHandlerActive){
			return;
		}
		
		Vertex vertex2;
		List<Integer> posList;
		List<Integer> posList2;
		List<Integer> posListMerged;
		byte[] b0;
		byte[] b1;
		int counter;
		
		Set<Vertex> skipSet = new HashSet<Vertex>();
		
		if (restoreLeafs){
			
			/* restore gaps in leafs - update the distance matrix now */
			
			if (distanceMatrix != null){
				for (Vertex vertex : vertices){
					if (vertex.isLeaf() && mapVertexToPosList.containsKey(vertex)){
						
						/* positions in the DNA that were gaps and substituted by characters */
						posList = mapVertexToPosList.get(vertex);
						b0 = vertex.getDna().getBytes();
						
						/* update the distance matrix */
						for (int j=0; j<vertices.size(); j++){
						
							vertex2 = vertices.get(j);
							if (vertex.equals(vertex2) || skipSet.contains(vertex2)){
								continue;
							}
							
							if (vertex2.isLeaf() && (mapVertexToPosList.containsKey(vertex2))){
								posList2 = mapVertexToPosList.get(vertex2);
								if (posList2 == null){//TEST
									log.error("The list shouldn`t be null!");
									System.exit(-1);
								}
							} else {
								posList2 = null;
							}
							
							posListMerged = mergeLists(posList, posList2);
							
							/* correct the distance between the vertex and vertex j */
							b1 = vertex2.getDna().getBytes();
							counter = 0;
							
							for (int pos : posListMerged){
								if ((b0[pos] != b1[pos]) && (b0[pos] != gapChar) && (b1[pos] != gapChar)){
									counter++;
								}
							}
							
							/* update the distance matrix */
							if (counter > 0){
								distanceMatrix.decreaseDistance(vertex, j, counter);
							}
						}
						skipSet.add(vertex);
					}
				}
			}
			
			/* restore gaps in leafs - replace characters by gaps now */
			
			for (Vertex vertex : vertices){
				if (vertex.isLeaf() && mapVertexToPosList.containsKey(vertex)){
					posList = mapVertexToPosList.get(vertex);
					for (int pos : posList){
						if (vertex.getDna().getBytes()[pos] == gapChar){//TEST !!!
							log.error("Wants to replace a gap that is already a gap!!!");
							System.exit(-1);
						}
						vertex.getDna().getBytes()[pos] = gapChar;
						if (vertex.getSecDna() != null){
							vertex.getSecDna().getBytes()[pos] = gapChar;
						}
					}
					/* update the hash code */
					vertex.getDna().updateHashCode();
					if (vertex.getSecDna() != null){
						vertex.getSecDna().updateHashCode();
					}
					
					mapVertexToPosList.remove(vertex).clear();
					setOfGapVertices.remove(vertex);
				}	
			}
		}
		
		/* find positions in the sequences that contain gaps now (not in leafs) */
		
		List<Integer> gapPositions = new ArrayList<Integer>();
		Vertex root = vertices.get(0);
		int seqLength = root.getDna().getBytes().length;
	
		for (int i=0; i<seqLength; i++){
			for (Vertex vertex : vertices){
				if ((!vertex.isLeaf()) && (vertex.getDna().getBytes()[i] == gapChar)){
					gapPositions.add(i);
					break;
				}
			}
		}
		
		/* roots of the subtrees that contain gaps in the internal nodes */
		List<Vertex> subTreeRoots = new ArrayList<Vertex>();
		List<Vertex> verticesWithGap = new ArrayList<Vertex>();
		
		Map<Vertex, List<Integer>> tempMapVertexToPosList = new HashMap<Vertex, List<Integer>>();
		//int tempCounter =0;
		/* cancel gaps for each position */
		for (int position : gapPositions){
			
			subTreeRoots.clear();
			
			/* find subtrees made of gaps where at least one leaf of a subtree is a character */
			findSubtrees(root, position, subTreeRoots);
			
			/* cancel gaps for each subtree - gaps are replaced by characters via sankoff algorithm */
			for (Vertex r : subTreeRoots){	
				verticesWithGap.clear();
				
				/* collect vertices that have a gap at the specified position */
				collectGapVertices(r, position, verticesWithGap);
				
				//test - collect all other vertices except for verticesWithGap
				/*List<Vertex> complementList = new ArrayList<Vertex>();
				Set<Vertex> helperSet = new HashSet<Vertex>();
				for (Vertex v : verticesWithGap){
					helperSet.add(v);
				}
				for (Vertex v : vertices){
					if (!helperSet.contains(v)){
						complementList.add(v);
					}
				}
				List<Byte> charsAtPositions = new ArrayList<Byte>();
				for (Vertex v : complementList){
					charsAtPositions.add(v.getDna().getBytes()[position]);
				}
				System.out.println("---------------VerticesWithGaps (count: " + verticesWithGap.size() + ")");
				for (Vertex v : verticesWithGap){
					System.out.println(v.getName());
				}
				System.out.println("---------------Tree before substitution");
				PTreeMethods.printTreePos(r, verticesWithGap, position, gapChar);
				*/
				
				/* replace gaps of the internal vertices by characters - via sankoff algorithm */
				substituteGapsInSubtree(r, verticesWithGap, position);
				
				/*System.out.println("---------------Tree after substitution");
				PTreeMethods.printTreePos(r, verticesWithGap, position, gapChar);
				
				tempCounter++;
				if (tempCounter == 1){
					System.out.println("OK!");
					System.exit(1);
				}
				
				//test
				boolean wrong = false;
				for (int i=0; i<complementList.size(); i++){
					if (complementList.get(i).getDna().getBytes()[position] != charsAtPositions.get(i)){
						log.error("Position changed out of allowed region !!! " + 
								complementList.get(i).getName() + " " + position + " " +
								((char)(charsAtPositions.get(i).byteValue())) + " -> " +
								(char)(complementList.get(i).getDna().getBytes()[position]) +
								" isLeaf: " + complementList.get(i).isLeaf() + " isOriginal: " +
								complementList.get(i).isOriginal());
						wrong = true;
					}
				}
				if (wrong){
					
					System.exit(-1);
				}
				*/
				
				
				/* note at which positions in a sequence (of a vertex) a gap was replaced by a character */
				for (Vertex v : verticesWithGap){
					if (v.getDna().getBytes()[position] != gapChar){
						/* the gap was replaced by a character */
						if (mapVertexToPosList.containsKey(v)){
							mapVertexToPosList.get(v).add(position);
						} else {
							posList = new ArrayList<Integer>();
							posList.add(position);
							mapVertexToPosList.put(v, posList);
						}
						
						if (tempMapVertexToPosList.containsKey(v)){
							tempMapVertexToPosList.get(v).add(position);
						} else {
							posList = new ArrayList<Integer>();
							posList.add(position);
							tempMapVertexToPosList.put(v, posList);
						}
					} else {//test
						log.error("There should not have been a gap*!!!"); // !!!!!!!!!!!!!!!!!!!!!!!!
						System.exit(-1);
					}
				}
			}
		}
		
		/* set new distances in the distance matrix */
		if (distanceMatrix != null){
			Vertex[] updatedVertices = tempMapVertexToPosList.keySet().toArray(new Vertex[0]);
			
			skipSet.clear();
			
			for (Vertex vertex : updatedVertices){
				b0 = vertex.getDna().getBytes();
				posList = tempMapVertexToPosList.get(vertex);
			
				for (int j=0; j<vertices.size(); j++){
					vertex2 = vertices.get(j);
					if (vertex.equals(vertex2) || skipSet.contains(vertex2)){
						continue;
					}
					
					posList2 = tempMapVertexToPosList.get(vertex2);
					posListMerged = mergeLists(posList, posList2);
					
					b1 = vertex2.getDna().getBytes();
					counter = 0;
					for (int pos : posListMerged){
						if ((b0[pos] != b1[pos]) && (b0[pos] != gapChar) && (b1[pos] != gapChar)){
							counter++;
						}
					}
					if (counter > 0){
						distanceMatrix.increaseDistance(vertex, j, counter);
					}
				}
				
				/* add the vertex to the set of the updated vertices */
				setOfGapVertices.add(vertex);
				skipSet.add(vertex);
				
				/* update the hash code of the sequences */
				vertex.getDna().updateHashCode();
				if (vertex.getSecDna() != null){
					vertex.getSecDna().updateHashCode();
				}
			}
			skipSet.clear();
		}
		
		/* update the mutation sets on the branches of the tree */
		if (!tempMapVertexToPosList.isEmpty()){
			repairMutationSets(root, new MutationManager(config, false));
			tempMapVertexToPosList.clear();
		}
		/*System.out.println("OK");
		System.exit(1);*/
	}


	/**
	 * All the gaps that were replaced by characters will be replaced back with the gaps.
	 * 
	 * @param repairMutations whether the mutations on the edges will be updated
	 * @param vertices the vertices of the tree, the first vertex is the root
	 * */
	public void restoreGaps(List<Vertex> vertices, List<Vertex> modifiedNonTreeVertices, 
			boolean repairMutations){
		
		if (!this.gapHandlerActive){
			return;
		}
		
		if (!setOfGapVertices.isEmpty()){
			
			verticesList = new ArrayList<Vertex>();
			positionList = new ArrayList<List<Integer>>();
			substitutionList = new ArrayList<List<Byte>>();
			
			Vertex vertex2;
			List<Integer> posList;
			List<Integer> posList2;
			List<Integer> posListMerged;
			Set<Vertex> ignoreSet = new HashSet<Vertex>();
			byte[] b0;
			byte[] b1;
			int counter;
			
			/* vertices with replaced gaps */
			
			List<Vertex> updatedVertices = new ArrayList<Vertex>();
			for (Vertex vertex : vertices){
				if (setOfGapVertices.contains(vertex)){
					updatedVertices.add(vertex);
				}
			}
			Set<Vertex> setOfNonTreeVertices = new HashSet<Vertex>();
			for (Vertex vertex : modifiedNonTreeVertices){
				if (setOfGapVertices.contains(vertex)){
					updatedVertices.add(vertex);
					setOfNonTreeVertices.add(vertex);
				}
				
			}
	
			/* update the distance matrix */
			
			for (Vertex vertex : updatedVertices){
				
				if (setOfNonTreeVertices.contains(vertex)){
					continue;
				}
				
				b0 = vertex.getDna().getBytes();
				if (mapVertexToPosList.containsKey(vertex)){
					posList = mapVertexToPosList.get(vertex);
					for (int j=0; j<vertices.size(); j++){
						vertex2 = vertices.get(j);
						if (vertex.equals(vertex2) || (ignoreSet.contains(vertex2))){
							continue;
						}
						
						if (mapVertexToPosList.containsKey(vertex2)){
							posList2 = mapVertexToPosList.get(vertex2);
						} else {
							posList2 = null;
						}
						
						posListMerged = mergeLists(posList, posList2);
						
						b1 = vertex2.getDna().getBytes();
						counter = 0;
						for (int pos : posListMerged){
							if ((b0[pos] != b1[pos]) && (b0[pos] != gapChar) && (b1[pos] != gapChar)){
								counter++;
							}
						}
						if (counter > 0){
							distanceMatrix.decreaseDistance(vertex, j, counter); 
						}
					}
					ignoreSet.add(vertex);
				}
			}
			
			/* assign gaps where they were and store changes */
			
			List<Integer> positionListTemp;
			List<Byte> substitutionListTemp;
			
			for (Vertex vertex : updatedVertices){
				
				positionListTemp = null;
				substitutionListTemp = null;
				b0 = vertex.getDna().getBytes();
				
				if (mapVertexToPosList.containsKey(vertex)){
					posList = mapVertexToPosList.get(vertex);
					
					for (int pos : posList){
						
						if (b0[pos] != gapChar){
							
							/* store substitution */
							if (!setOfNonTreeVertices.contains(vertex)){
								if (positionListTemp == null){
									positionListTemp = new ArrayList<Integer>();
									substitutionListTemp = new ArrayList<Byte>();
								}
								
								positionListTemp.add(pos);
								substitutionListTemp.add(b0[pos]);
							}
							
							b0[pos] = gapChar;
							if (vertex.getSecDna() != null){
								vertex.getSecDna().getBytes()[pos] = gapChar;
							}
						} else {//TEST !!!
							log.error("There shouldn`t be a gap !!!");
							System.exit(-1);
						}
					}
					
					if (positionListTemp != null){
						verticesList.add(vertex);
						positionList.add(positionListTemp);
						substitutionList.add(substitutionListTemp);
						
					}
					vertex.getDna().updateHashCode();
					if (vertex.getSecDna() != null){
						vertex.getSecDna().updateHashCode();
					}
				} else { // TEST !!!
					log.error("The set should contain this vertex  !!!");
					System.exit(-1);
				}	
			}
		
			/* repair the mutations sets on the edges if requested */
			if (repairMutations) {
				Vertex root = vertices.get(0);
				repairMutationSets(root, new MutationManager(config, false));
			}
			
			setOfGapVertices.clear();
			mapVertexToPosList.clear();
			ignoreSet.clear();
		}
	}
	
	
	/**
	 * Restore gaps in all original vertices.
	 * Does not update the distance matrix!!!
	 * */
	public static void restoreGapsInAllOriginals(GapHandler handler){
		
		if (!handler.gapHandlerActive){
			return;
		}
		
		List<Vertex> vertices = handler.verticesList;
		List<Integer> posList;
		Vertex vertex;
		byte[] b0;
		byte[] b1;
		
		for (int i=0; i<vertices.size(); i++){
			vertex = vertices.get(i);
			if (vertex.isOriginal()){
				posList = handler.positionList.get(i);
				b0 = vertex.getDna().getBytes();
				if (vertex.getSecDna() != null){
					b1 = vertex.getSecDna().getBytes();
					for (int pos : posList){
						b0[pos] = handler.gapChar;
						b1[pos] = handler.gapChar;
					}
					vertex.getDna().updateHashCode();
					vertex.getSecDna().updateHashCode();
				} else {
					for (int position : posList){
						b0[position] = handler.gapChar;
					}
					vertex.getDna().updateHashCode();
				}
			}
		}
	}
	
	
	/**
	 * Substitute gaps with the characters.
	 * Don`t update the distance matrix !!!
	 * */
	public void restoreGapSubstitution(List<Vertex> vertices){
		
		if (!this.gapHandlerActive){
			return;
		}
		
		Vertex vertex;
		List<Integer> posList;
		List<Byte> subList;
		byte[] b0;
		byte[] b1;
		int pos;
		byte b;
		
		/* replace gaps with characters */
		for (int i=0; i<verticesList.size(); i++){
			
			vertex = verticesList.get(i);
			posList = positionList.get(i);
			subList = substitutionList.get(i);
			b0 = vertex.getDna().getBytes();
			
			if (vertex.getSecDna() != null){
				b1 = vertex.getSecDna().getBytes();
				for (int j=0; j<posList.size(); j++){
					pos = posList.get(j);
					b = subList.get(j);
					if (b0[pos] != gapChar){
						log.error("does not contain gap char!!! (1) " + (char)b0[pos]);
						System.exit(-1);
					}
					b0[pos] = b;
					b1[pos] = b;
				}
				vertex.getDna().updateHashCode();
				vertex.getSecDna().updateHashCode();
			} else {
				for (int j=0; j<posList.size(); j++){
					if (b0[posList.get(j)] != gapChar){
						log.error("does not contain gap char!!! (2)");
						System.exit(-1);
					}
					b0[posList.get(j)] = subList.get(j); 
				}
				vertex.getDna().updateHashCode();
			}	
		}
		
		//Vertex vertex2;
		//int counter;
		
		/* adjust the distance matrix */
		/*for (int i=0; i<verticesList.size(); i++){
			
			vertex = verticesList.get(i);
			posList = positionList.get(i);
			subList = substitutionList.get(i);
			b0 = vertex.getDna().getBytes();
			
			for (int j=0; j<vertices.size(); j++){
				
				vertex2 = vertices.get(j);
				if (vertex.equals(vertex2)){
					continue;
				}
				b1 = vertex2.getDna().getBytes();
				counter = 0;
				for (int k=0; k<posList.size(); k++){
					pos = posList.get(k);
					if ((b1[pos] != gapChar) && (b0[pos] != b1[pos])){
						counter++;
					}
				}
				if (counter > 0){
					distanceMatrix.increaseDistance(vertex, j, counter);
				}		
			}	
		}*/
	}
	

	public void repairMutationSets(Vertex root){
		if (this.gapHandlerActive){
			repairMutationSets(root, new MutationManager(config, false));
		}
	}


	/**
	 * Mutations on the edges of the tree are computed again since the sequences may have been changed.
	 * */
	private void repairMutationSets(Vertex root, MutationManager mutationManager){
		
		List<Vertex> children = root.getOutcomingEdges();
		
		for (Vertex child : children){
			child.setMutations(mutationManager.getMutations(root, child));
			repairMutationSets(child, mutationManager);		
		}
	}
	

	/**
	 * Test whether the list contains only unique integers.
	 * 
	 * @return true if the list contain only unique integers
	 * */
	private boolean oneIndexOnlyOnce(List<Integer> list){
		Set<Integer> hashSet = new HashSet<Integer>();
		for (int elem : list){
			if (hashSet.contains(elem)){
				hashSet.clear();
				return false;
			} else {
				hashSet.add(elem);
			}
		}
		hashSet.clear();
		return true;
	}
	
	
	/**
	 * Merge two lists.
	 * 
	 *  @param list1 can be null
	 *  @return merged list without duplicates 
	 * */
	private List<Integer> mergeLists(List<Integer> list0, List<Integer> list1){
		
		if (!oneIndexOnlyOnce(list0) || ((list1 != null)&&(!oneIndexOnlyOnce(list1)))){ //TEST !!! 
			
			log.error("One index is contained more than once.");
			System.out.println("list0: ");
			for (int val : list0){
				System.out.print(" " + val);
			}
			System.out.println();
			if (list1 != null){
				System.out.println("list1: ");
				for (int val : list1){
					System.out.print(" " + val);
				}
				System.out.println();
			}
			System.exit(-1);
		}
		
		if (list1 == null){
			return list0;
		} else {
			List<Integer> listRet = new ArrayList<Integer>(Math.max(list0.size(), list1.size()));
			Set<Integer> hashSet = new HashSet<Integer>(Math.min(list0.size(), list1.size()));
			
			if (list0.size() > list1.size()){
				List<Integer> tempList = list0;
				list0 = list1;
				list1 = tempList;
			} 
			
			for (int val : list0){
				hashSet.add(val);
				listRet.add(val);
			}
			
			for (int val : list1){
				if (!hashSet.contains(val)){
					listRet.add(val);
				}
			}
			
			hashSet.clear();
			
			if (!oneIndexOnlyOnce(listRet)){ //TEST !!!
				log.error("One index is contained more than once.");
				System.out.println("merged list:");
				for (int val : listRet){
					System.out.print(" " + val);
				}
				System.out.println();
				System.exit(-1);
			}
			
			return listRet;
		}
	}
	
	
	/**
	 * Collect all vertices that contain a gap at a specified position (except for leafs).  
	 * 
	 * @return whether a leaf that is non-gap character was found in the subtree
	 * */
	private boolean collectGapVertices(Vertex root, int position, List<Vertex> vertices){
		
		byte b = root.getDna().getBytes()[position];
		
		if (root.isLeaf()){
			if (b != gapChar){
				return true;
			} else {
				return false;
			}
		} else {
			if (b != gapChar){
				return true;
			} else {
				boolean leafFound = false;
				
				for (Vertex child : root.getOutcomingEdges()){
					if (collectGapVertices(child, position, vertices)){
						leafFound = true;
					}
				}
				
				if (leafFound){
					vertices.add(root);
				}
				
				return leafFound;
			}
		}
	}
	
	
	/**
	 *  Find all roots of subtrees.
	 * */
	private void findSubtrees(Vertex root, int position, List<Vertex> subTreeRoots){
	
		if (!root.isLeaf()){
			if (root.getDna().getBytes()[position] == gapChar){
				
				VContainer vdata = getSuccessors(root, position);
				
				List<Vertex> children;
				if (vdata != null){
					if (vdata.vertex == null){
						children = vdata.vertices;
					} else {
						children = new ArrayList<Vertex>();
						children.add(vdata.vertex);
					}
				} else {
					children = null;
				}
				
				if (children != null){
					subTreeRoots.add(root);
					for (Vertex child : children){
						findSubtrees(child, position, subTreeRoots);
					}
				}
					
			} else {
	
				for (Vertex child : root.getOutcomingEdges()){
					findSubtrees(child, position, subTreeRoots);
				}
			}
		}
	}
	
	
	/**
	 * Get all leafs of a subtree where all internal nodes of a subtree are gaps and
	 * all leafs are characters (leafs of a subtree that are also leafs of the whole tree can be gaps.)  
	 * */
	private VContainer getSuccessors(Vertex root, int position){
	
		VContainer successors = null;
		VContainer successorsTemp = null;
		
		if (root.getDna().getBytes()[position] == gapChar){
			
			for (Vertex  child : root.getOutcomingEdges()){
				successorsTemp = getSuccessors(child, position);
				
				if (successorsTemp != null){
					
					if (successorsTemp.vertex != null){
						//add one vertex
						if (successors == null){
							successors = successorsTemp;
						} else {
							
							if (successors.vertex != null){
								List<Vertex> array = new ArrayList<Vertex>();
								array.add(successors.vertex);
								array.add(successorsTemp.vertex);
								successors = new VContainer(array);
							} else {
								successors.vertices.add(successorsTemp.vertex);
							}
						}
					} else {
						//add vertices
						if (successors == null){
							successors = successorsTemp;
						} else {
							if (successors.vertex != null){
								successorsTemp.vertices.add(successors.vertex);
								successors = successorsTemp;
							} else {
								successors.vertices.addAll(successorsTemp.vertices);
								successorsTemp.vertices.clear();
							}
						}
					}
				}
			}
		} else {
			successors = new VContainer(root);
		}
		return successors;
	}

	
	/**
	 * A container to either return a vertex or a list of vertices. 
	 * */
	private class VContainer { 
		
		public final List<Vertex> vertices;
		public final Vertex vertex;
		
		public VContainer(List<Vertex> vertices){
			this.vertices = vertices;
			this.vertex = null;
		}
		
		public VContainer(Vertex vertex){
			this.vertices = null;
			this.vertex = vertex;
		}
	}
	
	
	/**
	 * Replace gaps in the intermediate vertices using the sankoff algorithm.
	 * */
	private void substituteGapsInSubtree(Vertex root, List<Vertex> innerVertices, int position){
		
		//SankoffAlg sankoffAlg = new SankoffAlg(null, this.ignoreChar, new SankVertex(root, position));
		SankoffAlg sankoffAlg = new SankoffAlg(null, this.gapChar, new SankVertex(root, innerVertices, position));
		
		Vertex parent = root.getIncomingEdge();
		
		if (parent != null){
			byte[] b = new byte[1];
			b[0] = parent.getDna().getBytes()[position];
			sankoffAlg.assignAndSetSequences(b);
		} else {
			sankoffAlg.assignAndSetSequences();
		}
	}
	
	
	/**
	 * Represents one vertex of the tree that is passed to the sankoff alg. where one vertex
	 * represents one position in the sequence.
	 * */
	public class SankVertex implements SankoffAlgVertex {
		
		private Vertex vertex;
		private int position;
		private Set<Vertex> internalVerticesSet;
		
		
		/**
		 * @param position a position in a sequence that will be considered by the sankoff. algorithm.
		 * */
		public SankVertex(Vertex vertex, List<Vertex> internalVertices, int position){
			this.vertex = vertex;
			this.position = position;
			this.internalVerticesSet = new HashSet<Vertex>();
			for (Vertex v : internalVertices){
				this.internalVerticesSet.add(v);
			}
		}
		
		private SankVertex(Vertex vertex, Set<Vertex> internalVerticesSet, int position){
			this.vertex = vertex;
			this.position = position;
			this.internalVerticesSet = internalVerticesSet;
		}

		@Override
		public List<SankoffAlgVertex> getChildren(){
			
			List<SankoffAlgVertex> list = new ArrayList<SankoffAlgVertex>();
			
			if ((vertex.isLeaf()) || (vertex.getDna().getBytes()[position] != gapChar)){
				return list;
			} else {
				for (Vertex child : vertex.getOutcomingEdges()){
					if ((child.getDna().getBytes()[position] != gapChar) || 
							(internalVerticesSet.contains(child))){
						list.add(new SankVertex(child, internalVerticesSet, position));
					}	
				}
				return list;	
			}
		}
		
		
		@Override
		public boolean isLeaf() {
			if ((vertex.isLeaf()) || (vertex.getDna().getBytes()[position] != gapChar)){
				return true;
			}
			
			for (Vertex child : vertex.getOutcomingEdges()){
				if ((child.getDna().getBytes()[position] != gapChar) || 
						(internalVerticesSet.contains(child))){
					return false;
				}	
			}
			
			return true;
		}
		
		
		@Override
		public byte[] getDnaAsBytes() {
			
			byte[] b = new byte[1];
			b[0] = vertex.getDna().getBytes()[position];
			return b;
		}
		
		
		@Override
		public void setDna(byte[] dnaAsBytes) {
			
			vertex.getDna().getBytes()[position] = dnaAsBytes[0];
			if (vertex.getSecDna() != null){
				vertex.getSecDna().getBytes()[position] = dnaAsBytes[0];
			}
			
		}
		
	}
	
	public void clear(){
		mapVertexToPosList.clear();
		setOfGapVertices.clear();
	}
}
