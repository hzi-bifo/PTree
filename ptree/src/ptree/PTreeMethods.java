package ptree;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mst.prim.SEdge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;
import common.TimeStamp;
import common.Trace;


/**
 * Contains static methods to work with a tree.
 * */
public class PTreeMethods {

	private static Log log = LogFactory.getLog(PTreeMethods.class);
	
	
	
	/**
	 * Update the mutations record at each node according to the dna sequences.
	 * */
	protected static void updateMutations(Vertex root, Configuration config){
		
		if (!root.isLeaf()){
			
			Vertex child;
			Mutations mutation;
			for (int i=0; i<root.getChildren().size(); i++){
				child = root.getOutcomingEdges().get(i);
				mutation = new Mutations(root.getDna(), child.getDna(), config.getGapChar(), config.getCountGapAsChange());
				child.setMutations(mutation);
				updateMutations(child, config); 
			}
			
		}
		
	}
	
	
	/**
	 * Delete intermediates with in-degree==1 & out-degree==1.
	 * (Note that it does not delete the root)
	 * */
	protected static boolean deleteIntermediateDegree2(List<Vertex> vertices){
		boolean intermediateDeleted = false;
		Vertex vertex;
		for (int i=0; i<vertices.size(); ){
			vertex = vertices.get(i);
			if ((!vertex.isOriginal()) && (vertex.getDegree() == 2) && (vertex.getIncomingEdge() != null)
					&&  (vertex.getOutcomingEdges().size() == 1)){
				vertices.remove(i);
				intermediateDeleted = true;
				Trace.print("Deleted intermediate degree = 2 in final step: " + vertex.getName());
			} else {
				i++;
			}
		}
		return intermediateDeleted;
	}
	
	
	/** 
	 * @param vertices list of (sorted) vertices
	 * @param originals out array of original vertices that were removed
	 * 
	 * @return removed duplicates
	 * */
	protected static ArrayList<Vertex> removeDuplicateVerticesStoreOriginals(List<Vertex> vertices, List<Vertex> originals){
		
		ArrayList<Vertex> duplicateVertices = new ArrayList<Vertex>();
		
		Set<Dna> hashSet = new HashSet<Dna>();
		Vertex vertex;
		
		for (int i=0; i<vertices.size();){
			vertex = vertices.get(i);
			
			if (hashSet.contains(vertex.getDna())){
				// duplicate dna  
				duplicateVertices.add(vertex);
				if (vertex.isOriginal()){
					originals.add(vertex);
				}
				vertices.remove(i);
				//System.out.println("removed name: " + vertex.getName());
			} else {
				//System.out.println("added dna to hash table: " + vertex.getName());
				hashSet.add(vertex.getDna());
				i++;
			}	
		}
		hashSet.clear();
		return duplicateVertices;
	}
	
	
	/*protected static ArrayList<Vertex> removeDuplicateVerticesDontRemoveOriginals(List<Vertex> vertices){
	
	ArrayList<Vertex> duplicateVertices = new ArrayList<Vertex>();
	
	Set<Dna> hashSet = new HashSet<Dna>();
	Vertex vertex;
	
	for (int i=0; i<vertices.size();){
		vertex = vertices.get(i);
		
		if (hashSet.contains(vertex.getDna())){
			
			if (!vertex.isOriginal()){
				// duplicate dna  
				duplicateVertices.add(vertex);
				vertices.remove(i);
				continue;
			}
		}  
		hashSet.add(vertex.getDna());
		i++;
	}
	hashSet.clear();
	return duplicateVertices;
}*/
	
	
	/**
	 * Removes duplicate vertices. First instance (with the earliest sampling date) remains.
	 * (hash-based distinct)
	 * 
	 * @param vertices list of (sorted) vertices
	 * 
	 * @return removed duplicates
	 * */
	public static ArrayList<Vertex> removeDuplicateVertices1(List<Vertex> vertices){
		
		ArrayList<Vertex> duplicateVertices = new ArrayList<Vertex>();
		
		Set<Dna> hashSet = new HashSet<Dna>();
		Vertex vertex;
		
		for (int i=0; i<vertices.size();){
			vertex = vertices.get(i);
			
			if (hashSet.contains(vertex.getDna())){
				/* duplicate dna */
				duplicateVertices.add(vertex);
				vertices.remove(i);
			} else {
				hashSet.add(vertex.getDna());
				i++;
			}	
		}
		hashSet.clear();
		return duplicateVertices;
	}
	
	
	/**
	 * Removes duplicate vertices. First instance remains.
	 * (hash-based distinct)
	 * 
	 * @param vertices list of vertices
	 * */
	protected static void removeDuplicateVertices11(List<Vertex> vertices){
		
		Set<Dna> hashSet = new HashSet<Dna>();
		Vertex vertex;
		
		for (int i=0; i<vertices.size();){
			vertex = vertices.get(i);
			
			if (hashSet.contains(vertex.getDna())){
				/* duplicate dna */
				vertices.remove(i);
				if (vertex.isOriginal()){
					log.error("removing original!!");
				}
			} else {
				hashSet.add(vertex.getDna());
				i++;
			}	
		}
		hashSet.clear();
	}
	
	
	/**
	 * Removes duplicate vertices. First instance remains. Do not remove original vertices.
	 * (hash-based distinct)
	 * 
	 * @param vertices list of vertices
	 * */
	protected static void removeDuplicateVertices11NoOriginals(List<Vertex> vertices){
		
		Set<Dna> hashSet = new HashSet<Dna>();
		Vertex vertex;
		
		for (int i=0; i<vertices.size();){
			
			vertex = vertices.get(i);
			
			if (hashSet.contains(vertex.getDna())){
				if (!vertex.isOriginal()){
					vertices.remove(i);
				} else {
					i++;
				}
			} else {
				hashSet.add(vertex.getDna());
				i++;
			}	
		}
		hashSet.clear();
	}
	
	
	/**
	 * Remove duplicated entries from the input sorted list in terms of earliest time stamp. 
	 * If more than one vertex with earliest (smallest) time stamp exists, 
	 * leave only the first w.r.t. ID (lexicographical order).
	 * Removed vertices are added to the output list.
	 * 
	 * @param vertices input sorted list from which we remove duplicates
	 * @param duplicates output list to which we add removed duplicates
	 * */	
	public static void removeDuplicateVertices2(List<Vertex> vertices, List<Vertex> duplicates){
		
		for (;;){
			
			if (vertices.get(1).getTime() == vertices.get(0).getTime()){
				
				duplicates.add(vertices.get(1));
				
				vertices.remove(1);
				
			} else {
				break;
			}
		}
	}
	
	
	/**
	 * Set a status of each intermediate vertex.
	 * An intermediate has a status 2 iff is identical to a sequence with a status 1 or 2.
	 * It has a status 0 iff it is a new sequence.
	 * 
	 * @param intermediates list of vertices whose status will be set
	 * @param mixVerticesList list of intermediate or original vertices whose status has been already set
	 * @param originalVerticesList list of original vertices whose status has been already set.
	 * */
	public static void setStatus(List<Vertex> intermediates, List<Vertex> mixVerticesList, List<Vertex> originalVerticesList){
		
		Vertex intermediate;
		Vertex vertex;
		boolean statusFound;
		
		/* for each intermediate vertex */
		for (int i=0; i<intermediates.size(); i++){
			
			intermediate = intermediates.get(i);
			statusFound = false;
			
			for (int j=0; j< mixVerticesList.size(); j++){
				
				vertex = mixVerticesList.get(j);
				
				if (vertex.getStatus() == Vertex.STATUS_ONE || vertex.getStatus() == Vertex.STATUS_TWO){
				
					/* find out whether the intermediate vertex is identical to this vertex */
					//if (mm.getMutationsCount(vertex, intermediate) == 0){
					if (vertex.getDna().equals(intermediate.getDna())){
						intermediate.setStatus(Vertex.STATUS_TWO);
						statusFound = true;
						break;
					}
				}
			}
			
			if (!statusFound){
				/* original vertices has Vertex.STATUS_ONE */
				for (int j=0; j< originalVerticesList.size(); j++){
					
					vertex = originalVerticesList.get(j);

						/* find out whether the intermediate vertex is identical to this vertex */
						//if (mm.getMutationsCount(vertex, intermediate) == 0){
						if (vertex.getDna().equals(intermediate.getDna())){
							intermediate.setStatus(Vertex.STATUS_TWO);
							statusFound = true;
							break;
						}
				}	
			}
			
			if (!statusFound){
				intermediate.setStatus(Vertex.STATUS_ZERO);
				//System.out.println("STATUS 0: " + intermediate.getName());
			}
			//System.out.println("STATUS: " + intermediate.getStatus());
		}
	}
	
	
	protected static void setStatusIgnoringSampling(List<Vertex> intermediates, List<Vertex> mixVerticesList, List<Vertex> originalVerticesList){
		
		Vertex intermediate;
		Vertex vertex;
		boolean statusFound;
		Dna dna1, dna2;
		
		/* for each intermediate vertex */
		for (int i=0; i<intermediates.size(); i++){
			
			intermediate = intermediates.get(i);
			statusFound = false;
			
			for (int j=0; j< mixVerticesList.size(); j++){
				
				vertex = mixVerticesList.get(j);
				
				if (vertex.getStatus() == Vertex.STATUS_ONE || vertex.getStatus() == Vertex.STATUS_TWO){
				
					/* find out whether the intermediate vertex is identical to this vertex */
					//if (mm.getMutationsCount(vertex, intermediate) == 0){
					dna1 = (vertex.getSecDna() == null)?vertex.getDna():vertex.getSecDna();
					dna2 = (intermediate.getSecDna() == null)?intermediate.getDna():intermediate.getSecDna(); 
					if (dna1.equals(dna2)){
						intermediate.setStatus(Vertex.STATUS_TWO);
						statusFound = true;
						break;
					}
				}
			}
			
			if ((!statusFound) && (originalVerticesList != null)){
				/* original vertices has Vertex.STATUS_ONE */
				for (int j=0; j< originalVerticesList.size(); j++){
					
					vertex = originalVerticesList.get(j);

						/* find out whether the intermediate vertex is identical to this vertex */
						//if (mm.getMutationsCount(vertex, intermediate) == 0){
						dna1 = (vertex.getSecDna() == null)?vertex.getDna():vertex.getSecDna();
						dna2 = (intermediate.getSecDna() == null)?intermediate.getDna():intermediate.getSecDna(); 
						if (dna1.equals(dna2)){
							intermediate.setStatus(Vertex.STATUS_TWO);
							statusFound = true;
							break;
						}
				}	
			}
			
			if (!statusFound){
				intermediate.setStatus(Vertex.STATUS_ZERO);
				//System.out.println("STATUS 0: " + intermediate.getName());
			}
			//System.out.println("STATUS: " + intermediate.getStatus());
		}
	}
	
	
	/**
	 * Add original vertices from list to the list of original vertices.
	 * 
	 * @param list of original and inferred vertices
	 * @param originalVertices list of original vertices
	 * */
	public static void addOriginalVertices(List<Vertex> list, List<Vertex> originalVertices){
		for (int i=0; i< list.size(); i++){
			if (list.get(i).isOriginal()){
				originalVertices.add(list.get(i));
			}
		}
	}
	
	
	/** 
	 * Remove all inferred intermediates with degree 2 and node status 0.
	 * Removed intermediates are returned in the list.
	 * 
	 * @param vertices list of vertices that represents a tree
	 * 
	 * @return a list of the removed vertices or null (if no vertex was removed)
	 *  */
	public static List<Vertex> removeIntermediatesDegree2Status0(List<Vertex> vertices){
		
		Vertex vertex;
		List<Vertex> removedList = null;
		
		for (int i=0; i<vertices.size();){
			
			vertex = vertices.get(i);
			
			/*if (vertex.getName().equals("i201")){
				log.info("considered vertex: " + vertex.getName() + " degree:" + vertex.getDegree() 
						+ " status:" + vertex.getStatus() + " is original:" + vertex.isOriginal());
			}*/
			
			if ((vertex.getStatus() == Vertex.STATUS_ZERO) && (vertex.getDegree() == 2) && !vertex.isOriginal()){
				
				//System.out.println("removeIntermediatesDegree2Status0: " + vertices.get(i).getName());
				if (removedList == null){
					removedList = new ArrayList<Vertex>();
				}
				
				removedList.add(vertices.remove(i));
				
			} else {
				i++;
			}
		}
		
		return removedList;
	}
	
	
	protected static boolean removeIntermediatesDegree1NodeStatus0(List<Vertex> vertices){
		
		boolean vertexRemoved = false;
		
		/* run recursion from the root, degree of the vertices that are "removed" ~ "deforested" is set to 0. */
		removeInterDg1NS0(vertices.get(0));
		
		/* delete all vertices whose degree is 0 */
		for (int i=0; i<vertices.size();){
			if (vertices.get(i).getDegree() == 0){
				//System.out.println("removeIntermediatesDegree1NodeStatus0: " + vertices.get(i).getName());
				vertices.remove(i);
				vertexRemoved = true;
			} else {
				i++;
			}
		}
		return vertexRemoved;
	}
	
	
	private static boolean removeInterDg1NS0(Vertex vertex){
		
		if (vertex.isLeaf()){
			
			if ((!vertex.isOriginal()) && (vertex.getStatus() == Vertex.STATUS_ZERO)){
				
				vertex.deforest();
				
				return true;
			} else {
				return false;
			}
			
		} else {
			
			List<Vertex> children = vertex.getOutcomingEdges();
			
			for (int i=0; i< children.size();){
				
				if (removeInterDg1NS0(children.get(i))){
					
					children.remove(i);
					vertex.decDegree();
					
				} else {
					i++;
				}
			}
			
			if ((children.isEmpty()) && (!vertex.isOriginal()) && (vertex.getStatus() == Vertex.STATUS_ZERO)){
				
				vertex.deforest();
				return true;
				
			} else {
				
				return false;
			}	
		}
	}
	
	
	protected static void removeIntermediatesDegree1NodeStatus0or2(List<Vertex> vertices){
		
		/* run recursion from the root, degree of the vertices that are "removed" ~ "deforested" is set to 0. */
		removeInterDg1NS0or2(vertices.get(0));
		
		/* delete all vertices whose degree is 0 */
		for (int i=0; i<vertices.size();){
			if (vertices.get(i).getDegree() == 0){
				//System.out.println("removeIntermediatesDegree1NodeStatus0or2: " + vertices.get(i).getName());
				vertices.remove(i);
			} else {
				i++;
			}
		}
		
	}
	
	
	private static boolean removeInterDg1NS0or2(Vertex vertex){
		
		if (vertex.isLeaf()){
			
			if ((!vertex.isOriginal()) 
					&& ((vertex.getStatus() == Vertex.STATUS_ZERO) || (vertex.getStatus() == Vertex.STATUS_TWO))){
				
				vertex.deforest();
				
				return true;
			} else {
				return false;
			}
			
		} else {
			
			List<Vertex> children = vertex.getOutcomingEdges();
			
			for (int i=0; i< children.size();){
				
				if (removeInterDg1NS0or2(children.get(i))){
					
					children.remove(i);
					vertex.decDegree();
					
				} else {
					i++;
				}
			}
			
			if ((children.isEmpty()) && (!vertex.isOriginal()) 
					&& ((vertex.getStatus() == Vertex.STATUS_ZERO)||(vertex.getStatus() == Vertex.STATUS_TWO))){
				
				vertex.deforest();
				return true;
				
			} else {
				
				return false;
			}	
		}
	}
	
	
	/** 
	 * Traverse a tree and Remove all intermediates (recursively) with degree 1. 
	 * */
	public static void removeIntermediatesDegree1(List<Vertex> vertices){
		
		/* run recursion from the root, degree of the vertices that are "removed" ~ "deforested" is set to 0. */
		removeInterDg1(vertices.get(0));
		
		/* delete all vertices whose degree is 0 */
		for (int i=0; i<vertices.size();){
			if (vertices.get(i).getDegree() == 0){
				vertices.remove(i);
			} else {
				i++;
			}
		}
		
	}
	
	
	/**
	 * Recursive variant for removing intermediates with degree 1.
	 * 
	 * @return whether the vertex was removed
	 * */
	private static boolean removeInterDg1(Vertex vertex){
		
		if (vertex.isLeaf()){
			
			if (!vertex.isOriginal()){
				
				vertex.deforest();
				
				return true;
			} else {
				return false;
			}
			
		} else {
			
			List<Vertex> children = vertex.getOutcomingEdges();
			
			for (int i=0; i< children.size();){
				
				if (removeInterDg1(children.get(i))){
					
					children.remove(i);
					vertex.decDegree();
					
				} else {
					i++;
				}
			}
			
			if (children.isEmpty() && (!vertex.isOriginal())){
				
				vertex.deforest();
				return true;
				
			} else {
				
				return false;
			}	
		}
	}
	
	
	/** 
	 * Move all internal original sequences to leaf nodes, 
	 * make leaf copy for each internal original node.
	 * Traverse a tree and for each original node that is not a leaf 
	 * make a copy and connect this copy to an original node.
	 * 
	 * Merge these copies with an input vertex array.
	 * 
	 * @param vertices input and output tree
	 * 
	 * @return the list of original vertices that are leafs now (list of added vertices)
	 * */
	public static List<Vertex> moveOriginalsToLeaves(List<Vertex> vertices, boolean addCopiedVerticesToInputList){
		
		Vertex copy;
		List<Vertex> listOfAddedVertices = new ArrayList<Vertex>();
		
		for (Vertex vertex : vertices){
			if (vertex.isOriginal() && !vertex.isLeaf()){
				copy = new Vertex(vertex);
				copy.setIncomingEdge(vertex);
				vertex.setOutcomingEdge(copy);
				listOfAddedVertices.add(copy);
			}
		}
		
		if (addCopiedVerticesToInputList){
			vertices.addAll(listOfAddedVertices);
		}
		
		return listOfAddedVertices;
	}
	
	
	/**
	 * Merge old list of vertices with a new one.
	 * Clear the list of old vertices
	 * 
	 * @param oldVertices old sorted list of vertices
	 * @param new not sorted list of vertices
	 * 
	 * @return sorted list that contains elements of both input lists
	 * */
	public static List<Vertex> mergeNewVertices(List<Vertex> oldVertices, List<Vertex> newVertices){
		
		Comparator<Vertex> nameComparator = Vertex.getNameComparator();
		Comparator<Vertex> timeComparator = Vertex.getTimeComparator();
		
		/* sort according to lexicographical order */
		Collections.sort(newVertices,nameComparator);
		
		/* sort according to increasing time */
		Collections.sort(newVertices, timeComparator);
		
		List<Vertex> list = new ArrayList<Vertex>(oldVertices.size() + newVertices.size());
		int temp,i,j;
		
		for (i=0,j=0;i<oldVertices.size() && j<newVertices.size();){
			
			temp = timeComparator.compare(oldVertices.get(i), newVertices.get(j));
			
			if (temp < 0){
				list.add(oldVertices.get(i));
				i++;
			} else {
				if (temp > 0){
					list.add(newVertices.get(j));
					j++;
				} else {
					if (nameComparator.compare(oldVertices.get(i), newVertices.get(j)) < 0){
						list.add(oldVertices.get(i));
						i++;
						
					} else {
						list.add(newVertices.get(j));
						j++;
					}	
				}
			}
		}
		if (i != oldVertices.size()){
			for (;i<oldVertices.size();i++){
				list.add(oldVertices.get(i));
			}
		}
		if (j != newVertices.size()){
			for (;j<newVertices.size();j++){
				list.add(newVertices.get(j));
			}
		}
		
		oldVertices.clear();
		
		return list;
	}
	
	
	/**
	 * Destroy a tree (forest).
	 * (Removes all edges and mutations)
	 * */
	public static void deforestation(List<Vertex> vertices){
		for (int i=0; i<vertices.size(); i++){
			vertices.get(i).deforest();
		}
	}
	
	
	/** 
	 * Randomly deletes coef*100% of all elements from the list
	 * */
	public static List<Vertex> deleteElements(List<Vertex> inList, double coef, Random rand){
		List<Vertex> list = new ArrayList<Vertex>(inList);
		if (rand == null){
			rand = new Random();
		}
		int toDeleteCount = (int)((double)list.size()*coef);
		for (int i=0; i<toDeleteCount; i++){
			list.remove(rand.nextInt(list.size()));
		}
		return list;
	}
	
	
	
