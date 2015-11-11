package fitchcost;

import java.util.HashSet;
import java.util.Set;


/**
 * Implements the Sankoff Algorithm.
 * */
public class SankoffAlg {

	/* Characters that are allowed in the sequences */
	private byte[] chars;// {'A','C','G','T'};
	
	/* a character that is ignored */
	private byte ignoreChar;// '-';
	
	/* root of a tree */
	private SVertex sroot;
	
	boolean computeCostsRun = false;
	boolean assignAndSetSequencesRun = false;
	
	
	/**
	 * Constructor.
	 * 
	 * @param chars character allowed in the sequences 
	 * (can be null - the alg. will consider all except for ignore char)
	 * @param ignoreChar a character that is ignored in the sequences
	 * @param root the root of the input tree
	 * */
	public SankoffAlg(byte[] chars, byte ignoreChar, SankoffAlgVertex root){
		
		this.ignoreChar = ignoreChar;	
		
		/* find out which characters are contained in the sequences (except for the ignoreChar) */
		if (chars == null){
			Set<Byte> set = new HashSet<Byte>();
			collectChars(root, set);
			Byte [] array = set.toArray(new Byte[0]);
			this.chars = new byte[array.length];
			for (int i=0; i<array.length; i++){
				this.chars[i] = array[i];
			}
		} else {
			this.chars = chars;	
		}
		
		this.sroot = SVertex.getTree(root);
	}

	
	/**
	 * Collect chars that are contained in the sequences (except for the ignoreChar)
	 * 
	 * @param root the root of the tree
	 * @param set the input/output set of chars contained in sequences (except for the ignoreChar)
	 * */
	private void collectChars(SankoffAlgVertex root, Set<Byte> set){
		
		if (root.isLeaf()){
			byte[] array = root.getDnaAsBytes();
			for (int i=0; i<array.length; i++){
				if (array[i] != ignoreChar){
					if (!set.contains(array[i])){
						set.add(array[i]);
					}
				}
			}
		} else {
			for (int i=0; i<root.getChildren().size(); i++){
				collectChars(root.getChildren().get(i), set);
			}
		}	
		
	}


	/**
	 * Compute costs for the individual vertices.
	 * */
	public void computeCosts(){
		if (!computeCostsRun) {
			computeCosts(this.sroot);
		}
		computeCostsRun = true;
	}
	
	
	/**
	 * Assign sequences reconstructed by the Sankoff Algorithm to the internal 
	 * nodes of the original input tree.
	 * */
	public void assignAndSetSequences(){
		if (!computeCostsRun){
			computeCosts();
		}
		if (!assignAndSetSequencesRun){
			assignSequences(this.sroot, null);
			setDna(this.sroot);
		}
		assignAndSetSequencesRun = true;
	}
	
	public void assignAndSetSequences(byte[] seq){
		if (!computeCostsRun){
			computeCosts();
		}
		if (!assignAndSetSequencesRun){
			assignSequences2(this.sroot, seq);
			setDna(this.sroot);
		}
		assignAndSetSequencesRun = true;
	}
	
