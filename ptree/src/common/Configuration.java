package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.Charset;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.digester.Digester;


/**
 * Contains all configuration info.
 * 
 * Configuration is stored in two files config.xml (can be modified by user) 
 * and config (should not be modified by user).
 * 
 * */
public class Configuration {

	private Log log;
	
	private File defaultFilePath = new File(".");
	private String defaultConfigFilePath = ".";
	private String defaultConfigXMLPath = "config.xml";
	
	private File filenameDnaSeq = null;
	private File filenameDnaSeqMST = null;
	
	private File filenameAttributes = null;
	
	private File fileOutput = null;
	private File fileOutputMST = null;
	
	private Boolean readUserNonEditableConfig = null;
	
	private char csvDelimiter = '\t';
	
	private String csvCharSet = "UTF-8";
	
	/* config.xml */
	
	//dna
	private byte adenine = 'A';
	private byte thymine = 'T';
	private byte guanine = 'G';
	private byte cytosine = 'C';
	private byte maskChar = '*';
	private byte gapChar = '-';
	private boolean countGapAsChange = true; // <dna><countGapAsChange>true</countGapAsChange>
	
	//ptree
	private int maskingSitesCountMin = 10; // <ptree><maskingSitesCountMin>10</maskingSitesCountMin> <!-- default 10 -->
	private int maskingSitesCountMax = 10; // <ptree><maskingSitesCountMax>10</maskingSitesCountMax> <!-- default 10 -->
	private int samplingIterCount = 0;     // <ptree><samplingIterCount>0</samplingIterCount> <!-- default 20 -->
	private int threadCount = 1;
	private int skipDatasetNum = 0;
	private double deleteIntCoef = 0.1; //?
	
	private int intMaxProcess = 5000;
	private int intStrategy = 3;
	private double intStrategyCoefficient = 4.0;
	private double intStrategyThreshold = 0.7;
	private int intStrategyMinIntAtNode = 1;
	private boolean intFilterViaLocalTopology = true;
	private double intFilterViaLocalTopologyThreshold = 0.1;
	
	//??
	private int mstImplementationThreshold = 50; // if the dataset has more than "threshold" vertices, the Jarnik/Prim implementation that is good for smaller datasets (up to approx 1000) switches to Boruvka that is good from approx 250
	private int mstOptimizationThresholdVertexCount = 30; // if the number of vertices is bigger than "threshold" optimization can be used 
	private double mstOptimizationThresholdDeletedPart = 0.01; // if less than (threshold)*100% vertices were deleted optimization can be used
	private boolean usePrimOptimization = true; // optimization for complete graphs (recommended)
	
	boolean testDnaCharRepresentation = true;
	boolean testOutputTreeStructure = true;
	
	private String loggingLevel = "FINEST"; // <logging><level>FINEST <!-- SEVERE (highest), WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest)  -->
	private boolean trace = false;
	private int traceMode = 0;
	
	public static final byte NO_CORRECTION = 0;
	public static final byte JC_CORRECTION = 1;
	public static final byte KIMURA_CORRECTION = 2;
	
	private byte vnjImplementation = 1; // 0 ~ java; 1 ~ clearcut
	private byte vnjCorrection = NO_CORRECTION;
	private byte vnjRelaxedVersion = 1;// 0 ~ normal version; 1 ~ relaxed version
	private byte vnjVerbose = 0; //0~false 1~true
	
	private boolean computeFitchCost = true;
	private boolean reconstructOutputTree = false;
	private boolean noIntNodeLabels = true;
	
	public static final int TREE_COMPARISON_CURRENT_COST = 0;
	public static final int TREE_COMPARISON_CURRENT_FITCH_COST = 1;
	public static final int TREE_COMPARISON_CURRENT_FITCH_COST_ORIGINALS_LEAFS = 2;
	
