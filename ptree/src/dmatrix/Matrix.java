package dmatrix;

//import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptree.MutationManager;
import nj.NJAlgorithm;
import nj.NJVertex;
import ptree.DNAPreprocessor;
import ptree.Sampling;
import ptree.Tree;
import ptree.Vertex;
import tests.Stat;

import clearcut.Clearcut;

import common.Configuration;
import common.TimeStamp;


/**
 * A matrix with values of type short.
 * */
public class Matrix {

	private Log log; 
	
	protected Configuration config;
	
	/* symmetric matrix */
	private short matrix[][];
	
	/* vertices that corresponds to the rows and columns of the matrix */
	protected List<Vertex> vertices;
	
	/* map: vertex id -> position in the matrix */
	protected HashMap<Integer,Short> map; 
	
	/* current size of the matrix */
	protected short size;
	
	protected MutationManager mutationManager;

	private final DNAPreprocessor dPrep;

	/** 
	 * Initialize the matrix 
	 * */
	public Matrix(List<Vertex> vertices, DNAPreprocessor dPrep, Configuration config){
		
		log = LogFactory.getLog(Matrix.class);
		this.config = config;
		this.dPrep = dPrep;
		mutationManager = new MutationManager(config, false);
		
		this.vertices = vertices;
		this.size = (short)vertices.size();
		this.map = new HashMap<Integer,Short>(2*size);
		this.matrix = new short[2*size][];
		
		for (int i=0; i<(2*size); i++){
			matrix[i] = null;
		}
		
		for (int i=1; i<size; i++){
			matrix[i] = new short[i];
		}
		
		for (short i=0; i<vertices.size(); i++){
			map.put(vertices.get(i).getIntId(), i);
		}	
	}
	
	
	/**
	 * Creates a copy of the matrix but the matrix values will be copied from the newMatrix.
	 * */
	public Matrix(Matrix matrix, short newMatrix[][]){
		
		log = LogFactory.getLog(Matrix.class);
		this.config = matrix.config;
		this.dPrep = matrix.dPrep;
		this.mutationManager = matrix.mutationManager;
		
		this.vertices = matrix.vertices;
		this.size = matrix.size;
		this.map = matrix.map;
		this.matrix = newMatrix;
	}
	
	
	/** 
	 * Compute the distance matrix. 
	 * 
	 * @deprecated OLD IMPLEMENTATION
	 * */	
	public void computeDistanceMatrix(){
		TimeStamp ts = TimeStamp.getTimeStamp();
		//long time = System.currentTimeMillis();
		 
		for (int i=1; i<size;i++){
			for (int j=0; j<i; j++){
				//matrix[i][j] = mm.getMutationsCount(vertices.get(Math.min(i, j)), vertices.get(Math.max(i, j)));
				matrix[i][j] = mutationManager.getMutationsCount(
						vertices.get(Math.min(i, j)).getDna(), 
						vertices.get(Math.max(i, j)).getDna());
			}
		} 
		//System.out.println("compute distance matrix in Java:" + (System.currentTimeMillis() - time));
		Stat.computeDMFinished(ts);
	}
	
	
	/** 
	 * Compute the distance matrix. 
	 * 
	 * @param currentSampling the current sampling or null
	 * @param tree1 a tree or null
	 * @param tree2 a tree or null
	 * 
	 * */	
	public void computeDistanceMatrix(Sampling currentSampling, Tree tree1, Tree tree2){
		TimeStamp ts = TimeStamp.getTimeStamp();
		
		if ((currentSampling == null) && (tree1 == null) && (tree2 == null)){
		
			for (int i=1; i<size;i++){
				for (int j=0; j<i; j++){
					matrix[i][j] = mutationManager.getMutationsCount(
							vertices.get(Math.min(i, j)).getDna(), 
							vertices.get(Math.max(i, j)).getDna());
				}
			} 
		
		} else {
			
			Vertex v0;
			Vertex v1;
			byte[] dna0;
			byte[] dna1;
			Short distance;
			List<Integer> positions = null;
			if (currentSampling != null){
				positions = currentSampling.getSampling();
			}
			
			for (int i=1; i<size;i++){
				for (int j=0; j<i; j++){
					
					v0 = vertices.get(Math.min(i, j));
					v1 = vertices.get(Math.max(i, j));
					distance = null;
					
					if (tree1 != null){
						distance = tree1.getDistanceIgnoringSamplingTry(v0, v1);
					}
					
					if ((distance == null) && (tree2 != null)){
						distance = tree2.getDistanceIgnoringSamplingTry(v0, v1);
					}
					
					if ((distance != null) && (currentSampling != null)){
						
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
								distance--;
							}
						}
					}
					
					if (distance == null){
						distance = mutationManager.getMutationsCount(v0.getDna(), v1.getDna());
					}
					
					matrix[i][j] = distance;
				}
			} 

		}
		
		Stat.computeDMFinished(ts);
	}
	
	
	/**
	 * Update the distance matrix after some vertices were deleted.
	 * */
	public void updateDistanceMatrix(){
		TimeStamp ts = TimeStamp.getTimeStamp();
		if (vertices.size() == size){
			
			for (int i=0; i<size; i++){ //only a test !!!
				
				if (map.get(vertices.get(i).getIntId()) == null){
					log.error("no mapping for " + vertices.get(i).getName());
					System.exit(-3);
				}
			}
			return;
		}
		
		if (matrix.length < vertices.size()){
			
			short tempMatrix[][];
			tempMatrix = new short[2*vertices.size()][];
			for (int i=0; i<tempMatrix.length; i++){
				tempMatrix[i] = null;
			}
			
			for (int i=1; i<matrix.length; i++){
				if (matrix[i] == null){
					break;
				}
				tempMatrix[i] = matrix[i];
				matrix[i] = null;
			}
			
			matrix = tempMatrix;
		}
		
		for (int i=size; i< vertices.size(); i++){
			if (matrix[i] == null){
				matrix[i] = new short[i];
			}
		}
		
		
		for (short i=size; i<vertices.size(); i++){
		
			map.put(vertices.get(i).getIntId(), i);
				
			for (int j=0; j<i; j++){
				matrix[i][j] = mutationManager.getMutationsCount(
						vertices.get(Math.min(i, j)).getDna(), vertices.get(Math.max(i, j)).getDna());
			}
		}
		
		for (int i=0; i<vertices.size(); i++){ //only a test
			if (map.get(vertices.get(i).getIntId()) == null){
				log.error("NO MAPPING for: " + vertices.get(i).getName());
				System.exit(-1);
			}
		}//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
		size = (short)vertices.size();
		Stat.updateDMFinished(ts);
	}
	
	
	/**
	 * Restore the distance matrix. (After some intermediate vertices were removed)
	 * 
	 * @deprecated OLD IMPLEMENTATION
	 * */
	public void restoreDistanceMatrixOld(){
		TimeStamp ts = TimeStamp.getTimeStamp();
		short i;
		Short value;
		for (i=0; i<vertices.size(); i++){	
			
			value = map.get(vertices.get(i).getIntId());
			
			if (value == null || !value.equals(i)){
				break;
			}
		}
		
		for (; i<vertices.size(); i++){
			for (int j=0; j<i; j++){
				matrix[i][j] = mutationManager.getMutationsCount(
						vertices.get(Math.min(i, j)).getDna(), vertices.get(Math.max(i, j)).getDna());
			}
			map.remove(vertices.get(i).getIntId());
			map.put(vertices.get(i).getIntId(), i);
		}
		
		size = (short)vertices.size();
		Stat.restoreDMFinished(ts);
	}
	
	
	/**
	 * Restore the distance matrix after some vertices were deleted.
	 * */
	public void restoreDistanceMatrix(){
		
		TimeStamp ts = TimeStamp.getTimeStamp();
		short offset;
		Short temp;
		
		/* find the first index "i" of a row that corresponds to a vertex that was deleted */
		for (offset=0; offset<vertices.size(); offset++){	
			temp = map.get(vertices.get(offset).getIntId());
			if (!temp.equals(offset)){
				break;
			}
		}
		
		/* shift array provides mapping between the old and new indices of vertices in the distance matrix*/
		short listSize = (short)vertices.size();
		short shift[] = new short[listSize - offset];
		for (short k=offset; k<listSize; k++){
			shift[k-offset] = k;
		}
		
		/* the difference between indices in the new and old matrix */
		short difference = 0; 
		
		/* reconstruct the matrix - rows that correspond to the vertices that were deleted are removed */
		for (short i=offset; i<vertices.size(); i++){
			
			int verticesIntId = vertices.get(i).getIntId();
			short oldIndex = map.get(verticesIntId);
			short increment;
			
			/* update a row such that it correspond to its new position (row index)*/
			for (short k=offset; k<i; k++){
				matrix[oldIndex][k] = matrix[oldIndex][shift[k-offset]];
			}
			
			/* update the shift array if a row that correspond to a vertex was deleted*/
			if ((oldIndex - i) > difference){
				increment = (short)((oldIndex - i) - difference);
				difference = (short)(oldIndex - i);
				
				for (short k=i; k<listSize; k++){
					shift[k-offset]+= increment;
				}
			}

			matrix[i] = matrix[oldIndex];
			matrix[oldIndex] = null;
		
			map.remove(verticesIntId);
			map.put(verticesIntId, i);
		}
		
		size = (short)vertices.size();
		
		for (int i=0; i<vertices.size(); i++){ //only a test
			if (map.get(vertices.get(i).getIntId()) == null){
				log.error("NO MAPPING for: " + vertices.get(i).getName());
				System.exit(-1);
			}
		}//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		Stat.restoreDMFinished(ts);
	}


	/*
	 * Get the distance between two vertices from the matrix.
	 * */
	/*public short getDistance(Vertex v0, Vertex v1){ 
		
		Short i = map.get(v0.getIntId());
		if (i == null){
			log.error("Ask for wrong value 1 " + v0.getName());
			System.exit(-1);
			return Short.MAX_VALUE;
		}
		short j = i;
		i = map.get(v1.getIntId());
		if (i == null){
			log.error("Ask for wrong value 2 " + v1.getName());
			System.exit(-2);
			return Short.MAX_VALUE;
		}
		if (i.equals(j)){
			return 0;
		}
		
		return matrix[Math.max(i, j)][Math.min(i, j)];
	}*/
	
	
	public short getDistance(Vertex v0, int indexV1){ 
		
		Short indexV0 = map.get(v0.getIntId());
		if (indexV0 == null){
			log.error("Ask for wrong value 1 " + v0.getName());
			System.exit(-1);
			return Short.MAX_VALUE;
		}
		 
		if (indexV0.equals(indexV1)){
			return 0;
		}
		
		return matrix[Math.max(indexV0, indexV1)][Math.min(indexV0, indexV1)];
	}
	
	
	public void decreaseDistance(Vertex v0, int indexV1, int offsetDec){ 
		
		Short indexV0 = map.get(v0.getIntId());
		if (indexV0 == null){
			log.error("Ask for wrong value 1 " + v0.getName());
			System.exit(-1);
		}
		 
		if (indexV0.equals(indexV1)){
			log.error("Wants to adjust the distance with itself " + v0.getName());
			System.exit(-1);
			return;
		}
		
		matrix[Math.max(indexV0, indexV1)][Math.min(indexV0, indexV1)] -= offsetDec;
	}
	
	
	public void increaseDistance(Vertex v0, int indexV1, int offsetInc){ 
		decreaseDistance(v0, indexV1, (-1)*offsetInc);
	}

	
	/**
	 * Get the distance between two vertices from the matrix where
	 * the order of the vertices in the array correspond to its indices in the array.
	 * */
	public short getDistance(int indexV0, int indexV1){
		
		if (indexV0 == indexV1){
			return 0;
		}
		
		return matrix[Math.max(indexV0, indexV1)][Math.min(indexV0, indexV1)];
	}
	
	
	/**
	 * Try to get the distance between two vertices, returns null if unsuccessful.
	 * 
	 * @return the distance or null
	 * */
	public Short getDistanceTry(Vertex v0, Vertex v1){ 
		
		Short i = map.get(v0.getIntId());
		if (i == null){
			return null;
		}
		short j = i;
		i = map.get(v1.getIntId());
		if (i == null){
			return null;
		}
		if (i.equals(j)){
			return 0;
		}
		
		return matrix[Math.max(i, j)][Math.min(i, j)];
	}


	/**
	 * Compute the NJ matrix from this matrix.
	 * The NJ algorithm can be either Java implementation or it can use an external "Clearcut" library,
	 * depending on the configuration.
	 * */
	public DMatrix getNJMatrix(Configuration config){
		
		TimeStamp ts = TimeStamp.getTimeStamp();
		
		if (config.getNjImplementation() == 0){
			
			/* NJ Java implementation */
		
			float[][] copy = new float[size][];
			
			for (int i=0; i<size; i++){
				copy[i] = new float[i];
				for (int j=0;j<i; j++){
					//copy[i][j] = matrix[i][j];
					copy[i][j] = getCorrectionDistance(config.getNjCorrection(), i, j);
				}
			}
			
			float[][] njMatrix = new NJAlgorithm().computeNJMatrix(copy);
			Stat.computeNJFinished(ts);
			
			return new DMatrix(this,njMatrix);
		} else {
		
			/* "Clearcut implementation" */
			String file;
			
			//if (config.getComputeDmInJava() && (config.getNjCorrection() == 0)){
				
				/* suppose that the DM has been computed in Java */
				StringBuffer buf = new StringBuffer(vertices.size()*vertices.size()*3);
				buf.append(this.size + " \n");
				for (int i=0; i<vertices.size(); i++){
					buf.append(" s" + map.get(vertices.get(i).getIntId()));
					for (int j=0; j<i; j++){
						//buf.append(" " + this.getDistance(i, j));
						buf.append(" " + getCorrectionDistance(config.getNjCorrection(), i, j));
					}
					buf.append(" \n");
				}
				file = buf.toString();
				//System.out.println(file);
			/*} else {
				// the DM will be computed using Clearcut  
				
				// get a string in fasta format  
				int initBufSize = vertices.size();
				if (!vertices.isEmpty()){
					initBufSize *= (vertices.get(0).getDna().getBytes().length + 10);
				}
				StringBuffer buf = new StringBuffer(initBufSize);
				
				for (Vertex vertex : vertices){
					buf.append("> s" + map.get(vertex.getIntId()) + "\n");
					buf.append(new String(vertex.getDna().getBytes(), Charset.forName("UTF-8")));	
					buf.append("\n");
				}
				file = buf.toString();
			}*/
			
			float[][] clearcutMatrix = Clearcut.computeNJMatrix(file, config, this.size);
			//System.out.println("nj matrix computed");
			Stat.computeNJFinished(ts);
			return new DMatrix(this,clearcutMatrix);
		}
	}
	
	
	/**
	 * Compute the NJ matrix from this matrix.
	 * The NJ algorithm can be either Java implementation or it can use an external "Clearcut" library,
	 * depending on the configuration.
	 * */
	public NJVertex getNJTree(Configuration config){
		
		TimeStamp ts = TimeStamp.getTimeStamp();
		
		if (config.getNjImplementation() == 0){
			
			/* NJ Java implementation */
		
			float[][] copy = new float[size][];
			
			for (int i=0; i<size; i++){
				copy[i] = new float[i];
				for (int j=0;j<i; j++){
					//copy[i][j] = matrix[i][j]; // !!!
					copy[i][j] = getCorrectionDistance(config.getNjCorrection(), i, j);
				}
			}
			
			NJVertex root = new NJAlgorithm().computeNJTree(copy);
			Stat.computeNJFinished(ts);
			
			return root;
		} else {
		
			/* "Clearcut implementation" */
			String file;
			
			//if (config.getComputeDmInJava() && (config.getNjCorrection() == 0)){
				
				/* suppose that the DM has been computed in Java */
				StringBuffer buf = new StringBuffer(vertices.size()*vertices.size()*3);
				buf.append(this.size + " \n");
				for (int i=0; i<vertices.size(); i++){
					buf.append(" s" + map.get(vertices.get(i).getIntId()));
					for (int j=0; j<i; j++){
						//buf.append(" " + this.getDistance(i, j));
						buf.append(" " + getCorrectionDistance(config.getNjCorrection(), i, j));
					}
					buf.append(" \n");
				}
				file = buf.toString();
				 
				
				
			//} 
				/*else {
				// the DM will be computed using Clearcut   //never do this!!!
				
				// get a string in fasta format  
				int initBufSize = vertices.size();
				if (!vertices.isEmpty()){
					initBufSize *= (vertices.get(0).getDna().getBytes().length + 10);
				}
				StringBuffer buf = new StringBuffer(initBufSize);
				
				for (Vertex vertex : vertices){
					buf.append("> s" + map.get(vertex.getIntId()) + "\n");
					buf.append(new String(vertex.getDna().getBytes(), Charset.forName("UTF-8")));	
					buf.append("\n");
				}
				file = buf.toString();
			}*/
			
			NJVertex root = Clearcut.computeNJTree(file, config);
			Stat.computeNJFinished(ts);
			//return new DMatrix(this,clearcutMatrix);
			 
			return root;
		}
	}
	
	
	private float getJCDistance(int i, int j){//compute distance matrix prior to call this methode required
		
		if (i == j){
			return 0.0f;
		}
		
		double d = ((double)this.getDistance(i,j))/((double)dPrep.getDnaUncompressedLength());
		
		if ((d > 0.75) || (Math.abs(d - 0.75) < 0.00001)){
			//System.out.println("cut off");
			return 10.0f;
		}
		
		double dist = ((-0.75)*Math.log(1.0 - ((4.0/3.0)*d)));
		
		if (Math.abs(dist) < 0.00001){
			//System.out.println("zero");
			dist = 0.0f;
		}
		
		return (float)(dist);
	}
	
	
	private float getKimuraDistance(int i, int j){//compute distance matrix prior to call this method NOT required
		
		if (i == j){
			return 0.0f;
		}
		
		byte b0[] = vertices.get(i).getDna().getBytes();
		byte b1[] = vertices.get(j).getDna().getBytes();
		
		double transitions = 0.0;
		double transversions = 0.0;
		double seqLen = dPrep.getDnaUncompressedLength();
		for (int k=0; k<b0.length; k++){
			if (b0[k] != b1[k]){
				if ((((b0[k] == config.getA()) && (b1[k] == config.getG()))
				   ||((b0[k] == config.getG()) && (b1[k] == config.getA())))
				   ||
				   (((b0[k] == config.getC()) && (b1[k] == config.getT())||
				     (b0[k] == config.getT()) && (b1[k] == config.getC())))
				    ){
						transitions++;
					} else {
						
						if (((b0[k] == config.getGapChar()) || b1[k] == config.getGapChar()) && (!config.getCountGapAsChange())){
							seqLen--;
						} else {
							transversions++;
						}
					}
			}
		} 
		
		double p;
		double q;
		
		if (seqLen > 0.9){
			p = transitions/seqLen;
			q = transversions/seqLen;
		} else {
			p = 0.0;
			q = 0.0;
		}

		boolean blowup = false;
		double logX = 0.0;
		double logY = 0.0;
		
		if (Math.abs(2.0*p + q - 1.0) < 0.00001){
			blowup = true;
		} else {
			if ((1.0 - 2.0*p - q) < 0.00001){
				blowup = true;
			} else {
				logX = Math.log(1.0 - 2.0*p - q);
			}
		}
		
		if (2.0*q > 0.99999){
			blowup = true;
		} else {
			logY = Math.log(1.0 - 2.0*q);
		}
		
		double dist;
		
		if (blowup){
			dist = 10.0;
		} else {
			dist = (-0.5)*logX - 0.25*logY;
		}
		
		if (Math.abs(dist) < 0.00001){
			dist = 0.0;
		}
		
		return (float)dist;
	}
	
	
	private float getCorrectionDistance(byte correction, int i, int j){
		
		switch (correction){
		case Configuration.NO_CORRECTION: 
			return this.getDistance(i,j);
		case Configuration.JC_CORRECTION: 
			return this.getJCDistance(i,j);
		case Configuration.KIMURA_CORRECTION: 
			return this.getKimuraDistance(i,j);
		default: 
			log.error("Wrong correction index.");
			return this.getDistance(i,j);
		}
	}
	
	
	public short getSize(){
		return size;
	}
	
	
	/**
	 * Test whether the matrices are equal.
	 * */
	public static boolean equal(Matrix m1, Matrix m2){
		
		if (m1.size != m2.size){
			System.err.println("different matrix size..");
			return false;
		}
		
		int size = m1.size;
		//Vertex v1 = null;
		//Vertex v2 = null;
		int count = 0;
		
		for (int i=0; i<size; i++){
			for (int j=0; j<size; j++){
				//v1 = m1.vertices.get(i);
				//v2 = m1.vertices.get(j);
				if (m1.getDistance(i, j) != m2.getDistance(i, j)){
					System.err.println("Different values for (" + i + ", " + j + ") after: " + count + " correct.\n"
							+ " values: " + m1.getDistance(i, j) + ", " + m2.getDistance(i, j));
					return false;
				} else {
					count++;
				}
			}
		}
		return true;
	}	
	
	
	public void printMatrix(){
		System.out.println("Matrix values:");
		for (int i=0; i<this.size; i++){
			System.out.print(i + ": ");
			for (int j=0; j<i; j++){
				System.out.print(this.getDistance(i, j) + " ");	
			}
			System.out.println();
		}
	}
	
	
	/**
	 * Deletes all elements of the matrix.
	 * */
	public void clear(){
		
		try {
			map.clear();
			for (int i=0; i<matrix.length; i++){
				matrix[i] = null;
			}
			
			size = 0;
		} catch (Exception ex){
			log.error("An exception while clearing matrix",ex);
		}
	}
	
}
