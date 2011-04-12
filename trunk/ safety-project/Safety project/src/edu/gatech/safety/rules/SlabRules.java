package edu.gatech.safety.rules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.vecmath.Color3f;

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
import com.solibri.saf.plugins.visualizationplugin.tasks.ComponentVisualizationTask;

import edu.gatech.safety.utils.Utils;
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
		
		// clear ArrayList first then collect again
		visualizeObject.clear();
		
		model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
		stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
		slabs = (SSlab[]) model.findAll(SSlab.class);
		
		for (int i=0; i<stories.length; i++) {
			// print ==========
			System.out.print(stories[i].name.getStringValue() + " : " );
			SortedSet sSlab = stories[i].getRelated(SContains.class, true, SSlab.class);
			System.out.println("has " + sSlab.size() + " Slab objects.");
			
			Iterator itSlab = sSlab.iterator();
			while (itSlab.hasNext()) {
				Object oo = itSlab.next();
				SSlab ss = (SSlab) oo;
				
				// ****** ADD
				visualizeObject.add(ss);
				
				System.out.print("- " + ss.getDisplayName() + " : ");
				System.out.print("Bottom Area= " + Utils.sm2sf(ss.bottomArea.getDoubleValue(), 2) + " SF | ");
//				System.out.println("\tThickness= " + Utils.m2f(ss.thickness.getDoubleValue()/1000, 2) + " F");
//				System.out.println("\tBottom Height= " + Utils.m2f(ss.bottomElevation.getDoubleValue()/1000, 2) + " F");
				System.out.println("Area of Openings= " + Utils.sm2sf(ss.areaOfOpenings.getDoubleValue(), 2) + " SF");
				
				SortedSet sOpening = ss.getRelated(SVoids.class, false, SOpening.class);
				System.out.println("- " + ss.getDisplayName() + " has " + sOpening.size() + " Opening objects.");
				
				Iterator itOpening = sOpening.iterator();
				while (itOpening.hasNext()) {
					Object oo2 = itOpening.next();
					SOpening so = (SOpening) oo2;
					System.out.print("\t- " + so.getDisplayName() + " : ");
					System.out.print("Area= " + Utils.sm2sf(so.area.getDoubleValue(), 2) + " SF | ");
					System.out.println("Width= " + Utils.round(so.width.getDoubleValue(), 2) + " mm");
				}
				
			}
			
			
			
		}
		
//		System.out.println("Slabs *****");
//		for (int i =0; i < slabs.length; i++) {
//			visualizeObject.add(slabs[i]);
//			System.out.println(slabs[i].bottomArea.getDoubleValue() + " | " 
//					+ slabs[i].areaOfOpenings.getDoubleValue() );
//		}
//		System.out.println("Slabs end *****");
		
		
		Visualizer.visualizeNothing(); // clear current model view
		Visualizer.visualizeCol(visualizeObject); // visualize collected objects
		
	}
	
	
	
	
	private class SlabVisulizationTask extends VisualizationTask {

		@Override
		public void visualize(VisualizationInterface v) {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
	}
	
}
