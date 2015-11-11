package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import common.Configuration;


/**
 * Creates panel to select a file.
 * */
public class FileChooserPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private Configuration config;
	
	private int type;
	
	private JTextField filePathTextField;
	
	private JButton openButton;
	
	private File file;
	
	private Notificator notificator;
	
	private String openDialogueTitle;
	
	private String[] extensions;
	
	private Log log; 
	
	
	/**
	 * Constructor.
	 * 
	 * @param extensions list of all extensions that can be chosen
	 * */
	protected FileChooserPanel(Configuration config, int type, String borderLabel, String toolTipTextField, 
			String toolTipButton, String openDialogueTitle, String[] extensions, File file){
		super(false);
		
		setLayout(new GridBagLayout());
		
		log = LogFactory.getLog(FileChooserPanel.class);
		
		this.config = config;
		this.type = type;
		this.file = file;
		this.openDialogueTitle = openDialogueTitle;
		this.extensions = extensions;
		
		notificator = new Notificator();
		
		filePathTextField = new JTextField(50);
		filePathTextField.setToolTipText(toolTipTextField);
		filePathTextField.setEditable(false);
		filePathTextField.setBackground(Color.white);
		if (file != null){
			filePathTextField.setText(file.toString());
		}
		
		openButton = new JButton("Choose");
		openButton.setToolTipText(toolTipButton);
		openButton.setPreferredSize(new Dimension(80,21));
		openButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				 
				openButton.setEnabled(false);
				
				String filename = null;
						
				try {
					
					filename = Dialogue.fileDialogue(
							FileChooserPanel.this.type, FileChooserPanel.this, FileChooserPanel.this.config, 
							FileChooserPanel.this.openDialogueTitle, "all txt files", 
							FileChooserPanel.this.extensions, true);
							
					if (filename != null){
							FileChooserPanel.this.file = new File(filename);
							filePathTextField.setText(FileChooserPanel.this.file.toString());
							notificator.setChanges();
					}

				} catch (Exception ex){
					log.error("An exception occured while the file was being opened.",ex);  
				}
						
				openButton.setEnabled(true);
			}
			
		});
		
		setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(borderLabel),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		GridBagConstraints con;
		
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 0;
        
		this.add(filePathTextField,con);
		
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 0;
		
		this.add(openButton,con);
	}
	
	
	/**
	 * @return file or null
	 * */
	public File getFile(){
		return file;
	}
	
	
	/** 
	 * Register observers that will be notified when an update event occurs.
	 * (when a new filename is chosen)
	 * */
	public void addObserver(Observer o){
		notificator.addObserver(o);
	}
	
	
	/** 
	 * Notificator.
	 * */
	private class Notificator extends Observable  {
		
		/** 
		 * Notify all observers.
		 *  */
		public synchronized void setChanges(){
			setChanged();
			notifyObservers();
		}
		
	}
}
