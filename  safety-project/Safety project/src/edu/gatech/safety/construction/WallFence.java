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
import com.solibri.sae.solibri.SVoids;
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

import edu.gatech.safety.construction.SafetyFence.FenseCalculator;
import edu.gatech.safety.utils.Utils;

/**
 * This is for visualizing building skins and fences
 * 
 * @author Jin-Kook Lee
 */

public class WallFence {

	double skinArea = 0.0;

	SModel model;
	SSpace[] spaces;
	// SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;

	public WallFence() {
	}

	public void run() {
		VisualizationPlugin.getInstance().getVisualizer()
				.visualize(new FenceVisulizationTask(null));
	}

	public void runByStoreys(SWall[] walls) {
		VisualizationPlugin.getInstance().getVisualizer()
				.visualize(new FenceVisulizationTask(walls));
	}

	/*
	 * visualize exterior all perimeter polygons and points
	 */
	private class FenceVisulizationTask extends VisualizationTask {

		SBuildingStorey[] storeys;
		// SSlab[] slabs;
		SWall[] walls;
		SOpening[] openings;

		public FenceVisulizationTask(String process, int proc) {

		}

		public FenceVisulizationTask(SWall[] walls) {
			if (walls == null) {
				SModel model = (SModel) ProductModelHandlingPlugin
						.getInstance().getCurrentModel();
				this.walls = (SWall[]) model.findAll(SWall.class);
			} else {
				this.walls = walls;
			}
		}

		public void visualize(VisualizationInterface v) {
			ArrayList<Point> pointsBoundary = new ArrayList<Point>();
			ArrayList<Point> pointsHoles = new ArrayList<Point>();

			model = (SModel) ProductModelHandlingPlugin.getInstance()
					.getCurrentModel();
			storeys = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);

			walls = (SWall[]) model.findAll(SWall.class);

			for (int i = 0; i < storeys.length; i++) {
				FenseCalculator calculator = new FenseCalculator(storeys[i]);

				int minIndex = i;
				Comparable min = storeys[i].bottomElevation.getDoubleValue();
				for (int j = i + 1; j < storeys.length; j++) {
					if (min.compareTo(storeys[j].bottomElevation
							.getDoubleValue()) > 0)

					{
						min = storeys[j].bottomElevation.getDoubleValue();
						minIndex = j;
					}
				}
				swap(storeys, i, minIndex);
			}

			for (int i = 0; i < storeys.length; i++) {
				// // print ==========
				double disToL;
				if (i != 0) {
					disToL = Utils
							.round((storeys[i].bottomElevation.getDoubleValue() - storeys[i - 1].bottomElevation
									.getDoubleValue()), 2);
				} else {
					disToL = 0.0;
				}

				System.out.print(storeys[i].name.getStringValue() + " : ");
				System.out.print("DisToLower: " + disToL + "mm");

				SortedSet sWall = storeys[i].getRelated(SContains.class, true,
						SWall.class);

				System.out.println("  has " + sWall.size() + " Wall objects.");

				Iterator itWall = sWall.iterator();
				while (itWall.hasNext()) {
					Object oo = itWall.next();
					SWall ss = (SWall) oo;

					System.out.print("- " + ss.getDisplayName() + " : ");

					SortedSet sOpening = ss.getRelated(SVoids.class, false,
							SOpening.class);
					System.out.println("- " + ss.getDisplayName() + " has "
							+ sOpening.size() + " Opening objects.");

					Iterator itOpening = sOpening.iterator();
					while (itOpening.hasNext()) {

						// System.out.print(p);
						Object oo2 = itOpening.next();
						SOpening so = (SOpening) oo2;
						System.out.println("Bottom"
								+ so.bottomElevation.getDoubleValue());
						so.getCoordinateSystem();
						// width.add(Utils.m2f(so.width.getDoubleValue(), 2));

						// height.add(Utils.m2f(so.height.getDoubleValue(), 2));

						FenseCalculator calculator = new FenseCalculator(
								storeys[i]);
						ImmutableArea aa = null;
						aa = calculator.getHolesArea();
						ArrayList polygons = new ArrayList();
						LayoutPlugin.areaToPolygons(aa, polygons, null);
						for (Iterator iter2 = polygons.iterator(); iter2
								.hasNext();) {
							Point[] polygon = (Point[]) iter2.next();
							int pointCount = 0;
							for (int j = 0; j < polygon.length; j++) {
								Point p1 = new Point(polygon[j]);
								Point p2 = new Point(polygon[(j + 1)
										% polygon.length]);
								p1.z = p2.z = storeys[i].bottomElevation
										.getDoubleValue();
								pointsHoles.add(p1);
								pointsHoles.add(p2);

							}
							// postNum1 = postNum1 - polygon.length;
							// System.out.println("Post for edge: " + postNum1);
						}
					}
				}

				v.visualize(new LineArrayEntity(pointsHoles, new Color3f(
						Color.blue), 0.0f, 2.0f));
			}
		}

		private void swap(Comparable[] list, int a, int b) {
			Comparable temp = storeys[a];
			storeys[a] = storeys[b];
			storeys[b] = (SBuildingStorey) temp;
		}
	}

	/*
	 * FenseCalculator
	 */
	public class FenseCalculator {
		private final SBuildingStorey storey;
		// private final SWall wall;
		private Point[][] perimeter;
		private Point[][] holes;
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
					
					if (component instanceof SWall) {
						componentArea = LayoutPlugin.getAreaCopy(component);
						// Increase the space area by 10cm to fill possible
						// gaps:
						LayoutPlugin.resizeArea(componentArea, 200);
					}

					if (componentArea != null) {
						areas.add(componentArea);
					}
				}

				// LayoutPlugin.areaUnion(storeyArea, areas);

				LayoutPlugin.resizeArea(storeyArea, -350);

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
						vector.toArray(new Point[vector.size()]), 300,
						angleEpsilon);
				iterator.set(filtered);
			}
			return polygons.toArray(new Point[polygons.size()][]);
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