	private int treeComparisonMethod = TREE_COMPARISON_CURRENT_FITCH_COST_ORIGINALS_LEAFS; 
	private boolean useMetropolis = false; // <treeComparison><useMetropolis>false</useMetropolis> <!-- use the Metropolis-Hastings algorithm to choose the next tree to continue with -->
	private int treeComparisonFunction = 0; // <treeComparisonFunction>0</treeComparisonFunction> <!-- the function to be used, 0 ~ exp^-xb; 1 ~ adaptive -->
	//private double treeComparisonLowerBound = 0.3;
	//private double treeComparisonUpperBound = 0.7;
	private double treeComparisonStartingAR = 0.4; // <treeComparisonStartingAR>0.4</treeComparisonStartingAR>
	private double treeComparisonDecreaseOfAR = 0.02; // <treeComparisonDecreaseOfAR>0.02</treeComparisonDecreaseOfAR>
	private int treeComparisonIterCalibrating = 50; // <treeComparisonIterCalibrating>50</treeComparisonIterCalibrating>
	private int treeComparisonIterWithConstBeta = 200; // <treeComparisonIterWithConstBeta>200</treeComparisonIterWithConstBeta>
	
	private boolean initTreeAsNJ = true;
	private int initTreeBurnInIter = 100;
	private int initTreeAdditionalMaxIter = 10;
	
	private boolean tryNJAsFirstTempTree = false; // <tempTree><tryNJAsFirstTempTree>false <!-- try NJ tree as the first temp tree -->
	private int tempTreeMaxIterCount = 0; // <tempTree><tempTreeMaxIterCount>0 <!-- the maximum number of iterations in which I delete some int vertices and start again -->
	private int tempTreeIterCount = 0; // <tempTree><tempTreeIterCount>0 <!-- after I get X worse trees I break the loop  --> 
	
	private boolean computeCombinedTree = true; // <combinedTree><computeCombinedTree>true <!-- false ~ combined tree won`t be computed, combinedTree will correspond to the tempTree! -->
	private boolean computeCombinedTreeWithMasking = false; // <combinedTree><computeCombinedTreeWithMasking>false
	private int combinedTreeMaxIterCount = 4; // <combinedTree><combinedTreeMaxIterCount>4 <!-- the maximum number of iterations in which I delete some int from the combined tree and start again -->	
	private int combinedTreeIterCount = 2; // <combinedTree><combinedTreeIterCount>2 <!-- after I get X worse trees I break the loop  -->
	
	
	/* set and get methods */
	
	public double getTreeComparisonStartingAR(){
		return this.treeComparisonStartingAR;
	}
	
	public void setTreeComparisonStartingAR(double d){
		this.treeComparisonStartingAR = d;
		//System.out.println("this.treeComparisonStartingAR: " + this.treeComparisonStartingAR);
	}
	 
	public double getTreeComparisonDecreaseOfAR(){
		return this.treeComparisonDecreaseOfAR;
	}
	
	public void setTreeComparisonDecreaseOfAR(double d){
		this.treeComparisonDecreaseOfAR = d;
		//System.out.println("this.treeComparisonDecreaseOfAR: " + this.treeComparisonDecreaseOfAR);
	}
	
	public int getTreeComparisonIterCalibrating(){
		return this.treeComparisonIterCalibrating;
	}
	
	public void setTreeComparisonIterCalibrating(int i){
		this.treeComparisonIterCalibrating = i;
		//System.out.println("this.treeComparisonIterCalibrating: " + this.treeComparisonIterCalibrating);
	}
	
	public int getTreeComparisonIterWithConstBeta(){
		return this.treeComparisonIterWithConstBeta;
	}
	
	public void setTreeComparisonIterWithConstBeta(int i){
		this.treeComparisonIterWithConstBeta = i;
		//System.out.println("this.treeComparisonIterWithConstBeta: " + this.treeComparisonIterWithConstBeta);
	}
	
	public void setTreeComparisonFunction(int i){
		this.treeComparisonFunction = i;
		//System.out.println("this.treeComparisonFunction: " + i);
	}
	
