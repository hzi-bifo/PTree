package mst.boruvka;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import dmatrix.DMatrix;
import dmatrix.Matrix;

import ptree.MutationManager;
import ptree.Vertex;


public class BoruvkaMST {

	
	/**
	 * Implements Boruvka MST algorithm that is optimized for complete graphs.
	 * */
	public static void computeMST(List<Vertex> vertices, DMatrix distanceMatrix, MutationManager mm){
		
		//long time = System.currentTimeMillis();
		
		/* the size of the neighbor set */
		int logn = (int) ((Math.log(vertices.size())/Math.log(2.0)) + 1);
		
		SortedSet<Edge> tree;
		Vertex v1;
		Vertex v2;
		float dist;
		Edge last;
		List<Component> compList = new ArrayList<Component>(vertices.size());
		
		/* creates a tree of the nearest neighbor edges for each vertex */
		for (int i=0; i<vertices.size(); i++){
			
			tree = new TreeSet<Edge>();
			v1 = vertices.get(i);
			
			for (int j=0; j<vertices.size(); j++){
				
				v2 = vertices.get(j);
				
				if (v1.equals(v2)){
					continue;
				}
				
				dist = distanceMatrix.getDistance(v1, v2);
				
				if (tree.size() < logn){
					tree.add(new Edge(v1,v2,dist));
				} else {
					last = tree.last();
					if (dist < last.dist){
						if (!tree.remove(last)){
							System.exit(-1);
						}
						last.modify(v1, v2, dist);
						tree.add(last);
					} 
				}
				
			}
			
			compList.add(new Component(v1, tree));
		}
		
		//System.out.println("boruvka - init: " + (System.currentTimeMillis() - time) + "ms");
		
		Component comp1 = null;
		Component comp2;
		int idx = 0;
		Edge rec;
		
		/* merge components */
		while (compList.size() > 1){
		
			//System.out.println("while start - comp size: " + compList.size());
			
			comp2 = null;
			
			for (int i=0; i<compList.size();){
				
				comp1 = compList.get(i);
				
				rec = comp1.getEdge(distanceMatrix, vertices, logn);	
				
				rec.v1.setOutcomingEdge(rec.v2);
				rec.v2.setOutcomingEdge(rec.v1);
				
				/* search for the second component */
				for (int j=0; j<compList.size(); j++){
					if (i==j){
						continue;
					}
					if (compList.get(j).contains(rec.v2)){
						comp2 = compList.get(j);
						idx = j;
						break;
					}
				}
				
				/* merge two components */
				if (idx < i){	
					Component.merge(comp2, comp1);
					compList.remove(i);
				} else {
					Component.merge(comp1, comp2);
					compList.remove(idx);
					i++;
				}
				
			}
			
		}

		compList.get(0).clear();
		
		/* root and orient the graph */
		mst.prim.MSTPrim.root(vertices.get(0),  mm);
		
		//System.out.println("boruvka - finished: " + (System.currentTimeMillis() - time) + "ms");
		//System.out.println("tree repared: " + Component.addEdgesCount); 
	}
	
	
	/**
	 * Implements Boruvka MST algorithm that is optimized for complete graphs.
	 * */
	public static void computeMST(List<Vertex> vertices, Matrix distanceMatrix, MutationManager mm){
		
		//long time = System.currentTimeMillis();
		
		/* the size of the neighbor set */
		int logn = (int) ((Math.log(vertices.size())/Math.log(2.0)) + 1);
		
		SortedSet<IEdge> tree;
		Vertex v1;
		Vertex v2;
		short dist;
		IEdge last;
		List<IComponent> compList = new ArrayList<IComponent>(vertices.size());
		
		/* creates a tree of the nearest neighbor edges for each vertex */
		for (int i=0; i<vertices.size(); i++){
			
			tree = new TreeSet<IEdge>();
			v1 = vertices.get(i);
			
			for (int j=0; j<vertices.size(); j++){
				
				v2 = vertices.get(j);
				
				if (v1.equals(v2)){
					continue;
				}
				
				dist = distanceMatrix.getDistance(i, j);
				
				if (tree.size() < logn){
					tree.add(new IEdge(v1,v2,dist));
				} else {
					last = tree.last();
					if (dist < last.dist){
						if (!tree.remove(last)){
							System.exit(-1);
						}
						last.modify(v1, v2, dist);
						tree.add(last);
					} 
				}
				
			}
			
			compList.add(new IComponent(v1, tree));
		}
		
		//System.out.println("boruvka - init: " + (System.currentTimeMillis() - time) + "ms");
		
		mergeComponents(compList, vertices, distanceMatrix, logn);

		/* root and orient the graph */
		mst.prim.MSTPrim.root(vertices.get(0),  mm);
		
		//System.out.println("boruvka - finished: " + (System.currentTimeMillis() - time) + "ms");
		//System.out.println("tree repared: " + Component.addEdgesCount); 
	}
	