	/**
	 * Print the overview of the run of the algorithm.
	 * */
	protected static void printReport(List<Vertex> tree, TimeStamp ts, Integer fitchCost){
		
		/* collect edges */
		List<SEdge> edges = new ArrayList<SEdge>(tree.size()-1);
		Tree.collectEdges(edges, tree.get(0));
		
		/* compute cost */
		int cost = 0;
		for (int i=0; i<edges.size(); i++){
			cost+= edges.get(i).getWeight();
		}
		
		/* print report */
		log.info("Result for \"" + tree.get(0).getName() + "\" (1st vertex name)" + 
				((ts!=null)?("time: " +
						(new Time(System.currentTimeMillis() - ts.getTimeStampMillis() 
								- Calendar.getInstance().getTimeZone().getRawOffset()).toString()) 
						+ " (hh:mm:ss)"):"") + ((fitchCost != null)?(" fitch cost: " + fitchCost):"") 
						+ " output cost: " + cost);  
		
	}
	
	
	/**
	 * Print the tree for debugging.
	 *  
	 * */
	protected static void printTree(List<Vertex> tree, TimeStamp ts, Integer fitchCost){
		
		/* collect edges */
		List<SEdge> edges = new ArrayList<SEdge>(tree.size()-1);
		Tree.collectEdges(edges, tree.get(0));
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("Vertices: " + tree.size());
		System.out.println("Edges: " + edges.size());
		
		/* print tree */
		System.out.println("\nTree:");
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<edges.size(); i++){
			
			SEdge edge = edges.get(i);
			System.out.println("edge: " + edge.getV0().getName() + " -> " + edge.getV1().getName() + "  w:" + edge.getWeight());
			buf.append("mutations: " + edge.getV1().getName() + " " + edge.getV1().getMutations().toString() + "\n");
		}
        
