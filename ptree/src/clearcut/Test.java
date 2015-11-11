package clearcut;

import common.Configuration;

/**
 * Test of the method {@link Clearcut#computeNJMatrix(String, Configuration, short)}.
 */
public class Test {

	
	public static void main(String[] args) {

		Configuration config = new Configuration();
		config.readConfigFiles(false);
		
		System.out.println("nj implementation: " + config.getNjImplementation());
		System.out.println("nj correction: " + config.getNjCorrection());
		System.out.println("nj relaxed version: " + config.getNjRelaxedVersion());
		
		String fastaFile = "> s0\nGGCCAGCAAC\n> s1\nTGCCAATAGT\n> s2\nGGGGAATAGT\n> s3\nAATCGACGGT";
					
		short matrixOutputSize = 4;
			
		float[][] output_matrix = Clearcut.computeNJMatrix(fastaFile, config, matrixOutputSize);
	
		print(output_matrix);
		
	}
	
	
	public static void print(float[][] matrix){
		for (int i=0; i<matrix.length; i++){
			for (int j=0; j<matrix[i].length; j++){
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}
	
}


	