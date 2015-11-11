package mst.boruvka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import dmatrix.Matrix;

import ptree.Vertex;


/**
 * Represents one connected component. It contains a set of vertices and
 * a {@link mst.boruvka.VRecord record} for each vertex. The {@link mst.boruvka.VRecord records} are 
 * stored in a tree, s.t., the record that contains the shortest edge is the first element of this tree.
 * */
public class IComponent {
	
	Set<Vertex> set;
	SortedSet<IVRecord> trees;
	
	//public static long addEdgesCount = 0;
	
	
	protected IComponent(Vertex v, SortedSet<IEdge> tree){
		set = new HashSet<Vertex>();
		trees = new TreeSet<IVRecord>();
		set.add(v);
		trees.add(new IVRecord(v, tree));
	}
	
	
	protected boolean contains(Vertex v){
		return set.contains(v);
	}
	
	
	protected IComponent(Vertex root, List<Vertex> vertices, Matrix dMatrix, int neighborsInitCount){
		
		set = new HashSet<Vertex>();
		trees = new TreeSet<IVRecord>();
		
		/* collect vertices (set) */
		collectVertices(root, set);
		
		/* collect least cost edges (trees) */
		collectLeastCostEdges(root, vertices, set, trees, dMatrix, neighborsInitCount);
		
		/* cancel the orientation and delete stored mutation records */
		cancelOrientationAndMutations(root);
		
	}

	
	private void collectVertices(Vertex root, Set<Vertex> set){
		
		set.add(root);
		List<Vertex> children = root.getOutcomingEdges();
		for (int i=0; i<children.size(); i++){
			collectVertices(children.get(i), set);
		} 	
	}
	
	
	private void collectLeastCostEdges(Vertex root, List<Vertex> vertices, Set<Vertex> set, 
			SortedSet<IVRecord> trees, Matrix distanceMatrix, int neighborsInitCount){
		
			SortedSet<IEdge> tree = new TreeSet<IEdge>();
			Vertex v2;
			short dist;
			IEdge last;
			
			for (int j=0; j<vertices.size(); j++){
				v2 = vertices.get(j);
				if (set.contains(v2)){
					continue;
				}
				
				dist = distanceMatrix.getDistance(root, j);
				
				if (tree.size() < neighborsInitCount){
					tree.add(new IEdge(root,v2,dist));
				} else {
					last = tree.last();
					if (last.dist > dist){
						tree.remove(last);
						last.modify(root, v2, dist);
						tree.add(last);
					}
				}	
			}
			
			trees.add(new IVRecord(root, tree));
			
			List<Vertex> children = root.getOutcomingEdges();
			for (int i=0; i<children.size(); i++){
				collectLeastCostEdges(children.get(i), vertices, set, trees, distanceMatrix, neighborsInitCount);
			} 	
	}
	
	
	private void cancelOrientationAndMutations(Vertex root){
		
		root.setMutations(null);
		
		Vertex child;
		List<Vertex> children = root.getOutcomingEdges();
		
		for (int i=0; i<children.size(); i++){
			
			child = children.get(i);

			cancelOrientationAndMutations(child);
			
			child.removeIncomingEdge();
			child.setOutcomingEdge(root);
			
		}
	}
	
	
	protected IEdge getEdge(Matrix distanceMatrix, List<Vertex> vertices, int neighborsInitCount){
		
		short dist;
		SortedSet<IEdge> tree;	
		IEdge edge;
		IEdge last;
		IVRecord vrec;
		
		for (;;){
			
	
			vrec = trees.first();
			if (!trees.remove(vrec)){
				System.out.println("trees content: ");
				for (int i=0; i<trees.size(); i++){
					System.out.println(trees.toArray()[i].toString());
				}
				System.out.println("------------");
				System.out.println("Problem to remove 1: \n" + vrec.toString());
				System.exit(-1);
			}
			
			edge = vrec.neighbors.first();
			if (!vrec.neighbors.remove(edge)){
				System.out.println("Proglem to remove edge: " + edge.toString());
				System.out.println("vertex record: " + vrec.toString());
				System.exit(-1);
			}

			if (vrec.neighbors.isEmpty()){
				//addEdgesCount++;
				tree = vrec.neighbors;
				Vertex v1 = vrec.v;
				Vertex v2;
				for (int j=0; j<vertices.size(); j++){
					v2 = vertices.get(j);
					if (set.contains(v2)){
						continue;
					}
					
					//dist = distanceMatrix.getDistance(v1, v2);
					dist = distanceMatrix.getDistance(v1, j);
					
					if (tree.size() < neighborsInitCount){
						tree.add(new IEdge(v1,v2,dist));
					} else {
						last = tree.last();
						if (last.dist > dist){
							tree.remove(last);
							last.modify(v1, v2, dist);
							tree.add(last);
						}
					}	
				}
			}
			
			trees.add(vrec);
			
			if (!set.contains(edge.v2)){
				return edge;
			}
		}
	}
	
	
	/**
	 * Copy c2 to c1, returns c1.
	 * */
	protected static IComponent merge(IComponent c1, IComponent c2){
		
		if (c1.set.size() < c2.set.size()){
			
			c2.set.addAll(c1.set);
			c2.trees.addAll(c1.trees);
			c1.set.clear();
			c1.trees.clear();
			c1.set = c2.set;
			c1.trees = c2.trees;
			c2.set = null;
			c2.trees = null;
		} else {
			c1.set.addAll(c2.set);
			c1.trees.addAll(c2.trees);
			c2.set.clear();
			c2.trees.clear();
		}
		return c1;
	}
	
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("size: " + set.size() + "vert:");
		for (int i=0; i<set.size(); i++){
			buf.append(set.toArray()[i].toString().substring(0, 9) + " ");
		}
		//buf.append("\n tree: ");
		return buf.toString();
	}
	
	
	protected void clear(){
		set.clear();
		IVRecord vra[] = trees.toArray(new IVRecord[0]);
		for (int i=0; i<trees.size(); i++){
			vra[i].clear();
		}
	}
}
