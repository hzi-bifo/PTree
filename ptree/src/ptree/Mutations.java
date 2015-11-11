package ptree;

import java.util.List;
import java.util.ArrayList;


/**
 * Represents a list of mutations that defines difference between two DNAs.
 * */
public class Mutations implements Comparable<Mutations> {

	/* mutations are sorted according to the increasing position */
	protected List<Mutation> mutations;
	
	/* hash code of the list */
	private final int hashCode;
	
	private short occurrence = 2;
	
	
	/**
	 * Constructor.
	 *  */
	protected Mutations(List<Mutation> mutations){
		this.mutations = mutations;
		hashCode = getHashCode(mutations);
	}
	
	
	/**
	 * Constructor.
	 * */
	protected Mutations(){
		mutations = new ArrayList<Mutation>();
		hashCode = 0;
	}
	
	
	/**
	 * Constructor.
	 * */
	public Mutations(Dna dnaOut, Dna dnaIn, byte gapChar, boolean countGapAsChange){
		
		mutations = new ArrayList<Mutation>();
		byte[] arrayOut = dnaOut.getBytes();
		byte[] arrayIn = dnaIn.getBytes();
		int length = Math.min(arrayOut.length, arrayIn.length);
		for (int i=0; i<length; i++){
			if (arrayOut[i] != arrayIn[i]){
				if (countGapAsChange){
					mutations.add(new Mutation(arrayOut[i], i+1, arrayIn[i]));
				} else {
					if ((arrayOut[i] != gapChar) && (arrayIn[i] != gapChar)){
						mutations.add(new Mutation(arrayOut[i], i+1, arrayIn[i]));
					}
				}
			}
		}
		if (arrayOut.length < arrayIn.length){
			for (int i=arrayOut.length; i<arrayIn.length; i++){
					mutations.add(new Mutation((byte)' ', i+1, arrayIn[i]));
			}
		}
		if (arrayIn.length < arrayOut.length){
			for (int i=arrayIn.length; i<arrayOut.length; i++){
					mutations.add(new Mutation(arrayOut[i], i+1, (byte)' '));
			}
		}
		hashCode = getHashCode(mutations);
	}
	
	
	
	/**
	 * How many vertices participate in this mutation set.
	 * */
	public void setOccurrence(short occurrence){
		this.occurrence = occurrence;
	}
	
	
	/**
	 * The cost of the tree will decrease after we include this intermediate, 
	 * 
	 * @return the number of mutations that will be subtracted from the total cost of the tree.
	 * */
	public int getCostDecrease(){
		return (this.occurrence-1)*this.getMutationCount();
	}
	
	
	private static int getHashCode(List<Mutation> list){
		long hash = 0;
		for (int i=0; i< list.size(); i++){
			hash += i*(list.get(i).hashCode());
		}
		return (int)hash;
	}
	
	
	/**
	 * Get inverse mutations. 
	 * */
	public static Mutations getInverseMutations(Mutations m){
		
		List<Mutation> list = m.mutations;
		List<Mutation> inverseList = new ArrayList<Mutation>(list.size());
		
		for (int i=0; i<list.size(); i++){
			inverseList.add(list.get(i).getInverse());
		}
		
		return new Mutations(inverseList);
	}
	
	
	/**
	 * Returns whether this set (of mutations) is a subset of a set (of mutations) passed as a parameter. 
	 * Suppose that both mutations sets are not empty.
	 * */
	public boolean isSubsetOf(Mutations m){
		
		List<Mutation> mbigger = m.getMutations(); 
		int i=0;
		int j=0;
		int match = 0;
		
		while (i< mutations.size() && j< mbigger.size()){
			
			if (mutations.get(i).getPosition() < mbigger.get(j).getPosition()){
				i++;
				continue;
			}
			
			if (mutations.get(i).getPosition() > mbigger.get(j).getPosition()){
				j++;
				continue;
			}
			
			if (mutations.get(i).equals(mbigger.get(j))){
				/* I have found the same element in both arrays */
				match++;
			}
			i++;
			j++;
		}
		
		/* the number of equal elements is equal to the number of the smaller set -> this (smaller set) is a subset of m */
		if (match == mutations.size()){
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Returns an intersection set of two mutations sets or NULL.
	 * */
	public static Mutations getIntersection(Mutations mut1, Mutations mut2){
		
		List<Mutation> list1 = mut1.mutations;
		List<Mutation> list2 = mut2.mutations;
		
		if (list1.isEmpty() || list2.isEmpty()){
			return null;
		}
		
		List<Mutation> intersectionList = null;
		
		int i = 0;
		int j = 0;
		
		while (i < list1.size() && j < list2.size()){
			
			if (list1.get(i).getPosition() < list2.get(j).getPosition()){
				i++;
				continue;
			}
			
			if (list1.get(i).getPosition() > list2.get(j).getPosition()){
				j++;
				continue;
			}
			
			if (list1.get(i).equals(list2.get(j))){
				/* I have found the same element in both arrays */
				if (intersectionList == null){
					intersectionList = new ArrayList<Mutation>();
				}
				intersectionList.add(list1.get(i));
			}
			i++;
			j++;
		}
		
		if (intersectionList == null){
			return null;
		} else {
			return new Mutations(intersectionList);
		}
	}
	

	/**
	 * Number of elements in a mutation set.
	 * */
	public int getMutationCount(){
		return mutations.size();
	}
	
	
	protected void clear(){
		mutations.clear();
	}
	
	
	protected List<Mutation> getMutations(){
		return mutations;
	}
	
	
	@Override
	public boolean equals(Object arg){

		Mutations mut = (Mutations)arg;
		List<Mutation> m = mut.mutations;
		
		if (this.hashCode != mut.hashCode){
			return false;
		}
		
		if (mutations.size() != m.size()){
			return false;
		}
		
		for (int i=0; i< m.size(); i++){
			if (!mutations.get(i).equals(m.get(i))){
				return false;
			}
		}
		
		return true;
	}
	
	
	@Override
	public int hashCode(){
		return hashCode;
	}
	
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		if (mutations.size() > 0){
			buf.append(mutations.get(0).toString());
		}
		for(int i=1; i<mutations.size(); i++){
			buf.append(" ");
			buf.append(mutations.get(i).toString());
		}
		return buf.toString();
	}


	@Override
	public int compareTo(Mutations arg0) {
		if (this.equals(arg0)){
			return 0;
		} else {
			return arg0.getMutationCount()*(arg0.occurrence-1) - this.getMutationCount()*(this.occurrence-1);
			//if (this.getMutationCount()*(this.occurrence-1) > arg0.getMutationCount()*(arg0.occurrence-1)){
			//	return -1;
			//} else {
			//	return 1;
			//}
		}
	}

}