	public int getTreeComparisonFunction(){
		return this.treeComparisonFunction;
	}
	
	/*public void setTreeComparisonLowerBound(double d){
		this.treeComparisonLowerBound = d;
		//System.out.println("this.treeComparisonLowerBound: " + d);
	}
	
	public double getTreeComparisonLowerBound(){
		return this.treeComparisonLowerBound;
	}
	
	public void setTreeComparisonUpperBound(double d){
		this.treeComparisonUpperBound = d;
		//System.out.println("this.treeComparisonUpperBound: " + d);
	}
	
	public double getTreeComparisonUpperBound(){
		return this.treeComparisonUpperBound;
	}*/

	//------------------
	
	public boolean getUseMSTPrimOptimization(){
		return this.usePrimOptimization;
	}
	
	public void setUseMSTPrimOptimization(boolean b){
		this.usePrimOptimization = b;
	}
	
	//tempTreeMaxIterCount
	//tempTreeIterCount
	
	public void setTempTreeMaxIterCount(int i){
		this.tempTreeMaxIterCount = i;
		//System.out.println("this.tempTreeMaxIterCount: " + this.tempTreeMaxIterCount);
	}
	
	public int getTempTreeMaxIterCount(){
		return this.tempTreeMaxIterCount;
	}
	
	public void setTempTreeIterCount(int i){
		this.tempTreeIterCount = i;
		//System.out.println("this.tempTreeIterCount: " + this.tempTreeIterCount);
	}
	
	public int getTempTreeIterCount(){
		return this.tempTreeIterCount;
	}
	
	public void setComputeCombinedTree(boolean b){
		this.computeCombinedTree = b;
		//System.out.println("this.computeCombinedTree: " + this.computeCombinedTree);
	}
	
	public boolean getComputeCombinedTree(){
		return this.computeCombinedTree;
	}
	
	public void setCombinedTreeMaxIterCount(int i){
		this.combinedTreeMaxIterCount = i;
		//System.out.println("this.combinedTreeMaxIterCount: " + this.combinedTreeMaxIterCount);
	}
	
	public int getCombinedTreeMaxIterCount(){
		return this.combinedTreeMaxIterCount;
	}
	
	public void setCombinedTreeIterCount(int i){
		this.combinedTreeIterCount = i;
		//System.out.println("this.combinedTreeIterCount: " + this.combinedTreeIterCount);
	}
	
	public int getCombinedTreeIterCount(){
		return this.combinedTreeIterCount;
	}
	
	public void setComputeCombinedTreeWithMasking(boolean b){
		this.computeCombinedTreeWithMasking = b;
		//System.out.println("this.computeCombinedTreeWithMasking: " + this.computeCombinedTreeWithMasking);
	}
	
	public boolean getComputeCombinedTreeWithMasking(){
		return this.computeCombinedTreeWithMasking;
	}
	
	public void setTraceMode(int i){
		this.traceMode = i;
		//System.out.println("this.traceMode: " + this.traceMode);
	}
	
	public int getTraceMode(){
		return this.traceMode;
	}
	
	
	public void setTryNJAsFirstTempTree(boolean b){
		this.tryNJAsFirstTempTree = b;
		//System.out.println("this.tryNJAsFirstTempTree: " + this.tryNJAsFirstTempTree);
	}
	
	public boolean getTryNJAsFirstTempTree(){
		return this.tryNJAsFirstTempTree;
	}
	
	public void setInitTreeAsNJ(boolean b){
		this.initTreeAsNJ = b;
		//System.out.println("this.initTreeAsNJ: " + this.initTreeAsNJ);
	}
	
	public boolean getInitTreeAsNJ(){
		return this.initTreeAsNJ;
	}
	
	
	public void setInitTreeBurnInIter(int i){
		this.initTreeBurnInIter = i;
		//System.out.println("this.initTreeBurnInIter: " + this.initTreeBurnInIter);
	}
	
