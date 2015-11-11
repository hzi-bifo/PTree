package mst.prim;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import ptree.MutationManager;
import ptree.Vertex;

import common.Configuration;
import dmatrix.DMatrix;
import dmatrix.Matrix;

import jpaul.DataStructs.UnionFind;


/**
 * Computes (Jarník~Prim) MST algorithm using union-find data structure from the jPaul library. 
 * */
public class MSTPrim {

	/* I tried also "jpaul.DataStructs.WorkPriorityQueue" or "ExpPriorityQueue" but it was slower 
	 * 
	 * I use my own memory manager for priority queues and arrays 
	 * ~ so I reduce number of allocated objects.
	 * All edges are first stored in an array and then is created a priority queue at once 
	 * ~ reduce complexity (of priority queue creation) from nlogn to n.
	 * I am using union-find data structure to detect cycles 
	 * ~ fast
	 * */
	
	public static void computeMSTJarnik(List<Vertex> vertices, Matrix distanceMatrix, MutationManager mm, MemoryManager mem){
		
		//long time = System.currentTimeMillis();
		UnionFind<Integer> unionFind = new UnionFind<Integer>();
		int size = ((distanceMatrix.getSize() * distanceMatrix.getSize())/2);
		List<SEdge> array = mem.getEdgeArray(size);
		
		
		/* create a list of all edges */
		for (int i=1; i<vertices.size(); i++){
			for (int j=0; j<i; j++){
				
				array.add(new SEdge(vertices.get(Math.min(i,j)), vertices.get(Math.max(i, j)),
						distanceMatrix.getDistance(i, j)));
					 
			}
		}
		 
		/* creates priority queue from all edges (bulk-load operation, faster than adding edge by edge) */
		PriorityQueue<SEdge> queue = mem.getEdgePriorityQueue(size);
		queue.addAll(array);
		
		/* Jarník (Prim) - greedy algorithm */
		SEdge edge;
		int edgeCount = 0;
		int maxEdgeCount = vertices.size() - 1;
		
		while (edgeCount < maxEdgeCount){
			
			/* get the edge with minimum weight */
			edge = queue.poll();
			 
			/* are these vertices already unified ? */
			if (!unionFind.areUnified(edge.v0.getIntId(), edge.v1.getIntId())){
				
				/* add this edge */
				edge.v0.setOutcomingEdge(edge.v1);
				edge.v1.setOutcomingEdge(edge.v0);
				unionFind.union(edge.v0.getIntId(), edge.v1.getIntId());
				edgeCount++;
			}
		}

		/* root the graph and add mutations */
		root(vertices.get(0), mm);
		
		/* store the priority queue and the array for the next run of MST */
		mem.freeEdgePriorityQueue(queue);
		mem.freeEdgeArray(array);
		
		//System.out.println("Run MST short:" + (System.currentTimeMillis() - time) );
	}
	
	
	
	
	public static void computeMSTJarnik(List<Vertex> vertices, DMatrix distanceMatrix, MutationManager mm, MemoryManager mem){
		
		//long time = System.currentTimeMillis();
		UnionFind<Integer> unionFind = new UnionFind<Integer>();
		int size = ((distanceMatrix.getSize() * distanceMatrix.getSize())/2);
		List<FEdge> array = mem.getDEdgeArray(size);
		
		/* create a list of all edges */
		for (int i=1; i<vertices.size(); i++){
			for (int j=0; j<i; j++){
				array.add(new FEdge(vertices.get(Math.min(i,j)), vertices.get(Math.max(i, j)),
						distanceMatrix.getDistance(vertices.get(i), vertices.get(j))));
			}
		}
		
		/* creates priority queue from all edges (bulk-load operation, faster than adding edge by edge) */
		PriorityQueue<FEdge> queue = mem.getDEdgePriorityQueue(size);
		queue.addAll(array);
		
		/* Jarník (Prim) - greedy algorithm */
		FEdge edge;
		int edgeCount = 0;
		int maxEdgeCount = vertices.size() - 1;
		
		while (edgeCount < maxEdgeCount){
			
			/* get the edge with minimum weight */
			edge = queue.poll();
			
			/* are these vertices already unified ? */
			if (!unionFind.areUnified(edge.v0.getIntId(), edge.v1.getIntId())){
				
				/* add this edge */
				edge.v0.setOutcomingEdge(edge.v1);
				edge.v1.setOutcomingEdge(edge.v0);
				unionFind.union(edge.v0.getIntId(), edge.v1.getIntId());
				edgeCount++;
				
			}
		}
		
		/* root the graph and add mutations */
		root(vertices.get(0), mm);
		
		/* store the priority queue and the array for the next run of MST */
		mem.freeDEdgePriorityQueue(queue);
		mem.freeDEdgeArray(array);
		
		//System.out.println("Run MST float:" + (System.currentTimeMillis() - time) );
	}
	
	
	/**
	 * DFS the graph, root it and add mutations.
	 * */
	public static void root(Vertex vertex, MutationManager mm){
		
		Vertex vertex2;
		
		for (int i=0; i< vertex.getOutcomingEdges().size(); i++){
			
			vertex2 = vertex.getOutcomingEdges().get(i);
			
			vertex2.setIncomingEdge(vertex);
			vertex2.removeOutcomingEdge(vertex);
			vertex2.setMutations(mm.getMutations(vertex, vertex2));
			
			root(vertex2,mm);
		}
	
	}
	
	
	/**
	 * Test method
	 * */
	public static void main(String[] args) {
		
		Configuration config = new Configuration();
		
		List<Vertex> vertices = new ArrayList<Vertex>();
		MutationManager mm = new MutationManager(config,false);
		
		float [][] dmatrix = { {},
				{  12.4f },
				{  45.7f,  36.8f },
				{41.0f,  50.1f, 63.2f },
				{34.4f, 34.1f, 68.4f, 72.8f},
				{54.3f, 49.5f, 76.5f, 94.6f, 23.6f }};
		int size = 6;
		
		float [][] dmatrix2 = new float[size][];
		for (int i=0; i<size; i++){
			dmatrix2[i] = new float[size];
		}
		
		for (int i=0; i< size; i++){
			for (int j=0; j< size; j++){
				if (i==j){
					dmatrix2[i][j] = 0;
				} else {
					dmatrix2[i][j] = dmatrix[Math.max(i, j)][Math.min(i, j)];
				}
			}
		}
		
		for (int i=0; i< 6; i++){
			vertices.add(new Vertex(new Integer(i).toString(),""));
		}
		
		
		
		Matrix m1 = new Matrix(vertices,null, config);
		DMatrix m2 = new DMatrix(m1,dmatrix2);
		for (int i=0; i<size; i++){
			for (int j=0; j<size; j++){
				System.out.println(m2.getDistance(vertices.get(i), vertices.get(j)));
			}
		}
		
		mst.MST.computeMST(vertices, m2, mm,null, null);
		
	}
	
	
}
