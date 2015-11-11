package fitchcost;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The Fitch algorithm.
 * */
public class FitchAlg {

	public static int idCount = 0;
	
	private Log log;
	private final byte A,C,G,T;
	
	public FitchAlg(byte A, byte C, byte G, byte T){
		log = LogFactory.getLog(FitchAlg.class);
		this.A = A;
		this.C = C;
		this.G = G;
		this.T = T;
	}
	
	
	/**
	 * Run the fitch algorithm
	 * 
	 * @param noSameBaseInAllSpecies 
	 * @param sequences list of sequences
	 * @param label list of sequence labels
	 * */
	public int runFitchAlgorithm(List<String> labels, List<byte[]> sequences, boolean noSameBaseInAllSpecies){
		
		if (sequences.isEmpty() || (labels.size() != sequences.size())){
			log.error("Wrong arguments.");
			return -1;
		}
		
		List<Integer> relevantBases = new ArrayList<Integer>();

		List<FVertex> vertices = new ArrayList<FVertex>();
		
		/* it is possible that a base in all sequences is the same */
		if (!noSameBaseInAllSpecies){

			int seqLength = sequences.get(0).length;
			byte base;
			boolean sameBase;
			
			/* for all columns */
			for (int i=0; i<seqLength; i++){
				
				base = sequences.get(0)[i];
				sameBase = true;
				
				/* for all rows in a column */
				for (int j=1; j<sequences.size(); j++){
					
					if (sequences.get(j)[i] != base){
						sameBase = false;
						break;
					}
				}
				/* at least two sequences have this position different */
				if (!sameBase){
					relevantBases.add(i);
				}
			}
			
			byte[] newSeq;
			byte[] oldSeq;
			
			int relSeqLength = relevantBases.size();
			
			/* create vertices */
			for (int i=0; i<sequences.size(); i++){
				
				newSeq = new byte[relSeqLength];
				oldSeq = sequences.get(i);
				
				for (int j=0; j<relSeqLength; j++){
					newSeq[j] = oldSeq[relevantBases.get(j)]; 
				}
				
				vertices.add(new FVertex(labels.get(i),newSeq));
			}
		} else {
			
			/* create vertices */
			for (int i=0; i<sequences.size(); i++){
				vertices.add(new FVertex(labels.get(i),sequences.get(i)));
			}
		}
		
		/* build a tree */
		FVertex root = buildTree(vertices);
		
		IntWrapper overallScore = new IntWrapper(0);
		
		/* compute scores */
		computeScores(root, overallScore);
		
		//log.info("FitchXX: " + computeScoreCount(root));
		
		/* label nodes */
		labelNodes(root);
		
		/* print the tree */
		//printTree(root, 0);
		
		return overallScore.value;
	}

	
	private void printN(int n){
		for (int i=0; i<n; i++){
			System.out.print(" ");
		}
	}
	
	
	public void printTree(FVertex vertex, int depth){
		if(vertex == null){
			return;
		}
		printTree(vertex.right, depth+2);
		printN(depth); System.out.print(vertex.label + "  ");
		for (int i=0; i<vertex.sequence.length; i++){
			if (vertex.sequence[i] == A){
				System.out.print("A"); continue;
			}
			if (vertex.sequence[i] == C){
				System.out.print("C"); continue;
			}
			if (vertex.sequence[i] == G){
				System.out.print("G"); continue;
			}
			if (vertex.sequence[i] == T){
				System.out.print("T"); continue;
			}
		}
		System.out.println("");
		printTree(vertex.left, depth+2);
	}
	
	
	public int computeScoreCount(FVertex vertex){
		 
		if (vertex.isOriginal()){
			return 0;
		} else {
			
			return computeScoreCount(vertex.left) + computeScoreCount(vertex.right) + vertex.score;
		}
	}
	
	
	private void labelNodes(FVertex vertex){
		
		if (vertex.isOriginal()){
			return;
		}
		
		/* root */
		if (vertex.parent == null){
			for (int i=0; i<vertex.seqLength(); i++){
				vertex.sequence[i] = (vertex.a[i])?A:((vertex.c[i])?C: ((vertex.g[i])?G:T));
				if (!(vertex.a[i] || vertex.c[i] || vertex.g[i] || vertex.t[i])){
					log.error("No label available");
				}
			}
		}
		
		/* label children */
		for (int i=0; i<vertex.seqLength(); i++){
			
			/* for left child */
			if (((vertex.sequence[i] == A)&&(vertex.left.a[i]))||
					((vertex.sequence[i] == C)&&(vertex.left.c[i]))||
					((vertex.sequence[i] == G)&&(vertex.left.g[i]))||
					((vertex.sequence[i] == T)&&(vertex.left.t[i]))){
				
				/* child can have the same label as its parent */
				vertex.left.sequence[i] = vertex.sequence[i];
			} else {
				/* child will have another label than its parent */
				vertex.left.sequence[i] = (vertex.left.a[i])?A:((vertex.left.c[i])?C:((vertex.left.g[i])?G:T));
				if (!(vertex.left.a[i] || vertex.left.c[i] || vertex.left.g[i] || vertex.left.t[i])){
					log.error("No label available");
				}
			}
			
			/* for right child */
			if (((vertex.sequence[i] == A)&&(vertex.right.a[i]))||
					((vertex.sequence[i] == C)&&(vertex.right.c[i]))||
					((vertex.sequence[i] == G)&&(vertex.right.g[i]))||
					((vertex.sequence[i] == T)&&(vertex.right.t[i]))){
				
				/* child can have the same label as its parent */
				vertex.right.sequence[i] = vertex.sequence[i];
			} else {
				/* child will have another label than its parent */
				vertex.right.sequence[i] = (vertex.right.a[i])?A:((vertex.right.c[i])?C:((vertex.right.g[i])?G:T));
				if (!(vertex.right.a[i] || vertex.right.c[i] || vertex.right.g[i] || vertex.right.t[i])){
					log.error("No label available");
				}
			}
		}
		
		/* label children of children */
		labelNodes(vertex.left);
		labelNodes(vertex.right);
	}
	
	
	private void computeScores(FVertex vertex, IntWrapper overallScore){
	
		if (vertex.isOriginal()){
			return;
		}
		
		computeScores(vertex.left,overallScore);
		computeScores(vertex.right,overallScore);
		
		for (int i=0; i<vertex.seqLength(); i++){
			
			/* is there intersection */
			if ((vertex.left.a[i] && vertex.right.a[i]) || (vertex.left.c[i] && vertex.right.c[i]) || 
					(vertex.left.g[i] && vertex.right.g[i]) || (vertex.left.t[i] && vertex.right.t[i])){
				
				/* intersect */
				vertex.a[i] = vertex.left.a[i] && vertex.right.a[i];
				vertex.c[i] = vertex.left.c[i] && vertex.right.c[i];
				vertex.g[i] = vertex.left.g[i] && vertex.right.g[i];
				vertex.t[i] = vertex.left.t[i] && vertex.right.t[i];
			} else {
				/* union */
				vertex.a[i] = vertex.left.a[i] || vertex.right.a[i];
				vertex.c[i] = vertex.left.c[i] || vertex.right.c[i];
				vertex.g[i] = vertex.left.g[i] || vertex.right.g[i];
				vertex.t[i] = vertex.left.t[i] || vertex.right.t[i];
				vertex.score++;
				overallScore.value++;
			}
		}	
	}
	
	
	
