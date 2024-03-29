package edu.gatech.safety.ui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeNode;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.jdesktop.swingx.treetable.TreeTableNode;

import com.solibri.sae.redox.Entity;
import com.solibri.sae.redox.ItemFilter;
import com.solibri.sae.redox.Model;
import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.construction.SBuilding;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SSlab;
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
import com.solibri.saf.core.ui.PopupMenuListener;
import com.solibri.saf.plugins.checkingplugin.ConstraintResultsView;
import com.solibri.saf.plugins.checkingplugin.ui.ConstraintResultsPanel;
//import com.solibri.saf.plugins.checkingplugin.ui.ConstraintTreeTable;
import com.solibri.saf.plugins.checkingplugin.ui.RuleTreeTableMouseAdapter;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

import edu.gatech.safety.construction.ConstructionSafetyPlugin;
import edu.gatech.safety.construction.SafetyFence;
import edu.gatech.safety.construction.SafetyFenceDetect;
import edu.gatech.safety.construction.SafetyFence_German;
import edu.gatech.safety.construction.WallFence;
import edu.gatech.safety.rules.ConstructionProcess;
import edu.gatech.safety.rules.ConstructionProcess_German;
import edu.gatech.safety.rules.OpeningTest;
import edu.gatech.safety.rules.SlabRules;
import edu.gatech.safety.rules.SlabRules_German;
import edu.gatech.safety.rules.WallRules;

/**
 * This ViewPanel generates some Safety related module's user interfaces using
 * tabbed panels for each module.
 * 
 * @author Jin-Kook Lee
 */