	public int getInitTreeBurnInIter(){
		return this.initTreeBurnInIter;
	}
	
	public void setInitTreeAdditionalMaxIter(int i){
		this.initTreeAdditionalMaxIter = i;
		//System.out.println("initTreeAdditionalMaxIter: " + this.initTreeAdditionalMaxIter);
	}
	
	public int getInitTreeAdditionalMaxIter(){
		return this.initTreeAdditionalMaxIter;
	}
	
    //<initTreeBurnInIter>100</initTreeBurnInIter>
   	//<initTreeAdditionalMaxIter>10</initTreeAdditionalMaxIter>
	
	
	public void setTreeComparisonMethod(int c){
		this.treeComparisonMethod = c;
		//System.out.println("this.treeComparisonMethod: " + this.treeComparisonMethod);
	}
	
	public void setUseMetropolis(boolean b){
		this.useMetropolis = b;
		//System.out.println("this.useMetropolis: " + this.useMetropolis);
	}
	
	public int getTreeComparisonMethod(){
		return this.treeComparisonMethod;
	}
	
	public boolean getUseMetropolis(){
		return this.useMetropolis;
	}
	
	public void setDeleteIntCoef(double d){
		this.deleteIntCoef = d;
		//System.out.println("this.deleteIntCoef :" + this.deleteIntCoef);
	}
	
	public double getDeleteIntCoef(){
		return this.deleteIntCoef;
	}
	
	
	//maskingSitesCountMin
	public void setMaskingSitesCountMin(int m){
		this.maskingSitesCountMin = m;
		//System.out.println("this.maskingSitesCountMin :" + this.maskingSitesCountMin);
	}
	
	public void setMaskingSitesCountMax(int m){
		this.maskingSitesCountMax = m;
		//System.out.println("this.maskingSitesCountMax :" + this.maskingSitesCountMax);
	}
	
	public int getMaskingSitesCountMax(){
		return this.maskingSitesCountMax;
	}
	
	public int getMaskingSitesCountMin(){
		return this.maskingSitesCountMin;
	}
	
	public void setIntFilterViaLocalTopologyThreshold(double t){
		this.intFilterViaLocalTopologyThreshold = t;
		//System.out.println("this.intFilterViaLocalTopologyThreshold: " + t);
	}
	
	public double getIntFilterViaLocalTopologyThreshold(){
		return this.intFilterViaLocalTopologyThreshold;
	}
	
	public void setIntFilterViaLocalTopology(boolean b){
		this.intFilterViaLocalTopology = b;
		//System.out.println("this.intFilterViaLocalTopology: " + b);
	}
	
	public boolean getIntFilterViaLocalTopology(){
		return this.intFilterViaLocalTopology;
	}
	
	public void setIntStrategyThreshold(double s){
		this.intStrategyThreshold = s;
		//System.out.println("this.intStrategyThreshold: " + s);
	}
	
	public void setIntStrategyMinIntAtNode(int i){
		this.intStrategyMinIntAtNode = i;
		//System.out.println("this.intStrategyMinIntAtNode: " + i);
	}

	public double getIntStrategyThreshold(){
		return this.intStrategyThreshold;
	}
	
	public int getIntStrategyMinIntAtNode(){
		return this.intStrategyMinIntAtNode;
	}
	
	
	public void setComputeFitchCost(boolean f){
		this.computeFitchCost = f;
		//System.out.println("this.computeFitchCost: " + f);
	}
	
	public boolean getComputeFitchCost(){
		return this.computeFitchCost;
	}
	
	public boolean getNoIntNodeLabels(){
		return this.noIntNodeLabels;
	}
	
	public void setNoIntNodeLabels(boolean f){
		this.noIntNodeLabels = f;
		//System.out.println("this.noIntNodeLabels: " + f);
	}
	