	/**
	 * @return root
	 * */
	private FVertex buildTree(List<FVertex> vertices){
		
		if (vertices.isEmpty()){
			return null;
		} 
		if (vertices.size() == 1){
			return vertices.get(0);
		}
		 
		List<FVertex> list1 = new ArrayList<FVertex>(vertices.size());
		List<FVertex> list2 = new ArrayList<FVertex>(vertices.size());
		int seqLength = vertices.get(0).sequence.length;
		FVertex vertexI;
		FVertex vertexR;
		FVertex vertexL;
		list1.addAll(vertices);
		int oddNumCount = 0;
		
		for (;;){
			
			/* even number of nodes */
			if ((list1.size() % 2) == 0){
				
				for (int i=0; i<list1.size()-1; i+=2){
					/* join two neighbors */
					vertexI = new FVertex(seqLength);
					vertexL = list1.get(i);
					vertexR = list1.get(i+1);
					vertexI.left = vertexL;
					vertexI.right = vertexR;
					vertexL.parent = vertexI;
					vertexR.parent = vertexI;
					list2.add(vertexI);
				}
				
			} else {
				
				/* we have odd number of nodes */
				oddNumCount++;	
				
				if ((oddNumCount % 2) == 1){
					
					/* last node will not be joined */
					
					for (int i=0; i<list1.size()-2; i+=2){
						/* join two neighbors */
						vertexI = new FVertex(seqLength);
						vertexL = list1.get(i);
						vertexR = list1.get(i+1);
						vertexI.left = vertexL;
						vertexI.right = vertexR;
						vertexL.parent = vertexI;
						vertexR.parent = vertexI;
						list2.add(vertexI);
					}
					
					list2.add(list1.get(list1.size()-1));
	
				} else {
					
					/* first node will not be joined */
					
					list2.add(list1.get(0));
					
					for (int i=1; i<list1.size()-1; i+=2){
						/* join two neighbors */
						vertexI = new FVertex(seqLength);
						vertexL = list1.get(i);
						vertexR = list1.get(i+1);
						vertexI.left = vertexL;
						vertexI.right = vertexR;
						vertexL.parent = vertexI;
						vertexR.parent = vertexI;
						list2.add(vertexI);
					}	
				}			
			}
			
			list1.clear();
			list1.addAll(list2);
			list2.clear();
			
			if (list1.size() == 1){
				break;
			}
			
		}
		vertexI = list1.get(0);
		list1.clear();
		
		return vertexI;
	}
	
	
	public class FVertex {
		