public class ConstructionSafetyViewPanel extends ViewPanel implements
		ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane;

	private JPanel objectTabTopPanel, processButtons, processTabPanel1,
			processTabPanel, processBar, topPanel, bottomPanel, openingPanel;
	private JSplitPane splitPane1, splitPane2, splitPane3;

	private JButton S_open, S_run, S_run1,S_run2, S_opening, S_wOpening, S_slab,
			S_wall, Btn_others, P_open, P_generate, detect;
	private JButton b1, b2, b3, b4, b5, b6, b7, b8, b9, b10;
	public static JTextArea statusView = new JTextArea();

	private JSlider processSlider;

	private JFileChooser fc;
	private JComboBox floorList;

	static private final String newline = "\n";
	private Font font = new Font("Arial", Font.PLAIN, 11);

	private final Point3d MAX_NEGATIVE = new Point3d(Double.NEGATIVE_INFINITY,
			Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	private final Point3d MAX_POSITIVE = new Point3d(Double.POSITIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

	private JScrollPane safetyTreeScrollPane, openingScrollPane;
	private IObjectTreeTable treeTable;
	private JTable openingTable;
	private AbstractTableModel openingTableModel;
	private IObjectTreeTableModel treeTableModel;
	private RootNode root = new RootNode(null);

	private ConstructionProcess_German cp = new ConstructionProcess_German();
	//private ConstructionProcess cp = new ConstructionProcess();
	private int sw = 0;

	/**
	 * Constructor
	 */
	public ConstructionSafetyViewPanel(IView view) {
		super(view);
		initializeGUI();
		// setLayout(new BorderLayout());
		// add(getSafetyTreeScrollPane(), BorderLayout.CENTER);
	}

	/**
	 * initialize UI: build a tabbed pane and add them
	 */
	private void initializeGUI() {
		// Add Tabs
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Construction Objects", null, drawTab1(), null);
		tabbedPane.addTab("Construction Process", null, drawTab2(), null);
		tabbedPane.addTab("Safety Objects", null, drawTab3(), null);

		tabbedPane.setSelectedIndex(0);
		tabbedPane.setToolTipTextAt(0, new String(
				"<html>Construction Safety Plugin</html>"));
		tabbedPane.setToolTipTextAt(1, new String(
				"<html>Construction Safety Plugin</html>"));
		tabbedPane.setToolTipTextAt(2, new String(
				"<html>This module is under development.</html>"));

		// Disable tabs
		// tabbedPane.setEnabledAt(1, false);

		this.add(tabbedPane);
	}

	private JSplitPane drawTab1() {
		if (splitPane1 == null) {
			objectTabTopPanel = new JPanel(new BorderLayout());
			objectTabTopPanel.setBorder(BorderFactory.createEmptyBorder(0, 0,
					0, 0));
			objectTabTopPanel.add(drawObjectPanel1(), BorderLayout.NORTH);
			objectTabTopPanel.add(drawObjectPanel2(), BorderLayout.CENTER);
			// objectTab.add(getOpeningPanel(), BorderLayout.SOUTH);
			splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane1.setOneTouchExpandable(false);
			splitPane1.setTopComponent(objectTabTopPanel);
		}
		return splitPane1;
	}

	private JPanel drawObjectPanel1() {
		if (topPanel == null) {
			topPanel = new JPanel(new BorderLayout());
			topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			topPanel.add(drawButtons1(), BorderLayout.CENTER);
		}
		return topPanel;
	}

	private JPanel drawObjectPanel2() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel(new BorderLayout());
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			bottomPanel.add(getSafetyTreeScrollPane(), BorderLayout.CENTER);
		}
		return bottomPanel;
	}

	private JPanel drawObjectTabBottomPanel() {
		if (openingPanel == null) {
			openingPanel = new JPanel(new BorderLayout());
			openingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			openingPanel.add(getOpeningScrollPane(), BorderLayout.CENTER);
		}
		return openingPanel;
	}

	/**
	 * JPanel for the first tab
	 * 
	 * @return JPanel
	 */
	private JPanel drawButtons1() {

		fc = new JFileChooser(); // Create a file chooser
		fc.addChoosableFileFilter(new RequirementFileFilter());
		fc.setAcceptAllFileFilterUsed(false);

		// Selection - Floors
		String[] floorStrings = { "All Floors ---", "under dev" };

		floorList = new JComboBox(floorStrings);
		floorList.setSelectedIndex(1);
		floorList.addActionListener(this);

		S_open = new JButton("Open Safety Data",
				createImageIcon("/edu/gatech/safety/res/images/table.gif"));
		S_open.addActionListener(this);
		S_open.setToolTipText("Open safety data test.");

		S_slab = new JButton("Slab Objects",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_slab.addActionListener(this);
		S_slab.setToolTipText("Collect all slabs and more +");

		S_wall = new JButton("Wall Objects",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_wall.addActionListener(this);
		S_wall.setToolTipText("Collect all walls and more +");

		S_run = new JButton("Slab Fences",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run.addActionListener(this);
		S_run.setToolTipText("Safety Fence for Perimeter and slab opening");
		
		S_run2 = new JButton("Slab Fences_German",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run2.addActionListener(this);
		S_run2.setToolTipText("Safety Fence for Perimeter and slab opening according to German standard");

		S_run1 = new JButton("Wall Fences",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		S_run1.addActionListener(this);
		S_run1.setToolTipText("Safety Fence for wall openings");

		// S_opening = new JButton("Slab openings",
		// createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		// S_opening.addActionListener(this);
		// S_opening.setToolTipText("Collect all Holes on Slab");
		//
		// S_wOpening = new JButton("Wall openings",
		// createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		// S_wOpening.addActionListener(this);
		// S_wOpening.setToolTipText("Collect all Holes on Wall");

		JPanel panelTop = new JPanel(); // button panel
		panelTop.add(S_open);

		// panelTop.add(floorList);

		panelTop.add(S_slab);
		panelTop.add(S_wall);
		// panelTop.add(S_opening);
		// panelTop.add(S_wOpening);
		panelTop.add(S_run);
		panelTop.add(S_run2);
		panelTop.add(S_run1);

		// panelTop.setPreferredSize(new Dimension(320, 30));

		// Add the buttons and the status view to the panel.
		JPanel panelSafetyTab = new JPanel(); // main panel
		panelSafetyTab.setLayout(new FlowLayout()); // border layout
		panelSafetyTab.add(panelTop);

		return panelSafetyTab;
	}

	// status tab
	private JSplitPane drawTab2() {
		if (splitPane2 == null) {
			processTabPanel = new JPanel(new BorderLayout());
			processTabPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
					0));
			processTabPanel.add(drawProcessTabPanel1(), BorderLayout.NORTH);
			processTabPanel.add(drawProcessTabPanel2(), BorderLayout.CENTER);
			splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane2.setOneTouchExpandable(false);
			splitPane2.setTopComponent(processTabPanel);
		}
		return splitPane2;
	}

	private JPanel drawProcessTabPanel1() {
		if (processTabPanel1 == null) {
			processTabPanel1 = new JPanel(new BorderLayout());
			processTabPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 0,
					5));
			processTabPanel1.add(drawButtons2(), BorderLayout.CENTER);
		}
		return processTabPanel1;
	}

	private JPanel drawButtons2() {
		fc = new JFileChooser(); // Create a file chooser
		fc.addChoosableFileFilter(new RequirementFileFilter());
		fc.setAcceptAllFileFilterUsed(false);

		P_open = new JButton("Open 4D-BIM Schedule Data",
				createImageIcon("/edu/gatech/safety/res/images/table.gif"));
		P_open.addActionListener(this);
		P_open.setToolTipText("Open safety data test.");

		P_generate = new JButton("Generate 4D View",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		P_generate.addActionListener(this);
		P_generate.setToolTipText("Generate Safety Objects in current 4D View");

		JPanel panelTop2 = new JPanel(); // button panel
		panelTop2.add(P_open);
		panelTop2.add(P_generate);

		JPanel panelProcessTab = new JPanel(); // main panel
		panelProcessTab.setLayout(new FlowLayout()); // border layout
		panelProcessTab.add(panelTop2);

		return panelProcessTab;
	}

	private JPanel drawProcessTabPanel2() {
		if (processButtons == null) {
			b1 = new JButton("1");
			b2 = new JButton("2");
			b3 = new JButton("3");
			b4 = new JButton("4");
			b5 = new JButton("5");
			b6 = new JButton("6");
			b7 = new JButton("7");
			b8 = new JButton("8");
			b9 = new JButton("9");
			b10 = new JButton("10");
			b1.addActionListener(this);
			b2.addActionListener(this);
			b3.addActionListener(this);
			b4.addActionListener(this);
			b5.addActionListener(this);
			b6.addActionListener(this);
			b7.addActionListener(this);
			b8.addActionListener(this);
			b9.addActionListener(this);
			b10.addActionListener(this);
			processButtons = new JPanel(new FlowLayout());
			processButtons.setBorder(BorderFactory.createEmptyBorder(10, 10,
					10, 10));
			processButtons.add(b1);
			processButtons.add(b2);
			processButtons.add(b3);
			processButtons.add(b4);
			processButtons.add(b5);
			processButtons.add(b6);
			processButtons.add(b7);
			processButtons.add(b8);
			processButtons.add(b9);
			processButtons.add(b10);

		}
		return processButtons;
	}

	private JPanel drawProcessBottomPanel() {
		if (processBar == null) {
			processSlider = new JSlider();
			processSlider.setValue(0);

			processBar = new JPanel();
			processBar.add(processSlider);
		}
		return processBar;
	}

	// others tab
	private JSplitPane drawTab3() {
		if (splitPane3 == null) {
			JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			topPanel.add(drawThirdPanel1(), BorderLayout.NORTH);
			topPanel.add(drawThirdPanel2(), BorderLayout.CENTER);
			splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane3.setOneTouchExpandable(false);
			splitPane3.setTopComponent(topPanel);
		}
		return splitPane3;
	}
	
	private JPanel drawThirdPanel1() {
		JPanel p1 = new JPanel(new BorderLayout());
		p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		p1.add(drawButtons3(), BorderLayout.CENTER);
		return p1;
	}
	
	private JPanel drawButtons3() {
		detect = new JButton("Detect Protection Railing Places",
				createImageIcon("/edu/gatech/safety/res/images/open1.gif"));
		detect.addActionListener(this);
		detect.setToolTipText("Detect Protection Railing Places");
		
		JPanel panelTop2 = new JPanel(); // button panel
		panelTop2.add(detect);
		
		JPanel pp = new JPanel(); // main panel
		pp.setLayout(new FlowLayout()); // border layout
		pp.add(panelTop2);

		return pp;
	}
	
	private JPanel drawThirdPanel2() {
		JPanel p1 = new JPanel(new BorderLayout());
		return p1;
	}
	
	
	
	
	

	/**
	 * Actions
	 */
	public void valueChanged(ListSelectionEvent e) {
		
	}

	public void actionPerformed(ActionEvent e) {

		// -----------------------------------------
		if (e.getSource() == S_open) {
			int returnVal = fc.showOpenDialog(ConstructionSafetyViewPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				statusView.append("Opening: "
						+ file.getName().replace("%20", " ") + " " + newline);
			} else {
				statusView.append("Open file cancelled." + newline);
			}
			statusView.setCaretPosition(statusView.getDocument().getLength());

			// -----------Change Rule---------------------
		} else if (e.getSource() == S_slab) {
			//SlabRules sr = new SlabRules();
			SlabRules_German sr = new SlabRules_German();
			// sr.getSlabs();
			sr.getOpenings();
			//sw = 1; //sw==1 -->OSHA; sw==2 -->wall; sw==3 --> German
			sw = 3;
			splitPane1.setBottomComponent(drawObjectTabBottomPanel());
			splitPane1.setDividerLocation(150);

			this.repaint();
			// -----------------------------------------
		} else if (e.getSource() == S_wall) {
			WallRules sr = new WallRules();
			// sr.getWalls();
			sr.getOpenings();
			sw = 2;
			splitPane1.setBottomComponent(drawObjectTabBottomPanel());
			splitPane1.setDividerLocation(150);
			// -----------------------------------------
			// } else if (e.getSource() == S_opening) {
			// SlabRules sr = new SlabRules();
			// sr.getOpenings();
			// sw = 1;
			// splitPane1.setBottomComponent(drawObjectTabBottomPanel());
			// splitPane1.setDividerLocation(150);
			//
			// this.repaint();
			//
			// // -----------------------------------------
			// } else if (e.getSource() == S_wOpening) {
			// WallRules sr = new WallRules();
			// sr.getOpenings();
			// sw = 2;
			// splitPane1.setBottomComponent(drawObjectTabBottomPanel());
			// splitPane1.setDividerLocation(150);
			//
			// this.repaint();

			// -----------------------------------------
		} else if (e.getSource() == S_run) {
			SafetyFence sf = new SafetyFence();
			sf.run();
			
			// -----------------------------------------
		} else if (e.getSource() == S_run2) {
			SafetyFence_German sf = new SafetyFence_German();
			sf.run();
			
			// -----------------------------------------
		} else if (e.getSource() == S_run1) {
			WallFence sf = new WallFence();
			sf.run();

			// -----------------------------------------
		} else if (e.getSource() == Btn_others) {
			// JOptionPane.showMessageDialog(null,
			// "This is another test tab for different category.",
			// "Message", JOptionPane.INFORMATION_MESSAGE, null);
			OpeningTest ot = new OpeningTest();
			ot.run();
			System.out.println("Test--------------------------------");

			// -----------------------------------------
		} else if (e.getSource() == P_open) {
			int returnVal = fc.showOpenDialog(ConstructionSafetyViewPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

			} else {
			}

			// -----------------------------------------
		} else if (e.getSource() == P_generate) {
			splitPane2.setBottomComponent(drawProcessBottomPanel());
			splitPane2.setDividerLocation(150);

			// -----------------------------------------
		} else if (e.getSource() == b1) {
			cp.getProcess(1);

		} else if (e.getSource() == b2) {
			cp.getProcess(2);

		} else if (e.getSource() == b3) {
			cp.getProcess(3);

		} else if (e.getSource() == b4) {
			cp.getProcess(4);

		} else if (e.getSource() == b5) {
			cp.getProcess(5);

		} else if (e.getSource() == b6) {
			cp.getProcess(6);

		} else if (e.getSource() == b7) {
			cp.getProcess(7);

		} else if (e.getSource() == b8) {
			cp.getProcess(8);

		} else if (e.getSource() == b9) {
			cp.getProcess(9);

		} else if (e.getSource() == b10) {
			cp.getProcess(10);

			
			
		} else if (e.getSource() == detect) {	
			SafetyFenceDetect sfd = new SafetyFenceDetect();
			sfd.run();
			
			
			
		}
	}

	// =====================================================================
	/**
	 * update repaint UI
	 */

	public void update() {
		Model model = ConstructionSafetyPlugin.getInstance().getThisModel();
		root.setUserObject(model);
		root.applyFilter(null, getSafetyTreeTableModel());
		getSafetyTreeScrollPane().repaint();
	}

	private JScrollPane getSafetyTreeScrollPane() {
		if (safetyTreeScrollPane == null) {
			treeTable = new IObjectTreeTable(getSafetyTreeTableModel());
			// treeTable.addMouseListener(new PopupMenuListener(JPopupMenu));
			treeTable.getSelectionModel().addListSelectionListener(this);
			treeTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			treeTable.setTreeCellRenderer(new ItemTreeCellRenderer());
			treeTable.hideHeader();
			safetyTreeScrollPane = Ui.createScrollPane(treeTable);
		}
		return safetyTreeScrollPane;
	}

	private JScrollPane getOpeningScrollPane() {
		if (openingScrollPane == null) {
			openingTable = new JTable(new openingTableModel());
			// openingTable.getSelectionModel().addListSelectionListener(this);
			openingTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent event) {
							int viewRow = openingTable.getSelectedRow();
							if (viewRow < 0) {
								// Selection got filtered away.
								System.out.println(String.format("null"));
							} else {
								int modelRow = openingTable
										.convertRowIndexToModel(viewRow);
								System.out.println(String.format(
										"Selected Row in view: %d. "
												+ "Selected Row in model: %d.",
										viewRow, modelRow));
							}
						}

						// @Override
						// public void valueChanged(ListSelectionEvent e) {
						// // TODO Auto-generated method stub
						//
						// }
					});
			openingTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
			// treeTable.setTreeCellRenderer(new ItemTreeCellRenderer());
			// treeTable.hideHeader();
			openingScrollPane = Ui.createScrollPane(openingTable);

			openingTable.setPreferredScrollableViewportSize(new Dimension(500,
					150));
			// openingTable.setFillsViewportHeight(true);

			// Create the scroll pane and add the table to it.
			// JScrollPane scrollPane = new JScrollPane(openingTable);

			// Set up column sizes.
			// initColumnSizes(openingTable);

			// Fiddle with the Sport column's cell editors/renderers.
			setUpSportColumn(openingTable, openingTable.getColumnModel()
					.getColumn(7));
			this.repaint();
		}
		return openingScrollPane;
	}

	private void initColumnSizes(JTable table) {
		openingTableModel model = (openingTableModel) table.getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		Object[] longValues = model.longValues;
		TableCellRenderer headerRenderer = table.getTableHeader()
				.getDefaultRenderer();

		for (int i = 0; i < 9; i++) {
			column = table.getColumnModel().getColumn(i);

			comp = headerRenderer.getTableCellRendererComponent(null,
					column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			comp = table.getDefaultRenderer(model.getColumnClass(i))
					.getTableCellRendererComponent(table, longValues[i], false,
							false, 0, i);
			cellWidth = comp.getPreferredSize().width;

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	public void setUpSportColumn(JTable table, TableColumn sportColumn) {
		// Set up the editor
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Cover");
		comboBox.addItem("Guardrail System");
		comboBox.addItem("Personal Fall Arrest System");
		comboBox.addItem("Ignore");
		comboBox.addItem("...");
		sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		sportColumn.setCellRenderer(renderer);
	}

	public class openingTableModel extends AbstractTableModel {
		private String[] columnNames = { "No.", "Name", "Level",
				"DisToLowerLevel(m)", "Width(m)", "Height(m)", "Area(m2)", "Prevention",
				"Check" };
		int n;

		private int getSize() {
			if (sw == 1) {
				n = SlabRules.name.size();
			} else if (sw == 2) {
				n = WallRules.name.size();
			} else if (sw == 3) {
				n = SlabRules_German.name.size();
			}

			return n;

		}

		private Object resizeArray(Object oldArray, int newSize) {
			int oldSize = java.lang.reflect.Array.getLength(oldArray);
			Class elementType = oldArray.getClass().getComponentType();
			Object newArray = java.lang.reflect.Array.newInstance(elementType,
					newSize);
			int preserveLength = Math.min(oldSize, newSize);
			if (preserveLength > 0)
				System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
			return newArray;
		}

		public Object[][] data1 = new Object[getSize() - 1][9];

		private Object[][] getData() {
			data1 = (Object[][]) resizeArray(data1, getSize());

			for (int i = 0; i < data1.length; i++) {
				if (data1[i] == null)
					data1[i] = new Object[9];
				else
					data1[i] = (Object[]) resizeArray(data1[i], 9);
			}

			if (sw == 1) {
				// for (int i = 0; i < data1.length; i++)
				// for (int j = 0; j < 9; j++) {
				// data1[i][j]=null;
				// }
				// data1 = (Object[][]) resizeArray(data1, getSize());
				for (int i = 0; i < SlabRules.no.size(); i++)
					for (int j = 0; j < 9; j++) {
						if (j == 0) {
							data1[i][j] = SlabRules.no.get(i);
						} else if (j == 1) {
							data1[i][j] = SlabRules.name.get(i);
						} else if (j == 2) {
							data1[i][j] = SlabRules.level.get(i);
						} else if (j == 3) {
							data1[i][j] = SlabRules.disToLower.get(i);
						} else if (j == 4) {
							data1[i][j] = SlabRules.width.get(i);
						} else if (j == 5) {
							data1[i][j] = SlabRules.height.get(i);
						} else if (j == 6) {
							data1[i][j] = SlabRules.area.get(i);
						} else if (j == 7) {
							data1[i][j] = SlabRules.prevention.get(i);
						} else if (j == 8) {
							data1[i][j] = SlabRules.check.get(i);
						}
					}
			}

			else if (sw == 2) {

				for (int i = 0; i < WallRules.no.size(); i++)
					for (int j = 0; j < 9; j++) {
						if (j == 0) {
							data1[i][j] = WallRules.no.get(i);
						} else if (j == 1) {
							data1[i][j] = WallRules.name.get(i);
						} else if (j == 2) {
							data1[i][j] = WallRules.level.get(i);
						} else if (j == 3) {
							data1[i][j] = WallRules.disToLower.get(i);
						} else if (j == 4) {
							data1[i][j] = WallRules.width.get(i);
						} else if (j == 5) {
							data1[i][j] = WallRules.height.get(i);
						} else if (j == 6) {
							data1[i][j] = WallRules.area.get(i);
						} else if (j == 7) {
							data1[i][j] = WallRules.prevention.get(i);
						} else if (j == 8) {
							data1[i][j] = WallRules.check.get(i);
						}	
					}
			}
				else if (sw == 3) {

					for (int i = 0; i < SlabRules_German.no.size(); i++)
						for (int j = 0; j < 9; j++) {
							if (j == 0) {
								data1[i][j] = SlabRules_German.no.get(i);
							} else if (j == 1) {
								data1[i][j] = SlabRules_German.name.get(i);
							} else if (j == 2) {
								data1[i][j] = SlabRules_German.level.get(i);
							} else if (j == 3) {
								data1[i][j] = SlabRules_German.disToLower.get(i);
							} else if (j == 4) {
								data1[i][j] = SlabRules_German.width.get(i);
							} else if (j == 5) {
								data1[i][j] = SlabRules_German.height.get(i);
							} else if (j == 6) {
								data1[i][j] = SlabRules_German.area.get(i);
							} else if (j == 7) {
								data1[i][j] = SlabRules_German.prevention.get(i);
							} else if (j == 8) {
								data1[i][j] = SlabRules_German.check.get(i);
							}
						}
			}
			return data1;

		}

		public final Object[] longValues = { "01", "Slab 1", "Level2", "2.0",
				"Width", "Height", "Area", "Personal Fall Arrest System",
				Boolean.TRUE };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			getData();
			return data1.length;
			// return n;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			// getData();
			return data1[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			if (col < 2) {
				return false;
			} else {
				return true;
			}

		}

		public void setValueAt(Object value, int row, int col) {
			// getData();
			data1[row][col] = value;
			if (sw == 1) {
				// for (int i = 0; i < SlabRules.no.size(); i++)
				// for (int j = 0; j < 9; j++) {
				if (col == 0) {
					SlabRules.no.set(row, value);
				} else if (col == 1) {
					SlabRules.name.set(row, value);
				} else if (col == 2) {
					SlabRules.level.set(row, value);
				} else if (col == 3) {
					SlabRules.disToLower.set(row, value);
				} else if (col == 4) {
					SlabRules.width.set(row, value);
				} else if (col == 5) {
					SlabRules.height.set(row, value);
				} else if (col == 6) {
					SlabRules.area.set(row, value);
				} else if (col == 7) {
					SlabRules.prevention.set(row, value);
				} else if (col == 8) {
					SlabRules.check.set(row, value);
				}
				// }
			}

			else if (sw == 2) {

				if (col == 0) {
					WallRules.no.set(row, value);
				} else if (col == 1) {
					WallRules.name.set(row, value);
				} else if (col == 2) {
					WallRules.level.set(row, value);
				} else if (col == 3) {
					WallRules.disToLower.set(row, value);
				} else if (col == 4) {
					WallRules.width.set(row, value);
				} else if (col == 5) {
					WallRules.height.set(row, value);
				} else if (col == 6) {
					WallRules.area.set(row, value);
				} else if (col == 7) {
					WallRules.prevention.set(row, value);
				} else if (col == 8) {
					WallRules.check.set(row, value);
				}

			}

			fireTableCellUpdated(row, col);

		}
	}

	private IObjectTreeTableModel getOpeningTableModel() {
		if (treeTableModel == null) {
			treeTableModel = new SafetyTreeTableModel();
			treeTableModel.setRoot(root);
		}
		return treeTableModel;
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
				((Model) userObject).findAll(SBuilding.class, buildings);
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
			return building.getRelated(SContains.class, true,
					SBuildingStorey.class);
		}
	}

	private class StoreyTreeTableNode extends ThreeDParentNode {
		private static final long serialVersionUID = 1L;

		public StoreyTreeTableNode(SBuildingStorey storey) {
			super(storey);
		}

		protected IObjectTreeTableNode create(Object item) {
			return new SlabTreeTableNode((SSlab) item);
		}

		protected Collection getChildren() {
			SBuildingStorey storey = (SBuildingStorey) userObject;
			// System.out.println("~"
			// + storey.getRelated(SContains.class, true, SSlab.class)
			// .size());
			// System.out.println("!"
			// + storey.getRelated(SContains.class, true, SOpening.class)
			// .size());
			return storey.getRelated(SContains.class, true, SSlab.class);
		}
	}

	private class SlabTreeTableNode extends ThreeDParentNode {
		public SlabTreeTableNode(SSlab slab) {
			super(slab);
		}

		protected IObjectTreeTableNode create(Object item) {
			return new OpeningTreeTableNode((SOpening) item);
		}

		protected Collection getChildren() {
			SSlab slab = (SSlab) userObject;
			// System.out.println("~"+slab.getRelated(SContains.class, true,
			// SOpening.class).size());
			return slab.getRelated(SContains.class, true, SOpening.class);
		}
	}

	private class OpeningTreeTableNode extends ThreeDParentNode {
		public OpeningTreeTableNode(SOpening opening) {
			super(opening);
		}

		protected IObjectTreeTableNode create(Object arg0) {
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
		 * This method is overridden to ensure that the children list is
		 * populated. The method can be removed when the application is ported
		 * to SMC 5.0.
		 */
		public Enumeration children() {
			getChildCount();
			return super.children();
		}

		abstract BoundingBox getBounds();
	}

	// =====================================================================
	/**
	 * Utility
	 * 
	 * @param path
	 * @return
	 */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ConstructionSafetyViewPanel.class
				.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			// System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * only requirement doc files - XML or Excel files allowed
	 */
	public class RequirementFileFilter extends FileFilter {

		// Accept all directories and some file formats.
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = ExtensionUtils.getExtension(f);
			if (extension != null) {
				if (extension.equals(ExtensionUtils.xml)
						|| extension.equals(ExtensionUtils.csv)
						|| extension.equals(ExtensionUtils.xls)
						|| extension.equals(ExtensionUtils.xlsx)) {
					return true;
				} else {
					return false;
				}
			}

			return false;
		}

		// The description of this filter
		public String getDescription() {
			return "Requirement File: Excel or xml files";
		}
	}

	/**
	 * get appropriate extensions
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

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}

	// private class TreeMouse extends RuleTreeTableMouseAdapter {
	//
	// TreeMouse(ConstraintResultsPanel arg0, ConstraintTreeTable arg1) {
	// super(arg0, arg1);
	// // TODO Auto-generated constructor stub
	// }
	//
	// }
	//
	// private class RPanel extends ConstraintResultsPanel {
	// public RPanel(ConstraintResultsView arg0) {
	// super(arg0);
	// // TODO Auto-generated constructor stub
	// }
	//
	// }

}
