package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Computed trees are written one by one to a file in the Newick format.
 * First we {@link #open(File) open} the file. 
 * Then we {@link #add(String) add} trees one by one.
 * Finally we {@link #close() close} the file for writing.
 * */
public class Writer {

	private Log log;
	
	private BufferedWriter out = null;
	
	
	/**
	 * Constructor. 
	 * */
	public Writer(){
		log = LogFactory.getLog(Writer.class);
	}
	
	
	/**
	 * Open a file to write to.
	 * */
	public boolean open(File outputFile){
		
		try {
			/* create a file */
			FileWriter fstream = new FileWriter(outputFile);
			out = new BufferedWriter(fstream);
			
		} catch (Exception e){
			log.error("An exception..",e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * Write a string to a file.
	 * */
	public void add(String str){
		try {
			out.write(str);
		} catch (Exception ex){
			log.error("An exception..",ex);
		}
	}
	
	
	/**
	 * Close a file.
	 * */
	public void close(){
		try { 
			/* close the output stream */
			out.close();
		} catch (Exception ex){
			log.error("An exception..",ex);
		}
	}
	
}
