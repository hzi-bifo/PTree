package ptree;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import common.Configuration;
import common.TimeStamp;
import common.Trace;

import io.Output;
import io.Reader;
import io.Writer;

import mst.MST;
import mst.prim.MemoryManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tests.Stat;
import tests.StatInt;

import dmatrix.DMatrix;
import dmatrix.Matrix;
import fitchcost.SankoffAlg;


/**
 * Main class of the algorithm.
 * */
public class PTree {

	private Log log;
	
	private Configuration config;
	private File fileSeq;
	private File fileOutput;
	

	/**
	 * Constructor.
	 * */
	public PTree(Configuration config, File fileSeq, File fileOutput){
		
		this.config = config;
		this.fileSeq = fileSeq;
		this.fileOutput = fileOutput;
		log = LogFactory.getLog(PTree.class);
	}
	
	
	/**
	 * Run PTree (MST version).
	 * */
	public void runMST(){
		
		//log.info("run MST");
		//Statistics.startStatistics();
		
		try {	
		
			Reader reader = new Reader(this.config);
			Writer writer = new Writer();
			
			if (!reader.open(this.fileSeq) || !writer.open(this.fileOutput)){
				log.warn("Cannot open a file for reading or writing.");
				return;
			}
			
			List<Vertex> vertices;
			
			
			/* skip first data sets according to config */
			int skipNum = config.getSkipDatasetNum();
			for (int i=0; i<skipNum; i++){
				reader.getNextInputSet();
			}
			if (skipNum > 0){
				log.info("First " + skipNum + " data sets has been skipped!");
			}
			
			/* an array of threads that computes the input in parallel */
			ThreadMST thread[] = new ThreadMST[config.getThreadCount()];
			
			/* the number of active threads */
			int activThreadCount;
			
			for (;;){
			
				/* create threads */
				activThreadCount = 0;
				for (int i=0; i< thread.length; i++){
					vertices = reader.getNextInputSet();
					if (vertices != null){
						thread[i] = new ThreadMST(vertices);
						thread[i].start();
						activThreadCount++;
						Trace.print("Thread for dataset (" + (i+1) + ") started");
					} else {
						thread[i] = null;
					}
				}
				
				if (activThreadCount == 0){
					break;
				}
				
				/* joint threads & write results */
				for (int i=0; i<thread.length; i++){
					if (thread[i] != null){
						try {
							thread[i].join();
							writer.add(thread[i].getTree());
						} catch (Exception ex) {
							log.error("The current thread (" + thread[i].getName() + ") was interrupted..", ex);
						}
					}
				}
				
			}
			
			reader.close();
			writer.close();
			} catch (Exception ex){
				log.error("Exception..", ex);
			}
			
		//Statistics.printStatistics();
		/*double jar = ((double)MST.timeJarnik)/((double)MST.timeJarnikCount);
		double bor = ((double)MST.timeBoruvka)/((double)MST.timeBoruvkaCount);	
		System.out.println("time jarnik : " + jar + "ms");
		System.out.println("time boruvka: " + bor + "ms");
		System.out.println("jarnik/boruvka: " + (jar/bor) + " X");
		*/
		 
			
		//log.info("end MST");
	}
	
	
	/* 
	 * One MST thread.
	 * */
	class ThreadMST extends Thread {
		
		private List<Vertex> vertices;
		
		private ThreadMST(List<Vertex> vertices) {
            this.vertices = vertices;
        }

