package edu.gatech.safety.construction;

import java.awt.Color;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import com.solibri.sae.geometry.GeomUtils;
import com.solibri.sae.geometry.GeomUtils2D;
import com.solibri.sae.geometry.Point;
import com.solibri.sae.redox.Item;
import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.SEntity;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SColumn;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SOpening;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.sae.solibri.construction.SRoof;
import com.solibri.saf.plugins.layoutplugin.ImmutableArea;
import com.solibri.saf.plugins.layoutplugin.LayoutPlugin;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;
import com.solibri.saf.plugins.modelsearchtreeplugin.ModelSearchTreePlugin;
import com.solibri.saf.plugins.routeplugin.MergedSpace;
import com.solibri.saf.plugins.routeplugin.RoutePlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationInterface;
import com.solibri.saf.plugins.visualizationplugin.VisualizationPlugin;
import com.solibri.saf.plugins.visualizationplugin.VisualizationTask;
import com.solibri.saf.plugins.visualizationplugin.entities.LineArrayEntity;
import com.solibri.saf.plugins.visualizationplugin.entities.PointArrayEntity;
import com.solibri.sai.pmi.IComponent;

import edu.gatech.safety.utils.Utils;

/**
 * This is for visualizing building skins and fences
 * 
 * @author Jin-Kook Lee
 */

public class SafetyFenceDetect {

	double skinArea = 0.0;

	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;

	public SafetyFenceDetect() {
	}

	public void run() {
		VisualizationPlugin.getInstance().getVisualizer().visualize(new FenceVisulizationTask(null));
	}

	
	/*
	 * visualize exterior all perimeter polygons and points
	 */
	private class FenceVisulizationTask extends VisualizationTask {

		SBuildingStorey[] storeys;
		SSlab[] slabs;
		SWall[] walls;
		SOpening[] openings;

		public FenceVisulizationTask(SBuildingStorey[] storeys) {
			if (storeys == null) {
				SModel model = (SModel) ProductModelHandlingPlugin.getInstance().getCurrentModel();
				this.storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
				
			} else {
				this.storeys = storeys;
			}
		}
		
		private void swap(Comparable[] list, int a, int b) {
			Comparable temp = storeys[a];
			storeys[a] = storeys[b];
			storeys[b] = (SBuildingStorey) temp;
		}
		
