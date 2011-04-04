package edu.gatech.safety.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.ui.Ui;
import com.solibri.saf.core.Application;
import com.solibri.saf.core.ApplicationFrame;
import com.solibri.saf.core.IView;
import com.solibri.saf.core.ViewPanel;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

import edu.gatech.safety.construction.SafetyFense;
import edu.gatech.safety.rules.SlabRules;

/**
 * This ViewPanel generates some Safety related module's user interfaces using tabbed panels for each module.
 * @author Jin-Kook Lee
 */
public class ConstructionSafetyViewPanel extends ViewPanel implements ActionListener {	
	private static final long serialVersionUID = 1L;
	
	SModel model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
	
	
	static private final String newline = "\n";
    JButton S_open, S_run, S_run2, S_slab, Btn_others;
    
    //JTextArea statusView = SpaceProgramReview_Data.statusView;
    public static JTextArea statusView = new JTextArea();
    
    JFileChooser fc;    
    JComboBox floorList;

    Font font = new Font("Arial", Font.PLAIN, 11);
	
	public ConstructionSafetyViewPanel(IView view) {
		super(view);
		
		// Add Tabs
		JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Construction Safety", null, drawPanel1(), null);
        tabbedPane.addTab("Others", null, drawPanel2(), null);
        
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setToolTipTextAt(1, new String("<html>This module is under development.</html>"));
        tabbedPane.setToolTipTextAt(0, new String("<html>This module has been developed by Georgia Tech, jklee@gatech.edu.</html>"));
        //tabbedPane.setToolTipTextAt(2, new String("<html>This module is under development.</html>"));
        // Disable tabs TODO
        tabbedPane.setEnabledAt(1, true);
        //tabbedPane.setEnabledAt(2, true);
        
        add(tabbedPane);
		initialize();
	}
	
	
	private void initialize() {
		// initialize
	}
	
	
	/* panel for SPR */
	private JPanel drawPanel1() {
		
		//Border empty = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);		
		
		statusView = new JTextArea(5,20);
	    statusView.setMargin(new Insets(5,5,5,5));
	    statusView.setEditable(false);
	    statusView.setOpaque(false); // transparent textarea
	    statusView.setFont(font);
	    JScrollPane statusScrollPane = new JScrollPane(statusView); // status view panel  
	    
	    statusView.append("Status view is ready..\n" +
	    		"----------------------------------------------" + newline);

	    statusScrollPane.setPreferredSize(new Dimension(320, 150));
	    statusScrollPane.setOpaque(false); //transparent
	    statusScrollPane.setBorder(loweredetched);
	    
        fc = new JFileChooser(); // Create a file chooser
        fc.addChoosableFileFilter(new RequirementFileFilter()); // Filter for the appropriate file chooser
        fc.setAcceptAllFileFilterUsed(false);
        
        
        // Selection - Floors
        String[] floorStrings = { "All Floors ---", "under dev" };
        
        floorList = new JComboBox(floorStrings);
        floorList.setSelectedIndex(1);
        floorList.addActionListener(this);
        

        S_open = new JButton("Open Safety Data", 
                createImageIcon("/edu/gatech/safety/res/images/table.gif")); // images update later, or just use JButtons
		S_open.addActionListener(this);
		S_open.setToolTipText("Open safety data test.");
			
		
		S_slab = new JButton("Slabs", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_slab.addActionListener(this);
		S_slab.setToolTipText("Collect all slabs and more +");
		
		
		S_run = new JButton("Safety Fense: Perimeter", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run.addActionListener(this);
		S_run.setToolTipText("Safety Fense for Perimeter");
		
		
		S_run2 = new JButton("Safety Fense: Holes", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run2.addActionListener(this);
		S_run2.setToolTipText("Safety Fense for Holes");
		

		JPanel panelTop = new JPanel(); // button panel
		panelTop.add(S_open);
		
		panelTop.add(floorList);
		
		panelTop.add(S_slab);
		panelTop.add(S_run);
		panelTop.add(S_run2);
//		panelTop.setPreferredSize(new Dimension(320, 30));

		//Add the buttons and the status view to the panel.
		JPanel panelSPR = new JPanel(); // main panel
		panelSPR.setLayout(new FlowLayout()); // border layout
		panelSPR.add(panelTop);
		panelSPR.add(statusScrollPane);

		return panelSPR;
	}
	
		
	
	/* panel for Cost */
	private JPanel drawPanel2() {
		
		Border empty = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		//Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		
		Btn_others = new JButton("Test", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		Btn_others.addActionListener(this);
		Btn_others.setToolTipText("Test.");
		
		JPanel panelButtons = new JPanel(); // button panel
		panelButtons.add(Btn_others);
		panelButtons.setLayout(new FlowLayout());
		
				
		JTextArea co = new JTextArea(5,20);
		co.setMargin(new Insets(5,5,5,5));
		co.setEditable(false);		
		co.setFont(font);
		co.setPreferredSize(new Dimension(320, 130));
		co.setOpaque(false);
		co.setBorder(empty);
		
		co.append("This is an additional panel for others." + newline);
		
		JPanel panelCost = new JPanel(); //use FlowLayout
		panelCost.add(panelButtons, BorderLayout.PAGE_START);
		panelCost.add(co, BorderLayout.PAGE_END);			
		panelCost.setOpaque(true);	

		return panelCost;
	}
	
	
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ConstructionSafetyViewPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            //System.err.println("Couldn't find file: " + path);
            return null;
        }
    }



	
	/* button actions */
	public void actionPerformed(ActionEvent e) {

		
		//  ----------------------------------------- 
        if (e.getSource() == S_open) { 
        	int returnVal = fc.showOpenDialog(ConstructionSafetyViewPanel.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File file = fc.getSelectedFile();
            	
            	statusView.append("Opening: " + file.getName().replace("%20", " ") + " " + newline);
            
            } else {
            	
            	statusView.append("Open file cancelled." + newline);
            	
            }
            statusView.setCaretPosition(statusView.getDocument().getLength());
        	
        
                
        //  ----------------------------------------- 
        } else if (e.getSource() == S_slab) { 
          SlabRules sr = new SlabRules();
          sr.getSlabs();

          
      //  ----------------------------------------- 
        } else if (e.getSource() == floorList) { 
        	JComboBox cb = (JComboBox)e.getSource();
            String floorName = (String)cb.getSelectedItem();
            System.out.println(floorName);


          
        //  ----------------------------------------- 
        } else if (e.getSource() == S_run) { 
        	
        	SafetyFense sf = new SafetyFense();
        	sf.run();

        //  ----------------------------------------- 
        } else if (e.getSource() == S_run2) { 
        	

        
          
        //  ----------------------------------------- 
        } else if (e.getSource() == Btn_others) { 
          JOptionPane.showMessageDialog(null, 
			"This is another test tab for different category.", 
			"Message",
		    JOptionPane.INFORMATION_MESSAGE,
		    null);
       
        }
        
	}
	
	
	
	
	// only requirement doc files - XML or Excel files allowed
	public class RequirementFileFilter extends FileFilter {

	    //Accept all directories and some file formats.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }

	        String extension = ExtensionUtils.getExtension(f);
	        if (extension != null) {
	            if (extension.equals(ExtensionUtils.xml) ||
	                extension.equals(ExtensionUtils.csv) ||
	                extension.equals(ExtensionUtils.xls) ||
	                extension.equals(ExtensionUtils.xlsx)) {
	                    return true;
	            } else {
	                return false;
	            }
	        }

	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "Requirement File: Excel or xml files";
	    }
	}

	// get appropriate extensions
	public static class ExtensionUtils {
	    public final static String xml = "xml";
	    public final static String csv = "csv";
	    public final static String xls = "xls";
	    public final static String xlsx = "xlsx";
	    
	    // Get the extension
	    public static String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
	}


}
