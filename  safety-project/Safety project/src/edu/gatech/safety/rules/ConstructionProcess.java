package edu.gatech.safety.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;

import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SRoof;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

import edu.gatech.safety.utils.Utils;
import edu.gatech.safety.utils.Visualizer;

public class ConstructionProcess {

	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;
	
	public HashMap<Double, SBuildingStorey> fh = new HashMap<Double, SBuildingStorey>();	
	public HashMap<Integer, SBuildingStorey> f = new HashMap<Integer, SBuildingStorey>();
	
	public static ArrayList visualizeObject = new ArrayList();
	
	public ConstructionProcess() {
	}
	
	
	public void buildFloor() {
		model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
		stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
		
		fh.clear();
		f.clear();

		double bottomHeight = 0.0;
		
		for(int i = 0; i < stories.length; i++) {
			SBuildingStorey levelOfSpace = stories[i];			
			bottomHeight = levelOfSpace.bottomElevation.getDoubleValue(); // bottom height
			fh.put(bottomHeight, levelOfSpace); // put the level name and height into the hashmap	
		}
		
		// sorting from bottom level to top level using metric height
		Object[] key = fh.keySet().toArray();
		Arrays.sort(key); 
		
		for (int i=0; i<key.length; ++i) {	
			f.put(i+1, fh.get(key[i]));
			System.out.println((i+1) + " : " + fh.get(key[i]).name.getStringValue());
		}
		
		
	}
	
	public void getProcess(int p) {		
		if (model == null) {
			buildFloor();
		}
		
//		try {
			visualizeObject.clear();
			
			visualizeObject.addAll(getSlab(p));
			visualizeObject.addAll(getWall(p-2));
			
			Visualizer.visualizeNothing(); // clear current model view
			Visualizer.visualizeCol(visualizeObject); // visualize collected objects
			
//		} catch (Exception e) {
//			System.err.println(e);
//		}
	}
	
	public ArrayList<SSlab> getSlab(int p) {
		ArrayList<SSlab> ob = new ArrayList<SSlab>();
		if (p>0) {
			SortedSet<SSlab> st = null;
			for (int i=0; i<=p; i++) {
				SBuildingStorey sb = f.get(i+1);
				st = sb.getRelated(SContains.class, true, SSlab.class);
				if (st.size() > 0) {
					Iterator<SSlab> it = st.iterator();
					while (it.hasNext()) {
						Object oo = it.next();
						ob.add((SSlab) oo);
					}
				}
			}
		}
		return ob;
	}
	
	public ArrayList<SWall> getWall(int p) {
		ArrayList<SWall> ob = new ArrayList<SWall>();
		if (p>0) {
			SortedSet<SWall> st = null;
			for (int i=0; i<=p; i++) {
				SBuildingStorey sb = f.get(i+1);
				st = sb.getRelated(SContains.class, true, SWall.class);
				if (st.size() > 0) {
					Iterator<SWall> it = st.iterator();
					while (it.hasNext()) {
						Object oo = it.next();
						ob.add((SWall) oo);
					}
				}
			}
		}
		return ob;
	}
	
	
	
}
