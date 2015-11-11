package ptree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fitchcost.SankoffAlgVertex;


/**
 * Vertex of a tree.
 * */
public class Vertex implements SankoffAlgVertex {

	private Log log; 
	
	/* used to determine unique id */
	private static int idCount = 0;
	
	/* status of the vertex */
	protected static final byte STATUS_ZERO = 0;
	protected static final byte STATUS_ONE = 1;
	protected static final byte STATUS_TWO = 2;
	
	/* the name of a vertex corresponds to the name of a DNA seq. in an input file 
	 * or it is generated and named as "i" + number */
	private final String name;
	
	/* unique id of a vertex */
	private final int intId;
	
	/* the DNA that currently corresponds to this vertex, it can be also masked */
	private Dna dna;
	
	/* if initialized: here is stored the original DNA (without subsampling) */
	private Dna secDna = null;
	
	/* if sampling date is used: time in milliseconds (equal to -1 if not set) */
	private long time;
	
	/* the status of this vertex can be: STATUS_ZERO, STATUS_ONE, STATUS_TWO */
	private byte status;
	
	/* incoming edge */
	private Vertex incomingE = null;
	
	/* outcoming edges */
	private List<Vertex> outcomingE;
	
	/* mutations on the incoming edge */
	private Mutations mutations = null;
	
	/* is this vertex original or it was inferred (intermediate) */
	private final boolean isOriginal;

	/* degree of a vertex in a graph */
	private int degree = 0;
	
	
	/**
	 * Constructor for original vertices.
	 * 
	 * @param name name of the vertex
	 * @param dna DNA read from the input file
	 * */
	public Vertex(String name, String dna){
		
		log = LogFactory.getLog(Vertex.class);
		
		this.name = name;
		this.intId = generateUniqueId();
		this.time = -1;
		this.isOriginal = true;
		this.status = STATUS_ONE;
		
		outcomingE = new ArrayList<Vertex>();
		
		try {
			this.dna = new Dna(dna.getBytes("UTF-8"));	
			
		} catch (UnsupportedEncodingException ex){
			this.dna = new Dna(new byte[0]);
			log.error("Exception: ",ex);
		}
	}
	
	
	/**
	 * Constructor for inferred vertices.
	 * Generates the name.
	 * 
	 * @param timeStamp sampling date of a vertex
	 * @param dna inferred DNA
	 * */
	public Vertex(long timeStamp, Dna dna){
		
		log = LogFactory.getLog(Vertex.class);
		
		this.dna = dna;
		this.time = timeStamp;
		this.intId = generateUniqueId();
		this.name = "i" + this.intId;
		this.isOriginal = false;
		outcomingE = new ArrayList<Vertex>();
	}
	
	
	/**
	 * Copy constructor.
	 * 
	 * Generates unique id, doesn`t copy mutations or edges.
	 * */
	protected Vertex(Vertex v){
		log = LogFactory.getLog(Vertex.class);
		this.name = v.name;
		this.time = v.time;
		this.dna = new Dna(v.dna);
		this.isOriginal = v.isOriginal;
		this.intId = generateUniqueId();
		this.outcomingE = new ArrayList<Vertex>();
		this.mutations = new Mutations();
	}
	
	
	/**
	 * Construct the copy of the vertex except for the dna
	 * 
	 * Generates unique id, doesn`t copy mutations or edges.
	 * */
	protected Vertex(Vertex v, byte[] bytes, boolean isOriginal){
		log = LogFactory.getLog(Vertex.class);
		this.name = v.name;
		this.time = v.time;
		this.dna = new Dna(bytes);
		this.isOriginal = isOriginal;
		this.intId = generateUniqueId();
		this.outcomingE = new ArrayList<Vertex>();
		this.mutations = new Mutations();
	}
	
	
	/**
	 * Get the secondary DNA of a vertex (original DNA).
	 * */
	public Dna getSecDna(){
		return secDna;
	}
	
	
	/**
	 * Set the secondary DNA of a vertex (original DNA).
	 * */
	protected void setSecDna(Dna secDna){
		this.secDna = secDna;
	}
	
	
	/**
	 * Set the primary DNA, can be original or subsampled.
	 * */
	protected void setDna(Dna dna){
		this.dna = dna;
	}
	
	
	/**
	 * Remove all edges that are connected with this vertex.
	 * (Removes incoming edge, mutations, outcoming edges, set degree to 0)
	 * */
	protected void deforest(){
		incomingE = null;
		outcomingE.clear();
		mutations = null;
		degree = 0;
	}
	
	
	/**
	 * Generate a new vertex id for a new vertex, 
	 * whenever a new instance of a vertex is created, the counter is increased.
	 * If the max integer is reached, sets the counter to zero. 
	 * */
	private static synchronized int generateUniqueId(){
		if (idCount == Integer.MAX_VALUE){
			idCount = 0;
		}
		return idCount++;
	}
	
	
	/**
	 * Gets the name of a vertex for the output (vertex name and the list of mutations).
	 * */
	public String getGraphVertexName(){
		StringBuffer buf = new StringBuffer();
		buf.append(this.getName() + "[&Mutations=\"");
		
		/* insert list of mutations */
		if (mutations != null){
			buf.append(mutations.toString());
		}
		
		buf.append("\"]");
		return buf.toString();
	}
	
	
	/**
	 * @return number of point mutations
	 * */
	public int getGraphMutationCount(){
		if (mutations == null){
			return 0;
		} else {
			return mutations.getMutationCount();
		}
	}
	
	
	public Mutations getMutations(){
		return this.mutations;
	}
	
	
	/**
	 * Is this vertex a leaf?
	 * (i.e. it has no outcoming edge)
	 * */
	public boolean isLeaf(){
		return (outcomingE.isEmpty())?true:false;
	}
	
	
	/**
	 * Is this vertex original or inferred.
	 * @return true ~ original, false ~ inferred
	 * */
	protected boolean isOriginal(){
		return this.isOriginal;
	}
	
	
	public List<Vertex> getOutcomingEdges(){
		return outcomingE;
	}
		
	
	/**
	 * Set the sampling date of a sequence. 
	 * */
	public void setTimeInMillis(long time){
		if (this.time > 0){
			log.error("Second call of the function.");
		}
		this.time = time;
	}
	
	
	/**
	 * Set the parent of this vertex.
	 * */
	public void setIncomingEdge(Vertex incomingE){
		if (this.incomingE != null){
			log.error("Not allowed call!");
		}
		this.incomingE = incomingE;
		degree++;
	}
	
	
	/**
	 * @return incoming edge or null
	 * */
	public Vertex getIncomingEdge(){
		return incomingE;
	}
	
	
	/**
	 * Set a child of this vertex.
	 * */
	public void setOutcomingEdge(Vertex outcomingE){
		this.outcomingE.add(outcomingE);
		degree++;
	}
	
	
	/**
	 * Delete a child of this vertex.
	 * */
	public void removeOutcomingEdge(Vertex outcomingE){
		if (this.outcomingE.remove(outcomingE)){
			degree--;
		} else {
			log.error("Try to remove an element that wasn`t there!!!");
			System.exit(-1);
		}
	}
	
	
	public void removeIncomingEdge(){
		if (this.incomingE != null){
			this.incomingE = null;
			degree--;
		}
	}
	 
	
	/**
	 * Decrement the degree of a vertex. 
	 * */
	protected void decDegree(){
		degree--;
	}
	
	
	/**
	 * Set mutations on the incoming edge.
	 * */
	public void setMutations(Mutations mutations){
		this.mutations = mutations;
	}