		public void run(){
			
			try {
				
				/* run normal MST without subsampling */
				//runMST();
				
				/* run the MST with subsampling */
				runMSTSubsampling();
				
			} catch (Exception ex){
				log.error("An exception occured..",ex);
			}
		}
		
		
		/**
		 * The PTree (MST version) with subsampling.
		 * */
		private void runMSTSubsampling(){
	
			/* the input is in the variable "vertices" */
			
			PhyloTreeTest pTTest = null;
			TimeStamp startTS = Trace.getTimeStampAnyway();
			StatInt stat = new StatInt();
			
			/* initialize the tree test */
			if (config.getTestOutputTreeStructure()){
				pTTest = new PhyloTreeTest(vertices);
			}
			
			/* test of DNA character representation */
        	if (config.getTestDnaCharRepresentation()){
        		TestDnaChars.testDnaCharRepresentation(config, vertices);
        	}
        	
        	/* initialize the Memory- and Mutation- Manager */
        	MemoryManager mem = new MemoryManager();
        	MutationManager mm = new MutationManager(config, false);
        	Random rand = new Random();//set the random seed
        	
        	/* sites that are the same in all DNA`s will be filtered out 
        	 * (but not for Jukes-Cantor and Kimura corrections) *///!!!!!!!!!!!!!!
        	DNAPreprocessor dPrep = null;
        	//if (config.getNjCorrection() == Configuration.NO_CORRECTION){//!!!!!!!
        	dPrep = new DNAPreprocessor(vertices);
        	//}
        	
        	
        	/* remove duplicate vertices in terms of the same DNA sequence, first occurrences remain (hash based distinct) */
			ArrayList<Vertex> duplicateOriginalVertices = PTreeMethods.removeDuplicateVertices1(vertices);
			
			/* compute initial tree */
			Tree currentTree;
			Tree newTree;
			Tree bestTree;
			
			List<Vertex> temAltTreeVertices = new ArrayList<Vertex>(vertices.size());
			
			if (config.getInitTreeAsNJ()){
				/* compute init tree as NJ */
				currentTree = NJTree.getNJTree(vertices, dPrep, config);
				Trace.printBetterCost("Initial tree cost (NJ): " + currentTree.getCost());
			} else {
				/* compute a tree without masking */
				currentTree = computePartialTree(vertices, mem, rand, stat, null, null, null, dPrep);
				Trace.printBetterCost("Initial tree cost (min tree): " + currentTree.getCost());
			}
			Trace.printProposedCost("Proposed tree: " + currentTree.getCost());
			Trace.printCurrentCost("Current tree: " + currentTree.getCost());
			
			bestTree = currentTree;
			
			TreeComparator treeComparator = new TreeComparator(config);
			
			/* compute alternative init tree (1) */
			if (Math.abs(config.getDeleteIntCoef()) > 0.0001){
				
				int burnInIter = config.getInitTreeBurnInIter();
				int additionalMaxIter = config.getInitTreeAdditionalMaxIter();
				
				for (int iterCounter=0, iterCounterSinceImprovement = 0; 
						(iterCounter < burnInIter) || (iterCounterSinceImprovement < additionalMaxIter);
						iterCounter++, iterCounterSinceImprovement++){
					//System.out.println("iterCounter: " + iterCounter + " iterCounterSinceImprovement: " +
					//	iterCounterSinceImprovement);
					
					temAltTreeVertices.clear();
					temAltTreeVertices.addAll(currentTree.getOriginalVertices());
					temAltTreeVertices.addAll(PTreeMethods.deleteElements(currentTree.getIntermediateVertices(), 
						config.getDeleteIntCoef(), rand));
					PTreeMethods.deforestation(temAltTreeVertices);
					newTree = computePartialTree(temAltTreeVertices, mem, rand, stat, null, currentTree, null, dPrep);
					Trace.printProposedCost("Proposed tree: " + newTree.getCost());
					
					if (bestTree.getCost() > newTree.getCost()){
						//System.err.println("Alternative tree has a better cost (1): loop:" + initIter + " (" 
						//		+ (treeMin.getCost()-treeAlt.getCost()) + ") " + treeAlt.getCost());
						treeComparator.newTreeAccepted(currentTree.getCost(), newTree.getCost());
						currentTree = newTree;
						bestTree = newTree;
						treeComparator.setBestTree(bestTree.getCost());
						Trace.printBetterCost("New tree has a better cost: " + newTree.getCost());
						iterCounterSinceImprovement = -1;
					} else {
						if (currentTree.getCost() >= newTree.getCost()){
							treeComparator.newTreeAccepted(currentTree.getCost(), newTree.getCost());
							currentTree = newTree;
							Trace.printWorseCostAccepted("Better cost (or eq) than current tree: " + currentTree.getCost());
						} else {
							if (treeComparator.takeNewTree(currentTree, newTree, bestTree, iterCounterSinceImprovement)){
								treeComparator.newTreeAccepted(currentTree.getCost(), newTree.getCost());
								currentTree = newTree;
								Trace.printWorseCostAccepted("Worse tree accepted as the current tree: " + currentTree.getCost());
							} else {
								Trace.printWorseCostNotAccepted("Proposed tree has a worse cost: " + newTree.getCost());
								treeComparator.newTreeNotAccepted(currentTree.getCost(), newTree.getCost());
							}
						}
					}
					Trace.printCurrentCost("Current tree: " + currentTree.getCost());
				}
			}

			Trace.print("Compute Initial Tree finished - cost: " + currentTree.getCost());
			
			/* initialize the sampling manager */
			SamplingManager sm = new SamplingManager(currentTree.getOriginalVertices(), config);
			
			Sampling mask;
			Dna dna;
			
			/* the vertices that will be considered as original vertices in the further computation */
			List<Vertex> originals = currentTree.getOriginalVerticesCopyList();
			
			/* set secondary DNA of each vertex to its original DNA (backup the DNA) */
			for (Vertex vertex : currentTree.getAllVertices()){
				dna = vertex.getDna().clone();
				vertex.setSecDna(dna);
			}
			
			Tree semiRandomTree;
			Tree treeComb;
			
			/* the vertices of the combined tree */
			List<Vertex> treeCombVertices = new ArrayList<Vertex>(originals.size());
			
			/* maximum number of iterations */
			int maxIterCount = config.getSamplingIterCount();
			
			boolean njAsTempTreeAlreadyTried = config.getTryNJAsFirstTempTree()?false:true;
			
			int iterNotAccepted = 0;
			
			/* If there hasn`t been better tree in maxIterCount then stop. */
			for (int iterCount = 0; iterCount < maxIterCount; iterCount++){
				
				Stat.enterNextIteration();
				
				/* compute temp tree */
				if (!njAsTempTreeAlreadyTried){
					
					/* try to infer intermediates from the NJ tree */
					for (Vertex vertex : originals){
						dna = vertex.getSecDna().clone();
						vertex.setDna(dna);
					}
					PTreeMethods.deforestation(originals);
					semiRandomTree = NJTree.getNJTree(originals, dPrep, config);
					/* set sec dna of the intermediates */
					for (Vertex v : semiRandomTree.getIntermediateVertices()){
						dna = v.getDna().clone();
						v.setSecDna(dna);
					}
					
					Trace.printAnyCost("Compute partial tree (NJ) finished (iter: " + iterCount + ") " + semiRandomTree.getCost());
					njAsTempTreeAlreadyTried = true;
					mask = null;
				} else {
					
					/* get some random sampling mask */
					mask = sm.getRandomSampling(config.getMaskingSitesCountMin(), config.getMaskingSitesCountMax());
					
					/* mask original vertices according to the sampling mask */
					for (Vertex vertex : originals){
						dna = vertex.getSecDna().clone();
						dna.maskPositions(mask.getSampling(), config.getMaskChar());
						vertex.setDna(dna);
					}
					
					PTreeMethods.deforestation(originals);
					
					/* computes a new tree with masked positions */
					semiRandomTree = computePartialTree(originals, mem, rand, stat, mask, currentTree, null, dPrep);
					Trace.printAnyCost("Compute partial tree finished (iter: " + iterCount + ") " + semiRandomTree.getCost());	
				}
				
				Trace.printProposedCost("Proposed tree: " + semiRandomTree.getCost());
				Trace.printCurrentCost("Current tree: " + currentTree.getCost());
				
				/* compute alternative tree (2) */
				if (Math.abs(config.getDeleteIntCoef()) > 0.0001){
					int maxIter = config.getTempTreeMaxIterCount();
					int iter = config.getTempTreeIterCount();
					int iterCounter=0;
					for (int maxIterCounter=0; (maxIterCounter<maxIter) && (iterCounter<iter); maxIterCounter++){
						
						temAltTreeVertices.clear();
						temAltTreeVertices.addAll(semiRandomTree.getOriginalVertices());
						temAltTreeVertices.addAll(PTreeMethods.deleteElements(semiRandomTree.getIntermediateVertices(), 
								config.getDeleteIntCoef(), rand));
						PTreeMethods.deforestation(temAltTreeVertices);
						newTree = computePartialTree(temAltTreeVertices, mem, rand, stat, mask, semiRandomTree, null, dPrep);
						if (semiRandomTree.getCost() > newTree.getCost()){
							//System.err.println("Alternative tree has a better cost (1): loop:" + initIter + " (" 
							//		+ (treeTemp.getCost()-treeAlt.getCost()) + ") " + treeAlt.getCost());
							semiRandomTree = newTree;
							Trace.printBetterCost("Alternative tree has a better cost: " + newTree.getCost());
							iterCounter = 0;
						} else {
							Trace.printAnyCost("Alternative tree has a worse cost: " + newTree.getCost());
							iterCounter++;
						}
						Trace.printProposedCost("Proposed tree: " + newTree.getCost());
						Trace.printCurrentCost("Current tree: " + currentTree.getCost());
					}
				}
				
				originals.clear();
				originals = semiRandomTree.getOriginalVertices();
				
				/* compute combined tree */
				if (config.getComputeCombinedTree()){
				
					/* construct combined tree */
					treeCombVertices.clear();
					treeCombVertices.addAll(originals);
					treeCombVertices.addAll(currentTree.getIntermediateVerticesStatus0());
					treeCombVertices.addAll(semiRandomTree.getIntermediateVerticesStatus0());
					
					/* get some random mask */
					if (config.getComputeCombinedTreeWithMasking()){
						mask = sm.getRandomSampling(config.getMaskingSitesCountMin(), config.getMaskingSitesCountMax());
					
						/* mask positions of the combined tree */
						for (Vertex vertex : treeCombVertices){
							dna = vertex.getSecDna().clone();
							dna.maskPositions(mask.getSampling(), config.getMaskChar());
							vertex.setDna(dna);
						}
					} else {
						for (Vertex vertex : treeCombVertices){
							dna = vertex.getSecDna().clone();
							vertex.setDna(dna);
						}
						mask = null;//mask = sm.getRandomSampling(0, 0);//
					}
					
					PTreeMethods.deforestation(treeCombVertices);
					
					/* compute combined tree (with masked positions) */
					treeComb = computePartialTree(treeCombVertices, mem, rand, stat, mask, currentTree, semiRandomTree, dPrep);
					
					Trace.printAnyCost("Compute combined tree finished (iter: " + iterCount + ") " + treeComb.getCost());	
					Trace.printProposedCost("Proposed tree: " + treeComb.getCost());
					Trace.printCurrentCost("Current tree: " + currentTree.getCost());
					
					/* compute alternative tree (3) */
					if (Math.abs(config.getDeleteIntCoef()) > 0.0001){
							int maxIter = config.getCombinedTreeMaxIterCount();
							int iter = config.getCombinedTreeIterCount();
							int iterCounter = 0;
							for (int maxIterCounter=0; (maxIterCounter<maxIter) && (iterCounter<iter); maxIterCounter++){
								
								temAltTreeVertices.clear();
								temAltTreeVertices.addAll(treeComb.getOriginalVertices());
								temAltTreeVertices.addAll(PTreeMethods.deleteElements(treeComb.getIntermediateVertices(), 
										config.getDeleteIntCoef(), rand));
								PTreeMethods.deforestation(temAltTreeVertices);
								newTree = computePartialTree(temAltTreeVertices, mem, rand, stat, mask, treeComb, null, dPrep);
								if (treeComb.getCost() > newTree.getCost()){
									treeComb = newTree;
									Trace.printAnyCost("Alternative tree has a better cost: " + newTree.getCost());
									iterCounter = 0;
								} else {
									Trace.printAnyCost("Alternative tree has a worse cost: " + newTree.getCost());
									iterCounter++;
								}
								Trace.printProposedCost("Proposed tree: " + newTree.getCost());
								Trace.printCurrentCost("Current tree: " + currentTree.getCost());		
						}
					}
					
				} else {
					/* Tree combined is not computed */
					treeComb = semiRandomTree;
				}
				
				/* if the "combined tree" is better than "min tree" (minimum computed so far), 
				 * we will consider just computed "combined tree" as a new "min tree"  
				 * */
				
				Trace.printProposedCost("Proposed tree: " + treeComb.getCost());
				if (bestTree.getCost() > treeComb.getCost()){
					treeComparator.newTreeAccepted(currentTree.getCost(), treeComb.getCost());
					currentTree = treeComb;
					bestTree = treeComb;
					treeComparator.setBestTree(bestTree.getCost());
					iterCount = 0;//reset the iteration count!!!
					iterNotAccepted = 0;
					Trace.printBetterCost("Combined tree has better cost: " + treeComb.getCost());
				} else {
					
					if (currentTree.getCost() >= treeComb.getCost()){
						treeComparator.newTreeAccepted(currentTree.getCost(), treeComb.getCost());
						currentTree = treeComb;
						Trace.printWorseCostAccepted("Better (or eq.) cost than current tree: " + currentTree.getCost());
					} else {
						if (treeComparator.takeNewTree(currentTree, treeComb, bestTree, iterNotAccepted)){
							treeComparator.newTreeAccepted(currentTree.getCost(), treeComb.getCost());
							currentTree = treeComb;
							Trace.printWorseCostAccepted("Worse tree accepted as the current tree: " + currentTree.getCost());
						} else {
							treeComparator.newTreeNotAccepted(currentTree.getCost(), treeComb.getCost());
							Trace.printWorseCostNotAccepted("Proposed tree has a worse cost: " + treeComb.getCost());
							iterNotAccepted++;
						}	
					}
					iterCount++;	
				}
				Trace.printCurrentCost("Current tree: " + currentTree.getCost());
			}
			
			/* final step */
			Trace.print("Final step started.");
	
			//System.out.println("Acceptance ratio: " + treeComparator.getOverallAcceptanceRatio());			
			
			currentTree = bestTree;
			
			vertices.clear();
			vertices.addAll(currentTree.getOriginalVertices());
			vertices.addAll(currentTree.getIntermediateVertices());
			
			/* set vertices` DNAs to its original DNAs */
			for (Vertex vertex : vertices){
				dna = vertex.getSecDna();
				if (dna != null){
					vertex.setDna(dna);
				}
			}
			
			/* restore gaps */
			currentTree.restoreGapSubstitutions();
			
			Matrix dMatrix = null;
			
			//test
			//SankoffAlg sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(0): " + sa.getCost());
			
			for (;;){
				
				PTreeMethods.deforestation(vertices);
				
				/* compute the distance matrix */
				
				if (dMatrix == null){
					dMatrix = new Matrix(vertices, dPrep, config);
					dMatrix.computeDistanceMatrix(null, null, null);
				} else {
					dMatrix.restoreDistanceMatrix();
				}
				
				TimeStamp ts = TimeStamp.getTimeStamp();
				/* compute MST, root the graph, add mutations */
				mst.MST.computeMST(vertices, dMatrix, mm, mem, config);
				Stat.computeMSTStandardFinished(ts);
				
				/* delete intermediates with in-degree==1 & out-degree==1 */	
				if (!PTreeMethods.deleteIntermediateDegree2(vertices)){
					break;
				} 
			}
			
			//test
			//sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(1): " + sa.getCost());
			
			/* handle original duplicate vertices */  //to have gaps in originals !!!
			GapHandler.restoreGapsInAllOriginals(currentTree.getGapHandler());
			
			//Vertex vertexO = null;
			//Vertex vertexD = null;
			boolean found;
			
			for (Vertex vertexD : duplicateOriginalVertices){
				
				found = false;
				for (Vertex vertexO : vertices){
					
					if (vertexD.getDna().equals(vertexO.getDna())){
						if (!vertexO.isOriginal()){
							log.error("Equal to not original!!! " + vertexO.getName());
							System.exit(-1);
						} 
							
						/* vertex D will be a child of vertex O */
						vertexO.setOutcomingEdge(vertexD);
						vertexD.setIncomingEdge(vertexO);
						vertexD.setMutations(new Mutations());
						found = true;
						break;
					}
				}
				
				if (!found){
					log.error("Haven`t found corresponding vertex for " + vertexD.getName());
				}
			}
			
			//test
			//sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(2): " + sa.getCost());
			
			vertices.addAll(duplicateOriginalVertices);
			duplicateOriginalVertices.clear();
			
			/* remove intermediates with degree 1 status 0 or 2*/
			PTreeMethods.removeIntermediatesDegree1NodeStatus0or2(vertices);
			
			//test
			//sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(3): " + sa.getCost());
			
			dMatrix.clear();
			mm.clear();
			mem.clear();
			System.gc();
			
			/* move all internal original sequences to leaf nodes, make leaf copy for each internal original node */
			PTreeMethods.moveOriginalsToLeaves(vertices, true);
			
			//test
			//sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(4): " + sa.getCost());
			
			/* cancel gaps in internals */
			GapHandler gapHandler = new GapHandler(config, null);
			gapHandler.cancelGapsInInternals(vertices, true);
			gapHandler.repairMutationSets(vertices.get(0));
			
			/* restore all DNAs to its original length */
			if ((dPrep != null) && (dPrep.used())){
				dPrep.restoreDNA(vertices);
				dPrep.restoreMutations(vertices);
			}
			
			//test
			//sa = new SankoffAlg(null, (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar()), vertices.get(0));
			//System.out.println("final step cost(5): " + sa.getCost());
			
			/* compute the fitch cost */
			Integer fitchCost = null;
			if (config.getComputeFitchCost()){
				TimeStamp ts = Trace.getTimeStamp();
				byte[] chars = null;
				/* set the ignore character: gap or a "mask char" that is not contained in a sequence at this point */
				byte ignoreChar = (!config.getCountGapAsChange())?(config.getGapChar()):(config.getMaskChar());
				//long timeS = System.currentTimeMillis();//!!!
				SankoffAlg sankoffAlg = new SankoffAlg(chars, ignoreChar, vertices.get(0));
				
				/* compute fitch cost */
				sankoffAlg.computeCosts();
				fitchCost = sankoffAlg.getCost();
				//log.info("cost compute: " + (System.currentTimeMillis() - timeS));//!!!
				//timeS = System.currentTimeMillis();//!!!
				/* reconstruct the tree according to the fitch cost */
				if (config.getReconstructOutputTree()){	
					sankoffAlg.assignAndSetSequences();
					PTreeMethods.updateMutations(vertices.get(0), config);
					//log.info("tree reconstruct: " + (System.currentTimeMillis() - timeS));//!!!
				}
				
				sankoffAlg.clear();
				Trace.print("Sankoff alg. finished (fitch cost: " + fitchCost + ")", ts);
			}
			
			/* test the output tree */
			if (config.getTestOutputTreeStructure() && pTTest != null){
				if (!pTTest.checkTree(vertices.get(0), config)){
					log.error("Wrong tree..");
				}
				pTTest.clear();
			}

			PTreeMethods.printReport(vertices, startTS, fitchCost);
			//log.info("\nintermediates processed: " + stat.getProcessedIntermediates() + "\n" 
			//+ "speed: " + " " + ((double)stat.getProcessedIntermediates()/
			//		(((double)System.currentTimeMillis() - (double)startTS.getTimeStampMillis())/1000.0)) 
			//		+ " processed intermediates/sec");
			//
			//log.info(Stat.statToString(startTS));
        }
		
		
		/**
		 * Compute the partial tree as a subroutine of {@link #runMSTSubsampling()}.
		 * 
		 * @param currentSampling the current sampling
		 * @param tree1 a tree or null
		 * @param tree2 a tree or null
		 * 
		 * @throws UnsupportedEncodingException 
		 * */
		private Tree computePartialTree(List<Vertex> verticesP, MemoryManager mem, Random rand, StatInt stat,
				Sampling currentSampling, Tree tree1, Tree tree2, DNAPreprocessor dPrep) {
 
			TimeStamp ts;
			
			List<Vertex> allInputVertices = null;
			
			/* ensure that two sequences with gaps are considered equal if the # of point mutations is 0 */
			if (!config.getCountGapAsChange()){// !!!
				allInputVertices = new ArrayList<Vertex>(verticesP);
				for (Vertex v : allInputVertices){
					v.getDna().setGapIsChange(false);
					v.getDna().setGapChar(config.getGapChar());
				} 
			}

			/* remove duplicate vertices in terms of the same DNA sequence, first occurrence remains (hash based distinct) */
			List<Vertex> duplicateOriginalVertices = new ArrayList<Vertex>();
			
			@SuppressWarnings("unused")
			ArrayList<Vertex> duplicateVertices = PTreeMethods.removeDuplicateVerticesStoreOriginals(verticesP, duplicateOriginalVertices);

			int maxIntProcessedAtOnce = config.getIntMaxProcess();//get rid of this
			
			/* compute distance matrix */
			
			ts = Trace.getTimeStamp();			
			Matrix dMatrix = new Matrix(verticesP,dPrep, config);
			dMatrix.computeDistanceMatrix(currentSampling, tree1, tree2);
			Trace.print("Compute DM (" + verticesP.size() + ") finished", ts);
			
			//TEST
			/*Matrix dMatrix0 = new Matrix(verticesP, config);
			dMatrix0.computeDistanceMatrix(null,null,null);
			if (!Matrix.equal(dMatrix, dMatrix0)){
				log.error("Matrices are not equal! (0)");   
				System.exit(-1);
			} */
			
			/* init gap handler & mutation manager*/
			
			GapHandler gapHandler = new GapHandler(config, dMatrix);
			MutationManager mm = new MutationManager(config, !gapHandler.getIsActive());
			
			/* get the NJ matrix */
			
			ts = Trace.getTimeStamp();
			DMatrix njMatrix = dMatrix.getNJMatrix(config);
			Trace.print("Compute NJ (" + verticesP.size() + ") finished", ts);
			
			/* compute MST, root the graph, add mutations */
			
			ts = Trace.getTimeStamp();
			TimeStamp ts1 = TimeStamp.getTimeStamp();
			MST.computeMST(verticesP, njMatrix, mm, mem, config);
			Stat.computeMSTStandardFinished(ts1);
			Trace.print("Compute first MST (" + verticesP.size() + ") finished", ts);
			
			/* cancel gaps */
			
			gapHandler.cancelGapsInInternals(verticesP, false);
			
			//TEST
			/*Matrix dMatrixTemp1 = new Matrix(verticesP, config);  
			dMatrixTemp1.computeDistanceMatrix(null, null, null); 
			if (!Matrix.equal(dMatrixTemp1, dMatrix)){            
				log.error("Matrices are not equal! (1)");         
				System.exit(-1);                                  
			} else {
				System.out.println("gap canceled (1)");
			}*/
			
			List<Vertex> newIntermediates;
			List<Vertex> newIntStore;
			List<Vertex> newIntTemp = new ArrayList<Vertex>();
			
			/* infer new intermediates */
			Trace.print("Inferring intermediates started");

			boolean vertexRemoved;
			List<Vertex> removedVertices = null;
			
			int lastIntCount = Integer.MAX_VALUE;
			Set<Integer> hashSet = new HashSet<Integer>();
			Set<Dna> dnaSet = new HashSet<Dna>();
			boolean corresponds = true;
			Intermediates intGen = new Intermediates(config, mem);// get rid of it?
			
			for (long loop = 0;;loop++){
				 
				/* infer new intermediates */
				newIntermediates = intGen.getIntermediatesFromRepeatedMutations(verticesP.get(0), verticesP.size());
				stat.addIntermediates(newIntermediates.size());
				
				/* did I infer the same intermediates twice in a row */
				
				/* did I infer twice the same number of intermediates in a row */
				if ((!newIntermediates.isEmpty()) && (newIntermediates.size() == lastIntCount)){
					
					/* hash codes are not known yet */
					if (hashSet.isEmpty()){
						for (Vertex vertex : newIntermediates){
							hashSet.add(vertex.getDna().hashCode());
						}
					} else {
						/* some hash codes are already known */
						corresponds = true;
						for (Vertex vertex : newIntermediates){
							if (!hashSet.contains(vertex.getDna().hashCode())){
								corresponds = false;
								break;
							}
						}
							
						/* hash codes corresponds */
						if (corresponds){
					
							/* the DNA set is not stored yet */
							if (dnaSet.isEmpty()){
								for (Vertex vertex : newIntermediates){
									dnaSet.add(vertex.getDna().clone());
								}
							} else {
								/* the DNA set was already created */
								
								/* test whether DNAs corresponds */
								corresponds = true;
								for (Vertex vertex : newIntermediates){
									if (!dnaSet.contains(vertex.getDna())){
										corresponds = false;
										break;
									}
								}
								
								/* break the main (inferring) loop */
								if (corresponds){
									log.warn("The same vertices twice inferred and deleted, loop break");
									break;//break the main for loop
										
								} else {
									/* the DNA sets doesn`t correspond (hash codes corresponds) */
									dnaSet.clear();
									hashSet.clear();
								}
							}
								
						} else {
							/* the hash set does not correspond */
							hashSet.clear();//
							dnaSet.clear();
						}		
					}	
				} else {
					/* the number of vertices does not correspond */
					hashSet.clear();//
					dnaSet.clear();
				}
				if (loop > 50){
					log.warn("Too many loops:" + loop + " (maybe infinite loop), loop break");
					break;
				}
				
				lastIntCount = newIntermediates.size();
				
				Trace.print("Loop: " + loop + ", tree size: " + verticesP.size() +  ", new intermediates: " + newIntermediates.size());
				
				/* if no intermediate found, stop */
				if (newIntermediates.isEmpty()){
					break;
				}
				
				newIntStore = newIntermediates;
				
				while (!newIntStore.isEmpty()){
					
					newIntermediates = getIntPart(newIntStore, newIntTemp, maxIntProcessedAtOnce);
				
					if (!newIntStore.isEmpty()){
						Trace.print("intermediates left to process: " + newIntStore.size());
					}
					
					/* destroy the tree that was constructed last time */
					PTreeMethods.deforestation(verticesP);
					
					/* set node status of new Intermediates */
					PTreeMethods.setStatusIgnoringSampling(newIntermediates, verticesP, duplicateOriginalVertices);
					
					/* add new intermediates to the original sequences, clear the list of vertices */
					verticesP.addAll(newIntermediates);
					newIntermediates.clear();
					
					/* remove duplicate sequences - old vertices remain, new duplicate intermediates are thrown away */
					PTreeMethods.removeDuplicateVertices11(verticesP);
					
					/* update the distance matrix */
					dMatrix.updateDistanceMatrix();

					/* pick off the tree */
					for (int loop2=0;;loop2++){
						 
						TimeStamp ts3 = TimeStamp.getTimeStamp();
						/* compute MST, root the graph, add mutations */
						
						if (loop2 == 0){
							mst.MST.computeMST(verticesP, dMatrix, mm, mem, config);
							Stat.computeMSTStandardFinished(ts3);
							Trace.print("MST compute (ptree) (" + verticesP.size() + ")", ts3);
						} else {
							
							if (MST.getUseOptimization(config, verticesP.size(), removedVertices.size())){
								mst.MST.repairMST(verticesP, removedVertices, dMatrix, mm, mem, config);
								Stat.computeMSTRepairFinished(ts3);	
							} else {
								PTreeMethods.deforestation(verticesP);
								mst.MST.computeMST(verticesP, dMatrix, mm, mem, config);
								Stat.computeMSTStandardFinished(ts3);
							}
						}
						
						/* remove intermediates with degree 1 and node status 0 */
						vertexRemoved = PTreeMethods.removeIntermediatesDegree1NodeStatus0(verticesP);
						
						/* remove all inferred intermediates with degree 2 and node status 0 */
						removedVertices = PTreeMethods.removeIntermediatesDegree2Status0(verticesP);
						if (removedVertices == null){
							if (vertexRemoved){
								dMatrix.restoreDistanceMatrix();
							}
							break; //no intermediate vertex has been removed
						}
						
						/* restore the distance matrix (some intermediate vertices were deleted) */
						dMatrix.restoreDistanceMatrix();
					}
				}
				
				/* cancel gaps */
				gapHandler.cancelGapsInInternals(verticesP, false);
				
				//TEST
				/*Matrix dMatrixTemp2 = new Matrix(verticesP, config); 
				dMatrixTemp2.computeDistanceMatrix(null, null, null);
				if (!Matrix.equal(dMatrixTemp2, dMatrix)){           
					log.error("Matrices are not equal! (2)");       
					System.exit(-1);                                
				} else {
					System.out.println("gap canceled (2)");
				}*/
			}
			
			/* add duplicate original vertices */
			if (!duplicateOriginalVertices.isEmpty()){//refine !!!
				
				verticesP.addAll(duplicateOriginalVertices);
				dMatrix.updateDistanceMatrix();
				PTreeMethods.deforestation(verticesP);
				TimeStamp ts4 = TimeStamp.getTimeStamp();
				mst.MST.computeMST(verticesP, dMatrix, mm, mem, config);
				Stat.computeMSTStandardFinished(ts4);
			}
			
			/* cancel gaps */
			gapHandler.cancelGapsInInternals(verticesP, true);
			
			//TEST
			/*Matrix dMatrixTemp3 = new Matrix(verticesP, config); 
			dMatrixTemp3.computeDistanceMatrix(null, null, null);
			if (!Matrix.equal(dMatrixTemp3, dMatrix)){           
				log.error("Matrices are not equal! (3)");       
				System.exit(-1);                                
			} else {
				System.out.println("gap canceled (3)");
			}*/ 
			
			mm.clear();
			
			List<Vertex> modifiedNonTreeVertices = null;
			
			if (!config.getCountGapAsChange()){//!!!
				Set<Vertex> set = new HashSet<Vertex>();
				for (Vertex v : verticesP){
					v.getDna().setGapIsChange(true);
					set.add(v);
				}
				modifiedNonTreeVertices = new ArrayList<Vertex>(Math.max(0, allInputVertices.size() - verticesP.size()));
				for (Vertex v : allInputVertices){
					if (!set.contains(v)){
						v.getDna().setGapIsChange(true);
						modifiedNonTreeVertices.add(v);
					}	
				}
				allInputVertices.clear();
				set.clear();
			}
			
			return new Tree(verticesP, config, modifiedNonTreeVertices, currentSampling, dMatrix, gapHandler);
		}
		
		
		/**
		 * Choose at random count vertices from the list.
		 * */
		/*private void chooseAtRandom(List<Vertex> list, int count, Random rand){
			
			if (list.size() <= count){
				return;
			}
			
			Trace.print("Chooses at random: " + (count/(list.size()/100.0)) + "% intermediates");
			
			if (list.size() < (count*2)){
				int rem = list.size() - count;
				for (int i=0; i<rem; i++){
					list.remove(rand.nextInt(list.size()));
				}
				
			} else {
			
				List<Vertex> newList = new ArrayList<Vertex>(count);
				for (int i=0; i<count; i++){
					newList.add(list.remove(rand.nextInt(list.size())));
				}	
				list.clear();
				list.addAll(newList);
				newList.clear();
			}
		}*/
		
		
		private List<Vertex> getIntPart(List<Vertex> newIntStore, List<Vertex> newIntTemp, int maxSize){
			
			newIntTemp.clear();
			
			if (newIntStore.size() <= maxSize){
				newIntTemp.addAll(newIntStore);
				newIntStore.clear();
				return newIntTemp;
			} else {
				
				int lastIdx = newIntStore.size()-1;
				int firstIdx = newIntStore.size() - maxSize;
				 
				for (int i=lastIdx; i>= firstIdx; i--){
					newIntTemp.add(newIntStore.remove(i));
				}
				
			}
			
			return newIntTemp;
		}
		
		
		
        
        public String getTree(){
        	//return Output.graphToNewickFormat(vertices, false);//with headers?
        	return Output.graphToNewickFormat(vertices, true, config);
        }
    }

	

	
	
	
	/**
	 * Find new intermediate vertices for MST version.
	 * 
	 * @param vertices list of vertices after branching
	 * @return list of new intermediate vertices
	 * */
	/*private List<Vertex> getNewIntermediatesMST(List<Vertex> vertices){
		
		//the root of the tree is the first element of the array 
		return Intermediates.getIntermediatesFromRepeatedMutations(vertices.get(0), config.getIntStrategy(),
				config.getIntStrategyCoefficient());
	}*/

}