	public void setReconstructOutputTree(boolean f){
		this.reconstructOutputTree = f;
		//System.out.println("this.reconstructFitchTree: " + f);
	}
	
	public boolean getReconstructOutputTree(){
		return this.reconstructOutputTree;
	}
	 
	 
	
	public void setMstImplementationThreshold(int i){
		mstImplementationThreshold = i;
		//System.out.println("mst: " + i);
	}
	
	public int getMstImplementationThreshold(){
		return mstImplementationThreshold;
	}

	public void setMstOptimizationThresholdVertexCount(int s){
		 this.mstOptimizationThresholdVertexCount = s;
		 //System.out.println("this.mstOptimizationThresholdVertexCount " + s);
	} 
	
	 public int getMstOptimizationThresholdVertexCount(){
		 return this.mstOptimizationThresholdVertexCount;
	} 
	
	public void setMstOptimizationThresholdDeletedPart(double s){
		this.mstOptimizationThresholdDeletedPart = s;
		//System.out.println("this.mstOptimizationThresholdDeletedPart " + s);
	}
	 
	public double getMstOptimizationThresholdDeletedPart(){
		return mstOptimizationThresholdDeletedPart;
	}
	
	 
	public void setIntMaxProcess(int i){
		this.intMaxProcess = i;
		//System.out.println("this.intMaxProcess: " + this.intMaxProcess);
	}
	
	public int getIntMaxProcess(){
		return intMaxProcess;
	}
	
	public void setIntStrategy(int i){
		this.intStrategy = i;
		//System.out.println("this.intStrategy: " + this.intStrategy);
	}
	
	public int getIntStrategy(){
		return this.intStrategy;
	}
	
	public void setIntStrategyCoefficient(double d){
		this.intStrategyCoefficient = d;
		//System.out.println("this.intStrategyCoefficient: " + this.intStrategyCoefficient);
	}
	
	public double getIntStrategyCoefficient(){
		return this.intStrategyCoefficient;
	}
	
	
	public void setSkipDatasetNum(int s){
		this.skipDatasetNum = s;
	}
	
	public int getSkipDatasetNum(){
		return this.skipDatasetNum;
	}
	
	public void setImplementation(int n){
		this.vnjImplementation = (byte)n;
		//System.out.println("impl" + n);
	}
	
	public void setCorrection(int n){
		this.vnjCorrection = (byte)n;
		//System.out.println("corr" + n);
	}
	
	public void setRelaxedVersion(int n){
		this.vnjRelaxedVersion = (byte)n;
		//System.out.println("rel" + n);
	}
	
	
	public byte getNjImplementation(){
		return this.vnjImplementation;
	}
	
	public byte getNjCorrection(){
		return this.vnjCorrection;
	}
	
	public byte getNjRelaxedVersion(){
		return this.vnjRelaxedVersion;
	}
	
	public byte getVerbose(){
		return this.vnjVerbose;
	}

	
	public void setAdenine(char a){
		adenine = (byte)a;
		//System.out.println("!!!a: " + a);
	}
	
	public void setThymine(char t){
		thymine = (byte)t;
		//System.out.println("!!!t: " + t);
	}
	
	public void setGuanine(char g){
		guanine = (byte)g;
		//System.out.println("!!!g: " + g);
	}
	
	public void setCytosine(char c){
		cytosine = (byte)c;
		//System.out.println("!!!c: " + c);
	}
	
	public void setMask(char c){
		maskChar = (byte)c;
		//System.out.println("!!!mask char: " + c);
	}
	
	public void setGap(char c){
		gapChar = (byte)c;
		//System.out.println("!!!gap char: " + c);
	}
	
	public void setCountGapAsChange(boolean b){
		countGapAsChange = b;
		//System.out.println("count gap as change: " + b);
	}
	
	public byte getA(){
		return adenine;
	}
	
	public byte getT(){
		return thymine;
	}
	
	public byte getG(){
		return guanine;
	}
	
	public byte getC(){
		return cytosine;
	}
	
