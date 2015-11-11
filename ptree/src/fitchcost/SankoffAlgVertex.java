package fitchcost;

import java.util.List;


/**
 * Interface of an vertex of a tree that can be processed by the algorithm
 * */
public interface SankoffAlgVertex {

	public byte[] getDnaAsBytes();
	
	public void setDna(byte[] dnaAsBytes);
	
	public List<SankoffAlgVertex> getChildren();
	
	public boolean isLeaf();
}
