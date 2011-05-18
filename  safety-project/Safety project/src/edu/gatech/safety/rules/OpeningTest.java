package edu.gatech.safety.rules;

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
import javax.vecmath.Tuple3d;

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
import com.solibri.saf.plugins.visualizationplugin.entities.TextEntity;
import com.solibri.saf.plugins.visualizationplugin.tasks.AreaVisualizationTask;
import com.solibri.sai.pmi.IComponent;

import edu.gatech.safety.utils.Utils;

/**
 * This is for visualizing building skins and fences
 * 
 * @author Jin-Kook Lee
 */

public class OpeningTest {

	double skinArea = 0.0;

	SModel model;
	SSpace[] spaces;
	SBuildingStorey[] stories;
	SSlab[] slabs;
	SWall[] walls;
	SRoof[] roofs;

	public OpeningTest() {
	}

	public void run() {
		VisualizationPlugin.getInstance().getVisualizer()
				.visualize(new FenceVisulizationTask(null));
	}

	public void runByStoreys(SBuildingStorey[] storeys) {
		VisualizationPlugin.getInstance().getVisualizer()
				.visualize(new FenceVisulizationTask(storeys));
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
			if (storeys == null) {
				SModel model = (SModel) ProductModelHandlingPlugin
						.getInstance().getCurrentModel();
				this.storeys = (SBuildingStorey[]) model
						.findAll(SBuildingStorey.class);
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

			ArrayList<Point> pointsBoundary = new ArrayList<Point>();
			// ArrayList<Point> pointsHoles = new ArrayList<Point>();
			ArrayList num = new ArrayList();
			int n = 0;
			double fenseHeight = 400.0; // 1m height of fense
			ArrayList fenseLength = new ArrayList();
			double fLength = 0.0;
			String holeNums = "";
			String edgeNums = "";
			ArrayList postNum1 = new ArrayList();
			int postNum2 = 0;

			for (int i = 0; i < storeys.length; i++) {
				int minIndex = i;
				Comparable min = storeys[i].bottomElevation.getDoubleValue();
				for (int j = i + 1; j < storeys.length; j++) {
					if (min.compareTo(storeys[j].bottomElevation
							.getDoubleValue()) > 0) // list[j].compareTo(min)
													// <
													// 0
					{
						min = storeys[j].bottomElevation.getDoubleValue();
						minIndex = j;
					}
				}
				swap(storeys, i, minIndex);
			}

			// ## Visualize floor boundary
			for (int i = 0; i < storeys.length; i++) {

				if (storeys[i].bottomElevation.getDoubleValue() > 1000) { //
					// 1m
					// height
					FenseCalculator calculator = new FenseCalculator(storeys[i]);

					ImmutableArea aa = null;
					aa = calculator.getPerimeterArea();

					// ImmutableArea bb = null;
					// bb = calculator.getHolesArea();

					ArrayList polygons = new ArrayList();
					LayoutPlugin.areaToPolygons(aa, polygons, null);

					// ArrayList polygons2 = new ArrayList();
					// LayoutPlugin.areaToPolygons(bb, polygons2, polygons2);

					for (Iterator iter2 = polygons.iterator(); iter2.hasNext();) {
						n++;
						num.add(n);
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

							edgeNums += ", "
									+ Utils.round(
											GeomUtils2D.length(p1, p2) * 0.001,
											2);
							// System.out.println(postNum2);
							if (GeomUtils2D.length(p1, p2) * 0.001 % 2.4 > 0) {
								postNum2 += (GeomUtils2D.length(p1, p2) * 0.001 / 2.4 + 2);
								// System.out.println(GeomUtils2D.length(p1, p2)
								// * 0.001 / 2.4 + 2);
							} else {
								postNum2 += (GeomUtils2D.length(p1, p2) * 0.001 / 2.4 + 1);
								// System.out.println(GeomUtils2D.length(p1, p2)
								// * 0.001 / 2.4 + 1);
							}
							// System.out.println(postNum2);
							fLength += GeomUtils2D.length(p1, p2) * 0.001;

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
						// System.out.println("num: "+ (postNum2 -
						// polygon.length));
						postNum1.add(postNum2 - polygon.length);
						// SlabRules.post.add(postNum1.get(n - 1));
						// SlabRules.rail.add(fenseLength.get(n - 1));
						System.out.print("Opening: Num " + num.get(n - 1)
								+ " :");
						System.out.print("   Post:  " + postNum1.get(n - 1));
						// System.out.println("Length!:  " +
						// fenseLength.get(n-1));
						fenseLength.add(fLength);
						System.out.println("   Length:  "
								+ fenseLength.get(n - 1));
						fLength = 0.0;
						postNum2 = 0;
					}
					// fenseLength.add(fLength);
					// System.out.println("Length!:  " + fenseLength.get(n-1));
					// fLength=0.0;
					// postNum2=0;
				}

				v.visualize(new LineArrayEntity(pointsBoundary, new Color3f(
						Color.blue), 0.0f, 2.0f));
				// v.visualize(new LineArrayEntity(pointsHoles, new Color3f(
				// Color.blue), 0.0f, 2.0f));

				// v.visualize(new PointArrayEntity(pointsBoundary, new Color3f(
				// Color.red), 0.0f, 6.0f));
				// v.visualize(new PointArrayEntity(pointsHoles, new Color3f(
				// Color.red), 0.0f, 6.0f));

			}

			// System.out.println("Post for opening: " + postNum1);
			// System.out.println("Handrail & Midrail & Toeboard for opening: "
			// + Utils.round(fenseLength, 2) + " Meters");

		}
	}

