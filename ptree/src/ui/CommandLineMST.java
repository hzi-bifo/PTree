package ui;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptree.PTree;


import common.Configuration;


/**
 * Main class that starts the program as a Java Command Line application. 
 * */
public class CommandLineMST {

	private Log log;
	
	String inputFileName;
	String outputFileName;
	
	
	/**
	 * Constructor.
	 * */
	public CommandLineMST(String inputFileName, String outputFileName, String configFile){
		
		log = LogFactory.getLog(CommandLineMST.class);
		
		Configuration config;
    	
    	if (configFile != null){
    		config = new Configuration(configFile);
    	} else {
    		config = new Configuration();
    	}
    	
    	if (!config.readConfigFiles(false)){
    		String msg = "Reading of configuration files failed, default setting will be used!";
    		log.warn(msg);
    		System.err.println(msg);
    		return;
    	}
    	
		try {
   		 
   		 	setUpLogging("." + File.separator + "log"  + File.separator + "log", 
   		 			config.getLoggingLevel(), (1048576*20) /*1MB*/ ,30);
   		 
    	} catch (Exception ex){
    		log.error("Logging file configuration failed.",ex); 
    	}

    	//log.info("Program MST command line started.");
		
    	try {
    		
			/* run the MST alg. version */
			PTree ptree = new PTree(config,new File(inputFileName),
					new File(outputFileName));
			
			ptree.runMST();
			
		} catch (Exception ex){
			log.error("An exception occured..",ex);  
		}
    	
		//log.info("Program MST command line finished.");
	}
	
	
	/** 
     * Set up logging settings.
     * 
     * @param logFilePath path of the directory with log files 
     * @param logLevel logging level (log all messages of this level and higher) 
     * @param logFileSize max. size of log files in bytes
     * @param logFileCount max. number of log files in the directory
     * */
    private void setUpLogging(String logFilePath, String logLevel, int logFileSize, 
    		int logFileCount) throws Exception { 
    	
        LogManager.getLogManager().reset();
        LogManager.getLogManager().readConfiguration();
        Logger.getLogger("branching").setLevel(Level.OFF); 
        
        
        File logFile = new File(logFilePath);
        if (logFile.getParentFile() != null) 
            logFile.getParentFile().mkdirs();
        Handler fh = new FileHandler(logFile.getPath(),logFileSize,logFileCount); 
        fh.setLevel(Level.parse(logLevel));
        fh.setFormatter(new SimpleFormatter());
        Logger.getLogger("").addHandler(fh); 
    }    
	
	
    /**
	 * Starts the program in a separate thread as a Java Command Line application.
	 * 
	 * @param args input file name, output file name, the directory that contains configuration files
	 */
	public static void main(String[] args) {
		
		String configFilePath = null;
		
		if ((args.length == 0) || args[0].equals("-h")){
			System.out.println("PTree arguments: input_file output_file [config_file_xml]");
			return;
		}
		
		if (args.length > 2){
			configFilePath = args[2];
		}
		
		new CommandLineMST(args[0], args[1], configFilePath);
	}

}
