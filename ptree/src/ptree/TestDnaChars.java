package ptree;

import common.Configuration;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test DNA representation.
 * */
public class TestDnaChars {

	private static Log log = LogFactory.getLog(TestDnaChars.class);
	
	/**
	 * Test whether the DNA sequence contains only elements that are allowed.
	 * Allowed are charecters from the configuration file that corresponds to
	 * adenine, thymine, guanine and cytosine. Additionally also a character used to mask positions.  
	 * */
	public static void testDnaCharRepresentation(Configuration config, List<Vertex> list){
		
		byte a = config.getA();
		byte t = config.getT();
		byte g = config.getG();
		byte c = config.getC();
		byte s = config.getMaskChar();
		byte m = config.getGapChar();
		
		List<Integer> errorPositions = new ArrayList<Integer>();
		List<String> errorDnaName = new ArrayList<String>();
		List<String> errorIdxs = new ArrayList<String>();
		long errorCount = 0;
		StringBuffer buf; 
		
		Vertex vertex;
		byte dna[];
		for (int i=0; i<list.size(); i++){
			vertex = list.get(i);
			dna = vertex.getDna().getBytes();
			for (int j=0; j<dna.length; j++){
				if (dna[j] != a && dna[j] != t && dna[j] != g && dna[j] != c && dna[j] != s && dna[j] != m){
					errorPositions.add(j);
					errorCount++;
				}
			}
			if (!errorPositions.isEmpty()){
				errorDnaName.add(vertex.getName());
				buf = new StringBuffer();
				buf.append(errorPositions.get(0));
				
				for (int k=1; k<errorPositions.size(); k++){
					buf.append(", ");
					buf.append(errorPositions.get(k));
				}
				errorIdxs.add(buf.toString());
				
				errorPositions.clear();
			}
		}
		
		if (errorCount > 0){
			/* we have encountered some errors, so we print them out */
			
			buf = new StringBuffer();
			
			buf.append(errorCount + " character errors has been encountered in dna sequences.\n\n");
			buf.append("List of errors:\n\n");
			
			for (int i=0; (i< errorDnaName.size())&&(i < 10); i++){
				buf.append("DNA name: \"" + errorDnaName.get(i) + "\"  Error at position(s): " + errorIdxs.get(i) + "\n");
			}
			
			if (errorDnaName.size() > 10){
				buf.append("\n");
				buf.append(" ... next " + (errorDnaName.size() - 10) + " dna sequences contain errors.");
			}
			
			String str = buf.toString();
			str = (str.length() < 300)?str:(str.substring(0,300) + "..");
			
			//Dialogue.showWarningMessage(str);
			log.warn(str);
		}
		
	}
	
}
