package edu.gatech.construction.safety;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Color3f;

import com.solibri.sae.greenox.ConstraintToolsPanel;
import com.solibri.sae.redox.Entity;
import com.solibri.sae.solibri.construction.SDoor;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SStair;
import com.solibri.sae.solibri.construction.util.ConstructionUtils;
import com.solibri.saf.plugins.checkingplugin.rule.DefaultRule;
import com.solibri.saf.plugins.modelfactory.ProductModelFactory;
import com.solibri.saf.plugins.routeplugin.RoutePlugin;
import com.solibri.saf.plugins.routeplugin.graph.RouteEndPoint;
import com.solibri.saf.plugins.routeplugin.graph.RouteGraphEdge;
import com.solibri.saf.plugins.routeplugin.graph.routegenerators.DefaultRouteGenerator;
import com.solibri.saf.plugins.unitsettingplugin.UnitSettingPlugin;
import com.solibri.saf.plugins.unitsettingplugin.ValueWithUnitField;
import com.solibri.saf.plugins.visualizationplugin.VisualizationInterface;
import com.solibri.saf.plugins.visualizationplugin.VisualizationPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationTask;
import com.solibri.sai.pmi.IComponent;

public class ConstructionSafetyRule extends DefaultRule {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class getCheckedClass() {
		return SSlab.class;
	}

	public void preCheck() {
	}

	@Override
	public void check(Entity arg0) {
		// do nothing at this time
	}

	/**
	 * This method must be implemented to enable toolspanel
	 */
	public boolean hasToolsPanel() {
		return true;
	}

	/**
	 * This method must be implemented to return the custom tools panel
	 */
	public ConstraintToolsPanel getToolsPanel() {
		return new ConstructionSafetyToolsPanel();
	}

	// a test tools panel for visualizations and tools
	private static class ConstructionSafetyToolsPanel extends
			ConstraintToolsPanel {
		private static final long serialVersionUID = 1L;

		private ConstructionSafetyToolsPanel() {
			this.setLayout(new GridLayout(9, 1));
			JPanel panel1 = new JPanel();
			JPanel panel2 = new JPanel();

			panel1.setLayout(new GridLayout(1, 3));
			panel2.setLayout(new GridLayout(1, 5));

			panel1.setBorder(BorderFactory
					.createTitledBorder("Safety Rule Visualization"));
			panel2.setBorder(BorderFactory.createTitledBorder("Test Tools"));

			this.add(panel1);
			this.add(panel2);

			panel1.add(new JButton(new VisualizeFenseComponentsAction()));
			panel2.add(new JButton("Test: N/A"));

		}
	}

	private static class VisualizeFenseComponentsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		SafetyFense sf = new SafetyFense();

		private VisualizeFenseComponentsAction() {
			putValue(Action.NAME, "Visualize Construction Safety Fenses");
		}

		public void actionPerformed(ActionEvent e) {
			sf.run();
		}
	}

}
