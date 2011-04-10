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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.SortedSet;

import javax.media.j3d.BoundingBox;
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
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeNode;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.solibri.sae.redox.Entity;
import com.solibri.sae.redox.Model;
import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.construction.SBuilding;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.ui.ItemTreeCellRenderer;
import com.solibri.sae.ui.IObjectTreeTable;
import com.solibri.sae.ui.IObjectTreeTableModel;
import com.solibri.sae.ui.IObjectTreeTableNode;
import com.solibri.sae.ui.Ui;
import com.solibri.saf.core.Application;
import com.solibri.saf.core.ApplicationFrame;
import com.solibri.saf.core.IView;
import com.solibri.saf.core.ViewPanel;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

import edu.gatech.safety.construction.ConstructionSafetyPlugin;
import edu.gatech.safety.construction.SafetyFense;
import edu.gatech.safety.rules.SlabRules;


/**
 * This ViewPanel generates some Safety related module's user interfaces using tabbed panels for each module.
 * @author Jin-Kook Lee
 */
public class ConstructionSafetyViewPanel extends ViewPanel implements ActionListener, ListSelectionListener {	
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private final JPanel topPanel = new JPanel();
	private JPanel bottomPanel;
	
	static private final String newline = "\n";
    JButton S_open, S_run, S_run2, S_slab, Btn_others;
    
    //JTextArea statusView = SpaceProgramReview_Data.statusView;
    public static JTextArea statusView = new JTextArea();
    
    JFileChooser fc;    
    JComboBox floorList;

    Font font = new Font("Arial", Font.PLAIN, 11);
	
	
	private final Point3d MAX_NEGATIVE = new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	private final Point3d MAX_POSITIVE = new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

	
	private JScrollPane safetyTreeScrollPane;
	private IObjectTreeTable treeTable;
	private IObjectTreeTableModel treeTableModel;
	private RootNode root = new RootNode(null);
	
	
	
		
	
	
    
    /**
     * Constructor
     */
	public ConstructionSafetyViewPanel(IView view) {
		super(view);
		initializeGUI();
//		setLayout(new BorderLayout());
//		add(getSafetyTreeScrollPane(), BorderLayout.CENTER);
	}
	
	public void update() {
		Model model = ConstructionSafetyPlugin.getInstance().getConceptModel();
		root.setUserObject(model);
		root.applyFilter(null, getSafetyTreeTableModel());
		getSafetyTreeScrollPane().repaint();
	}
	
	private JScrollPane getSafetyTreeScrollPane() {
		if (safetyTreeScrollPane == null) {
			treeTable = new IObjectTreeTable(getSafetyTreeTableModel());
			//treeTable.addMouseListener(new PopupMouseListener());
			treeTable.getSelectionModel().addListSelectionListener(this);
			treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			treeTable.setTreeCellRenderer(new ItemTreeCellRenderer());
			treeTable.hideHeader();
			safetyTreeScrollPane = Ui.createScrollPane(treeTable);
		}
		return safetyTreeScrollPane;
	}
	
	private IObjectTreeTableModel getSafetyTreeTableModel() {
		if (treeTableModel == null) {
			treeTableModel = new SafetyTreeTableModel();
			treeTableModel.setRoot(root);
		}
		return treeTableModel;
	}
	
	private class SafetyTreeTableModel extends IObjectTreeTableModel {
		public Class getColumnClass(int arg0) {
			return super.getColumnClass(arg0);
		}
	}
	
	private class RootNode extends IObjectTreeTableNode {
		private static final long serialVersionUID = 1L;

		public RootNode(Model model) {
			super(model);
		}

		protected IObjectTreeTableNode create(Object item) {
			return new BuildingTreeTableNode((SBuilding) item);
		}

		protected Collection getChildren() {
			Collection buildings = Collections.EMPTY_LIST;
			if (userObject != null) {
				buildings = new ArrayList();
				((Model)userObject).findAll(SBuilding.class, buildings);
			}
			return buildings;
		}
		
	}
	
	private class BuildingTreeTableNode extends ThreeDParentNode {
		private static final long serialVersionUID = 1L;

		public BuildingTreeTableNode(SBuilding item) {
			super(item);
		}

		protected IObjectTreeTableNode create(Object item) {
			return new StoreyTreeTableNode((SBuildingStorey) item);
		}
		
		protected Comparator getComparator() {
			return SBuildingStorey.STOREY_COMPARATOR;
		}
		
		protected Collection getChildren() {
			SBuilding building = (SBuilding) userObject;
			return building.getRelated(SContains.class, true, SBuildingStorey.class);
		}
				
	}
	
	private class StoreyTreeTableNode extends ThreeDParentNode {
		private static final long serialVersionUID = 1L;

		public StoreyTreeTableNode(SBuildingStorey storey) {
			super(storey);
		}

		protected IObjectTreeTableNode create(Object item) {
			return null;
		}

		protected Collection getChildren() {
			return null;
		}

	}
	
	private abstract class ThreeDParentNode extends ThreeDBoundsNode {

		public ThreeDParentNode(Object item) {
			super(item);
		}
		
		/**
		 * Implementation that goes through child bounds
		 */
		BoundingBox getBounds() {
			BoundingBox bb = new BoundingBox(MAX_POSITIVE, MAX_NEGATIVE);
			for (int i = 0; i < getChildCount(); i++) {
				TreeNode child = getChildAt(i);
				if (child instanceof ThreeDBoundsNode) {
					ThreeDBoundsNode boundsNode = (ThreeDBoundsNode) child;
					bb.combine(boundsNode.getBounds());
				}
			}			
			return bb;
		}
	}
	
