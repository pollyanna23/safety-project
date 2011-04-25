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

public class SafetyFence {

	double skinArea = 0.0;

	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;

	
	public SafetyFence() {
	}

	public void run() {
		VisualizationPlugin.getInstance().getVisualizer().visualize(new FenceVisulizationTask(null));
	}
	
	public void runByStoreys(SBuildingStorey[] storeys) {
		VisualizationPlugin.getInstance().getVisualizer().visualize(new FenceVisulizationTask(storeys));
	}
	
	
	
	/*
	 * visualize exterior all perimeter polygons and points
	 */
	private class FenceVisulizationTask extends VisualizationTask {

		SBuildingStorey[] storeys;		
		SSlab[] slabs;
		SWall[] walls;
		SOpening[] openings;
		
		
		public FenceVisulizationTask(String process, int proc) {
			
		}
		
		public FenceVisulizationTask(SBuildingStorey[] storeys) {
			if (storeys==null) {
				SModel model = (SModel) ProductModelHandlingPlugin.getInstance().getCurrentModel();
				this.storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
			} else {
				this.storeys = storeys;
			}
		}
		
		public void visualize(VisualizationInterface v) {
			
			ArrayList<Point> pointsBoundary = new ArrayList<Point>();
			ArrayList<Point> pointsHoles = new ArrayList<Point>();
			double fenseHeight = 400.0; // 1m height of fense
			double fenseLength = 0.0;
			double holeLength = 0.0;
			String holeNums = "";
			

			// ## Visualize floor boundary
			for (int i = 0; i < storeys.length; i++) {
				
//				if (storeys[i].bottomElevation.getDoubleValue() > 2000) { // 2m
																			// height
					FenseCalculator calculator = new FenseCalculator(storeys[i]);

					ImmutableArea aa = null;
					aa = calculator.getPerimeterArea();

					ImmutableArea bb = null;
					bb = calculator.getHolesArea();

					ArrayList polygons = new ArrayList();
					LayoutPlugin.areaToPolygons(aa, polygons, null);

					ArrayList polygons2 = new ArrayList();
					LayoutPlugin.areaToPolygons(bb, polygons2, polygons2);

					for (Iterator iter2 = polygons.iterator(); iter2.hasNext();) {
						Point[] polygon = (Point[]) iter2.next();
						int pointCount = 0;
						for (int j = 0; j < polygon.length; j++) {
							Point p1 = new Point(polygon[j]);
							Point p2 = new Point(polygon[(j + 1)
									% polygon.length]);
							p1.z = p2.z = storeys[i].bottomElevation
									.getDoubleValue() + fenseHeight;
							pointsBoundary.add(p1);
							pointsBoundary.add(p2);

							fenseLength += GeomUtils2D.length(p1, p2) * 0.001;

							Point p3 = new Point(polygon[j]);
							Point p4 = new Point(polygon[(j + 1)
									% polygon.length]);
							p3.z = p4.z = storeys[i].bottomElevation
									.getDoubleValue() + fenseHeight * 2;
							pointsBoundary.add(p3);
							pointsBoundary.add(p4);
							
							Point p5 = new Point(polygon[j]);
							Point p6 = new Point(polygon[(j + 1)
									% polygon.length]);
							p5.z = p6.z = storeys[i].bottomElevation
									.getDoubleValue();
							pointsBoundary.add(p5);
							pointsBoundary.add(p6);
						}
					}

					for (Iterator iter2 = polygons2.iterator(); iter2.hasNext();) {
						Point[] polygon = (Point[]) iter2.next();
						// Vector<Point> vector = new
						// Vector<Point>(Arrays.asList(polygon));
						// double angleEpsilon = Math.toRadians(15);
						// Point3d[] polygonFiltered =
						// GeomUtils.filterPolyline(vector.toArray(new
						// Point[vector.size()]), 100, angleEpsilon);
						int pointCount = 0;

						for (int j = 0; j < polygon.length; j++) {
							Point p1 = new Point(polygon[j]);
							Point p2 = new Point(polygon[(j + 1)
									% polygon.length]);
							p1.z = p2.z = storeys[i].bottomElevation
									.getDoubleValue() + fenseHeight;
							pointsHoles.add(p1);
							pointsHoles.add(p2);

							holeLength += GeomUtils2D.length(p1, p2) * 0.001;
							holeNums += ", "
									+ Utils.round(
											GeomUtils2D.length(p1, p2) * 0.001,
											2);

							Point p3 = new Point(polygon[j]);
							Point p4 = new Point(polygon[(j + 1)
									% polygon.length]);
							p3.z = p4.z = storeys[i].bottomElevation
									.getDoubleValue() + fenseHeight * 2;
							pointsHoles.add(p3);
							pointsHoles.add(p4);
							
							Point p5 = new Point(polygon[j]);
							Point p6 = new Point(polygon[(j + 1)
									% polygon.length]);
							p5.z = p6.z = storeys[i].bottomElevation
									.getDoubleValue();
							pointsHoles.add(p5);
							pointsHoles.add(p6);
						}
					}

//				}
				
				

				// visualize perimeter polygon
				v.visualize(new LineArrayEntity(pointsBoundary, new Color3f(
						Color.black), 0.0f, 2.0f));
				v.visualize(new LineArrayEntity(pointsHoles, new Color3f(
						Color.blue), 0.0f, 2.0f));
//				 v.visualize(new PointArrayEntity(pointsBoundary, new
//				 Color3f(Color.red), 0.0f, 6.0f));
//				 v.visualize(new PointArrayEntity(pointsHoles, new
//				 Color3f(Color.red), 0.0f, 6.0f));

				
			}

			// String str = "Fense length is total = " +
			// Utils.round(fenseLength, 2) + " M\n"
			// + "Holes length is total = " + Utils.round(holeLength, 2) + " M";
			// JOptionPane.showMessageDialog(null,
			// str,
			// "Message",
			// JOptionPane.INFORMATION_MESSAGE,
			// null);
			
			// print ==========
			System.out.println("Fense length is total = "
					+ Utils.round(fenseLength, 2) + " M");
			System.out.println("Holes length is total = "
					+ Utils.round(holeLength, 2) + " M");
			System.out.println("Holes length is = " + holeNums);

		}

		
	}
	
	
	

