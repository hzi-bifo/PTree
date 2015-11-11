package mst.boruvka;

import java.util.Comparator;
import java.util.SortedSet;

import ptree.Vertex;



/**
 * Maintains a list of edges to the nearest neighbors.
 * */
public class IVRecord implements Comparable<IVRecord>, Comparator<IVRecord>{

	Vertex v;
	SortedSet<IEdge> neighbors;
	
	IVRecord(Vertex v, SortedSet<IEdge> neighbors){
		this.v = v;
		this.neighbors = neighbors;
	}

	@Override
	public int compareTo(IVRecord o) {
		return neighbors.first().compareTo(o.neighbors.first());
		 
	}

	@Override
	public int compare(IVRecord o1, IVRecord o2) {
		return neighbors.first().compare(o1.neighbors.first(), o2.neighbors.first());
	}
	
	@Override
	public int hashCode(){
		return v.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return ((VRecord)o).v.equals(v);
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("VRecord - " + v.toString().substring(0,9) + "\n");
		buf.append("Edges:\n");
		for (int i=0; i<neighbors.size(); i++){
			buf.append(neighbors.toArray()[i].toString() + " ");
		}
		
		return buf.toString();
	}
	
	
	public void clear(){
		neighbors.clear();
	}
}
