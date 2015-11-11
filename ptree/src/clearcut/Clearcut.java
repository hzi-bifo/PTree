package clearcut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nj.NJVertex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import ptree.Vertex;


import common.Configuration;


/**
 * Provides access to the "Clearcut" C library that computes the NJ tree.
 * NJ alg. can be standard or relaxed. Jukes-Cantor or Kimura correction can be used 
 * (if we provide the sequences, not only the distance matrix).
 * The "Clearcut" library can be used only mutually exclusive which is handled by this class
 * (there is a lock).
 * */
public class Clearcut {

	private static Log log = LogFactory.getLog(Clearcut.class);
	
	/* to handle access to the "Clearcut" library */
	private static Lock lock = new ReentrantLock();

	/* loads the library */
	static {
		System.loadLibrary("Clearcut");
	}
	
	/** We can`t create Clearcut instance outside of this class. */
	private Clearcut(){
	}
	
	
	/**
	 * Native method that calls method of the "Clearcut" library (see clearcut_Clearcut.h).
	 * Details are described in "howToCreateClearcutLibrary.txt"
	 * 
	 * @param fastaFile file that represents either dna sequences or a distance matrix in fasta format 
	 * @param arguments arguments that are passed to clearcut
	 * @param argumentCount number of arguments
	 * 
	 * @return matrix that represents an NJ tree (not a distance matrix!)
	 * */
	private native float[][] clearcutMain(String fastaFile, String arguments, int argumentCount);
	
	
	/**
	 * Computes an NJ tree using the "Clearcut" library.
	 * 
	 * @param fastaFile file that represents either dna sequences or a distance matrix in fasta format 
	 * @param config the configuration
	 * 
	 * @return the root of the NJ tree
	 * */
	public static NJVertex computeNJTree(String fastaFile, Configuration config){
		
		/* matrix returned by clearcut (linearized NJ tree, not distance matrix!) */
		float njTree[][] = runClearcut(fastaFile, config);
		
		/* transform the NJ tree given as a matrix (linearized tree) to an NJ tree */
		NJVertex root = new Clearcut().clearcutNjTreeToGraph(njTree);
		
		return root;
	}

	
	/**
	 * Computes the NJ matrix using the "Clearcut" library.
	 * 
	 * @param fastaFile file that represents either dna sequences or a distance matrix in fasta format (see {@link Test#main(String[])})
	 * @param config configuration
	 * @param matrixOutputSize size of the output matrix (number of leafs of the NJ tree ~ original sequences)
	 * 
	 * @return distance matrix that corresponds to the distances in the NJ tree 
	 * */
	public static float[][] computeNJMatrix(String fastaFile, Configuration config, short matrixOutputSize){
	
		//System.out.println("before run clearcut");
		/* matrix returned by clearcut (linearized NJ tree, not distance matrix!) */
		float clearcut_matrix[][] = runClearcut(fastaFile, config);
		//System.out.println("after run clearcut");
		//printOutputEdges(clearcut_matrix);
		/* compute an NJ matrix from a NJ tree */
		float[][] distance_matrix = new Clearcut().computeNJMatrix(clearcut_matrix, matrixOutputSize);
		return distance_matrix;
	}

	
	/**
	 * Runs "Clearcut" to get a linearized NJ tree.
	 * 
	 * @param fastaFile file that represents either dna sequences or a distance matrix in fasta format
	 * @param config configuration
	 * 
	 * @return linearized NJ tree
	 * */
	private static float[][] runClearcut(String fastaFile, Configuration config){
		
		/* printings */
		//System.out.println("run clearcut");
		//System.out.println("Fasta file: " + fastaFile);
		//System.out.println("Matrix output size: " + matrixOutputSize);
		//System.out.println("nj implementation: " + config.getNjImplementation());
		//System.out.println("nj correction: " + config.getNjCorrection());
		//System.out.println("nj relaxed version: " + config.getNjRelaxedVersion());
		
		Clearcut c = new Clearcut();
		
		/* arguments of the "Clearcut" alg. 
		 * clearcut01.exe ~ name of the program (is not considered can be anything)
		 * --out ~ name of the output file (is not considered can be anything)
		 * --in ~ name of the input file (is not considered can be anything)
		 * 
		 * -a ~ input is a set of aligned sequences
		 * -D ~ input alignment are DNA sequences
		 * */
		StringBuffer arg = new StringBuffer("clearcut01.exe --in=alignment1.dist --out=output.txt");
		int argCount = 3;
		
		//if (config.getComputeDmInJava() && (config.getNjCorrection() == 0)){
			/* the input of clearcut is DM */
			arg.append(" -d");
			argCount++;
			//System.out.println("Compute in java");
		/*} else {
			// the input of clearcut is a list of DNA sequences  
			arg.append(" -a -D");
			argCount+=2;
		
			// correction  
			if (config.getNjCorrection() == 1){
				
				// use Jukes-Cantor correction  
				arg.append(" -j");
				argCount++;
				
			} else {
				if (config.getNjCorrection() == 2){
					
					// use Kimura correction  
					arg.append(" -k");
					argCount++;
				}
			}	
		}*/
		
		if (config.getNjRelaxedVersion() == 0){
			
			/* use traditional Neighbor-Joining algorithm (not relaxed NJ) */
			arg.append(" -N");
			argCount++;
		}
		
		if (config.getVerbose() == 1){
			
			/* verbose output (more printings) */
			arg.append(" -v");
			argCount++;
		}
		
		/* matrix returned by clearcut (linearized NJ tree, not distance matrix!) */
		float clearcut_matrix[][] = null;
		
		/* call native method (must be mutually exclusive) */
		lock.lock();
		try {
			//System.out.println("arg: " + arg.toString() + "\n argc:" + argCount + "\n fastaFile: " + fastaFile);
			clearcut_matrix = c.clearcutMain(fastaFile, arg.toString(), argCount);
		} catch (Exception e){
			log.error("An exception...",e); 
		} finally {
			lock.unlock();
		}
		
		return clearcut_matrix;
	}
	
	
	/**
	 * Compute a NJ matrix from a given linearized NJ tree.
	 * 
	 * @param njTree linearized NJ tree returned from the "Clearcut"
	 * @param matrixSize the size of the output matrix (~number of leafs==number of clearcut`s input sequences)
	 * */
	private float[][] computeNJMatrix(float njTree[][], int matrixSize){
		
		/* allocate the output matrix */
		float matrix[][] = new float[matrixSize][];
		for (int i=0; i<matrixSize; i++){
			matrix[i] = new float[i];
		}
		  
		/* initialize the output matrix (with max values) */ 
		for (int i=0; i<matrixSize; i++){
			for (int j=0; j<matrix[i].length; j++){
				matrix[i][j] = Float.MAX_VALUE;
			}
		}
		  
		/* compute a graph from a NJ tree */
		GCVertex root = clearcutNjTreeToGraph(njTree);
		//print(root, null);//print the graph
		  
		/* collect leafs */
		List<GCVertex> leafs = new ArrayList<GCVertex>(matrixSize);
		collectLeafs(root,null, leafs);
		  
		/* print leafs */
		//for (int i=0; i<leafs.size(); i++){
		//	  System.out.println(leafs.get(i).number);
		// }
		  
		GCVertex leaf;
		GCVertex node;
		
		/* From each leaf: go to the other leafs (DFS) and set the distance, delete the leaf from which we searched */
		for (int i=0; i<leafs.size(); i++){
			leaf = leafs.get(i);
			setDistance(leaf, leaf, null, matrix); 
			node = leaf.neighbours.get(0);
			for (int j=0; j<node.neighbours.size(); j++){
				if (node.neighbours.get(j) == leaf){
					node.neighbours.remove(j);
					node.neighboursDist.remove(j);
					break;
				}
			}
		}
		  
		//printMatrix(matrix);//print the matrix
		
		return matrix;
	}