	public byte getMaskChar(){
		return maskChar;
	}
	
	public byte getGapChar(){
		return gapChar;
	}
	
	public boolean getCountGapAsChange(){
		return countGapAsChange;
	}
	
	public void setThreadCount(int tc){
		threadCount = tc;
		//System.out.println("tc:" + tc);
	}
	
	/*public void setMaskingSitesCount(int msc){
		maskingSitesCount = msc;
		//System.out.println("msc:" + msc);
	}*/
	
	public void setSamplingIterCount(int sic){
		samplingIterCount = sic;
		//System.out.println("samplingIterCount:" + sic);
	}
	
	public int getThreadCount(){
		return threadCount;
	}
	
	/*public int getMaskingSitesCount(){
		return maskingSitesCount;
	}*/
	
	public int getSamplingIterCount(){
		return samplingIterCount;
	}
	
	
	public boolean getTestDnaCharRepresentation(){
		return testDnaCharRepresentation;
	}
	
	
	public void setDnaCharRepresentation(boolean t){
		testDnaCharRepresentation = t;
		//System.out.println("char rep test:"+t);
	}
	
	public boolean getTestOutputTreeStructure(){
		return testOutputTreeStructure;
	}
	
	public void setOutputTreeStructure(boolean t){
		testOutputTreeStructure = t;
		//System.out.println("output ts:"+t);
	}
	
	
	public synchronized void setDefaultFilePath(File defaultDir){
		this.defaultFilePath = defaultDir;
	}
	
	public synchronized File getDefaultFilePath(){
		return defaultFilePath;
	}
	
	public File getFileDnaSeq(){
		return filenameDnaSeq;
	}
	
	public File getFileDnaSeqMST(){
		return filenameDnaSeqMST;
	}
	
	public File getFileAttributes(){
		return filenameAttributes;
	}
	
	public File getFileOutput(){
		return fileOutput;
	}
	
	public File getFileOutputMST(){
		return fileOutputMST;
	}
	
	public synchronized char getCsvDelimiter(){
		return csvDelimiter;
	}
	
	public synchronized String getCsvCharSet(){
		return csvCharSet;
	}
	
	public void setLevel(String l){
		this.loggingLevel = l;
		//System.out.println("logging leven: " + l);
	}
	
	public void setTrace(boolean t){
		//System.out.println("trace: " + t);
		trace = t;
	}
	
	public boolean getTrace(){
		return trace;
	}
	
	
	public String getLoggingLevel(){
		return this.loggingLevel;
	}
	