	/**
	 * Gets the primary DNA of a vertex. 
	 * */
	public Dna getDna(){
		return dna;
	}

	
	/**
	 * Gets the unique id of this vertex.
	 * */
	public int getIntId(){
		return intId;
	}
	
	
	/**
	 * The name of a vertex corresponds to the name of a DNA seq. in an input file 
	 * or it is named as "i" + vertex number (if inferred). 
	 * */
	public String getName(){
		return name;
	}
	

	/**
	 * Gets the sampling date of a vertex.
	 * */
	public long getTime(){
		if (time < 0){
			log.warn("Wants to know the sampling date that hasn`t been set.");
		}
		return time;
	}

	
	/**
	 * Gets the degree of a vertex in a tree.
	 * */
	protected int getDegree(){
		return this.degree;
	}
	
	
	/**
	 * Return a status of the vertex: ({@link #STATUS_ZERO STATUS_ZERO}, 
	 * {@link #STATUS_ONE STATUS_TWO} or {@link #STATUS_ONE STATUS_TWO}).
	 * */
	protected byte getStatus(){
		return this.status;
	}

	
	/**
	 * Set a status of a vertex: ({@link #STATUS_ZERO STATUS_ZERO}, 
	 * {@link #STATUS_ONE STATUS_TWO} or {@link #STATUS_ONE STATUS_TWO}).
	 * */
	protected void setStatus(byte status){
		this.status = status;
	}
	
	
	/**
	 * Gets the time comparator to compare vertices in terms of the sampling date.
	 * */
	public static Comparator<Vertex> getTimeComparator(){
		return new Comparator<Vertex>(){
			
			@Override
			public int compare(Vertex v0, Vertex v1) {
				
				if (v0.time < v1.time){
					return -1;
				}
				if (v0.time > v1.time){
					return 1;
				}
				return 0;
			}
			
		};
	}

	
	/**
	 * Gets the name comparator to compare vertices in terms of its name.
	 * 
	 * */
	public static Comparator<Vertex> getNameComparator(){
		return new Comparator<Vertex>(){
			
			@Override
			public int compare(Vertex v0, Vertex v1) {	
				return v0.getName().compareTo(v1.getName());
			}
		};
	}
	
	
	@Override
	public String toString(){
		StringBuffer str = new StringBuffer(name + " ");
		
		for (int i=0; i< this.dna.getBytes().length; i++){
			switch (dna.getBytes()[i]) {
			case 65: str.append("A"); break;
			case 84: str.append("T"); break;
			case 71: str.append("G"); break;
			case 67: str.append("C"); break;
			default: str.append("X");
			} 
		}
		return str.toString();
	}
	
	
	protected static void print(List<Vertex> list){
		for (int i=0; i<list.size(); i++){
			System.out.println("name: " + list.get(i).name + " time: " + list.get(i).time);
		}
	}
	
	@Override 
	public int hashCode(){
		return intId;
	}
	
	@Override
	public boolean equals(Object o){
		return (this == o)?true:false;
	}


	@Override
	public List<SankoffAlgVertex> getChildren() {
		List<SankoffAlgVertex> list = new ArrayList<SankoffAlgVertex>(outcomingE.size());
		for (Vertex v : outcomingE){
			list.add(v);
		}
		return list;
	}


	@Override
	public byte[] getDnaAsBytes() {
		return this.dna.getBytes();
	}


	@Override
	public void setDna(byte[] dnaAsBytes) {
		this.dna = new Dna(dnaAsBytes);
	}
	
}
