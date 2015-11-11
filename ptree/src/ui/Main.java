package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptree.PTree;
//import ptree.branching.PTreeBranching;

import common.Configuration;
import common.SwingWorker;


/**
 * Main class that starts the program as a Java GUI application. 
 * */
public class Main implements Observer {

	private Configuration config;
	
	private FileChooserPanel panelDnaSeq;
	
	private FileChooserPanel panelDnaSeqMST;
	
	private FileChooserPanel panelAttributes;
	
	private FileChooserPanel panelOutput;
	private FileChooserPanel panelOutputMST;
	
	@SuppressWarnings("unused")
	private LogTextPanel logPanel;
	
	private JButton runButton; 
	private JButton runButtonMST; 
	
	private JFrame mainFrame;
	
	private Log log; 
	
	
	/**
	 * Creates the panel that contains all control components for the MST algorithm version.
	 * */
	private JPanel createPanelMST(){
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		
		Border border = BorderFactory.createTitledBorder(
         		null,//Border
         		null,//"MST Settings",//String title
         		TitledBorder.CENTER,//int titleJustification,
         		TitledBorder.DEFAULT_JUSTIFICATION,//int titlePosition,
                null,//Font titleFont,
                Color.black); //Color titleColor
		
		panel.setBorder(//outside border, inside border
        		BorderFactory.createCompoundBorder(border,BorderFactory.createEmptyBorder(5,5,5,5)));
		
		GridBagConstraints con;
		
		/* panel DNA seq. file */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 0;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panelDnaSeqMST = new FileChooserPanel(config, Dialogue.OPEN_FILE_DIALOGUE_TYPE, "DNA seq. file",
				"File path of the currently chosen file.", "Choose a file that contains a list of DNA sequences.",
				"Choose DNA Seq. file",null,config.getFileDnaSeqMST());
		panelDnaSeqMST.addObserver(this);
		panel.add(panelDnaSeqMST, con);
		
		/* panel output */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 1;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panelOutputMST = new FileChooserPanel(config,Dialogue.SAVE_FILE_DIALOGUE_TYPE, "Output file",
				"File path of the currently chosen file.", "Choose a file to store an output.",
				"Choose file to store results.", new String[]{"txt","nxs"},config.getFileOutputMST());
		panelOutputMST.addObserver(this);
		panel.add(panelOutputMST,con);
		
		/* panel Run */
		JPanel panelRun = new JPanel();
		panelRun.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		panelRun.setLayout(new GridBagLayout());
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 2;
		runButtonMST = new JButton("Run");
		
		if (config.getFileDnaSeqMST() == null || config.getFileOutputMST() == null){
			runButtonMST.setEnabled(false);
		}
			
		panelRun.add(runButtonMST,con);
		
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 2;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panel.add(panelRun,con);
		
		runButtonMST.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				 
				mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				runButtonMST.setEnabled(false);
				
				final SwingWorker worker = new SwingWorker(){
					
					/* thread declaration */
					public Object construct(){
						
						try {
					
							/* run MST alg */
							PTree branching = new PTree(config,panelDnaSeqMST.getFile(),
									panelOutputMST.getFile());
							
							branching.runMST();
							
						} catch (Exception ex){
							log.error("An exception occured..",ex);  
						}
						return null;
					}
					
					public void finished(){
						try {
							//..
						} catch (Exception ex){
							log.error("An exception occured..",ex);
						}
						runButtonMST.setEnabled(true);
						mainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					
				};
				/* run the thread */
				worker.start();
				}
		});
		
		return panel;
	}
	
	
	/**
	 * Creates the panel that contains all control components for the Branching algorithm version.
	 * */
	private JPanel createPanelBranching(){
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		
		Border border = BorderFactory.createTitledBorder(
         		null,//Border
         		"Branching Settings",//String title
         		TitledBorder.CENTER,//int titleJustification,
         		 TitledBorder.DEFAULT_JUSTIFICATION,//int titlePosition,
                 null,//Font titleFont,
                 Color.black); //Color titleColor
                 
		panel.setBorder(//outside border, inside border
        		BorderFactory.createCompoundBorder(border,BorderFactory.createEmptyBorder(5,5,5,5)));

		GridBagConstraints con;
		
		/* panel log */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 0;
		con.weightx = 1.0;
		con.weighty = 1.0;
		con.fill = GridBagConstraints.BOTH;
		con.gridheight = 4;
		logPanel = new LogTextPanel(new Dimension(300, 300));
		//panel.add(logPanel, con);
		
		/* panel DNA seq. file */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 0;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panelDnaSeq = new FileChooserPanel(config, Dialogue.OPEN_FILE_DIALOGUE_TYPE, "DNA seq. file",
				"File path of the currently chosen file.", "Choose a file that contains a list of DNA sequences.",
				"Choose DNA Seq. file",null,config.getFileDnaSeq());
		panelDnaSeq.addObserver(this);
		panel.add(panelDnaSeq, con);
		
		/* panel DNA attributes */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 1;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panelAttributes = new FileChooserPanel(config, Dialogue.OPEN_FILE_DIALOGUE_TYPE, "DNA attributes file",
				"File path of the currently chosen file.", "Choose a file that contains DNA attributes.",
				"Choose DNA attributes file", new String[]{"txt"},config.getFileAttributes());
		panelAttributes.addObserver(this);
		panel.add(panelAttributes,con);
		
		/* panel output */
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 2;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panelOutput = new FileChooserPanel(config,Dialogue.SAVE_FILE_DIALOGUE_TYPE, "Output file",
				"File path of the currently chosen file.", "Choose a file to store an output.",
				"Choose file to store results.", new String[]{"txt","nxs"},config.getFileOutput());
		panelOutput.addObserver(this);
		panel.add(panelOutput,con);
		
		
		/* panel Run */
		JPanel panelRun = new JPanel();
		panelRun.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		panelRun.setLayout(new GridBagLayout());
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 0;
		runButton = new JButton("Run");
		
		if (config.getFileDnaSeq() == null || config.getFileAttributes() == null || config.getFileOutput() == null){
			runButton.setEnabled(false);
		}
			
		panelRun.add(runButton,con);
		
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 3;
		con.weightx = 0.2;
		con.weighty = 0.2;
		con.fill = GridBagConstraints.BOTH;
		panel.add(panelRun,con);
		
		runButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				 
				mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				runButton.setEnabled(false);
				
				final SwingWorker worker = new SwingWorker(){
					
					/* thread declaration */
					public Object construct(){
						
						try {
							
							/*run the Branching algorithm */
							
							//PTreeBranching branching = new PTreeBranching(config,panelDnaSeq.getFile(),panelAttributes.getFile(),
							//		panelOutput.getFile());
							
							//branching.runBranching();
							System.err.println("Branching is not supported anymore!");
							
						} catch (Exception ex){
							log.error("An exception occured..",ex);  
						}
						return null;
					}
					
					public void finished(){
						try {
							//..
						} catch (Exception ex){
							log.error("An exception occured..",ex);
						}
						runButton.setEnabled(true);
						mainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					
				};
				/* run the thread */
				worker.start();
				}
		});
		
		return panel;
	}
	
	
	/**
	 * Creates and shows the main window.
	 * */
	private void createAndShowGUI() {
		
		mainFrame = new JFrame("PTree: Pattern-based, Stochastic Search for Maximum Parsimony Phylogenies.");
		
		Dimension screenSize = new Dimension(950,450);
		
		mainFrame.setPreferredSize(screenSize);
		
		/* how to close program with cross */ 
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                close();
            }
        });
        
        /* Create tabbed pane */
        JTabbedPane tabbedPane = new JTabbedPane();
        
        /* add Panel MST */
        tabbedPane.addTab("PTree",null, createPanelMST(),"Compute Phylogenetic tree using PTree.");
        
        /* add Panel Branching */
        //tabbedPane.addTab("Branching",null, createPanelBranching(),"Compute Phylogenetic tree using MST.");
        createPanelBranching();// removed, since it`s not supported anymore
        
        /* add tabbed panel */
        mainFrame.add(tabbedPane);
        
        mainFrame.pack();
        mainFrame.setVisible(true);
	}
	
	
	/**
	 * Constructor.
	 * */
     private Main(){
    	
    	 config = new Configuration();
      	
      	if (!config.readConfigFiles(true)){
      		String msg = "Reading of configuration files failed, default setting will be used!";
      		log.warn(msg);
      		Dialogue.showWarningMessage(msg);
      	}
      	
    	 try {
    		 
    		 setUpLogging("." + File.separator + "log"  + File.separator + "log", config.getLoggingLevel(), (1048576*500) /*1MB*/,30);
    		 log = LogFactory.getLog(Main.class);
    		 
     	} catch (Exception ex){
     		System.err.println("Logging file configuration failed.");
     		ex.printStackTrace();
     	}

     	createAndShowGUI();
    	try { 
    		//log.info("Program started.");
    	} catch (Exception e){
    		e.printStackTrace();
    	}
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
         Logger.getLogger("branching").setLevel(Level.parse(logLevel)); 
         
         File logFile = new File(logFilePath);
         if (logFile.getParentFile() != null) 
             logFile.getParentFile().mkdirs();
         Handler fh = new FileHandler(logFile.getPath(),logFileSize,logFileCount); 
         fh.setLevel(Level.parse(logLevel));
         fh.setFormatter(new SimpleFormatter());
         Logger.getLogger("").addHandler(fh); 
     }    
	
     
    /**
     * Perform all tasks before program close and close it.
     * */ 
	private void close(){
		
		try {
		config.writeChangesOnExit(panelDnaSeq.getFile(),
				panelAttributes.getFile(),
				panelOutput.getFile(),
				panelDnaSeqMST.getFile(), 
				panelOutputMST.getFile() );
		} catch (Exception e) {
			log.error("",e);
		}
		
    	//log.info("Program closed."); 
    	System.exit(0);
    }
	
	
	@Override
	public void update(Observable arg0, Object arg1) {
		
		if (panelDnaSeq.getFile() != null && panelAttributes.getFile() != null && panelOutput.getFile() != null){
			runButton.setEnabled(true);
		}
		
		if (panelDnaSeqMST.getFile() != null &&  panelOutputMST.getFile() != null){
			runButtonMST.setEnabled(true);
		}
	}

	
	/**
	 * Starts the program in a separate thread as a Java GUI application.
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		new Main();
        	}
        });		
	}
	
}