	/*
	 * FenseCalculator
	 */
	public class FenseCalculator {
		private final SBuildingStorey storey;
		private Point[][] perimeter;
		private Point[][] holes;
		private Point[][] internalPerimeter;
		private double zMin = Double.MAX_VALUE;
		private double zMax = -Double.MAX_VALUE;
		private ImmutableArea perimeterArea;
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
//					if (component instanceof SSpace) {
//						componentArea = LayoutPlugin.getAreaCopy(component);
//						// Increase the space area by 10cm to fill possible
//						// gaps:
//						LayoutPlugin.resizeArea(componentArea, 200);
//					} else if (component instanceof SWall) {
//						componentArea = LayoutPlugin.getAreaCopy(component);
//						// Increase the space area by 10cm to fill possible
//						// gaps:
//						LayoutPlugin.resizeArea(componentArea, 200);
//					} else if (component instanceof SColumn) {
//						componentArea = LayoutPlugin.getAreaCopy(component);
//						// Increase the space area by 10cm to fill possible
//						// gaps:
//						LayoutPlugin.resizeArea(componentArea, 200);
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
						vector.toArray(new Point[vector.size()]), 100,
						angleEpsilon);
				iterator.set(filtered);
			}
			return polygons.toArray(new Point[polygons.size()][]);
		}

//		public Point[][] getInternalPerimeter() {
//			if (internalPerimeter == null) {
//				Area storeyArea = new Area();
//				Point3d upper = new Point3d();
//				Point3d lower = new Point3d();
//				// collect walls and spaces
//				SortedSet components = storey.getRelated(SContains.class, true,
//						SEntity.class, Item.ANY_DEPTH);
//				Collection<Area> areas = new ArrayList<Area>(components.size());
//				for (Iterator iterator = components.iterator(); iterator
//						.hasNext();) {
//					IComponent component = (IComponent) iterator.next();
//					ModelSearchTreePlugin.getInstance().getBounds(component,
//							lower, upper);
//					zMin = Math.min(zMin, lower.z);
//					zMax = Math.max(zMax, upper.z);
//					Area componentArea = null;
//					if (component instanceof SSpace) {
//						componentArea = LayoutPlugin.getAreaCopy(component);
//						// Increase the space area by 10cm to fill possible
//						// gaps:
//						LayoutPlugin.resizeArea(componentArea, 400);
//					}
//					if (componentArea != null) {
//						areas.add(componentArea);
//					}
//				}
//
//				LayoutPlugin.areaUnion(storeyArea, areas);
//
//				LayoutPlugin.resizeArea(storeyArea, -400);
//
//				ArrayList<Point3d[]> polygons = new ArrayList<Point3d[]>();
//				LayoutPlugin.areaToPolygons(storeyArea, polygons, null);
//				for (ListIterator<Point3d[]> iterator = polygons.listIterator(); iterator
//						.hasNext();) {
//					Point[] polygon = (Point[]) iterator.next();
//					Vector<Point> vector = new Vector<Point>(
//							Arrays.asList(polygon));
//					GeomUtils.toClosedPolygon(vector, 10);
//					double angleEpsilon = Math.toRadians(10);
//					Point3d[] filtered = GeomUtils.filterPolyline(
//							vector.toArray(new Point[vector.size()]), 100,
//							angleEpsilon);
//					iterator.set(filtered);
//				}
//				internalPerimeter = polygons
//						.toArray(new Point[polygons.size()][]);
//			}
//			return internalPerimeter;
//		}

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
				}
				perimeterArea = new ImmutableArea(totalArea);
			}
			return perimeterArea;
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

//		public ImmutableArea getInternalPerimeterArea() {
//			if (internalPerimeterArea == null) {
//				Area totalArea = new Area();
//				Point[][] perimeter = getInternalPerimeter();
//				for (int i = 0; i < perimeter.length; i++) {
//					Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
//					totalArea.add(area);
//				}
//				internalPerimeterArea = new ImmutableArea(totalArea);
//			}
//			return internalPerimeterArea;
//		}

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