	/**
	 * Do DFS and collect all leafs.
	 * 
	 * @param vertex the vertex from which we start the DFS
	 * @param parent parent of the vertex or null
	 * @param leafs list to collect leafs
	 * */
	private void collectLeafs(GCVertex vertex, GCVertex parent, List<GCVertex> leafs){
		  
		if (vertex.isLeaf){
			leafs.add(vertex);
		} else {
			GCVertex child;
			for (int i=0; i<vertex.neighbours.size(); i++){
				child = (GCVertex)vertex.neighbours.get(i);
				if (child != parent){
					collectLeafs(child,vertex,leafs);
				}  
			}
		}
	}
	  
	
	/**
	 * Transform the linearized NJ tree from "Clearcut" to a graph with vertices of class {@link GCVertex}.
	 * 
	 * @param clearcutOutput output of the "Clearcut".
	 * 
	 * @return the root of the graph
	 * */  
	private GCVertex clearcutNjTreeToGraph(float[][] clearcutOutput){
		  
		/* number of graph`s edges */
		int size = (int)clearcutOutput[3][0];
		
		//System.out.println("edges num: " + size);
		
		if (size ==0){
			return null;
		}
		  
		GCVertex root = null; // root of the graph
		GCVertex cv = null;//current vertex
		GCVertex t = null; //temp vertex
		
		//one edge is (from, to):dist
		int from = (int)clearcutOutput[0][0];//from vertex
		int to;//to vertex
		float dist;//edge`s length
		    
		root = new GCVertex(from);
		cv = root;
		  
		/* add edges one by one to a graph in order that is given by the DFS alg (first all left, then all right) */
		for (int i=0; i<size; i++){
			
			from = (int)clearcutOutput[0][i];
			to = (int)clearcutOutput[1][i];
			dist = clearcutOutput[2][i];
			  
			while (cv.number != from){
				cv = cv.parent;
			} 
			  
			t = new GCVertex(to);
			t.parent = cv;
			t.neighbours.add(cv);
			t.neighboursDist.add(dist);
			cv.neighbours.add(t);
			cv.neighboursDist.add(dist);
			if (to >= 0){
				t.isLeaf = true;//leafs have positive number
			} else {
				t.isLeaf = false;//internal nodes have negative number
			}  
			cv = t;
		}
		  
		return root;
	}
	  
	
	/**
	 * One vertex of a graph.
	 * */
	private class GCVertex implements NJVertex {
		  
