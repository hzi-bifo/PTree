package ptree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;

/**
 * To compare the current tree with a new tree to decide which tree will be as the current tree in the next iteration.
 * */
public class TreeComparator {

	private Log log;
	
	/* there are two functions so far */
	private static final int FUNCTION_EXP = 0;
	private static final int FUNCTION_ADAPTIVE = 1;
	
	private boolean useMetropolisAlg;
	private int functionIndex;
	
	private Random random = null;
	
	private double allAcceptedTrees = 0.0;
	private double allNotAcceptedTrees = 0.0;
	
	private double oneRunAcceptedTrees = 0.0;
	private double oneRunNotAcceptedTrees = 0.0;
	
	private int iterCalibrating = 0;
	private int iterRunning = 0;
	
	private double beta = 1.0;
	//private double beta = 0.12039728;
	//private double beta = 0.179697433;
	//private double beta = 0.230258509;
	
	private double desiredRatio;
	
	private final int STATE_CALIBRATING = 0;
	private final int STATE_RUNNING = 1;
	
	private int state = STATE_CALIBRATING;
	
	private List<Integer> bestTrees;
	
	private double treeComparisonDecreaseOfAR;
	private int treeComparisonIterCalibrating;
	private int treeComparisonIterWithConstBeta;
	
	double diffSum = 0;
	double diffSumItems = 0;
	