		public String label;
		public byte[] sequence;
		public boolean original;
		
		public FVertex left = null;
		public FVertex right = null;
		public FVertex parent = null;
		
		int score = 0;
		
		public boolean[] a;
		public boolean[] c;
		public boolean[] g;
		public boolean[] t;
		
		public FVertex(String label, byte[] sequence){
			this.label = label;
			this.sequence = sequence;
			a = new boolean[sequence.length];
			c = new boolean[sequence.length];
			g = new boolean[sequence.length];
			t = new boolean[sequence.length];
			
			this.original = true;
			
			for (int i=0; i<sequence.length; i++){
				if (sequence[i] == A){
					a[i] = true;
					c[i] = false;
					g[i] = false;
					t[i] = false;
				} else {
					if (sequence[i] == C){
						a[i] = false;
						c[i] = true;
						g[i] = false;
						t[i] = false;
					} else {
						if (sequence[i] == G){
							a[i] = false;
							c[i] = false;
							g[i] = true; 
							t[i] = false;
						} else {
							if (sequence[i] == T){
								a[i] = false;
								c[i] = false;
								g[i] = false;
								t[i] = true;
							} else {
								FitchAlg.this.log.error("wrong site");
								System.exit(1);
							}
						}
					}
				}
			}
		}
		
		public FVertex(int sequenceLength){
			this.original = false;
			idCount++;
			this.label = "i" + idCount;
			this.sequence = new byte[sequenceLength];
			a = new boolean[sequenceLength];
			c = new boolean[sequenceLength];
			g = new boolean[sequenceLength];
			t = new boolean[sequenceLength];
			for (int i=0; i<sequenceLength; i++){
				a[i] = false;
				c[i] = false;
				g[i] = false;
				t[i] = false;
			}
			
		}
		public boolean isOriginal(){
			return original;
		}
		
		public int seqLength(){
			return sequence.length;
		}
	}
	
	
	public class IntWrapper {
		public int value;
		public IntWrapper(int value){
			this.value = value;
		}
	}
	
}