		/* print vertices */
		for (int i=0; i<tree.size(); i++){
			System.out.println(tree.get(i).getName() + ": " + tree.get(i).getDna().toString());
		}
		System.out.println("");
		
		/* print mutations */
		System.out.println("\nMutations:");
		System.out.println(buf.toString());
		
		/* compute cost */
		int cost = 0;
		for (int i=0; i<edges.size(); i++){
			cost+= edges.get(i).getWeight();
		}
		//System.out.println("Cost: " + cost);
		
		log.info("Result for \"" + tree.get(0).getName() + "\" (1st vertex name) time: " + 
				((ts!=null)?(
						new Time(System.currentTimeMillis() - ts.getTimeStampMillis() 
								- Calendar.getInstance().getTimeZone().getRawOffset()).toString() 
						+ " (hh:mm:ss)"):"") + ((fitchCost != null)?(" fitch cost: " + fitchCost):"") 
						+ " output cost: " + cost);  
		System.out.println("----------------------------------------------------------------------");
		
	}
	
	
	protected static void printTreePos(Vertex root, List<Vertex> internalNodes, int pos, byte gapChar){
		
		Set<Vertex> set = new HashSet<Vertex>();
		for (Vertex v : internalNodes){
			set.add(v);
		}
		
		System.out.println("name: " + root.getName() + " char:" + (char)root.getDna().getBytes()[pos]);
		
		if (!root.getOutcomingEdges().isEmpty()){
			System.out.println("children: ");
			List<Vertex> relevantVertices = new ArrayList<Vertex>();
			for (Vertex v : root.getOutcomingEdges()){
				if ((v.getDna().getBytes()[pos] != gapChar) || (set.contains(v))){
					relevantVertices.add(v);
				}
			}
			for (Vertex v : relevantVertices){
				System.out.println("child: " + v.getName());
			}
			for (Vertex v : relevantVertices){
				printTreePos(v, internalNodes, pos, gapChar);
			}
		}
	}
	
	/*protected static void collectLeafs(List<Vertex> list, Vertex vertex){
		
		if (vertex.isLeaf()){
			list.add(vertex);
		} else {
			
			for (int i=0; i< vertex.getOutcomingEdges().size(); i++){
				collectLeafs(list,vertex.getOutcomingEdges().get(i));
			}
			
		}
		
	}*/
	
}
