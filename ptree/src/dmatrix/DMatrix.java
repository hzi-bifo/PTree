package dmatrix;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptree.Vertex;


/**
 * A matrix with values of type float.
 * */
public class DMatrix {

	private Log log; 
	
	/* vertices that corresponds to the rows and columns of the matrix */
	//private List<Vertex> vertices;
	
	/* map: vertex id -> position in the matrix */
	private HashMap<Integer,Short> map; 
	
	/* float matrix */
	private float matrix[][];
	
	/* current size of the matrix */
	private short size;
	
	
	@SuppressWarnings("unused")
	private DMatrix(){
	}
	
	
	/**
	 * Creates a Float matrix.
	 * 
	 * @param smatrix matrix from which are taken vertices` positions (hashmap)
	 * @param fmatrix matrix from which are taken values
	 * */
	public DMatrix(Matrix smatrix, float fmatrix[][]){
		
		log = LogFactory.getLog(DMatrix.class);
		
		//this.vertices = smatrix.vertices;
		this.map = smatrix.map;
		this.size = smatrix.size;
		
		this.matrix = fmatrix;
	}
	
	
	/**
	 * Get the distance between two vertices from the matrix.
	 * */
	public float getDistance(Vertex v0, Vertex v1){
		
		Short i = map.get(v0.getIntId());
		if (i == null){
			log.error("Ask for wrong value");
			System.exit(-1);
			return Float.MAX_VALUE;
		}
		short j = i;
		i = map.get(v1.getIntId());
		if (i == null){
			log.error("Ask for wrong value");
			System.exit(-2);
			return Float.MAX_VALUE;
		}
		if (i.equals(j)){
			return 0;
		}
		
		return matrix[Math.max(i, j)][Math.min(i, j)];
	}
	
	
	public float getDistance(int indexV0, int indexV1){
		
		if (indexV0 == indexV1){
			return 0.0f;
		}
		
		return matrix[Math.max(indexV0, indexV1)][Math.min(indexV0, indexV1)];
	}
	
	
	/**
	 * Get the current size of the matrix.
	 * */
	public short getSize(){
		return size;
	}
	
}
