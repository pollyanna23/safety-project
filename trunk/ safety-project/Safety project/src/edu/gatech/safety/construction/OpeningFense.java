package edu.gatech.safety.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.SVoids;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SRoof;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationInterface;
import com.solibri.saf.plugins.visualizationplugin.VisualizationPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationTask;

import edu.gatech.safety.utils.Visualizer;


public class OpeningFense {

	SModel model;
	
	public ArrayList visualizeObject = new ArrayList();
	
		
	public OpeningFense() {
	}
	
	
	public void run() {
		VisualizationPlugin.getInstance().getVisualizer().visualize(new OpeningFenseVisulizationTask(null));
	}
	
	public void runByStoreys(SBuildingStorey[] storeys) {
		VisualizationPlugin.getInstance().getVisualizer()
				.visualize(new OpeningFenseVisulizationTask(storeys));
	}
	
	
	private class OpeningFenseVisulizationTask extends VisualizationTask {
		
		SBuildingStorey[] storeys;
		SSlab[] slabs;
		SWall[] walls;
		SOpening[] openings;
		
		public OpeningFenseVisulizationTask(SBuildingStorey[] storeys) {
			if (storeys == null) {
				model = (SModel) ProductModelHandlingPlugin.getInstance().getCurrentModel();
				this.storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
			} else {
				this.storeys = storeys;
			}
		}

		public void visualize(VisualizationInterface v) {
			visualizeObject.clear();
			
			for (int i = 0; i < storeys.length; i++) {
				
				SortedSet sWall = storeys[i].getRelated(SContains.class, true, SWall.class);
				Iterator itWall = sWall.iterator();
				while (itWall.hasNext()) {
					Object oo = itWall.next();
					SWall sw = (SWall) oo;
					SortedSet sOpening = sw.getRelated(SVoids.class, false,	SOpening.class);
					Iterator itOpening = sOpening.iterator();
					while (itOpening.hasNext()) {
						Object oo2 = itOpening.next();
						SOpening so = (SOpening) oo2;
						visualizeObject.add(sw);
						System.out.println("***** " + so.getDisplayName() + " : ");
					}
					
				}
				
				
			}
			
			
			Visualizer.visualizeNothing();
			Visualizer.visualizeCol(visualizeObject);			
			
		}
		
	}
	
}