	private abstract class ThreeDBoundsNode extends IObjectTreeTableNode {

		public ThreeDBoundsNode(Object item) {
			super(item);
		}
		
		/**
		 * This method is overridden to ensure that the children
		 * list is populated. The method can be removed when the 
		 * application is ported to SMC 5.0.
		 */
		public Enumeration children() {
			getChildCount();
			return super.children();
		}
		
		abstract BoundingBox getBounds();
	}
	
	
	
	
	/**
	 * initialize UI: build a tabbed pane and add them
	 */
	private void initializeGUI() {
		// Add Tabs
		tabbedPane.addTab("Construction Safety", null, drawTab1(), null);
		tabbedPane.addTab("Monitoring", null, drawTab2(), null);
        tabbedPane.addTab("Others", null, drawTab3(), null);
        
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setToolTipTextAt(0, new String("<html>Construction Safety Plugin</html>"));
        tabbedPane.setToolTipTextAt(1, new String("<html>Construction Safety Plugin</html>"));
        tabbedPane.setToolTipTextAt(2, new String("<html>This module is under development.</html>"));
        
        // Disable tabs
//        tabbedPane.setEnabledAt(1, false);
        
        add(tabbedPane);
	}
	
	private JPanel drawTab1() {
		JPanel jp = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		topPanel.add(drawSafetyPanel1(), BorderLayout.NORTH);
		topPanel.add(new JPanel(), BorderLayout.CENTER); // to take empty space
		jp.add(topPanel, BorderLayout.NORTH);
		jp.add(getBottomPanel(), BorderLayout.CENTER);
		return jp;
	}
	
	private JPanel drawTab2() {
		JPanel statusPanel = new JPanel();
		//Border empty = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);		
		
		statusView = new JTextArea(8,30);
	    statusView.setMargin(new Insets(5,5,5,5));
	    statusView.setEditable(false);
	    statusView.setOpaque(false); // transparent textarea
	    statusView.setFont(font);
	    JScrollPane statusScrollPane = new JScrollPane(statusView); // status view panel  
	    
	    statusView.append("Status view is ready..\n" +
	    		"----------------------------------------------" + newline);

	    statusScrollPane.setPreferredSize(new Dimension(400, 200));
	    statusScrollPane.setOpaque(false); //transparent
	    statusScrollPane.setBorder(loweredetched);
	    statusPanel.add(statusScrollPane);
	    return statusPanel;
	    
	}
	
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel(new BorderLayout());
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			bottomPanel.add(getSafetyTreeScrollPane(), BorderLayout.CENTER);
		}
		return bottomPanel;
	}
	
	
	
	
	/**
	 * JPanel for the first tab
	 * @return JPanel
	 */
	private JPanel drawSafetyPanel1() {
	    
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
			
		
		S_slab = new JButton("Safety Objects", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_slab.addActionListener(this);
		S_slab.setToolTipText("Collect all slabs and more +");
		
		
		S_run = new JButton("Safety Fenses", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run.addActionListener(this);
		S_run.setToolTipText("Safety Fense for Perimeter");
		
		
		S_run2 = new JButton("Safety Fense: Holes", 
                createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run2.addActionListener(this);
		S_run2.setToolTipText("Safety Fense for Holes");
		

		JPanel panelTop = new JPanel(); // button panel
		panelTop.add(S_open);
		
//		panelTop.add(floorList);
		
		panelTop.add(S_slab);
		panelTop.add(S_run);
//		panelTop.add(S_run2);
//		panelTop.setPreferredSize(new Dimension(320, 30));

		//Add the buttons and the status view to the panel.
		JPanel panelSafetyTab = new JPanel(); // main panel
		panelSafetyTab.setLayout(new FlowLayout()); // border layout
		panelSafetyTab.add(panelTop);

		return panelSafetyTab;
	}
	
		
	
	/* panel for Cost */
	private JPanel drawTab3() {
		
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
		
		JPanel panelOthers = new JPanel(); //use FlowLayout
		panelOthers.add(panelButtons, BorderLayout.PAGE_START);
		panelOthers.add(co, BorderLayout.PAGE_END);			
		panelOthers.setOpaque(true);	

		return panelOthers;
	}
	
	
	/**
	 * Utility
	 * @param path
	 * @return
	 */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ConstructionSafetyViewPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            //System.err.println("Couldn't find file: " + path);
            return null;
        }
    }



	
	/**
	 * Actions
	 */
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
          
          update();

          
      
          
        //  ----------------------------------------- 
        } else if (e.getSource() == S_run) { 
        	
        	SafetyFense sf = new SafetyFense();
        	sf.run();
        	

        
        
          
        //  ----------------------------------------- 
        } else if (e.getSource() == Btn_others) { 
          JOptionPane.showMessageDialog(null, 
			"This is another test tab for different category.", 
			"Message",
		    JOptionPane.INFORMATION_MESSAGE,
		    null);
       
        }
        
	}
	
	
	
	
	/**
	 * only requirement doc files - XML or Excel files allowed
	 */
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

	/**
	 *  get appropriate extensions
	 */
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

	
	
	
	
	
	
	
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}


}
