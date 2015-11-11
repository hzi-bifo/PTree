package nj;

import java.util.List;
import java.util.ArrayList;

import nj.NJVertex;

import pal.tree.NeighborJoiningTree;
import pal.tree.Node;
import pal.distance.DistanceMatrix;
import pal.misc.SimpleIdGroup;



/**
 * Java implementation of the NJ algorithm that uses PAL library.
 * 
 * Previously I used (but It was too slow): 
 * (http://www.itu.dk/people/sestoft/bsa.html and http://www.itu.dk/~sestoft/bsa/Match7.java implementations)
 * */
public class NJAlgorithm {

	
	/**
	 * @param dmatrix distance matrix
	 * 
	 * @return the root of the NJ tree
	 * */
	public NJVertex computeNJTree(float dmatrix[][]){
		
		/* create matrix for PAL input */
		double dist[][] = new double[dmatrix.length][];
		for (int i=0; i<dmatrix.length; i++){ 
			dist[i] = new double[dmatrix.length];
		}

		/* copy the distance matrix for PAL input */
		for (int i=0; i<dmatrix.length; i++){
			for (int j=0; j<dmatrix.length; j++){
				if (i==j){
					dist[i][j] = 0;
				} else {
					dist[i][j] = dmatrix[Math.max(i, j)][Math.min(i,j)];
				}
			}
		}
		
		/* generate nodes` names */
		String str[] = new String[dmatrix.length];
		for (int i=0; i<dmatrix.length; i++){
			str[i] = new String(new Integer(i).toString());
		}

		/* compute NJ tree using PAL library */
		SimpleIdGroup idGroup = new SimpleIdGroup(str);
		DistanceMatrix distanceMatrix = new DistanceMatrix(dist, idGroup);
		NeighborJoiningTree njTree = new NeighborJoiningTree(distanceMatrix);
		Node root = njTree.getRoot();
  
		/* get internal and external node count */
		int internalNodeCount = njTree.getInternalNodeCount();
		int externalNodeCount = njTree.getExternalNodeCount();

		/* set numbers of external nodes */
		for (int i=0; i< externalNodeCount; i++){
			njTree.getExternalNode(i).setNumber(new Integer(njTree.getExternalNode(i).getIdentifier().getName()));
		}
	  
		/* set number of internal nodes */
		for (int i=0; i<internalNodeCount; i++){
			njTree.getInternalNode(i).setNumber(njTree.getInternalNode(i).getNumber() + externalNodeCount);
	 	}
		
		GNJVertex njRoot = new GNJVertex(root.getNumber());
	  
		/* transform the NJ tree to a graph and collect leafs */
		treeToNJTree(root, njRoot);
	  
		return njRoot;
	}
	
	
	/**
	 * Computes a NJ matrix from a given distance matrix. 
	 * 
	 * @param dmatrix distance matrix
	 * 
	 * @return NJ matrix (distances corresponds to the distances in the NJ tree)
	 * */
	public float[][] computeNJMatrix(float dmatrix[][]){
		
		/* create matrix for PAL input */
		double dist[][] = new double[dmatrix.length][];
		for (int i=0; i<dmatrix.length; i++){ 
			dist[i] = new double[dmatrix.length];
		}

		/* copy the distance matrix for PAL input */
		for (int i=0; i<dmatrix.length; i++){
			for (int j=0; j<dmatrix.length; j++){
				if (i==j){
					dist[i][j] = 0;
				} else {
					dist[i][j] = dmatrix[Math.max(i, j)][Math.min(i,j)];
				}
			}
		}
		
		/* generate nodes` names */
		String str[] = new String[dmatrix.length];
		for (int i=0; i<dmatrix.length; i++){
			str[i] = new String(new Integer(i).toString());
		}

		/* compute NJ tree using PAL library */
		SimpleIdGroup idGroup = new SimpleIdGroup(str);
		DistanceMatrix distanceMatrix = new DistanceMatrix(dist, idGroup);
		NeighborJoiningTree njTree = new NeighborJoiningTree(distanceMatrix);
		Node root = njTree.getRoot();
  
		/* get internal and external node count */
		int internalNodeCount = njTree.getInternalNodeCount();
		int externalNodeCount = njTree.getExternalNodeCount();
		
		/* number of all nodes in a tree */  
		int size = externalNodeCount;
	    
		/* allocate the output matrix  */
		float matrix[][] = new float[size][];
		for (int i=0; i<size; i++){
			matrix[i] = new float[size];
		}
	  
		/* initialize the output matrix */  
		for (int i=0; i<size; i++){
			for (int j=0; j<size;j++){
				matrix[i][j] = Integer.MAX_VALUE;
			}
		}
		for (int i=0; i<size; i++){
			matrix[i][i] = 0;
		}

		/* set numbers of external nodes */
		for (int i=0; i< externalNodeCount; i++){
			njTree.getExternalNode(i).setNumber(new Integer(njTree.getExternalNode(i).getIdentifier().getName()));
		}
	  
		/* set number of internal nodes */
		for (int i=0; i<internalNodeCount; i++){
			njTree.getInternalNode(i).setNumber(njTree.getInternalNode(i).getNumber() + externalNodeCount);
	 	}
		
		/* leafs of the NJ tree */
		List<GVertex> leafs = new ArrayList<GVertex>(externalNodeCount);
	  
		/* transform the NJ tree to a graph and collect leafs */
		treeToGraph(leafs, root, new GVertex(root.getNumber()));
	  
		GVertex leaf;
		GVertex node;
	  
		/* From each leaf: go to the other leafs (DFS) and set the distance, delete the leaf from which we searched */
		for (int i=0; i<leafs.size(); i++){
			leaf = leafs.get(i);
			setDistance(leaf, leaf, null, matrix);
			node = leaf.neighbours.get(0);
			for (int j=0; j<node.neighbours.size(); j++){
				if (node.neighbours.get(j) == leaf){
					node.neighbours.remove(j);
					node.neighboursDist.remove(j);
					break;
				}
			}
		}
		
		return matrix;
	}

	
	/**
	 * Set the distance of two leafs in a NJ tree.
	 * */  
	private void setDistance(GVertex leaf, GVertex node, GVertex pnode, float matrix[][]){
		
		GVertex tempNode;
	  
		if (leaf == node){
			for (int i=0; i<node.neighbours.size(); i++){
				tempNode = node.neighbours.get(i);
				tempNode.tempDistance = node.neighboursDist.get(i);
				setDistance(leaf, tempNode, node, matrix);
			}
			return;
		}
	  
		if (node.isLeaf){
		  
			matrix[node.number][leaf.number] = node.tempDistance;
			matrix[leaf.number][node.number] = node.tempDistance;
			node.tempDistance = 0.0f;
			return;
		}
	  
		for (int i=0; i<node.neighbours.size(); i++){
			tempNode = node.neighbours.get(i);
			if (tempNode == pnode){
				continue;
			}
			tempNode.tempDistance = node.tempDistance + node.neighboursDist.get(i);
			setDistance(leaf,tempNode, node, matrix);
		} 
	}
  
	
	/**
	 * One vertex of a graph.
	 * */
	private class GVertex {
		