	/*
	 * FenseCalculator
	 */
	public class FenseCalculator {
		private final SBuildingStorey storey;
		private Point[][] perimeter;
		// private Point[][] holes;
		// private Point[][] internalPerimeter;
		private double zMin = Double.MAX_VALUE;
		private double zMax = -Double.MAX_VALUE;
		private ImmutableArea perimeterArea;

		// private ImmutableArea holesArea;
		// private ImmutableArea internalPerimeterArea;

		public FenseCalculator(SBuildingStorey storey) {
			this.storey = storey;
		}

		/**
		 * Gets the perimeter
		 * 
		 * @return the perimeter
		 */
		public Point[][] getPerimeter() {
			ArrayList sOpeningSub = new ArrayList();
			ArrayList sPreventionSub = new ArrayList();
			Area componentArea = null;
			Area componentArea1 = null;
			// Collection<Area> areas = new ArrayList<Area>(components.size());
			if (perimeter == null) {
				Area storeyArea = new Area();
				Point3d upper = new Point3d();
				Point3d lower = new Point3d();

				ArrayList sOpening1 = SlabRules.openings;
				for (int i = 0; i < sOpening1.size(); i++) {
					SOpening so = new SOpening();
					so = (SOpening) sOpening1.get(i);
					System.out.println("Opening level"
							+ so.getContainer().getDisplayName());
					System.out.println("Slab level" + storey.getDisplayName());
					if (so.getContainer().getDisplayName()
							.equals(storey.getDisplayName())) {
						sOpeningSub.add(so);
						sPreventionSub.add(SlabRules.prevention.get(i));
					}
				}
				System.out.println("HOLE!" + sOpeningSub.size());
				Collection<Area> areas1 = new ArrayList<Area>(
						sOpeningSub.size());
				// Collection<Area> areas2 = new
				// ArrayList<Area>(sOpeningSub.size());
				// Iterator itOpening1 = sOpening1.iterator();
				for (int i = 0; i < sOpeningSub.size(); i++) {
					if (sPreventionSub.get(i) == "Guardrail System") {
						componentArea = LayoutPlugin
								.getAreaCopy((IComponent) sOpeningSub.get(i));

					} else if (sPreventionSub.get(i) == "Cover") {

					}

					if (componentArea != null) {
						areas1.add(componentArea);
					}

				}

				LayoutPlugin.areaUnion(storeyArea, areas1);

				// LayoutPlugin.resizeArea(storeyArea, -100);

				ArrayList<Point3d[]> polygons = new ArrayList<Point3d[]>();
				ArrayList<Point3d[]> holes = new ArrayList<Point3d[]>();
				LayoutPlugin.areaToPolygons(storeyArea, polygons, holes);
				perimeter = getCleanPolygons(polygons);
				// this.holes = getCleanPolygons(holes);

			}

			return perimeter;
		}

		// public Point[][] getHoles() {
		// if (holes == null) {
		// getPerimeter();
		// }
		// return holes;
		// }

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
					// System.out.println("P"+perimeter[i][0].toString());
				}
				perimeterArea = new ImmutableArea(totalArea);
			}
			return perimeterArea;
		}

		// public ImmutableArea getHolesArea() {
		// if (holesArea == null) {
		// Area totalArea = new Area();
		// Point[][] perimeter = getHoles();
		// for (int i = 0; i < perimeter.length; i++) {
		// Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
		// totalArea.add(area);
		// }

		// holesArea = new ImmutableArea(totalArea);
		// }
		// return holesArea;
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
