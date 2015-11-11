package mst;

import java.util.List;

import mst.prim.MSTPrim;
import mst.prim.MemoryManager;

//import ptree.GapHandler;
import ptree.MutationManager;
//import ptree.PTreeMethods;
//import ptree.Tree;
import ptree.Vertex;
import tests.Stat;

import common.Configuration;
import common.TimeStamp;

import dmatrix.DMatrix;
import dmatrix.Matrix;

public class MST {
	


/*public static long timeJarnik = 0;
	public static long timeJarnikCount = 0;
	public static long timeBoruvka = 0;
	public static long timeBoruvkaCount = 0;
	*/
	
	/**
	 * Computes MST, root the graph (root is the first vertex from the list of vertices) and adds mutations.
	 * 
	 * @param vertices list of the vertices
	 * @param distanceMatrix distances that will be used to compute MST
	 * @param mm mutation manager used to add mutations on edges
	 * @param mem memory manager to allocate queues and arrays for priority queue
	 * */
	public static void computeMST(List<Vertex> vertices, Matrix distanceMatrix, MutationManager mm, MemoryManager mem, Configuration config){
		
		//long time = System.currentTimeMillis();//
		
		TimeStamp ts = TimeStamp.getTimeStamp();
		if (config.getUseMSTPrimOptimization()){
			mst.primcompletegraph.PrimCompleteGraph.computeMST(vertices, distanceMatrix, mm);
		} else {
			if (vertices.size() > config.getMstImplementationThreshold()){
				mst.boruvka.BoruvkaMST.computeMST(vertices, distanceMatrix, mm);
			} else {
				mst.prim.MSTPrim.computeMSTJarnik(vertices, distanceMatrix, mm, mem);
			}
		}
		Stat.computeMSTFinished(ts);
		
		/*
		
		
		long timeBoruvka = System.currentTimeMillis() - time; 
			 
		Tree t1 = new Tree(vertices, config, null, null, distanceMatrix, new GapHandler(config, distanceMatrix));
		int costT1 = t1.getCost();
		
		
		PTreeMethods.deforestation(vertices); 
		
		time = System.currentTimeMillis();//
		mst.primcompletegraph.PrimCompleteGraph.computeMST(vertices, distanceMatrix, mm);
		long timePrim = System.currentTimeMillis() - time;
		
		Tree t2 = new Tree(vertices, config, null, null, distanceMatrix, new GapHandler(config, distanceMatrix));
		int costT2 = t2.getCost(); 
		
		if (costT1 != costT2){
				
			System.err.println("wrong costs..");
			System.err.println("MST boruvka: " + timeBoruvka + "ms cost " + costT1 + "| prim " + timePrim + "ms cost "
					+ costT2);
			System.exit(-1);
		}
		
		if (timeBoruvka > 10){
			System.out.println("MST boruvka: " + timeBoruvka + "ms " + "| prim " + timePrim + "ms");
		}
		*/ 
		 
		/*long time = System.currentTimeMillis();
		computeMSTJarnik(vertices, distanceMatrix, mm, mem);
		Tree t1 = new Tree(vertices,config);
		int costT1 = t1.getCost();
		//System.out.println("prim: " + (System.currentTimeMillis() - time) + "ms");
		timeJarnik += (System.currentTimeMillis() - time);
		timeJarnikCount++;
		PTree.deforestation(vertices); 
		time = System.currentTimeMillis();
		*/
		//boruvka.BoruvkaMST.computeMST(vertices, distanceMatrix, mm);
		/*//System.out.println("boruvka: " + (System.currentTimeMillis() - time) + "ms");
		timeBoruvka += (System.currentTimeMillis() - time);
		timeBoruvkaCount++;
		Tree t2 = new Tree(vertices,config);
		int costT2 = t2.getCost();
		if (costT1 != costT2){
			System.err.println("Different costs MSTI: " + costT1 + " and " + costT2 + " : " + (costT2 - costT1));
			//System.exit(-1);
		} else {
			//System.out.println("same cost MSTI: " + costT1);
		}
		*/
	}

	
	/**
	 * Repair an MST tree from which some vertices were deleted.
	 * It reconstructs a tree such that it becomes an MST again
	 * 
	 *  @param vertices a list of remained vertices
	 *  @param removedVertices a list of deleted vertices
	 * 
	 * */
	public static void repairMST(List<Vertex> vertices, List<Vertex> removedVertices,
			Matrix distanceMatrix, MutationManager mm, MemoryManager mem, Configuration config){
		TimeStamp ts = TimeStamp.getTimeStamp();
		if (removedVertices == null){
			System.err.println("repair MST after no vertex removed !!!");
			System.exit(-1);
		}
		
		mst.boruvka.BoruvkaMST.repairMST(vertices, removedVertices, distanceMatrix, mm);
		Stat.computeMSTFinished(ts);
	}
	
	
	
