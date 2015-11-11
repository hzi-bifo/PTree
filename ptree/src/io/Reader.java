package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptree.Vertex;


import com.csvreader.CsvReader;
import common.Configuration;


/**
 * Manages reading of multiple input sets from one file.
 * Create an instance, open a file, then read inputs one by one, close the file.
 * */
public class Reader {

	private Configuration config;
	private CsvReader seqReader;
	
	private Log log;
	
	/**
	 * Constructor.
	 * */
	public Reader(Configuration config){
		this.config = config;
		log = LogFactory.getLog(Reader.class);
	}
	
	
	/**
	 * Open a given file for reading.
	 * */
	public boolean open(File inputFile){
		
		try {
			
			/* file name, csv delimiter, char set */
			seqReader = new CsvReader(inputFile.getPath(), config.getCsvDelimiter(), 
					Charset.forName(config.getCsvCharSet()));
			
			return true;
			
		} catch (FileNotFoundException ex){
			log.error("File not found exception. (" + inputFile.getPath() + ")",ex);
			return false;
		}
			
	}
	
	
	/**
	 * Reads one input set from a file (only name, DNA).
	 * @return set of vertices or null
	 * */
	public List<Vertex> getNextInputSet(){
		
		String name;
		String dna;
		int count;
		List<Vertex> list = null;
		
		try {
			
			if (!seqReader.readRecord()){
				return null;
			}
			
			StringTokenizer stringTokenizer = new StringTokenizer(seqReader.get(0));
			
			if (!stringTokenizer.hasMoreElements()){
				return null;
			}
			
			try {
				count = new Integer(stringTokenizer.nextToken());
			} catch (Exception e){
				return null;
			}
			
			list = new ArrayList<Vertex>(count*2);
			
			while(seqReader.readRecord()){
				
				stringTokenizer = new StringTokenizer(seqReader.get(0));
				if (seqReader.getColumnCount() == 1){
					stringTokenizer = new StringTokenizer(seqReader.get(0));
				} else {
					stringTokenizer = new StringTokenizer(seqReader.get(0) + " " + seqReader.get(1));
				}
				
				name = stringTokenizer.nextToken();
				dna = stringTokenizer.nextToken();
				list.add(new Vertex(name,dna));
				
				count--;
				if (count == 0){
					break;
				}
				
			}
			
		} catch (IOException ex){
			log.error("IOException occured.",ex);
			return null;
		}
		
		return list;
	}
	
	
	/** Close a file. */
	public void close(){
		seqReader.close();
	}
	
}