		public final int number;
		public boolean isLeaf = false;
		public float tempDistance = 0.0f;
		  
		public List<GVertex> neighbours;
		public List<Float> neighboursDist;
		  
		public GVertex(int number){
			this.number = number;
			neighbours = new ArrayList<GVertex>(3);
			neighboursDist = new ArrayList<Float>(3);
		}
	}
  
	
	/**
	 * One vertex of an NJ tree. 
	 * */
	private class GNJVertex implements NJVertex {
		
		public final int name;
		public boolean isLeaf = false;  
		public List<GNJVertex> children;
		  
		public GNJVertex(int name){
			this.name = name;
			this.children = new ArrayList<GNJVertex>(3);
		}
		
		@Override
		public List<NJVertex> getChildren() {
			List<NJVertex> childrenOut = new ArrayList<NJVertex>(children.size());
			if (isLeaf){
				return childrenOut;
			}
			for (GNJVertex v : children){
				childrenOut.add(v);
			}
			return childrenOut;
		}

		@Override
		public int getName() {
			return name;
		}

		@Override
		public boolean isLeaf() {
			return isLeaf;
		} 
	}
	
  
	/**
	 * Convert the NJ tree to a graph.
	 * */
	private void treeToGraph(List<GVertex> leafs, Node node, GVertex gnode){
	  
		if (node.isLeaf()){
			gnode.isLeaf = true;
			leafs.add(gnode);
			return;
		} else {
		  
			Node child;
			GVertex gchild;
		  
			for (int i=0; i< node.getChildCount(); i++){
			  
				child = node.getChild(i);
				gchild = new GVertex(child.getNumber());
			  
				gnode.neighbours.add(gchild);
				gnode.neighboursDist.add((float)child.getBranchLength());
			  
				gchild.neighbours.add(gnode);
				gchild.neighboursDist.add((float)child.getBranchLength());
			  
				treeToGraph(leafs,child,gchild);
			}
		}
	}

	
	/**
	 * Convert the NJ tree from PAL to an NJ tree.
	 * */
	private void treeToNJTree(Node node, GNJVertex gnode){
	  
		if (node.isLeaf()){
			gnode.isLeaf = true;
			return;
		} else {
		  
			Node child;
			GNJVertex gchild;
		  
			for (int i=0; i< node.getChildCount(); i++){
			  
				child = node.getChild(i);
				gchild = new GNJVertex(child.getNumber());
			  
				gnode.children.add(gchild);
				treeToNJTree(child, gchild);
			}
		}
	}
	
  
	/**
	 * Print the matrix.
	 * */
	public static void printMatrix(double[][] matrix, int size){
		for (int i=0; i<size; i++){
			System.out.println("");
			for (int j=0; j<size; j++){
				System.out.print(" " + matrix[i][j]);
			}
			System.out.println("");
		}
	}
	
}