	public Configuration(){
		log = LogFactory.getLog(Configuration.class);
	}
	
	
	public Configuration(String configFile){
		log = LogFactory.getLog(Configuration.class);
		this.defaultConfigXMLPath = configFile;
	}
	
	
	/**
	 * Read config and config.xml 
	 * 
	 * @return whether the file config.xml has been successfully read;
	 * */
	public boolean readConfigFiles(boolean readUserNonEditableConfig){
		
		
		if (this.readUserNonEditableConfig == null){
			this.readUserNonEditableConfig = readUserNonEditableConfig;
		} else {
			log.warn("Reads the configuration for the second time!\n");
		}
		
		/* read config */
		
		if (this.readUserNonEditableConfig) {
			CsvReader configReader = null;
		
			try {
				
				/* file name, csv delimiter, char set */
				configReader = new CsvReader(defaultConfigFilePath + File.separator + "config", '\t', Charset.forName("UTF-8"));
				File file;
				
				if (configReader.readRecord()){
					
					file = new File(configReader.get(0));
					if (file.exists()){
						defaultFilePath = file;
					}
					
					if (configReader.readRecord()){
						file = new File(configReader.get(0));
						if (file.exists()){
							filenameDnaSeq = file;
						}
						
						if (configReader.readRecord()){
							file = new File(configReader.get(0));
							if (file.exists()){
								filenameAttributes = file;
							}
							
							if (configReader.readRecord()){
								file = new File(configReader.get(0));	
								if (!file.toString().equals("null")){
									fileOutput = file;
								}
								
								if (configReader.readRecord()){
									file = new File(configReader.get(0));
									if (file.exists()){
										filenameDnaSeqMST = file;
									}
									
									if (configReader.readRecord()){
										file = new File(configReader.get(0));
										if (file.exists()){
											fileOutputMST = file;
										}
									}
								}
							}
						}
					} 	
				} 
				
			} catch (FileNotFoundException ex){
				log.error("File \"config\" not found exception.",ex);
			} catch (IOException ex2){
				log.error("IO exception occured while the file \"config\" has been read.",ex2);
			} finally {
				configReader.close();
			}
		}
		
		/* we don`t report if an error ocurred while reading config, default values are returned instead */
		
		/* read config.xml */
		
		try {
            Digester digester = new Digester();
            
            /* push this object on the stack */
            digester.push(this);

            /* dna */
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/adenine");
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/thymine");
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/guanine");
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/cytosine");
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/mask");
            digester.addBeanPropertySetter("configuration/dna/charRepresentation/gap");
            digester.addBeanPropertySetter("configuration/dna/countGapAsChange");
            
            /* ptree */
            
            //digester.addBeanPropertySetter("configuration/ptree/maskingSitesCount");
            digester.addBeanPropertySetter("configuration/ptree/maskingSitesCountMin");
            digester.addBeanPropertySetter("configuration/ptree/maskingSitesCountMax");
            digester.addBeanPropertySetter("configuration/ptree/samplingIterCount");
            digester.addBeanPropertySetter("configuration/ptree/threadCount");
            digester.addBeanPropertySetter("configuration/ptree/skipDatasetNum");
       
            /* ptree/intermediates */
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intMaxProcess"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intStrategy"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intStrategyCoefficient"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intStrategyThreshold"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intStrategyMinIntAtNode"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intFilterViaLocalTopology"); 
            digester.addBeanPropertySetter("configuration/ptree/intermediates/intFilterViaLocalTopologyThreshold");
            digester.addBeanPropertySetter("configuration/ptree/intermediates/deleteIntCoef");
            
            /* mst */
            digester.addBeanPropertySetter("configuration/mst/mstImplementationThreshold");
            digester.addBeanPropertySetter("configuration/mst/mstOptimizationThresholdVertexCount");
            digester.addBeanPropertySetter("configuration/mst/mstOptimizationThresholdDeletedPart");
            digester.addBeanPropertySetter("configuration/mst/useMSTPrimOptimization");
            
            /* nj alg */
            digester.addBeanPropertySetter("configuration/njAlg/implementation");
            digester.addBeanPropertySetter("configuration/njAlg/correction");
            digester.addBeanPropertySetter("configuration/njAlg/relaxedVersion");
      
            /* tree comparison */
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonMethod");
            digester.addBeanPropertySetter("configuration/treeComparison/useMetropolis");
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonFunction");
            //digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonLowerBound");
            //digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonUpperBound");
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonStartingAR");
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonDecreaseOfAR");
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonIterCalibrating");
            digester.addBeanPropertySetter("configuration/treeComparison/treeComparisonIterWithConstBeta");
            
            /* init tree */
            digester.addBeanPropertySetter("configuration/initTree/initTreeAsNJ");
            digester.addBeanPropertySetter("configuration/initTree/initTreeBurnInIter");
            digester.addBeanPropertySetter("configuration/initTree/initTreeAdditionalMaxIter");
           
            /* temp tree */
            digester.addBeanPropertySetter("configuration/tempTree/tryNJAsFirstTempTree");
            digester.addBeanPropertySetter("configuration/tempTree/tempTreeMaxIterCount");
            digester.addBeanPropertySetter("configuration/tempTree/tempTreeIterCount");
            
            /* combined tree */
            digester.addBeanPropertySetter("configuration/combinedTree/computeCombinedTree");
            digester.addBeanPropertySetter("configuration/combinedTree/combinedTreeMaxIterCount");
            digester.addBeanPropertySetter("configuration/combinedTree/combinedTreeIterCount");
            digester.addBeanPropertySetter("configuration/combinedTree/computeCombinedTreeWithMasking");
            
            /* output tree */
            digester.addBeanPropertySetter("configuration/outputTree/computeFitchCost");
            digester.addBeanPropertySetter("configuration/outputTree/reconstructOutputTree");
            digester.addBeanPropertySetter("configuration/outputTree/noIntNodeLabels");
            
            /* test */
            digester.addBeanPropertySetter("configuration/test/dnaCharRepresentation");
            digester.addBeanPropertySetter("configuration/test/outputTreeStructure");
           
            /* logging */
            digester.addBeanPropertySetter("configuration/logging/level");
            digester.addBeanPropertySetter("configuration/logging/trace");
            digester.addBeanPropertySetter("configuration/logging/traceMode");
            
            /* parse */
            Configuration config = (Configuration) digester.parse(new File(this.defaultConfigXMLPath));
            
            if (config == null){
            	throw new Exception("Parser returned null value.");
            }
            
        } catch (Exception e) {
        	log.error("Parse error: ",e); 
            return false;
        }
		
