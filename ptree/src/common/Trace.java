package common;

public class Trace {
	
	
	public static boolean printingOn = false;
	
	public static int printingMode = 0;
	
	public static final int MODE_ALL = 0;
	public static final int MODE_ALL_COSTS = 1;
	public static final int MODE_ACCEPTED_COSTS = 2;
	public static final int MODE_BETTER_COSTS = 3;
	public static final int MODE_CURRENT_COSTS_ONLY = 4;
	public static final int MODE_ACCEPTED_AND_CURRENT_COSTS = 5;
	public static final int MODE_ACCEPTED_CURRENT_AND_PROPOSED_COSTS = 6;
	
	
	public static TimeStamp getTimeStamp(){
		if (printingOn){
			return TimeStamp.getTimeStamp();
		} else {
			return null;
		}
	}
	
	
	public static TimeStamp getTimeStampAnyway(){
		return TimeStamp.getTimeStamp();
	}
	
	public static void print(String msg, TimeStamp ts){
		if ((printingOn) && (printingMode == MODE_ALL)){
			System.out.println(msg + " " + (System.currentTimeMillis() - ts.getTimeStampMillis()) + "ms");
		}
	}
	
	public static void print(String msg){
		if ((printingOn) && (printingMode == MODE_ALL)){
			System.out.println(msg);
		}
	}
	
	public static void printBetterCost(String msg){
		if (printingOn && (printingMode != MODE_CURRENT_COSTS_ONLY)){
			System.out.println(msg);
		}
	}
	
	public static void printWorseCostAccepted(String msg){
		if (printingOn && (printingMode != MODE_CURRENT_COSTS_ONLY) && (printingMode != MODE_BETTER_COSTS)){
			System.out.println(msg);
		}
	} 
	
	public static void printWorseCostNotAccepted(String msg){
		if (printingOn && (printingMode != MODE_CURRENT_COSTS_ONLY) && (printingMode != MODE_BETTER_COSTS) 
				&& (printingMode != MODE_ACCEPTED_COSTS) && (printingMode != MODE_ACCEPTED_AND_CURRENT_COSTS)){
			System.out.println(msg);
		}
	} 
	
	public static void printCurrentCost(String msg){
		if (printingOn && ((printingMode == MODE_CURRENT_COSTS_ONLY) || (printingMode == MODE_ACCEPTED_AND_CURRENT_COSTS) 
				|| (printingMode == MODE_ACCEPTED_CURRENT_AND_PROPOSED_COSTS))){
			System.out.println(msg);
		}
	}
	
	public static void printProposedCost(String msg){
		if (printingOn && (printingMode == MODE_ACCEPTED_CURRENT_AND_PROPOSED_COSTS)){
			System.out.println(msg);
		}
	}
	
	public static void printAnyCost(String msg){
		if (printingOn && ((printingMode == MODE_ALL) || (printingMode == MODE_ALL_COSTS))){
			System.out.println(msg);
		}
	} 
}