		public void visualize(VisualizationInterface v) {
			int n = 0;
			ArrayList<Point> pointsBoundary = new ArrayList<Point>();
			ArrayList<Point> pointsHoles = new ArrayList<Point>();
			ArrayList<Point> pointsWall = new ArrayList<Point>();
			ArrayList<Point> pointsIntersect = new ArrayList<Point>();
			
			double fenseHeight = 400.0; // 1m height of fense
			double lengthBoundary = 0.0;
			double lengthFence = 0.0;
			
			// ## Visualize floor boundary
			for (int i = 0; i < storeys.length; i++) {
				int minIndex = i;
				Comparable min = storeys[i].bottomElevation.getDoubleValue();
				for (int j = i + 1; j < storeys.length; j++) {
					if (min.compareTo(storeys[j].bottomElevation.getDoubleValue()) > 0) 
					{
						min = storeys[j].bottomElevation.getDoubleValue();
						minIndex = j;
					}
				}
				swap(storeys, i, minIndex);
			}

			
			Point[] perimeterPoly;
			Point[] wallPoly;
			for (int i = 0; i < storeys.length; i++) {

				if (storeys[i].bottomElevation.getDoubleValue() >= 0) {
					FenseCalculator calculator = new FenseCalculator(storeys[i]);

					ImmutableArea aa = null;
					ImmutableArea bb = null;
					aa = calculator.getPerimeterArea();
					bb = calculator.getWallsArea();
					
					ArrayList polygons = new ArrayList();
					ArrayList polygonsWall = new ArrayList();
					LayoutPlugin.areaToPolygons(aa, polygons, null);
					LayoutPlugin.areaToPolygons(bb, polygonsWall, null);
					
					// points for boundary
					for (Iterator iter2 = polygons.iterator(); iter2.hasNext();) {
						n++;
						perimeterPoly = (Point[]) iter2.next();
						Point intersection = new Point();
						for (int j = 0; j < perimeterPoly.length; j++) {
							Point p1 = new Point(perimeterPoly[j]);
							Point p2 = new Point(perimeterPoly[(j + 1) % perimeterPoly.length]);
							p1.z = p2.z = storeys[i].bottomElevation.getDoubleValue();
							pointsBoundary.add(p1);
							pointsBoundary.add(p2);

							lengthBoundary += GeomUtils2D.length(p1, p2) * 0.001;

						}
						
					}
					
					
					// points for walls
					for (Iterator iter2 = polygonsWall.iterator(); iter2.hasNext();) {
						
						wallPoly = (Point[]) iter2.next();
						for (int j = 0; j < wallPoly.length; j++) {
							Point p1 = new Point(wallPoly[j]);
							Point p2 = new Point(wallPoly[(j + 1) % wallPoly.length]);
							p1.z = p2.z = storeys[i].bottomElevation.getDoubleValue();
							pointsWall.add(p1);
							pointsWall.add(p2);

						}
					}
					
					
					// intersection between wall and perimeter polygons
					Point[] wallPolygons = new Point[pointsWall.size()];
					pointsWall.toArray(wallPolygons);
					Point intersectionStart = new Point();
					
					for (int k = 0; k < pointsBoundary.size();) {
						Point p1 = (Point) pointsBoundary.get(k++);
						Point p2 = (Point) pointsBoundary.get(k++);
						for (int m = 0; m < pointsWall.size();) {
							Point p3 = (Point) pointsWall.get(m++);
		                    Point p4 = (Point) pointsWall.get(m++);
		                    
		                    // detect intersection points 
		                    Point intersection = new Point();
		                    if (GeomUtils2D.segmentSegmentIntersection(p1, p2, p3, p4, intersection)) {
		                    	if (! pointsIntersect.contains(intersection)) {
		                    		pointsIntersect.add(intersection);
		                    	}
		                    }
						}
						
						// add boundary points that are located out of wall polygons (e.g. corners)
	                    if (! GeomUtils2D.pointInPolygon(p1, wallPolygons)) {
	                    		pointsIntersect.add(p1);
	                    }
	                    if (! GeomUtils2D.pointInPolygon(p2, wallPolygons)) {
	                    		pointsIntersect.add(p2);
	                    }
						
					}
					
					// calculate fence's length
					for (int r = 0; r < pointsIntersect.size();){
						Point p1 = (Point) pointsIntersect.get(r++);
						Point p2 = (Point) pointsIntersect.get(r++);
						lengthFence += GeomUtils2D.length(p1, p2) * 0.001;
					}
					
					System.out.println("number of points for fences =" + pointsIntersect.size());
					System.out.println("lengthBoundary = " + lengthBoundary);
					System.out.println("lengthFence = " + lengthFence);
										
					
					// visualize perimeter polygon
					v.visualize(new LineArrayEntity(pointsBoundary,	new Color3f(Color.black), 0.0f, 2.0f));
//					v.visualize(new PointArrayEntity(pointsBoundary, new Color3f(Color.black), 0.0f, 6.0f));
					
					v.visualize(new LineArrayEntity(pointsWall,	new Color3f(Color.blue), 0.0f, 2.0f));
//					v.visualize(new PointArrayEntity(pointsWall, new Color3f(Color.blue), 0.0f, 6.0f));
					
					v.visualize(new LineArrayEntity(pointsIntersect, new Color3f(Color.red), 0.0f, 6.0f));
					v.visualize(new PointArrayEntity(pointsIntersect, new Color3f(Color.red), 0.0f, 6.0f));
					
				}
			}

			
		}

	}

	/*
	 * FenseCalculator
	 */
	public class FenseCalculator {
		private final SBuildingStorey storey;
		private Point[][] perimeter;
		private Point[][] walls;
		private Point[][] wallsSlabs;
		private Point[][] holes;
		private Point[][] internalPerimeter;
		private double zMin = Double.MAX_VALUE;
		private double zMax = -Double.MAX_VALUE;
		private ImmutableArea perimeterArea;
		private ImmutableArea wallsArea;
		private ImmutableArea wallsSlabsArea;
		private ImmutableArea holesArea;
		private ImmutableArea internalPerimeterArea;