        /* set trace */
        Trace.printingOn = this.getTrace();
        Trace.printingMode = this.getTraceMode();
        
        //log.info("config.xml: a: " + adenine + " t: " + thymine + " g: " + guanine + " c: " + cytosine +
        //		" testDnaCharRepresentation: " + this.testDnaCharRepresentation);
        
		return true;
	}
	
	
	/**
	 * Writes the configuration info in the config file on exit.
	 * */
	public synchronized void writeChangesOnExit(File fileDnaSeq, File fileNodeAttributes, File fileOutput,
			File fileDnaSeqMST, File fileOutputMST){
		
		/* write config file */
		
		if ((this.readUserNonEditableConfig != null) && this.readUserNonEditableConfig){
		
			/* creates csv writer with parameters: CSV file name, delimiter, char set */
			CsvWriter configWriter = new CsvWriter(defaultConfigFilePath + File.separator + "config",'\t',Charset.forName("UTF-8"));
			
			try {
				configWriter.writeRecord(new String[]{defaultFilePath.toString()});
				if (fileDnaSeq != null){
					configWriter.writeRecord(new String[]{fileDnaSeq.toString()});
				} else {
					configWriter.writeRecord(new String[]{"null"});
				}
				if (fileNodeAttributes != null){
					configWriter.writeRecord(new String[]{fileNodeAttributes.toString()});
				} else {
					configWriter.writeRecord(new String[]{"null"});
				}
				if (fileOutput != null){
					configWriter.writeRecord(new String[]{fileOutput.toString()});
				} else {
					configWriter.writeRecord(new String[]{"null"});
				}
				if (fileDnaSeqMST != null){
					configWriter.writeRecord(new String[]{fileDnaSeqMST.toString()});
				} else {
					configWriter.writeRecord(new String[]{"null"});
				}
				if (fileOutputMST != null){
					configWriter.writeRecord(new String[]{fileOutputMST.toString()});
				} else {
					configWriter.writeRecord(new String[]{"null"});
				}
			} catch (IOException ex){
				log.error("An error occured while the ",ex);
			} finally {
				configWriter.close();
			}
		}
	}
	
	
	/**
	 * Test.
	 */
	public static void main(String[] args) {

		Configuration config = new Configuration();
		
		config.readConfigFiles(true);
		
		System.out.println(" \ndefault file path: " + config.getDefaultFilePath() + 
				           " \ndna seq: " + config.getFileDnaSeq() + 
				           " \nfile attributes: " + config.getFileAttributes() +
				           " \nfile output: " + config.getFileOutput());
		
	}
}
