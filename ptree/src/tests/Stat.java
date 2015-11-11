package tests;

import common.TimeStamp;

public class Stat {

	/* distance matrix*/
	
	private static long computeDM = 0;
	
	private static long updateDM = 0;
	
	private static long restoreDM = 0;
	
	/* NJ algorithm */
	
	private static long algNJ = 0;
	
	/* MST algorithm */
	private static long algMST = 0;
	
	private static long mstStandard = 0;
	
	private static long mstMSTRepair = 0;
	
	/* other */
	private static long totalIterCount = 0;
	
	public static void computeDMFinished(TimeStamp time){
		computeDM += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	
	public static void updateDMFinished(TimeStamp time){
		updateDM += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	
	public static void restoreDMFinished(TimeStamp time){
		restoreDM += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	public static void computeNJFinished(TimeStamp time){
		algNJ += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	public static void computeMSTFinished(TimeStamp time){
		algMST += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	public static void computeMSTStandardFinished(TimeStamp time){
		mstStandard += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	public static void computeMSTRepairFinished(TimeStamp time){
		mstMSTRepair += System.currentTimeMillis() - time.getTimeStampMillis();
	}
	
	public static void enterNextIteration(){
		totalIterCount++;
	}
	
	
	public static String statToString(TimeStamp programStartTimeStamp){
		
		double dmSum =  computeDM + updateDM + restoreDM;
		double totalRuntime = System.currentTimeMillis() - programStartTimeStamp.getTimeStampMillis();
		
		double dmFromTotalRuntime = (dmSum/totalRuntime)*100.0;
		
		double computeDMPercentage = (((double)computeDM)/dmSum)*100.0;
		double updateDMPercentage = (((double)updateDM)/dmSum)*100.0;
		double restoreDMPercentage = (((double)restoreDM)/dmSum)*100.0;
		
		double njFromTotalRuntime = (((double)algNJ)/totalRuntime)*100.0;
		
		double mstFromTotalRuntime = (((double)algMST)/totalRuntime)*100.0;

		double mstTimeTotal = mstStandard + mstMSTRepair;
		double mstTimeStandardPart = (((double)mstStandard)/mstTimeTotal)*100.0;
		double mstTimeRepairPart = (((double)mstMSTRepair)/mstTimeTotal)*100.0;
		
		double otherAlg = 100.0 - dmFromTotalRuntime - njFromTotalRuntime - mstFromTotalRuntime;
		
		return "Stat: \n" + 
		       "ComputeDM: " + dmFromTotalRuntime + "% (compute: " + computeDMPercentage + "% update: " 
		                     + updateDMPercentage + "% restore: " + restoreDMPercentage + ")\n" +
		       "ComputeNJ: " + njFromTotalRuntime + "%\n" +
			   "ComputeMST: " + mstFromTotalRuntime + "% (standard: " + mstTimeStandardPart + "% " +
			   		"repair: " + mstTimeRepairPart + "%)\n" +
			   "Other alg: " + otherAlg + "%" + "\n total iter: " + totalIterCount;
	}
	
}
