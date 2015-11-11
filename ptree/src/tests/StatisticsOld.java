package tests;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains methods that can be used to compute statistics about the run of the algorithm,
 * does not support multithreading, only 1 executive thread can be set in the configuration.
 * Every block whose duration is measured is placed between a pair of functions: "start" and "end".
 * 
 * @deprecated this class is for testing (tuning up)
 * */
public class StatisticsOld {
	
	private static boolean computeStatistics = false;
	
	private static long startTime = 0;
	
	//private static List<Long> removeOriginalVertices = null;
	private static List<Long> computeDistanceMatrix = null;
	//private static List<Long> createDistanceMatrix = null;
	private static List<Long> computeNJAlg = null;
	private static List<Long> computeMSTNJ = null;
	//private static List<Long> computeIntermediates = null;
	//private static List<Long> deforestation = null;
	//private static List<Long> setStatusIgnoringSampling = null;
	//private static List<Long> removeDuplicates11 = null;
	private static List<Long> updateDistanceMatrix = null;
	private static List<Long> restoreDistanceMatrix = null;
	private static List<Long> computeMST = null;
	
	//
	/*public static synchronized void startRemoveOriginalVertices(){
		if (removeOriginalVertices == null){
			removeOriginalVertices = new ArrayList<Long>();
		}
		removeOriginalVertices.add(System.currentTimeMillis());
	}
	
	public static synchronized void endRemoveOriginalVertices(){
		Long time = removeOriginalVertices.get(removeOriginalVertices.size()-1);
		removeOriginalVertices.remove(removeOriginalVertices.size()-1);
		removeOriginalVertices.add(System.currentTimeMillis() - time);
	}*/
	
	public static synchronized void startStatistics(){
		startTime = System.currentTimeMillis();
	}
	
	
	
	//
	/*public static synchronized void startCreateDistanceMatrix(){
		if (createDistanceMatrix == null){
			createDistanceMatrix = new ArrayList<Long>();
		}
		createDistanceMatrix.add(System.currentTimeMillis());
	}
	
	public static synchronized void endCreateDistanceMatrix(){
		Long time = createDistanceMatrix.get(createDistanceMatrix.size()-1);
		createDistanceMatrix.remove(createDistanceMatrix.size()-1);
		createDistanceMatrix.add(System.currentTimeMillis() - time);
	}*/
	