	/**
	 * Returns the fitch cost.
	 * */
	public int getCost(){
		if (!computeCostsRun){
			computeCosts();
		}
		return getCost(this.sroot);
	}
	
	
	/**
	 * Frees helper data structures allocated by the Sankoff Algorithm.
	 * */
	public void clear(){
		clear(this.sroot);
	}
	
	
	/**
	 * Compute costs for the individual vertices.
	 * 
	 * @param root the root of the tree
	 * */
	private void computeCosts(SVertex root){
		
		if (root.isLeaf()){
			//Trace.print("is leaf");
			for (int i=0; i<root.seq.length; i++){
				root.cost[i] = new int[chars.length];
				
				for (int j=0; j<chars.length; j++){
					
					if ((root.seq[i] == chars[j]) || (root.seq[i] == ignoreChar)){ 
						root.cost[i][j] = 0;
					} else {
						root.cost[i][j] = Integer.MAX_VALUE;
					}
				}
				root.vcost[i] = 0;
			}
			
		} else {
			//Trace.print("is child");
			
			SVertex child;
			for (int k=0; k<root.children.size(); k++){
				child = root.children.get(k);
				computeCosts(child);
			}

			for (int i=0; i<root.seq.length; i++){
				root.cost[i] = new int[chars.length];
				
				for (int j=0; j<chars.length; j++){
					 
					int sum = 0;					
					for (int k=0; k<root.children.size(); k++){
						child = root.children.get(k);
						if (!child.isLeaf() || (child.seq[i] != ignoreChar)) {
							sum+= Math.min(child.vcost[i] + 1, child.cost[i][j]);
						} 
					}
					root.cost[i][j] = sum;
				}
				
				int min = Integer.MAX_VALUE;
				for (int p=0; p<chars.length; p++){
					min = Math.min(min, root.cost[i][p]);
					
				}
				root.vcost[i] = min;
			}
		}	
	}
	
	
	/**
	 * Assign sequences reconstructed by the Sankoff Algorithm to the internal 
	 * nodes of the tree.
	 * 
	 * @param parent the parent of the vertex or null if the vertex is the root
	 * */
	private void assignSequences(SVertex vertex, SVertex parent){
		
		if (!vertex.isLeaf()){
		
			int index;
			int prefIndex;
			for (int i=0; i<vertex.seq.length; i++){ 
				prefIndex = ((parent != null)?charToCharIndex(parent.seq[i]):-1);
				index = getIndexOfMin(vertex.cost[i], prefIndex);
				vertex.seq[i] = chars[index]; 
			}
			
			for (int i=0; i<vertex.children.size(); i++){
				assignSequences(vertex.children.get(i), vertex); 
			}
		}
	}
	
	//new
	private void assignSequences2(SVertex vertex, byte[] seq){
		
		if (!vertex.isLeaf()){
		
			int index;
			int prefIndex;
			for (int i=0; i<vertex.seq.length; i++){ 
				prefIndex = ((seq != null)?charToCharIndex(seq[i]):-1);
				index = getIndexOfMin(vertex.cost[i], prefIndex);
				vertex.seq[i] = chars[index]; 
			}
			
			for (int i=0; i<vertex.children.size(); i++){
				assignSequences(vertex.children.get(i), vertex); 
			}
		}
	}

		
	/**
	 * Maps a character to its index in the array {@link #chars}.
	 * */
	private int charToCharIndex(byte ch){
		for (int i=0; i<chars.length; i++){
			if (ch == chars[i]){
				return i;
			}
		}
		return -1;
	}
	

	/**
	 * Index of the minimum cost.
	 * 
	 * @param array array of values
	 * @param prefIndex if the minimum is situated at this index then this index is returned
	 * 
	 * @return index of the minimum cost (prefIndex if the minimum is at this index)
	 * */
	private int getIndexOfMin(int[] array, int prefIndex){
		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i=0; i<array.length; i++){
			if (min > array[i]){
				index = i;
				min = array[i];
			}
		}
		
		if ((prefIndex >= 0) && (array[prefIndex] == min)){
			index = prefIndex;
		}
		
		return index;
	}
	
	
	/**
	 * Set the reconstructed sequences to the internal nodes
	 * of the original input tree
	 * */
	private void setDna(SVertex root){
		if (!root.isLeaf()){
			root.setDna();
			for (int i=0; i<root.children.size(); i++){
				setDna(root.children.get(i));
			}
		}
	}
	
	
	/**
	 * Compute the fitch cost.
	 * */
	private int getCost(SVertex root){
		int cost = 0;
		for (int i=0; i<root.seq.length; i++){
			cost += root.cost[i][getIndexOfMin(root.cost[i],-1)];
		}
		
		return cost;
	}
	
	
	/**
	 * Frees the helper data structures.
	 * */
	private void clear(SVertex root){
		if (root.isLeaf()){
			root.clear();
		} else {
			for (int i=0; i<root.children.size(); i++){
				clear(root.children.get(i));
			}
		}
	}
	
}
