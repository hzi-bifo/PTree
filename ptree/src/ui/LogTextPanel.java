package ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The panel to which the text can be added line by line.
 * */
public class LogTextPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private JTextPane textPanel;
	
	private StyledDocument logDoc;
	
	private Log log; 
	
	
	/**
	 * Constructor.
	 * 
	 * @param size preferred size of the text panel 
	 * */
	LogTextPanel(Dimension size){
		
		super(false);
		setLayout(new GridBagLayout());
		
		log = LogFactory.getLog(LogTextPanel.class);
		
		setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Log"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		
		textPanel = new JTextPane();
		textPanel.setEditable(false);
		textPanel.setToolTipText("Shows steps of the program."); 
		JScrollPane editorScrollPane = new JScrollPane(textPanel);
		editorScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setPreferredSize(size);
		
		/* document to be displayed */
		logDoc = textPanel.getStyledDocument();
		
		GridBagConstraints con;
		
		con = new GridBagConstraints();
		con.insets = new Insets(5,5,5,5);
		con.gridx = 0;
		con.gridy = 0;
		con.weightx = 1.0;
		con.weighty = 1.0;
		con.fill = GridBagConstraints.BOTH;
        
		this.add(textPanel,con);
		
	}
	
	
	/**
	 * Adds one line to the log panel.
	 * */
	public void addLine(String text){
		
		try {
		
			logDoc.insertString(logDoc.getLength(), text + "\n", null);			 
		    textPanel.scrollRectToVisible(new Rectangle(0, textPanel
		    		.getHeight(), 0, textPanel.getHeight()));
		
		} catch (BadLocationException e) {
		    log.error("Couldn't insert text into text pane.",e); 
		}
	}
	
}
