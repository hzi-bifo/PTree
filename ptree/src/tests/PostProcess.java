package tests;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PostProcess {

	public static boolean checkLine(String node){
		
		String line = node.substring(1, node.lastIndexOf(")"));
		///System.out.println(node);
		/*count childrens*/
		int start=0;
		int end=0;
		int open = 0;
		int children = 0;
		for (int i=0; i< line.length(); i++){
			if (line.charAt(i) == '('){
				open++;
				if (open == 1){
					start = i;
				}
			}
			if (line.charAt(i) == ')'){
				open--;
				if (open == 0){
					end = i;
					children++;
					if (!checkLine(line.substring(start, end+1))) {
						//System.err.println("check failed for: " + line.substring(start, end+1));
						return false;
					}
				}
			}
			if (line.charAt(i) == 'T'){
				if (open == 0){
					children++;
				}
			}	
			
		}
		
		
		
		if (children < 2){
			System.err.println("not enough children: " + node);
			return false;
		}
		//System.out.println("children: " + children);
		
		return true;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	 
		/* read the file */
		 
		//if (!reader.open(new File("C:\\Users\\Ivan\\Documents\\A_SAAR\\HIWI\\benchmark\\results\\output_all_seq5000_40_mask10_iter200_clearcut\\intree200_postprocessed"))){
			 
		try{
		    // Open the file that is the first 
		    // command line parameter
		    FileInputStream fstream = new FileInputStream("C:\\Users\\Ivan\\Documents\\A_SAAR\\HIWI\\benchmark\\results\\output_all_seq_5000_40_mask10" +
		    		"\\clearcut_Kimura_correction\\100_iter\\intree");
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String line;
		    //Read File Line By Line
		    List<Integer> list = new ArrayList<Integer>();
		    int counter = 0;
		    while ((line = br.readLine()) != null)   {
		    	counter++;
		    	
		    	/* check the line */
		    	if (!checkLine(line.substring(0,line.lastIndexOf(")")+1))){
		    		list.add(counter);
		    		//System.err.println("ERROR");
		    		//System.out.println (line);	
		    	}
		    	
		    	// Print the content on the console
		      
		    	
		    }
		    System.out.println("Bad lines:");
		    for (int i=0; i< list.size(); i++){
		    	System.out.println(list.get(i));
		    }
		    
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
 
		
	}

}