		public FenseCalculator(SBuildingStorey storey) {
			this.storey = storey;
		}

		/**
		 * Gets the perimeter
		 * 
		 * @return the perimeter
		 */
		public Point[][] getPerimeter() {
			if (perimeter == null) {
				Area storeyArea = new Area();
				Point3d upper = new Point3d();
				Point3d lower = new Point3d();
				// collect walls and spaces
				SortedSet components = storey.getRelated(SContains.class, true, SEntity.class, Item.ANY_DEPTH);
				Collection<Area> areas = new ArrayList<Area>(components.size());
				for (Iterator iterator = components.iterator(); iterator.hasNext();) {
					IComponent component = (IComponent) iterator.next();
					ModelSearchTreePlugin.getInstance().getBounds(component, lower, upper);
					zMin = Math.min(zMin, lower.z);
					zMax = Math.max(zMax, upper.z);
					Area componentArea = null;
					
					if (component instanceof SSlab) {
						componentArea = LayoutPlugin.getAreaCopy(component);
						// Increase the space area by 10cm to fill possible
						// gaps:
						LayoutPlugin.resizeArea(componentArea, 200);
					}

					if (componentArea != null) {
						areas.add(componentArea);
					}
				}

				LayoutPlugin.areaUnion(storeyArea, areas);

				LayoutPlugin.resizeArea(storeyArea, -200);

				ArrayList<Point3d[]> polygons = new ArrayList<Point3d[]>();
				ArrayList<Point3d[]> holes = new ArrayList<Point3d[]>();
				LayoutPlugin.areaToPolygons(storeyArea, polygons, holes);
				perimeter = getCleanPolygons(polygons);
				this.holes = getCleanPolygons(holes);
			}
			return perimeter;
		}
		
		
		public Point[][] getWalls() {
			if (walls == null) {
				Area storeyArea = new Area();
				Point3d upper = new Point3d();
				Point3d lower = new Point3d();
				// collect walls and spaces
				SortedSet components = storey.getRelated(SContains.class, true, SEntity.class, Item.ANY_DEPTH);
				Collection<Area> areas = new ArrayList<Area>(components.size());
				for (Iterator iterator = components.iterator(); iterator.hasNext();) {
					IComponent component = (IComponent) iterator.next();
					ModelSearchTreePlugin.getInstance().getBounds(component, lower, upper);
					zMin = Math.min(zMin, lower.z);
					zMax = Math.max(zMax, upper.z);
					Area componentArea = null;
					
					if (component instanceof SWall) {
						componentArea = LayoutPlugin.getAreaCopy(component);
						// Increase the space area by 10cm to fill possible
						// gaps:
						LayoutPlugin.resizeArea(componentArea, 300);
					}

					if (componentArea != null) {
						areas.add(componentArea);
					}
				}

				LayoutPlugin.areaUnion(storeyArea, areas);

				LayoutPlugin.resizeArea(storeyArea, -100);

				ArrayList<Point3d[]> polygons = new ArrayList<Point3d[]>();
				LayoutPlugin.areaToPolygons(storeyArea, polygons, null);
				walls = getCleanPolygons(polygons);
			}
			return walls;
		}
		
		
		
		
		

		public Point[][] getHoles() {
			if (holes == null) {
				getPerimeter();
			}
			return holes;
		}

		public Point[][] getCleanPolygons(ArrayList<Point3d[]> polygons) {
			for (ListIterator<Point3d[]> iterator = polygons.listIterator(); iterator
					.hasNext();) {
				Point[] polygon = (Point[]) iterator.next();
				Vector<Point> vector = new Vector<Point>(Arrays.asList(polygon));
				GeomUtils.toClosedPolygon(vector, 50);
				double angleEpsilon = Math.toRadians(5);
				Point3d[] filtered = GeomUtils.filterPolyline(
						vector.toArray(new Point[vector.size()]), 300,
						angleEpsilon);
				iterator.set(filtered);
			}
			return polygons.toArray(new Point[polygons.size()][]);
		}

