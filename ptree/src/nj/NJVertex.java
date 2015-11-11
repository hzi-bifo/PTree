package nj;

import java.util.List;


/**
 * Represents one vertex of an NJ tree.
 * */
public interface NJVertex {

	public int getName();
	
	public boolean isLeaf();
	
	/** @param parent parent of this vertex or null */
	public List<NJVertex> getChildren();
	
}
