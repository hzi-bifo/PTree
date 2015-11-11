package ui;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;
import common.ExampleFileFilter;



/**
 * Contains static methods to show simple dialogues.
 * */
public class Dialogue {

	public static final int OPEN_FILE_DIALOGUE_TYPE = 1;
	public static final int SAVE_FILE_DIALOGUE_TYPE = 2;
	
	private static Log log = LogFactory.getLog(Dialogue.class);
	
	
	/** 
	 * Shows an open file dialogue.
	 * 
	 * @param component parent component
	 * @param extensions list of all extensions that can be chosen
	 * @param whether it is possible to choose *.*
	 * 
	 * @return file name or null if canceled 
	 * */
	public static String fileDialogue(int type, JComponent component, Configuration config, String title, String description, 
			String[] extensions, boolean allExtensions){
		
			JFileChooser chooser = new JFileChooser();
	        ExampleFileFilter filter = new ExampleFileFilter();
	        chooser.setCurrentDirectory(config.getDefaultFilePath());
	        
	        chooser.setDialogTitle(title);
	        /* you can choose *.*         */
	        chooser.setAcceptAllFileFilterUsed(allExtensions); 
	        
	        if (extensions != null) {
	        	/* set all possible extensions */
	        	for (int i=0; i< extensions.length; i++){ 
	        		filter.addExtension(extensions[i]);
	        	}
	        	filter.setDescription(description); 
		        chooser.setFileFilter(filter);
	        }
	        
	        switch (type){
	        case OPEN_FILE_DIALOGUE_TYPE: 
	        	if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION){   
	        		/* no file has been chosen */
		            return null;
		        }
	        	break;
	        case SAVE_FILE_DIALOGUE_TYPE: 
	        	if (chooser.showSaveDialog(component) != JFileChooser.APPROVE_OPTION){   
	        		/* no file has been chosen */
	        		return null; 
		        }
	        	break;
	        default: 
	        	log.error("Wrong branche, Dialogue.fileDialogue.");
	         	return null;
	        }
	        
	        String fileName = new String (chooser.getSelectedFile().toString());
	        
	        /* next time will be opened this directory */
	        config.setDefaultFilePath(chooser.getSelectedFile().getParentFile());	
			
			return fileName;
	}
	
	
	/** 
	 * Show "warning message" dialogue. 
	 * */
	public static void showWarningMessage(String message){
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE); 
	}
	
}
