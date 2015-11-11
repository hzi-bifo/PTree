package ptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manages subsampling. 
 * */
public class SamplingManager {

	
	/* positions that can be masked */
	private List<Integer> candidatePositions;	
	
	/* random generator */
	private Random random;
	
	@SuppressWarnings("unused")
	private Log log;
	
	
    /** 
     * Constructor.
     * */
	protected SamplingManager(List<Vertex> originalVertices, Configuration config){
		
		log = LogFactory.getLog(SamplingManager.class);
		
		random = new Random(123456789);
		
		byte[][] array = new byte[originalVertices.size()][];
		for (int i=0; i<originalVertices.size(); i++){
			array[i] = originalVertices.get(i).getDna().getBytes();
		}
		
		final byte a = config.getA();
		final byte c = config.getC();
		final byte g = config.getG();
		final byte t = config.getT();
		int countA = 0;
		int countC = 0;
		int countG = 0;
		int countT = 0;
		int temp = 0;
		
		/* get positions that can be masked */
		candidatePositions = new ArrayList<Integer>();
		
		if (originalVertices.size() < 4){
			return;
		}
		
		int sequenceLength = originalVertices.get(0).getDna().getBytes().length;
		
		/* for all columns */
		for (int i=0; i<sequenceLength; i++){
			
			/* for all rows in a column i */
			for (int j=0; j<originalVertices.size(); j++){
				
				if (array[j][i] == a) {
					countA++;
					continue;
				}
				if (array[j][i] == c) {
					countC++;
					continue;
				}
				if (array[j][i] == g) {
					countG++;
					continue;
				}
				if (array[j][i] == t) {
					countT++;
					continue;
				}
				//log.error("Wrong branche.");
			}
			
			if (countA >= 2){
				temp++;
			}
			if (countC >= 2){
				temp++;
			}
			if (countG >= 2){
				temp++;
			}
			if (countT >= 2){
				temp++;
			}
			
			if (temp >= 2){
				candidatePositions.add(i);
			}
			temp = 0;
			countA = 0;
			countC = 0;
			countG = 0;
			countT = 0;
		}
	}
	
	
	/**
	 * Get random sampling - positions to be masked.
	 * 
	 * @param numberOfPositions maximum number of positions to be masked. 
	 * */
	public Sampling getRandomSampling(int numberOfPositionsMin, int numberOfPositionsMax){
		
		int numberOfPositions = random.nextInt(numberOfPositionsMax - numberOfPositionsMin + 1) 
								+ numberOfPositionsMin;
		
		List<Integer> outputList = new ArrayList<Integer>(numberOfPositions);
		
		/* copy the list of candidates */
		List<Integer> candidateCopy = new ArrayList<Integer>(candidatePositions.size());
		candidateCopy.addAll(candidatePositions);
		
		int index;
		
		for (int it=0; it<candidatePositions.size(); it++){
			
			if (outputList.size() == numberOfPositions){
				break;
			}
			
			/* draw one random candidate position */
			index = random.nextInt(candidateCopy.size());
			outputList.add(candidateCopy.get(index));
			candidateCopy.remove(index);
			
		}
		
		return new Sampling(outputList);
	}
	
}
