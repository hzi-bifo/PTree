package ptree;

import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.Adler32;


/**
 * Represents one DNA sequence.
 * */
public class Dna implements Comparable<Dna> {

	/** DNA is stored as an array of bytes */
	private byte bytes[];
	
	/** hash code of a DNA is given by Adler32 hash function */
	private long hashCode;
	
	private boolean gapIsChange = true;
	
	private byte gapChar = (byte)'-';
	
	/**
	 * Constructor.
	 * */
	protected Dna(byte dna[]){
		this.bytes = dna;
		this.hashCode = getHashCode(dna);
	}
	
	/**
	 * Constructor.
	 * */
	private Dna(byte dna[], long hashCode, boolean gapIsChange, byte gapChar){
		this.bytes = dna;
		this.gapIsChange = gapIsChange;
		this.gapChar = gapChar;
		this.hashCode = hashCode;
	}
	
	
	/**
	 * Copy constructor.
	 * */
	protected Dna(Dna dna){
		this.bytes = new byte[dna.bytes.length];
		for (int i=0; i<dna.bytes.length; i++){
			this.bytes[i] = dna.bytes[i];
		}
		this.gapIsChange = dna.gapIsChange;
		this.gapChar = dna.gapChar;
		this.hashCode = dna.hashCode;
	}
	
	
	/**
	 * Return a hashcode of a byte array.
	 * */
	private long getHashCode(byte[] array){
		if (this.gapIsChange){
			Adler32 adler = new Adler32();
			adler.reset();
			adler.update(array);
			return adler.getValue();
		} else {
			return 0;
		}
	}
	
	
	protected void setGapIsChange(boolean gapIsChange){
		this.gapIsChange = gapIsChange;
		if (gapIsChange){
			this.hashCode = getHashCode(this.bytes);
		} else {
			this.hashCode = 0;
		}
	}
	
	protected void setGapChar(byte gapChar){
		this.gapChar = gapChar;
	}
	
	/**
	 * Get DNA as an array of bytes.
	 * */
	public byte[] getBytes(){
		return bytes;
	}
	
	
	/**
	 * Copy the DNA.
	 * */
	@Override
	public Dna clone(){
		
		byte copyBytes[] = new byte[bytes.length];
		for (int i=0; i<copyBytes.length; i++){
			copyBytes[i] = bytes[i];
		}
		
		return new Dna(copyBytes,hashCode,gapIsChange,gapChar);
	}
	
	
	/**
	 * Apply changes introduced in mut to the sequence and recompute its hash code.
	 * */
	public void applyMutations(Mutations mut){
		
		List<Mutation> mutations = mut.getMutations();
		
		for (Mutation m : mutations){
			bytes[m.getPosition()-1] = m.getToChar();
		}
		
		this.hashCode = getHashCode(bytes);
	}
	
	
	/**
	 * Mask positions with a given xChar character.
	 * Positions starts from 0 index.
	 * Recompute the DNA`s hash code.
	 * */
	protected void maskPositions(List<Integer> positions, byte xChar){
		
		for (int position : positions){
			bytes[position] = xChar;
		}
		
		this.hashCode = getHashCode(bytes);
	}
	
	
	protected void updateHashCode(){
		this.hashCode = getHashCode(this.bytes);
	}
	
	@Override
	public boolean equals(Object dna){
		
		Dna d = (Dna)dna;
		
		byte array[] = d.bytes;
		
		if (this.gapIsChange){
		
			if (this.hashCode != d.hashCode){
				return false;
			}
			
			for (int i=0; i<this.bytes.length; i++){
				if (this.bytes[i] != array[i]){
					return false;
				}
			}
			return true;
		} else {
			
			for (int i=0; i<this.bytes.length; i++){
				if ((this.bytes[i] != array[i]) && (this.bytes[i] != gapChar) && (array[i] != gapChar)){
					return false;
				}
			}
			return true;
		}
	}
	

	@Override
	public int compareTo(Dna arg0) {
		if (this.equals(arg0)){
			return 0;
		}
		byte[] b0 = this.bytes;
		byte[] b1 = arg0.getBytes();
		for (int i=0; i<b0.length; i++){
			if (b0[i] < b1[i]){
				return -1;
			} else {
				if (b0[i] > b1[i]){
					return 1;
				}
			}
		}
		System.err.println("Wrong branche in ptree.Dna.compareTo");
		System.exit(-1);
		return 0;
	}
	
	
	@Override
	public int hashCode(){
		return (int)hashCode;
	}
	
	
	@Override
	public String toString(){
		return new String(bytes,Charset.forName("UTF-8"));
	}

	
	/**
	 * Test method.
	 */
	public static void main(String[] args) {
		
		
		//String r = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTCG";
		//String h = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTCG";
		//String s = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTCG";
		//String t = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTCG";
		String str1 = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTC";
		String str2 = "ATGAAGACTATCATTGCTTTGAGCTACATTTTATGTCTGGTTTTC";
		int count = 0;
		//String str1 = r;
		//String str2 = t;
		for (int i=0; i< str1.length(); i++){	
			if (str1.charAt(i) != str2.charAt(i)){
				count++;
				System.out.print( (char)str1.charAt(i) + new Integer(i+1).toString() + (char)str2.charAt(i) + " ");
			}
		}
		System.out.println();
		System.out.println("Difference between sequences: " + count);
		
	}
	
}