	//
	public static synchronized void startComputeDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		if (computeDistanceMatrix == null){
			computeDistanceMatrix = new ArrayList<Long>();
		}
		computeDistanceMatrix.add(System.currentTimeMillis());
	}
	
	public static synchronized void endComputeDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		Long time = computeDistanceMatrix.get(computeDistanceMatrix.size()-1);
		computeDistanceMatrix.remove(computeDistanceMatrix.size()-1);
		computeDistanceMatrix.add(System.currentTimeMillis() - time);
	}
	
	//NJ
	public static synchronized void startComputeNJAlg(){
		if (!computeStatistics){
			return;
		}
		if (computeNJAlg == null){
			computeNJAlg = new ArrayList<Long>();
		}
		computeNJAlg.add(System.currentTimeMillis());
	}
	
	public static synchronized void endComputeNJAlg(){
		if (!computeStatistics){
			return;
		}
		Long time = computeNJAlg.get(computeNJAlg.size()-1);
		computeNJAlg.remove(computeNJAlg.size()-1);
		computeNJAlg.add(System.currentTimeMillis() - time);
	}
	
	//MST NJ
	public static synchronized void startComputeMSTNJ(){
		if (!computeStatistics){
			return;
		}
		if (computeMSTNJ == null){
			computeMSTNJ = new ArrayList<Long>();
		}
		computeMSTNJ.add(System.currentTimeMillis());
	}
	
	public static synchronized void endComputeMSTNJ(){
		if (!computeStatistics){
			return;
		}
		Long time = computeMSTNJ.get(computeMSTNJ.size()-1);
		computeMSTNJ.remove(computeMSTNJ.size()-1);
		computeMSTNJ.add(System.currentTimeMillis() - time);
	}
	
	//intermediates
	/*public static synchronized void startComputeIntermediates(){
		if (computeIntermediates == null){
			computeIntermediates = new ArrayList<Long>();
		}
		computeIntermediates.add(System.currentTimeMillis());
	}
	
	public static synchronized void endComputeIntermediates(){
		Long time = computeIntermediates.get(computeIntermediates.size()-1);
		computeIntermediates.remove(computeIntermediates.size()-1);
		computeIntermediates.add(System.currentTimeMillis() - time);
	}*/
	
	//deforestation
	/*public static synchronized void startDeforestation(){
		if (deforestation == null){
			deforestation = new ArrayList<Long>();
		}
		deforestation.add(System.currentTimeMillis());
	}
	
	public static synchronized void endDeforestation(){
		Long time = deforestation.get(deforestation.size()-1);
		deforestation.remove(deforestation.size()-1);
		deforestation.add(System.currentTimeMillis() - time);
	}*/
	
	//set status ignoring sampling
	/*public static synchronized void startSetStatusIgnoringSampling(){
		if (setStatusIgnoringSampling == null){
			setStatusIgnoringSampling = new ArrayList<Long>();
		}
		setStatusIgnoringSampling.add(System.currentTimeMillis());
	}
	
	public static synchronized void endSetStatusIgnoringSampling(){
		Long time = setStatusIgnoringSampling.get(setStatusIgnoringSampling.size()-1);
		setStatusIgnoringSampling.remove(setStatusIgnoringSampling.size()-1);
		setStatusIgnoringSampling.add(System.currentTimeMillis() - time);
	}*/
	
	//remove duplicates 11
	/*public static synchronized void startRemoveDuplicates11(){
		if (removeDuplicates11 == null){
			removeDuplicates11 = new ArrayList<Long>();
		}
		removeDuplicates11.add(System.currentTimeMillis());
	}
	
	public static synchronized void endRemoveDuplicates11(){
		Long time = removeDuplicates11.get(removeDuplicates11.size()-1);
		removeDuplicates11.remove(removeDuplicates11.size()-1);
		removeDuplicates11.add(System.currentTimeMillis() - time);
	}*/
	
	//update distance matrix
	public static synchronized void startUpdateDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		if (updateDistanceMatrix == null){
			updateDistanceMatrix = new ArrayList<Long>();
		}
		updateDistanceMatrix.add(System.currentTimeMillis());
	}
	
	public static synchronized void endUpdateDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		Long time = updateDistanceMatrix.get(updateDistanceMatrix.size()-1);
		updateDistanceMatrix.remove(updateDistanceMatrix.size()-1);
		updateDistanceMatrix.add(System.currentTimeMillis() - time);
	}
	
	//restore distance matrix 
	public static synchronized void startRestoreDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		if (restoreDistanceMatrix == null){
			restoreDistanceMatrix = new ArrayList<Long>();
		}
		restoreDistanceMatrix.add(System.currentTimeMillis());
	}
	
	public static synchronized void endRestoreDistanceMatrix(){
		if (!computeStatistics){
			return;
		}
		Long time = restoreDistanceMatrix.get(restoreDistanceMatrix.size()-1);
		restoreDistanceMatrix.remove(restoreDistanceMatrix.size()-1);
		restoreDistanceMatrix.add(System.currentTimeMillis() - time);
	}
	
	//compute MST
	public static synchronized void startComputeMST(){
		if (!computeStatistics){
			return;
		}
		if (computeMST == null){
			computeMST = new ArrayList<Long>();
		}
		computeMST.add(System.currentTimeMillis());
	}
	
	public static synchronized void endComputeMST(){
		if (!computeStatistics){
			return;
		}
		Long time = computeMST.get(computeMST.size()-1);
		computeMST.remove(computeMST.size()-1);
		computeMST.add(System.currentTimeMillis() - time);
	}
	
	
	public static synchronized void printStatistics(){
		if (!computeStatistics){
			return;
		}
		
		System.out.println("Time statistics -----------------------------------");
		/*System.out.println("RemoveOriginalVertices:");
		for (int i=0; i<removeOriginalVertices.size(); i++){
			System.out.println(removeOriginalVertices.get(i) + "ms");
		}*/
		
		/* compute stat */
		long totalTime = System.currentTimeMillis() - startTime;
		double totalTimePercent = ((double)totalTime/100.0);
		
		//Create distance matrix
		/*long createDistanceMatrixTime = 0;
		for (int i=0; i<createDistanceMatrix.size(); i++){
			createDistanceMatrixTime += createDistanceMatrix.get(i);
		}
		System.out.println("Create distance matrix:");
		System.out.println((createDistanceMatrixTime/totalTimePercent) + "% " + createDistanceMatrixTime + "ms" +
				" (" + (createDistanceMatrixTime/createDistanceMatrix.size()) + "ms)" );
		*/
		//compute distance matrix
		long computeDistanceMatrixTime = 0;
		for (int i=0; i<computeDistanceMatrix.size(); i++){
			computeDistanceMatrixTime += computeDistanceMatrix.get(i);
		}
		System.out.println("Compute distance matrix:");
		System.out.println((computeDistanceMatrixTime/totalTimePercent) + "% " + computeDistanceMatrixTime + "ms"+
				" (" + (computeDistanceMatrixTime/computeDistanceMatrix.size()) + "ms)" );
		
		//update distance matrix
		long updateDistanceMatrixTime = 0;
		for (int i=0; i<updateDistanceMatrix.size(); i++){
			updateDistanceMatrixTime += updateDistanceMatrix.get(i);
		}
		System.out.println("Update distance matrix:");
		System.out.println((updateDistanceMatrixTime/totalTimePercent) + "% " + updateDistanceMatrixTime + "ms"+
				" (" + (updateDistanceMatrixTime/updateDistanceMatrix.size()) + "ms)");
		
		//restore distance matrix
		long restoreDistanceMatrixTime = 0;
		for (int i=0; i<restoreDistanceMatrix.size(); i++){
			restoreDistanceMatrixTime += restoreDistanceMatrix.get(i);
		}
		System.out.println("Restore distance matrix:");
		System.out.println((restoreDistanceMatrixTime/totalTimePercent) + "% " + restoreDistanceMatrixTime + "ms"+
				" (" + (restoreDistanceMatrixTime/restoreDistanceMatrix.size()) + "ms)");
		
		//compute NJ alg
		long computeNJAlgTime = 0;
		for (int i=0; i<computeNJAlg.size(); i++){
			computeNJAlgTime += computeNJAlg.get(i);
		}
		System.out.println("Compute NJ alg:");
		System.out.println((computeNJAlgTime/totalTimePercent) + "% " + computeNJAlgTime + "ms"+
				" (" + (computeNJAlgTime/computeNJAlg.size()) + "ms)");
		
		//compute MSTNJ
		long computeMSTNJTime = 0;
		for (int i=0; i<computeMSTNJ.size(); i++){
			computeMSTNJTime += computeMSTNJ.get(i);
		}
		System.out.println("Compute MST NJ alg:");
		System.out.println((computeMSTNJTime/totalTimePercent) + "% " + computeMSTNJTime + "ms"+
				" (" + (computeMSTNJTime/computeMSTNJ.size()) + "ms)");
		
		//compute MST
		long computeMSTTime = 0;
		for (int i=0; i<computeMST.size(); i++){
			computeMSTTime += computeMST.get(i);
		}
		System.out.println("Compute MST:");
		System.out.println((computeMSTTime/totalTimePercent) + "% " + computeMSTTime + "ms"+
				" (" + (computeMSTTime/computeMST.size()) + "ms)");
		
		//compute intermediates
		/*long computeIntermediatesTime = 0;
		for (int i=0; i<computeIntermediates.size(); i++){
			computeIntermediatesTime += computeIntermediates.get(i);
		}
		System.out.println("Compute Intermediates:");
		System.out.println((computeIntermediatesTime/totalTimePercent) + "% " + computeIntermediatesTime + "ms"+
				" (" + (computeIntermediatesTime/computeIntermediates.size()) + "ms)");
		*/
		System.out.println("Other alg:");
		long otherTime = totalTime - ( 
				computeDistanceMatrixTime +
				updateDistanceMatrixTime +
				restoreDistanceMatrixTime +
				computeNJAlgTime +
				computeMSTNJTime +
				computeMSTTime);
		
		System.out.println((otherTime/totalTimePercent) + "% " + otherTime + "ms");
		
		
		/*System.out.println("Deforestation:");
		for (int i=0; i<deforestation.size(); i++){
			System.out.println(deforestation.get(i) + "ms");
		}
		
		System.out.println("Set status ignoring sampling:");
		for (int i=0; i<setStatusIgnoringSampling.size(); i++){
			System.out.println(setStatusIgnoringSampling.get(i) + "ms");
		}
		
		System.out.println("Remove duplicates:");
		for (int i=0; i<removeDuplicates11.size(); i++){
			System.out.println(removeDuplicates11.get(i) + "ms");
		}*/
		
		
		
		 
	}
}