	/**
	 * @param useMetropolisAlg whether to use the metropolis algorithm
	 * */
	public TreeComparator(Configuration config){
		
		log = LogFactory.getLog(TreeComparator.class);
		
		this.useMetropolisAlg = config.getUseMetropolis();
		this.functionIndex = config.getTreeComparisonFunction();
		this.treeComparisonDecreaseOfAR = config.getTreeComparisonDecreaseOfAR();
		this.treeComparisonIterCalibrating = config.getTreeComparisonIterCalibrating();
		this.treeComparisonIterWithConstBeta = config.getTreeComparisonIterWithConstBeta();
		this.desiredRatio = config.getTreeComparisonStartingAR();
		
		if (useMetropolisAlg){
			random = new Random();
			switch (functionIndex){
			case FUNCTION_EXP:
				
				break;
			case FUNCTION_ADAPTIVE:
				bestTrees = new ArrayList<Integer>();
				break;			
			default:
				log.error("Wrong function index");
			}
		}
	}
	
	
	private void newTreeConsidered(int currentCost, int proposedCost){
		
		diffSum += Math.abs(currentCost - proposedCost);
		diffSumItems++;
		
		//double proposedBeta = 
		
		//System.out.println("Diff avg: " + (diffSum/diffSumItems));
		
		double avg = (diffSum/diffSumItems);
		
		if ((iterRunning > 0) && (iterRunning % this.treeComparisonIterWithConstBeta == 0)){
			this.desiredRatio -= this.treeComparisonDecreaseOfAR;
		} 
		
		if (((iterRunning > 0) && (iterRunning % this.treeComparisonIterWithConstBeta == 0)) || 
				(iterRunning == this.treeComparisonIterCalibrating)){
			
			beta = Math.log1p(1.0/Math.max(0.00001,this.desiredRatio))/avg;
			
			if (iterRunning != this.treeComparisonIterCalibrating){
				diffSum = 0.0;
				diffSumItems = 0;
				oneRunAcceptedTrees = 0.0;
				oneRunNotAcceptedTrees = 0.0;
			}
		}
		
		double recentRatio = (oneRunAcceptedTrees)/(oneRunAcceptedTrees + oneRunNotAcceptedTrees);
		
		iterRunning++;
		//System.out.println("Calibration beta: " + beta + " average: " + avg);
		//System.out.println("Calibration desired ratio: " + this.desiredRatio + " recent ratio: " + recentRatio
		//		+ " overall ratio: " + getOverallAcceptanceRatio());
		
		/*if (functionIndex == FUNCTION_EXP){
			switch (state){
			
			case STATE_CALIBRATING:
				
				if (iterCalibrating >= 100){
					
					// modify beta to keep the acceptance ratio around the desired acceptance ratio 
					double acceptanceRatio = (oneRunAcceptedTrees/(oneRunAcceptedTrees + oneRunNotAcceptedTrees));
					if (acceptanceRatio < (desiredRatio - treeComparisonDecreaseOfAR/2)){
						beta *= 0.75;
					} else { 
						if ((desiredRatio + treeComparisonDecreaseOfAR/2) < acceptanceRatio){
							beta *= 1.333;	
						}
					}
				} 
				
				if (iterCalibrating >= this.treeComparisonIterCalibrating){
					state = STATE_RUNNING;
					iterCalibrating = 0;
					iterRunning = 0;
				} else {
					iterCalibrating++;
				}				
				
				
				break;
			case STATE_RUNNING:
				
				if (iterRunning >= this.treeComparisonIterWithConstBeta){
					state = STATE_CALIBRATING;
					System.out.println("Desired ratio: " + desiredRatio);
					System.out.println("Avg. run acc. ratio :" + (oneRunAcceptedTrees/(oneRunAcceptedTrees + oneRunNotAcceptedTrees)));
					iterCalibrating = 0;
					iterRunning = 0;
					oneRunAcceptedTrees = 0.0;
					oneRunNotAcceptedTrees = 0.0;
					desiredRatio -= this.treeComparisonDecreaseOfAR;
					desiredRatio = Math.max(0, desiredRatio);
				} else {
					iterRunning++;
				}
				
				break;
			default:
				log.error("not defined state");
			}
			
		}*/
		
	}
	
	
	/**
	 * Should I take the new tree even if it is worse than the best tree? 
	 * */
	public boolean takeNewTree(Tree currentTree, Tree newTree, Tree bestTree, int iterWithoutImprovement){
		
		//test------------
		/*if (currentTree.getRealFitchCost() < newTree.getRealFitchCost()){
			
			if (currentTree.getTreeCost() >= newTree.getTreeCost()){
				System.err.println("COST TEST: tree cost does not correspond: (" + currentTree.getTreeCost() +
						" >= " + newTree.getTreeCost() + ") but right is (" + currentTree.getRealFitchCost() 
						+ " < " + newTree.getRealFitchCost() + ")");
			}
			if (currentTree.getPartialFitchCost() >= newTree.getPartialFitchCost()){
				System.err.println("COST TEST: partial fitch cost does not correspond: (" + currentTree.getPartialFitchCost() +
						" >= " + newTree.getPartialFitchCost() + ") but right is (" + currentTree.getRealFitchCost() 
						+ " < " + newTree.getRealFitchCost() + ")");
			}
			
		} else {
			
			if (currentTree.getRealFitchCost() > newTree.getRealFitchCost()){
				if (currentTree.getTreeCost() <= newTree.getTreeCost()){
					System.err.println("COST TEST: tree cost does not correspond: (" + currentTree.getTreeCost() +
							" <= " + newTree.getTreeCost() + ") but right is (" + currentTree.getRealFitchCost() 
							+ " > " + newTree.getRealFitchCost() + ")");
				}
				if (currentTree.getPartialFitchCost() <= newTree.getPartialFitchCost()){
					System.err.println("COST TEST: partial fitch cost does not correspond: (" + currentTree.getPartialFitchCost() +
							" <= " + newTree.getPartialFitchCost() + ") but right is (" + currentTree.getRealFitchCost() 
							+ " > " + newTree.getRealFitchCost() + ")");
				}
			}
			
		}*/
		//test------------
		
		/* use the metropolis algorithm */
		if (useMetropolisAlg){
			
			double u = random.nextDouble(); //get u (uniformly) from <0.0, 1.0>
			double ratio;
			
			switch (functionIndex){
			case FUNCTION_EXP: 
				
				//System.out.println("acceptance ratio: " + getAcceptanceRatio());
				
				ratio = Math.exp( (-1) * (newTree.getCost() - currentTree.getCost()) * (beta) );
		
				break;
			case FUNCTION_ADAPTIVE: 
				
				//adaptive function
				if (iterWithoutImprovement == 0){
					ratio = 0.0;
				} else {
					
					double f1 = Math.max(bestTrees.get(
							Math.max(bestTrees.size()-1-iterWithoutImprovement,0)) - newTree.getCost(), 0);
					
					double f2 = Math.max(bestTrees.get(
							Math.max(bestTrees.size()-1-iterWithoutImprovement,0)) - bestTrees.get(bestTrees.size()-1), 1);
					
					ratio = f1/f2;
				}
				break;
			default: log.error("Wrong function index");
				ratio = 0.0;
			}
		
			if (ratio > u){
				return true;
			} else {
				return false;
			}
		
		} else {
			return false;
		}
	}
	
	
	/**
	 * Call this method if a new tree was accepted.
	 * */
	public void newTreeAccepted(int currentCost, int proposedCost){
		newTreeConsidered(currentCost, proposedCost);
		allAcceptedTrees++;
		oneRunAcceptedTrees++;
	}
	
	
	/**
	 * Call this method if a new tree was NOT accepted.
	 * */
	public void newTreeNotAccepted(int currentCost, int proposedCost){
		newTreeConsidered(currentCost, proposedCost);
		allNotAcceptedTrees++;
		oneRunNotAcceptedTrees++;
	}
	
	
	/**
	 * Get the acceptance ratio (#of accepted trees / #of all generated trees).
	 * */
	public double getOverallAcceptanceRatio(){
		return allAcceptedTrees/(allAcceptedTrees + allNotAcceptedTrees);
	}
	
	/**
	 * Call this method if a better tree was found.
	 * */
	public void setBestTree(int costBestTree){
		if (functionIndex == FUNCTION_ADAPTIVE){
			bestTrees.add(costBestTree);
		}
	}
}
