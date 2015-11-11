package ptree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import common.Configuration;

public class MutationManager {

	byte gapChar;
	boolean countGapAsChange;
	boolean bufferMutations;
	
	/* mapping: edge hash(vertex from vertex to) -> mutation record */
	private Map<Long,Mutations> vertexToMutations;
	
	public MutationManager(Configuration config, boolean bufferMutations){
		this.gapChar = config.getGapChar();
		this.countGapAsChange = config.getCountGapAsChange();
		this.bufferMutations = bufferMutations;
		if (this.bufferMutations){
			vertexToMutations = new HashMap<Long,Mutations>();
		}
	}
	
	
	public short getMutationsCount(Dna dna0, Dna dna1){
		
		byte[] array0 = dna0.getBytes();
		byte[] array1 = dna1.getBytes();
		int length = Math.min(array0.length, array1.length);
		short mutationCount = (short)Math.abs(array0.length - array1.length);
		for (int i=0; i<length; i++){
			if (array0[i] != array1[i]){
				if (countGapAsChange){
					mutationCount++;
				} else {
					if ((array0[i] != gapChar) && (array1[i] != gapChar)){
						mutationCount++;
					}
				}
			}
		}
		return mutationCount;
	}

	
	public boolean countAsChange(byte p0, byte p1){
		
		if (p0 != p1){
			if (countGapAsChange){
				return true;
			} else {
				if ((p0 != gapChar) && (p1 != gapChar)){
					return true;
				}
			}
		}
		
		return false;
	}


	public Mutations getMutations(Vertex vertexFrom, Vertex vertexTo){
		
		if (this.bufferMutations){
			
			long pairCode = encode(vertexFrom.getIntId(),vertexTo.getIntId());
			
			Mutations mutations = vertexToMutations.get(pairCode);
			if (mutations == null){
				mutations = new Mutations(vertexFrom.getDna(), vertexTo.getDna(), gapChar, countGapAsChange);
				vertexToMutations.put(pairCode, mutations);
			}
			return mutations;
		} else {
			
			return new Mutations(vertexFrom.getDna(), vertexTo.getDna(), gapChar, countGapAsChange);
		}
	}
	
	
	public void clear(){
		if (this.bufferMutations){
			vertexToMutations.clear();
		}
	}

	
	/**
	 * Encode two 32bit integers to the corresponding 64bit integer.
	 * */
	private static long encode(int first, int second){
		return ((long)first << 32) | second;
	}
	
	
	/**
	 * Get first 32bits of a long.
	 * */
	private static long decodeFirst(long code){
		return code >> 32;
	}
	
	
	/**
	 * Get second 32bits of a long.
	 * */
	private static long decodeSecond(long code){
		return code & Integer.MAX_VALUE;
	}
	
	private static void test1(){
		Random rand = new Random(986579876);
		
		int a,b;
		long c;
		for (int i=0;i<10000000;i++){
			a = rand.nextInt(Integer.MAX_VALUE);
			b = rand.nextInt(Integer.MAX_VALUE);
			c = encode(a,b);
			if (a != decodeFirst(c)){
				System.err.println("error for a=" + a + " b=" + b + " c=" + c);
				break;
			}
			if (b != decodeSecond(c)){
				System.err.println("error for b=" + b + " a=" + a + " c=" + c);
				break;
			}
		}
		System.out.println("Test End ");
		
	}
	
	
	/**
	 * Test
	 */
	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
		
		test1();
		
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
		
	}

	
}
