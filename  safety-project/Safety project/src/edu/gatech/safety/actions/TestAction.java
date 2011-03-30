package edu.gatech.safety.actions;

import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;

import com.solibri.sae.geometry.GeomUtils2D;
import com.solibri.sae.geometry.Point;
import com.solibri.sae.solibri.construction.SBuilding;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SRoof;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.core.AbstractPluginAction;
import com.solibri.saf.core.PlugInInterface;
import com.solibri.saf.core.PluginStateChangedEvent;
import com.solibri.saf.plugins.layoutplugin.ImmutableArea;
import com.solibri.saf.plugins.layoutplugin.LayoutPlugin;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

public final class TestAction extends AbstractPluginAction {
	
	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;
	
	//private ArrayList<GlobalWalls> globalWalls = new ArrayList<GlobalWalls>();

	private static final long serialVersionUID = 1L;
	
	public TestAction(PlugInInterface plugin) {
		super(plugin, "TestAction.NAME",
			"TestAction.DESCRIPTION_KEY",
			"TestAction.ICON_KEY", 1);
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
		// This is for a test
		System.out.println("Test ====================\n");
		
		getModelInfo();
	
		System.out.println("Test ====================\n");
	}
	
	
	
	
	
	
	
	
	public static void getModelInfo() {
		SModel model = (SModel) ProductModelHandlingPlugin.getInstance().getCurrentModel();
		SModel[] subModels = (SModel[]) model.findAll(SModel.class);
		String name = subModels[0].getName();
		String timeStamp = (String) subModels[0].getPropertyValue("IFC_FILE_NAME", "TIME_STAMP");
		String schemaname = (String) subModels[0].getPropertyValue("IFC_FILE_SCHEMA", "SCHEMA_NAME");
		String application = subModels[0].getApplicationName();
		
		System.out.println("name = " + name);
		System.out.println("timeStamp = " + timeStamp);
		System.out.println("schemaname = " + schemaname);
		System.out.println("application = " + application);
		
	}
	
	public static void getSpaceType() {
		
		SModel model = (SModel) ProductModelHandlingPlugin.getInstance().getCurrentModel();
        SBuildingStorey[] storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
        SSpace[] spaces = (SSpace[]) model.findAll(SSpace.class);        
        
        for (int i=0; i<spaces.length; ++i) {
        	System.out.print("[" + (i+1) + "] ");
        	System.out.print("Name: " + spaces[i].name.getStringValue());
        	System.out.print(" | ");
        	System.out.print("getType: " + spaces[i].getType(true));
        	//System.out.println("getDisplayName: " + spaces[i].getDisplayName());
        	System.out.println();
        }
        
        System.out.println("APP: " + model.getApplicationName());
		
	}
	
	
	
	
	
	
	private void collectGlobalWalls() {
		SModel model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
		SBuildingStorey[] storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
		SWall[] walls = (SWall[]) model.findAll(SWall.class);
		
		Double globalBottom = 0.0;
		Double globalTop = 0.0;
		Double storeyBottom = 0.0;
		
		for (int i=0; i<walls.length; ++i) {
			SBuildingStorey levelOfWall = (SBuildingStorey)walls[i].getContainer();
			
			
		}
		
		for (int i=0; i<storeys.length; ++i) {
			storeyBottom = storeys[i].bottomElevation.getDoubleValue();
			for(int j=0; j<walls.length; ++j) {
				SBuildingStorey levelOfSpace = (SBuildingStorey)walls[i].getContainer();
				if (storeys[i].name.getStringValue() == levelOfSpace.name.getStringValue()) { // same floor
					globalBottom = storeyBottom + walls[i].bottomElevation.getDoubleValue();
					globalTop = storeyBottom + walls[i].topElevation.getDoubleValue();
					
					GlobalWalls gw = new GlobalWalls();
					gw.setSBuildingStorey(storeys[i]);
					gw.setGlobalBottomElevation(globalBottom);
					gw.setGlobalTopElevation(globalTop);
					gw.setBottomArea(walls[i].bottomArea.getDoubleValue());
					//globalWalls.add(gw);
				}
				
			}
			
		}		
		
	}
	
	private class GlobalWalls extends SBuildingStorey {
		SBuildingStorey SBuildingStorey;
		double globalBottomElevation;
		double globalTopElevation;
		double bottomArea;
		GlobalWalls(){			
		}
		public SBuildingStorey getSBuildingStorey() {
			return SBuildingStorey;
		}
		public void setSBuildingStorey(SBuildingStorey buildingStorey) {
			SBuildingStorey = buildingStorey;
		}
		public double getGlobalBottomElevation() {
			return globalBottomElevation;
		}
		public void setGlobalBottomElevation(double globalBottom) {
			this.globalBottomElevation = globalBottom;
		}
		public double getGlobalTopElevation() {
			return globalTopElevation;
		}
		public void setGlobalTopElevation(double globalTop) {
			this.globalTopElevation = globalTop;
		}
		public double getBottomArea() {
			return bottomArea;
		}
		public void setBottomArea(double bottomArea) {
			this.bottomArea = bottomArea;
		}		
	}
	
	
	
			

	public void reactToPluginEvent(PluginStateChangedEvent event) {
		// TODO Auto-generated method stub

	}
}
