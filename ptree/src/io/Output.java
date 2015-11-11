package io;

import java.util.List;
import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;

import ptree.Vertex;


/**
 * Computed trees are written to a file in the Newick format.
 * */
public class Output {

	private static Log log = LogFactory.getLog(Output.class);
	
	public static final int NEWICK_FORMAT = 1;
	
	
	/**
	 * Writes a graph to a file.
	 * 
	 * @param vertices a graph to be written to a file
	 * */
	public static void graphToFile(int format, List<Vertex> vertices, String outputFileName, Configuration config){
		
		try {
			/* create file */
			FileWriter fstream = new FileWriter(outputFileName);
			BufferedWriter out = new BufferedWriter(fstream);
			
			/* write graph */
			switch (format){
				case NEWICK_FORMAT: out.write(graphToNewickFormat(vertices,false, config)); break;
				default: log.error("alg.Output: undefined output format"); 
			}
		    
			/* close the output stream */
		    out.close();
		} catch (Exception e){
			log.error("An exception: ",e);
		}
	}
	
	
	/**
	 * Transforms specified tree to the Newick format.
	 * 
	 * @param vertices a graph to be written to a file
	 * @param rowDataOnly whether we write only row data (1-line ~ 1-tree) i.e. without header "#NEXUS"
	 * */
	public static String graphToNewickFormat(List<Vertex> vertices, boolean rowDataOnly, Configuration config){
	
		StringBuffer buf = new StringBuffer(); 
		if (!rowDataOnly){
			buf.append("#NEXUS\n\n");
			buf.append("Begin trees;\n");
			buf.append("tree TREE1 = [&R]\n\n");
		}
		List<Vertex> roots = new ArrayList<Vertex>();
		Vertex tempVertex;
		
		/* inspect all first nodes with the same time, they don`t have an incoming edge */
		for (int i=0; i<vertices.size(); i++){
			tempVertex = vertices.get(i);
			if (tempVertex.getIncomingEdge() == null){
				roots.add(tempVertex);
			} else {
				break;//has an incoming edge, has parent
			}
		}
		
		Vertex root = null;
		if (roots.size() == 1){
			/* we have only one root */
			root = roots.get(0);
		} else {
			/* we have more roots, introduce their common parent */
			root = new Vertex("root", "");
			for (int i=0; i<roots.size(); i++){
				tempVertex = roots.get(i);
				root.setOutcomingEdge(tempVertex);
				tempVertex.setIncomingEdge(root);
			}
		}
		
		/* start DFS from the root */
		dfs(root, buf, config);
		
		if (!rowDataOnly){
			buf.append(";\n\nend;");
		} else {
			buf.append(";\n");
		}
		return buf.toString();
	}
	
	
	/**
	 * Run DFS on a tree and store it in Newick format to a buffer.
	 * */
	private static void dfs(Vertex vertex, StringBuffer buf, Configuration config){
		
		List<Vertex> outcomingEdges = vertex.getOutcomingEdges();
		
		if (outcomingEdges.size() == 0){
			/* vertex is a leaf, output it */
			if (config.getNoIntNodeLabels()){
				buf.append(vertex.getName() + ":" + vertex.getGraphMutationCount()); //get only name!!!
			} else {
				buf.append(vertex.getGraphVertexName() + ":" + (vertex.getGraphMutationCount() + 0.5)); 
			}
		} else {
			/* vertex is an internal node, output first all its children */
			buf.append("(");
			dfs(outcomingEdges.get(0),buf, config);
			for(int i=1; i<outcomingEdges.size(); i++){
				buf.append(",");
				dfs(outcomingEdges.get(i),buf,config);
			}
			buf.append(")");
			if (config.getNoIntNodeLabels()){
				buf.append(":" + vertex.getGraphMutationCount()); //!!!
			} else {
				buf.append(vertex.getGraphVertexName() + ":" + (vertex.getGraphMutationCount() + 0.5));
			}
		}
	}
	
	
	/**
	 * Creates a file writes a string closes a file.
	 * */
	public static void stringToFile(String inputStr, String outputFileName){
		
		try {
			/* create file */
			FileWriter fstream = new FileWriter(outputFileName);
			BufferedWriter out = new BufferedWriter(fstream);
			
			/* write string */
			out.write(inputStr); 
				 
			/* close the output stream */
		    out.close();
		} catch (Exception e){
			log.error("An exception..",e);
		}		
	}
	
}
