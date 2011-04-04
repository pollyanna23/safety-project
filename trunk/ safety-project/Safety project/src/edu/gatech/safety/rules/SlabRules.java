package edu.gatech.safety.rules;

import java.awt.Color;
import java.util.ArrayList;

import javax.vecmath.Color3f;

import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SRoof;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationInterface;
import com.solibri.saf.plugins.visualizationplugin.VisualizationPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationTask;
import com.solibri.saf.plugins.visualizationplugin.tasks.ComponentVisualizationTask;

import edu.gatech.safety.utils.Visualizer;

public class SlabRules {

	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;
	
	public static ArrayList visualizeObject = new ArrayList();
	
	public SlabRules() {		
	}
	
	public void getSlabs(){
		
		model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
		slabs = (SSlab[]) model.findAll(SSlab.class);
		
		
		System.out.println("Slabs *****");
		for (int i =0; i < slabs.length; i++) {
			visualizeObject.add(slabs[i]);
			System.out.println(slabs[i].bottomArea.getDoubleValue() + " | " 
					+ slabs[i].areaOfOpenings.getDoubleValue() );
		}
		System.out.println("Slabs end *****");
		
		Visualizer.visualizeCol(visualizeObject);
		
	}
	
	
	
	
	private class SlabVisulizationTask extends VisualizationTask {

		@Override
		public void visualize(VisualizationInterface v) {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
	}
	
}
