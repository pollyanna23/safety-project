package edu.gatech.safety.utils;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.vecmath.Color3f;

import com.solibri.sae.solibri.construction.SDoor;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SStair;
import com.solibri.sae.solibri.construction.util.ConstructionUtils;
import com.solibri.saf.plugins.modelfactory.ProductModelFactory;
import com.solibri.saf.plugins.routeplugin.RoutePlugin;
import com.solibri.saf.plugins.routeplugin.graph.RouteEndPoint;
import com.solibri.saf.plugins.routeplugin.graph.RouteGraphEdge;
import com.solibri.saf.plugins.visualizationplugin.VisualizationInterface;
import com.solibri.saf.plugins.visualizationplugin.VisualizationPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationTask;
import com.solibri.saf.plugins.visualizationplugin.tasks.ComponentVisualizationTask;
import com.solibri.sai.pmi.IComponent;

public class Visualizer {

	
	
	
	
	
	
	public static void visualizeNothing() {
		VisualizationTask task = new NothingVisulizationTask();
		VisualizationPlugin.getInstance().getVisualizer().visualize(task);
	}
	
	public static void visualizeCol(Collection col) {
		VisualizationPlugin.getInstance().getVisualizer().visualize(new ComponentVisualizationTask(col));
	}
	
	
	// draw footprint
	public static void visualizeFootprint() {
		VisualizationTask task = new RouteGraphComponentVisulizationTask();
		VisualizationPlugin.getInstance().getVisualizer().visualize(task);
	}
	
	
	
	private static class NothingVisulizationTask extends VisualizationTask {
		public void visualize(VisualizationInterface v) {
			v.setVisible(false);
		}
	}
	
	
	/**
     * This visualization visualizes the components linked to the graph.
     * Components that are not well linked have red color.
     */
    private static class RouteGraphComponentVisulizationTask extends VisualizationTask {
        
        public void visualize(VisualizationInterface v) {
            RoutePlugin plugin = RoutePlugin.getInstance();
            plugin.calculateRouteGraph();
            
            // init res spaces with normal spaces that dont have end points
            HashSet badSpaces = new HashSet();
            SModel model = (SModel) ProductModelFactory.getInstance().getModel();
            model.findAll(SSpace.class, badSpaces);
            for (Iterator iter = badSpaces.iterator(); iter.hasNext();) {
                SSpace space = (SSpace) iter.next();
                if(space.isSpaceGroup() ||
                        plugin.getEndPoints(plugin.getMergedSpace(space)).length > 0) {
                    iter.remove();
                }
            } 
            // init red components with doors and stairs
            HashSet badComponents = new HashSet();
            model.findAll(SDoor.class, badComponents);
            model.findAll(SStair.class, badComponents);

            Set allEdges = plugin.getGraph().edgeSet();
            HashSet okComponents = new HashSet();
            for (Iterator iter = allEdges.iterator(); iter.hasNext();) {
                Object next = iter.next();
                if(next instanceof RouteGraphEdge) {
                    RouteGraphEdge edge = (RouteGraphEdge) next;
                    RouteEndPoint start = (RouteEndPoint) edge.getSource();
                    RouteEndPoint end = (RouteEndPoint) edge.getTarget();
                    okComponents.add(getComponent(start));
                    okComponents.add(getComponent(end));
                }
            }
            badComponents.removeAll(okComponents);
            
            v.setVisible(false);
            v.resetColors(okComponents);
            v.setVisible(okComponents, false); // door objects invisible
//            v.setColors(badComponents, new Color3f(Color.white));
//            v.setColors(badSpaces, new Color3f(Color.white), 0.8f);
            v.setFootprintsVisible(okComponents);
        }
    }
    
    
    /**
     * Returns the component of the end point. If the component is an opening,
     * its door is returned.
     * 
     * @param endPoint the endpoint
     * @return         the component
     */
    private static IComponent getComponent(RouteEndPoint endPoint) {
        IComponent component = endPoint.getComponent();
        if (component instanceof SOpening) {
            SOpening opening = (SOpening) component;
            IComponent fill = ConstructionUtils.getFillFromOpening(opening, SDoor.class);
            if (fill != null) {
                component = fill;
            }
        }
        return component;
    }
	
}