	/**
	 * 
	 * 
	 * */
	public static boolean getUseOptimization(Configuration config, int vertexCount,
			int deletedVertexCount){
		
		if (config.getUseMSTPrimOptimization()){
			return false;
		}
		
		return ((config.getMstOptimizationThresholdVertexCount() < vertexCount)
				&& ((((double)deletedVertexCount)/((double)vertexCount)) < 
						config.getMstOptimizationThresholdDeletedPart()))?true:false;
	}
	
	
	/**
	 * Computes MST, root the graph (root is the first vertex from the list of vertices) and adds mutations.
	 * 
	 * @param vertices list of the vertices
	 * @param distanceMatrix distances that will be used to compute MST
	 * @param mm mutation manager used to add mutations on edges
	 * @param mem memory manager to allocate queues and arrays for priority queue
	 * */
	public static void computeMST(List<Vertex> vertices, DMatrix distanceMatrix, MutationManager mm, MemoryManager mem, Configuration config){
		
		//long time = System.currentTimeMillis();//
		
		TimeStamp ts = TimeStamp.getTimeStamp();
		if (config.getUseMSTPrimOptimization()){
			mst.primcompletegraph.PrimCompleteGraphD.computeMST(vertices, distanceMatrix, mm);
		} else {
			if (vertices.size() > config.getMstImplementationThreshold()){
				mst.boruvka.BoruvkaMST.computeMST(vertices, distanceMatrix, mm);
			} else {
				MSTPrim.computeMSTJarnik(vertices, distanceMatrix, mm, mem);
			}
		}
		Stat.computeMSTFinished(ts);
		
		/*long timeBoruvka = System.currentTimeMillis() - time; 
		 
		Tree t1 = new Tree(vertices, config, null, null, null, new GapHandler(config, null));
		int costT1 = t1.getCost();
		
		
		PTreeMethods.deforestation(vertices); 
		
		time = System.currentTimeMillis();//
		mst.primcompletegraph.PrimCompleteGraphD.computeMST(vertices, distanceMatrix, mm);
		long timePrim = System.currentTimeMillis() - time;
		
		Tree t2 = new Tree(vertices, config, null, null, null, new GapHandler(config, null));
		int costT2 = t2.getCost(); 
		
		if (costT1 != costT2){
				
			System.err.println("Dwrong costs..");
			System.err.println("D MST boruvka: " + timeBoruvka + "ms cost " + costT1 + "| prim " + timePrim + "ms cost "
					+ costT2);
			//System.exit(-1);
		}
		
		if (timeBoruvka > 10){
			System.out.println("D MST boruvka: " + timeBoruvka + "ms " + "| prim " + timePrim + "ms");
		}
		*/
		
		/*long temp;
		long time = System.currentTimeMillis();
		
		computeMSTJarnik(vertices, distanceMatrix, mm, mem);
		Tree t1 = new Tree(vertices,config);
		int costT1 = t1.getCost();
		temp = (System.currentTimeMillis() - time);
		System.out.println("prim: " + temp + "ms");
		timeJarnik += temp;
		timeJarnikCount++;
		PTree.deforestation(vertices); 
		time = System.currentTimeMillis();
		*/
		//boruvka.BoruvkaMST.computeMST(vertices, distanceMatrix, mm);
		/*temp = (System.currentTimeMillis() - time);
		System.out.println("boruvka: " + temp + "ms");
		timeBoruvka += temp;
		timeBoruvkaCount++;
		Tree t2 = new Tree(vertices,config);
		int costT2 = t2.getCost();
		if (costT1 != costT2){
			System.err.println("Different costs MSTD: " + costT1 + " and " + costT2 + " : " + (costT2 - costT1));
			//System.exit(-1);
		} else {
			//System.out.println("same cost MSTD: " + costT1);
		}
		*/
		/*double jar = ((double)MST.timeJarnik)/((double)MST.timeJarnikCount);
		double bor = ((double)MST.timeBoruvka)/((double)MST.timeBoruvkaCount);	
		System.out.println("" + jar + " ms");
		System.out.println("" + bor + " ms");
		System.out.println("" + (jar/bor) + " X");
		System.exit(0);
		*/
		 
		
	}


}