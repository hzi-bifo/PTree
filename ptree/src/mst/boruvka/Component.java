package mst.boruvka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import dmatrix.DMatrix;

import ptree.Vertex;



/**
 * Represents one connected component. It contains a set of vertices and
 * a {@link mst.boruvka.VRecord record} for each vertex. The {@link mst.boruvka.VRecord records} are 
 * stored in a tree, s.t., the record that contains the shortest edge is the first element of this tree.
 * */
public class Component {
	
	Set<Vertex> set;
	SortedSet<VRecord> trees;
	
	//public static long addEdgesCount = 0;
	
	protected Component(Vertex v, SortedSet<Edge> tree){
		set = new HashSet<Vertex>();
		trees = new TreeSet<VRecord>();
		set.add(v);
		trees.add(new VRecord(v, tree));
	}
	
	
	protected boolean contains(Vertex v){
		return set.contains(v);
	}
	
	
	protected Edge getEdge(DMatrix distanceMatrix, List<Vertex> vertices, int neighborsInitCount){
		
		float dist;
		SortedSet<Edge> tree;	
		Edge edge;
		Edge last;
		VRecord vrec;
		
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
					dist = distanceMatrix.getDistance(v1, v2);
					if (tree.size() < neighborsInitCount){
						tree.add(new Edge(v1,v2,dist));
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
	protected static Component merge(Component c1, Component c2){
		
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
		VRecord vra[] = trees.toArray(new VRecord[0]);
		for (int i=0; i<trees.size(); i++){
			vra[i].clear();
		}
	}
}
