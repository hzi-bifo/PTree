package tests;

import java.util.Random;


/**
 * Testing class to estimate duration of elementary steps of a NJ alg.
 * 
 * @deprecated this class is only for testing
 */
public class SpeedTests {

	
	public static void main(String[] args) {

		long time = System.currentTimeMillis();
		
		Random random = new Random(763447);
		
		int size = 2500;
		
		double[][] m = new double[size][];
		double[][] m2 = new double[size][];
		double[][] q = new double[size][];
		
		for (int i=0; i<size; i++){
			m[i] = new double[size];
		}
		for (int i=0; i<size; i++){
			m2[i] = new double[size];
		}
		for (int i=0; i<size; i++){
			q[i] = new double[size];
		}
		
		System.out.println("allocation: " + (System.currentTimeMillis() - time) + "ms");	
		time = System.currentTimeMillis();
		
		for (int i=0; i<size; i++){
			for (int j=0; j<size; j++){
				m[i][j] = Math.abs(random.nextDouble());
			}
		}
		
		double d_ij[] = new double[size];
		
		//System.out.println("get random val: " + (System.currentTimeMillis() - time) );
		time = System.currentTimeMillis();
		long searchingTime = 0;
		long temp;
		
		long copyingMatrixTime = 0;
		long temp2;
		
		//------------------searching time
		long time2 = System.currentTimeMillis();
		while (size > 10){
			size--;

		double size_2 = size-2;
		
		for (int i=0; i<size; i++){
			d_ij[i] = 0;
			for (int j=0; j<size; j++){
				d_ij[i]+= m[i][j];
			}
		}

		//System.out.println("compute Q matrix step 1: " + (System.currentTimeMillis() - time) );
		time = System.currentTimeMillis();
		
		for (int i=0; i<size; i++){
			for (int j=0; j<i; j++){
				q[i][j] = (size_2 * m[i][j]) - d_ij[i] - d_ij[j];
				
			}
		}
		
		//System.out.println("compute Q matrix step 2: " + (System.currentTimeMillis() - time) );
		time = System.currentTimeMillis();
		
		temp = System.currentTimeMillis();
		
		@SuppressWarnings("unused")
		int iMin = 0;
		@SuppressWarnings("unused")
		int jMin = 0;
		double min = Double.MAX_VALUE;
		
		for (int i=0; i<size; i++){
			for (int j=0; j<i; j++){
				if (m[i][j] < min){
					min = m[i][j];
					iMin = i;
					jMin = j;
				}
			}
		}

		searchingTime += System.currentTimeMillis() - temp;
		
		//System.out.println("find min: " + (System.currentTimeMillis() - time) );	
		
		for (int i=0; i<size; i++){
				m[i][5] = 0.5 * (m[i][2] - m[i][7]) +  0.5 * (m[8][i] - m[9][i]);
				m[i][56] = 0.5 * (m[i][1] - m[i][7]) +  0.5 * (m[7][i] - m[5][i]);
		}
		
		//System.out.println("recompute distance matrix: " + (System.currentTimeMillis() - time) );
		time = System.currentTimeMillis();
		
		temp2 = System.currentTimeMillis();
		
		for (int i=0; i<size; i++){
			for (int j=0; j<i; j++){
				m2[i][j] = m[i][j];
				
			}
		}
		copyingMatrixTime+= System.currentTimeMillis() - temp2;
		
		//System.out.println("copy distance matrix: " + (System.currentTimeMillis() - time) );
		time = System.currentTimeMillis();
		}
		System.out.println("copy distance matrix: " + (System.currentTimeMillis() - time2) + "ms");
		System.out.println("searching time: " + (searchingTime) + "ms");
		System.out.println("copying matrix: " + (copyingMatrixTime) + "ms");
	}

}