	private static void mergeComponents(List<IComponent> compList, List<Vertex> vertices, Matrix distanceMatrix,
			int logn){
		
		IComponent comp1 = null;
		IComponent comp2;
		int idx = 0;
		IEdge rec;
		
		/* merge components */
		while (compList.size() > 1){
		
			//System.out.println("while start - comp size: " + compList.size());
			
			comp2 = null;
			
			for (int i=0; i<compList.size();){
				
				comp1 = compList.get(i);
				
				rec = comp1.getEdge(distanceMatrix, vertices, logn);	
				
				rec.v1.setOutcomingEdge(rec.v2);
				rec.v2.setOutcomingEdge(rec.v1);
				
				/* search for the second component */
				for (int j=0; j<compList.size(); j++){
					if (i==j){
						continue;
					}
					if (compList.get(j).contains(rec.v2)){
						comp2 = compList.get(j);
						idx = j;
						break;
					}
				}
				
				/* merge two components */
				if (idx < i){	
					IComponent.merge(comp2, comp1);
					compList.remove(i);
				} else {
					IComponent.merge(comp1, comp2);
					compList.remove(idx);
					i++;
				}
				
			}
			
		}
		
		compList.get(0).clear();
	}
	
	
	/**
	 * Repairs the MST after some vertices were deleted.
	 * (For each connected component finds the least cost edge)
	 * 
	 * @param vertices a list of vertices that remain in the tree (were not deleted)
	 * @param removedVertices a list of vertices that were removed (are still wired in the tree)  
	 * */
	public static void repairMST(List<Vertex> vertices, List<Vertex> removedVertices, 
			Matrix distanceMatrix, MutationManager mm){
		
		Vertex v;
		Vertex child;
		Vertex parent;
		List<Vertex> children;
		
		List<Vertex> componentRepresentatives = new ArrayList<Vertex>();
		
		/* add the root */
		componentRepresentatives.add(vertices.get(0));
		
		/* collect ids of all vertices that were removed */
		Set<Integer> removedIds = new HashSet<Integer>(removedVertices.size());
		for (int i=0; i<removedVertices.size(); i++){
			removedIds.add(removedVertices.get(i).getIntId());
		}
		
		/* collect the set of all "roots" of the connected components */
		for (int i=0; i<removedVertices.size(); i++){
			
			v = removedVertices.get(i);
			children = v.getOutcomingEdges();
			
			for (int j=0; j<children.size(); j++){
				
				child = children.get(j);
				
				if (!removedIds.contains(child.getIntId())){
					componentRepresentatives.add(child);
				}
			}
		}

		/* correct the tree such that it is a correct forest of oriented trees */
		for (int i=0; i<removedVertices.size(); i++){
			
			 v = removedVertices.get(i);
			 parent = v.getIncomingEdge();
			 children = v.getOutcomingEdges();
			 
			 if ((parent != null) && (!removedIds.contains(parent.getIntId()))){
				 parent.removeOutcomingEdge(v);
			 }
			 
			 if (!children.isEmpty()){
				 
				 for (int j=0; j<children.size(); j++){
					 
					 child = children.get(j);
					 if (!removedIds.contains(child.getIntId())){
						 child.removeIncomingEdge(); 
					 } 
				 } 
			 }
		}
		
		/* creates a component from each component representative */
		int componentCount = componentRepresentatives.size();
		List<IComponent> compList = new ArrayList<IComponent>(); 
		int neighborsInitCount = (int) ((Math.log(componentCount)/Math.log(2.0)) + 1);
		for (int i=0; i<componentCount; i++){
			compList.add(new IComponent(componentRepresentatives.get(i), vertices, distanceMatrix, 
					neighborsInitCount));
		}

		
		mergeComponents(compList, vertices, distanceMatrix, neighborsInitCount);
		
		/* root and orient the graph */
		mst.prim.MSTPrim.root(vertices.get(0),  mm);
	}
	
}
