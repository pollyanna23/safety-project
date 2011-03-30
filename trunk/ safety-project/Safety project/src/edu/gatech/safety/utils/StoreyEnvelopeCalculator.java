package edu.gatech.safety.utils;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.Vector;

import javax.vecmath.Point3d;

import com.solibri.sae.geometry.GeomUtils;
import com.solibri.sae.geometry.Point;
import com.solibri.sae.redox.Item;
import com.solibri.sae.solibri.SContains;
import com.solibri.sae.solibri.SEntity;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SColumn;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.plugins.layoutplugin.ImmutableArea;
import com.solibri.saf.plugins.layoutplugin.LayoutPlugin;
import com.solibri.saf.plugins.modelsearchtreeplugin.ModelSearchTreePlugin;
import com.solibri.sai.pmi.IComponent;

/**
 * A calculator for getting the perimeter of a storey
 * 
 * @author ppa
 * 
 */
public class StoreyEnvelopeCalculator {
	private final SBuildingStorey storey;
	private Point[][] perimeter;
	private Point[][] holes;
	private Point[][] internalPerimeter;
	private double zMin = Double.MAX_VALUE;
	private double zMax = -Double.MAX_VALUE;
	private ImmutableArea perimeterArea;
	private ImmutableArea holesArea;
	private ImmutableArea internalPerimeterArea;

	public StoreyEnvelopeCalculator(SBuildingStorey storey) {
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
			for (Iterator iterator = components.iterator(); iterator.hasNext();) {
				IComponent component = (IComponent) iterator.next();
				ModelSearchTreePlugin.getInstance().getBounds(component, lower,
						upper);
				zMin = Math.min(zMin, lower.z);
				zMax = Math.max(zMax, upper.z);
				Area componentArea = null;
				if (component instanceof SSpace) {
					componentArea = LayoutPlugin.getAreaCopy(component);
					// Increase the space area by 10cm to fill possible gaps:
					LayoutPlugin.resizeArea(componentArea, 200);
				} else if (component instanceof SWall) {
					componentArea = LayoutPlugin.getAreaCopy(component);
					// Increase the space area by 10cm to fill possible gaps:
					LayoutPlugin.resizeArea(componentArea, 200);
				} else if (component instanceof SColumn) {
					componentArea = LayoutPlugin.getAreaCopy(component);
					// Increase the space area by 10cm to fill possible gaps:
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
			GeomUtils.toClosedPolygon(vector, 10);
			double angleEpsilon = Math.toRadians(10);
			Point3d[] filtered = GeomUtils
					.filterPolyline(vector.toArray(new Point[vector.size()]),
							100, angleEpsilon);
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
			for (Iterator iterator = components.iterator(); iterator.hasNext();) {
				IComponent component = (IComponent) iterator.next();
				ModelSearchTreePlugin.getInstance().getBounds(component, lower,
						upper);
				zMin = Math.min(zMin, lower.z);
				zMax = Math.max(zMax, upper.z);
				Area componentArea = null;
				if (component instanceof SSpace) {
					componentArea = LayoutPlugin.getAreaCopy(component);
					// Increase the space area by 10cm to fill possible gaps:
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
				Vector<Point> vector = new Vector<Point>(Arrays.asList(polygon));
				GeomUtils.toClosedPolygon(vector, 10);
				double angleEpsilon = Math.toRadians(10);
				Point3d[] filtered = GeomUtils.filterPolyline(
						vector.toArray(new Point[vector.size()]), 100,
						angleEpsilon);
				iterator.set(filtered);
			}
			internalPerimeter = polygons.toArray(new Point[polygons.size()][]);
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

	public ImmutableArea getInternalPerimeterArea() {
		if (internalPerimeterArea == null) {
			Area totalArea = new Area();
			Point[][] perimeter = getInternalPerimeter();
			for (int i = 0; i < perimeter.length; i++) {
				Area area = LayoutPlugin.polygonToArea(perimeter[i], 0);
				totalArea.add(area);
			}
			internalPerimeterArea = new ImmutableArea(totalArea);
		}
		return internalPerimeterArea;
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