		public Point[][] getInternalPerimeter() {
			if (internalPerimeter == null) {
				Area storeyArea = new Area();
				Point3d upper = new Point3d();
				Point3d lower = new Point3d();
				// collect walls and spaces
				SortedSet components = storey.getRelated(SContains.class, true,
						SEntity.class, Item.ANY_DEPTH);
				Collection<Area> areas = new ArrayList<Area>(components.size());
				for (Iterator iterator = components.iterator(); iterator
						.hasNext();) {
					IComponent component = (IComponent) iterator.next();
					ModelSearchTreePlugin.getInstance().getBounds(component,
							lower, upper);
					zMin = Math.min(zMin, lower.z);
					zMax = Math.max(zMax, upper.z);
					Area componentArea = null;
					if (component instanceof SSpace) {
						componentArea = LayoutPlugin.getAreaCopy(component);
						// Increase the space area by 10cm to fill possible
						// gaps:
						LayoutPlugin.resizeArea(componentArea, 400);
					}
					if (componentArea != null) {
						areas.add(componentArea);
					}
				}

				LayoutPlugin.areaUnion(storeyArea, areas);

				LayoutPlugin.resizeArea(storeyArea, -400);

				ArrayList<Point3d[]> polygons = new ArrayList<Point3d[]>();
				LayoutPlugin.areaToPolygons(storeyArea, polygons, null);
				for (ListIterator<Point3d[]> iterator = polygons.listIterator(); iterator
						.hasNext();) {
					Point[] polygon = (Point[]) iterator.next();
					Vector<Point> vector = new Vector<Point>(
							Arrays.asList(polygon));
					GeomUtils.toClosedPolygon(vector, 10);
					double angleEpsilon = Math.toRadians(10);
					Point3d[] filtered = GeomUtils.filterPolyline(
							vector.toArray(new Point[vector.size()]), 100,
							angleEpsilon);
					iterator.set(filtered);
				}
				internalPerimeter = polygons
						.toArray(new Point[polygons.size()][]);
			}
			return internalPerimeter;
		}

		/**
		 * Returns the perimeter area
		 * 
		 * @return the perimeter area
		 */
		public ImmutableArea getPerimeterArea() {
			if (perimeterArea == null) {
				Area totalArea = new Area();
				Point[][] perimeter = getPerimeter();
				for (int i = 0; i < perimeter.length; i++) {
					Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
					totalArea.add(area);
					// System.out.println("P"+perimeter[i][0].toString());
				}
				perimeterArea = new ImmutableArea(totalArea);
			}
			return perimeterArea;
		}
		
		public ImmutableArea getWallsArea() {
			if (wallsArea == null) {
				Area totalArea = new Area();
				Point[][] perimeter = getWalls();
				for (int i = 0; i < perimeter.length; i++) {
					Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
					totalArea.add(area);
					// System.out.println("P"+perimeter[i][0].toString());
				}
				wallsArea = new ImmutableArea(totalArea);
			}
			return wallsArea;
		}
		
		
		public ImmutableArea getHolesArea() {
			if (holesArea == null) {
				Area totalArea = new Area();
				Point[][] perimeter = getHoles();
				for (int i = 0; i < perimeter.length; i++) {
					Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
					totalArea.add(area);
				}
				holesArea = new ImmutableArea(totalArea);
			}
			return holesArea;
		}

		// public ImmutableArea getInternalPerimeterArea() {
		// if (internalPerimeterArea == null) {
		// Area totalArea = new Area();
		// Point[][] perimeter = getInternalPerimeter();
		// for (int i = 0; i < perimeter.length; i++) {
		// Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
		// totalArea.add(area);
		// }
		// internalPerimeterArea = new ImmutableArea(totalArea);
		// }
		// return internalPerimeterArea;
		// }

		/**
		 * Getter for the maximum Z value
		 * 
		 * @return the max z value
		 */
		public double getZMax() {
			getPerimeter();
			return zMax;
		}

		/**
		 * Getter for the minimum Z value
		 * 
		 * @return the min z value
		 */
		public double getZMin() {
			getPerimeter();
			return zMin;
		}
	}

}
