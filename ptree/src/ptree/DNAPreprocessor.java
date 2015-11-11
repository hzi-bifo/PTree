package ptree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Preprocess all DNAs and store aside all positions that are the same in all DNAs.
 * Work with positions that left. After the program finishes add positions that were previously left out.
 * */
public class DNAPreprocessor {

	private Log log;
	
	private byte[] sameSites;
	
	private List<Integer> idxSameSites;
	private final int dnaLength;
	private final int compressedDnaLength;
	
	
	//public int getSameSitesCount(){
	//	return this.dnaLength - this.compressedDnaLength;
	//}
	
	public int getDnaUncompressedLength(){
		return dnaLength;
	}
	
	public DNAPreprocessor(int dnaLength){
		this.dnaLength = dnaLength;
		this.compressedDnaLength = dnaLength;
	}
	
	/**
	 * Change the DNA of each vertex. Leave out sites that are the same in all DNA`s.
	 * */
	public DNAPreprocessor(List<Vertex> vertices){
		
		log = LogFactory.getLog(DNAPreprocessor.class);
		
		if (!vertices.isEmpty()){
			
			/* dna length */
			dnaLength = vertices.get(0).getDna().getBytes().length;
			
			/* find out which sites are the same in all sequences */
			idxSameSites = computeSameSitesIdx(vertices);

			if (used()){

				/* store same sites */
				sameSites = new byte[idxSameSites.size()];
				byte[] dna = vertices.get(0).getDna().getBytes();
				for (int i=0; i<idxSameSites.size(); i++){
					sameSites[i] = dna[idxSameSites.get(i)];
				}
				
				compressedDnaLength = dnaLength - idxSameSites.size();
				
				/* change DNA of each vertex */
				Vertex vertex;
				byte[] dnaOld;
				int iOld;
				int iNew = 0;
				int iS;
				
				for (int i=0; i<vertices.size(); i++){
					
					vertex = vertices.get(i);
					dnaOld = vertex.getDna().getBytes();
					dna = new byte[compressedDnaLength];
					iOld = 0;
					iNew = 0;
					iS = 0;
					for (; iOld < dnaOld.length; iOld++){
						if ((iS < idxSameSites.size()) && (iOld == idxSameSites.get(iS))){
							
							iS++;
						} else {
							
							dna[iNew] = dnaOld[iOld];
							iNew++;
						}
					}
					if (iNew != compressedDnaLength){
						log.error("Wrong Compressed size!!!");
					}
					vertex.setDna(new Dna(dna));
				}
			} else {
				this.compressedDnaLength = this.dnaLength;
			}
		} else {
			this.dnaLength = 0;
			this.compressedDnaLength = 0;
		}
	}
	
	
	/**
	 * Change the DNA of each vertex to its initial form. 
	 * */
	public void restoreDNA(List<Vertex> vertices){
		
		if (used()){
		
			byte[] dna;
			byte[] dnaOld;
			Vertex vertex;
			
			int iS; 
			int iDnaOld;
			
			for (int i=0; i<vertices.size(); i++){
			
				vertex = vertices.get(i);
				
				dnaOld = vertex.getDna().getBytes();;
				dna = new byte[dnaLength];
				
				iS = 0;
				iDnaOld = 0;
				
				for (int iDna = 0; iDna<dnaLength; iDna++){
			
					if ((iS < idxSameSites.size()) && (iDna == idxSameSites.get(iS))){
						dna[iDna] = sameSites[iS];
						iS++;
					} else {
						dna[iDna] = dnaOld[iDnaOld];
						iDnaOld++;
					}
					
				}
				
				vertex.setDna(new Dna(dna));
			}
		}
	}
	
	
	/**
	 * Change the positions of mutations` records s.t. it corresponds to the restored DNAs.
	 * */
	public void restoreMutations(List<Vertex> vertices){
		
		if (used()){
			
			Map<Integer,Integer> map = new HashMap<Integer,Integer>();
			int skipCount = 0;
			int iS = 0;
			for (int i=0; i<dnaLength; i++){
				map.put((i-skipCount)+1, (i)+1);
				if ((iS <idxSameSites.size()) && (idxSameSites.get(iS) == i)){
					iS++;
					skipCount++;
				}	
			}
			
			Vertex vertex;
			List<Mutation> mutations;
			
			for (int i=0; i<vertices.size(); i++){
				vertex = vertices.get(i);
				if (vertex.getMutations() != null){
					mutations = vertex.getMutations().mutations;
					for (int j=0; j<mutations.size(); j++){
						mutations.get(j).setPosition(map.get(mutations.get(j).getPosition()));
					}
				}
			}
		}
	}
	
	
	/**
	 * Returns whether the DNA preprocessor found some sites that would be the same.	
	 * */
	protected boolean used(){
		return (idxSameSites.isEmpty())?false:true;
	}

	
	/**
	 * Return the list of indices of the same site.
	 * */
	private List<Integer> computeSameSitesIdx(List<Vertex> vertices){
			
			List<Integer> idxSame = new ArrayList<Integer>();
			boolean same;
			byte b;
			
			for (int i=0; i<dnaLength; i++){
				
				same = true;
				
				b = vertices.get(0).getDna().getBytes()[i];
				for (int j=1; j<vertices.size(); j++){
					
					if (vertices.get(j).getDna().getBytes()[i] != b){
						same = false;
						break;
					}
					
				}
				if (same){
					idxSame.add(i);
				}
				
			}
			return idxSame;
		}
	
}