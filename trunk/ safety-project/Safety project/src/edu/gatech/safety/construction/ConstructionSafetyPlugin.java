package edu.gatech.safety.construction;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JToolBar;

import com.solibri.sae.redox.Model;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.saf.core.DefaultPlugIn;
import com.solibri.saf.core.DefaultView;
import com.solibri.saf.core.DefaultViewToolBar;
import com.solibri.saf.core.IView;
import com.solibri.saf.core.Perspective;
import com.solibri.saf.core.PlugInExitException;
import com.solibri.saf.core.PluginStateChangedEvent;
import com.solibri.saf.plugins.modelhandling.ModelHandlingPluginStateChangedEvent;
import com.solibri.saf.plugins.modelhandling.ModelHandlingPluginStateEventTypes;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

import edu.gatech.safety.actions.TestAction;
import edu.gatech.safety.ui.ConstructionSafetyViewPanel;

public class ConstructionSafetyPlugin extends DefaultPlugIn {

	private DefaultView view;
	private TestAction TestAction = null;

	public Action getTestAction() {
		if (TestAction == null) {
			TestAction = new TestAction(this);
		}
		return TestAction;
	}

	private static ConstructionSafetyPlugin INSTANCE;
	ConstructionSafetyViewPanel panel = null;
	private JToolBar toolBar;

	public ConstructionSafetyPlugin() {
		if (INSTANCE != null) {
			throw new IllegalStateException("SafetyPlugin is singleton");
		}
		INSTANCE = this;
	}

	public ResourceBundle initResources(Locale currentLocale) {
		return ResourceBundle.getBundle(
				"edu.gatech.safety.res.ConstructionSafety", currentLocale);
	}

	public static ConstructionSafetyPlugin getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Returns the model containing the architectural concept
	 * @return the concept model
	 */
	public Model getConceptModel() {
		Model model = (Model) ProductModelHandlingPlugin.getInstance()
		.getCurrentModel();
		if (model != null) {
			// Find all sub models
			Model[] submodels = (Model[]) model.findAll(SModel.class);
			// Assume that the first sub model contains the concept
			Model conceptModel = submodels[0];
			return conceptModel;
		}
		return null;
	}

	public void exit() throws PlugInExitException {

	}

	public IView[] getViews(Perspective perspective) {
		if (perspective.getId().startsWith("SMC")) {
			return new IView[] { getView() };
		}
		return new IView[0];
	}

	private IView getView() {
		if (view == null) {
			view = new DefaultView("GT_CONSTRUCTION_SAFETY_PLUG-IN",
					getResources().getString("VIEW_NAME"));
			panel = new ConstructionSafetyViewPanel(view);
			view.setIcon(getIcon("/edu/gatech/safety/res/images/gt.gif"));
			view.setViewPanel(panel);
			view.setToolBar(getToolBar());
		}
		return view;
	}

	public JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new DefaultViewToolBar("");
			toolBar.add(getTestAction());
			// toolBar.add(getPacesSpaceNameMappingAction());

		}
		return toolBar;
	}

	public void update(PluginStateChangedEvent event) {
		if (event instanceof ModelHandlingPluginStateChangedEvent
				&& event.getPlugin() == ProductModelHandlingPlugin
						.getInstance()) {
			ModelHandlingPluginStateChangedEvent mhpsce = (ModelHandlingPluginStateChangedEvent) event;
			if (mhpsce.getType() == ModelHandlingPluginStateEventTypes.MODEL_MODIFIED
					|| mhpsce.getType() == ModelHandlingPluginStateEventTypes.MODEL_OPENED) {
				// panel.updateGraphics();
			}
		}
	}

	public void visualize() {
		//
	}
}