		public GCVertex parent;
		  
		public final int number;
		public boolean isLeaf = false;
		public float tempDistance = 0.0f;
		  
		public List<GCVertex> neighbours;
		public List<Float> neighboursDist;
		  
		private GCVertex(int number){
			this.number = number;
			neighbours = new ArrayList<GCVertex>(3);
			neighboursDist = new ArrayList<Float>(3);
		}

		@Override
		public List<NJVertex> getChildren() {
			List<NJVertex> children = new ArrayList<NJVertex>(neighbours.size());
			if (isLeaf){
				return children;
			}
			for (GCVertex v : neighbours){
				if ((this.parent == null) || (v.isLeaf)){
					children.add(v);
				} else {
					if (v.number != this.parent.getName()){
						children.add(v);
					}
				}
			}
			return children;
		}

		@Override
		public int getName() {
			return number;
		}

		@Override
		public boolean isLeaf() {
			return isLeaf;
		}
	}
	  
	
	/**
	 * Set the distance of two leafs in a NJ tree.
	 * */  
	private void setDistance(GCVertex leaf, GCVertex node, GCVertex pnode, float matrix[][]){
		  
		GCVertex tempNode;
		  
		if (leaf == node){
			for (int i=0; i<node.neighbours.size(); i++){
				tempNode = node.neighbours.get(i);
				tempNode.tempDistance = node.neighboursDist.get(i);
				setDistance(leaf, tempNode, node, matrix);
			}
			return;
		}
		  
		if (node.isLeaf){
			  
			matrix[Math.max(node.number,leaf.number)][Math.min(node.number,leaf.number)] = node.tempDistance;
			node.tempDistance = 0.0f;
			return;
		}
		  
		for (int i=0; i<node.neighbours.size(); i++){
			tempNode = node.neighbours.get(i);
			if (tempNode == pnode){
				continue;
			}
			tempNode.tempDistance = node.tempDistance + node.neighboursDist.get(i);
			setDistance(leaf,tempNode, node, matrix);
		}  
	}

	
	/**
	 * Print a graph.
	 * */
	public static void print(GCVertex vertex, GCVertex parent){
		  
		if (vertex.isLeaf){
			System.out.println(vertex.number);
		} else {
			GCVertex child;
			System.out.println(vertex.number);
			for (int i=0; i<vertex.neighbours.size(); i++){
				child = (GCVertex)vertex.neighbours.get(i);
				if (child != parent){
					print(child,vertex);
				}  
			}
		}
	}
	
	
	/**
	 * Print a matrix.
	 * */
	public static void printMatrix(float[][] matrix){
		for (int i=0; i<matrix.length; i++){
			for (int j=0; j<matrix[i].length; j++){
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}	
	
	
	/**
	 * Print edges returned by "Clearcut".
	 * */
	public static void printOutputEdges(float output_matrix[][]){
		if ((output_matrix != null)&&(output_matrix[0]!=null)&&(output_matrix[1]!=null)
				&&(output_matrix[2]!=null)&&(output_matrix[3]!=null)){
			
			int edges_count = (int)output_matrix[3][0];
			
			System.out.println("edges count: " + edges_count);
			
			for (int i=0; i< edges_count; i++){
				System.out.println("edge: (" + output_matrix[0][i] + ", " + output_matrix[1][i] + "):" + output_matrix[2][i]);
			}
		}
	}
	
}



