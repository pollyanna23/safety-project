package edu.gatech.safety.rules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.vecmath.Color3f;

import com.solibri.sae.redox.DoubleProperty;
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
	public static ArrayList openings = new ArrayList();
	public static ArrayList data = new ArrayList();
	public static ArrayList no = new ArrayList();
	public static ArrayList name = new ArrayList();
	public static ArrayList level = new ArrayList();
	public static ArrayList width = new ArrayList();
	public static ArrayList height = new ArrayList();
	public static ArrayList area = new ArrayList();
	public static ArrayList disToLower = new ArrayList();
	public static ArrayList prevention = new ArrayList();
	public static ArrayList post = new ArrayList();
	public static ArrayList rail = new ArrayList();
	
	public static ArrayList check = new ArrayList();

	public static ArrayList visualizeObject = new ArrayList();

	// public static int p = 0;

	public SlabRules() {
	}

	public void getOpenings() {
		int p = 0;
		visualizeObject.clear();
		// no.clear();
		// name.clear();
		// level.clear();
		// width.clear();
		// height.clear();
		// area.clear();
		// disToLower.clear();
		// prevention.clear();
		if (no.size() == 0) {
			model = (SModel) ProductModelHandlingPlugin.getInstance()
					.getCurrentModel();
			stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
			slabs = (SSlab[]) model.findAll(SSlab.class);
			// for (int i = 0; i < stories.length; i++) {
			// System.out.println(stories[i].name.getStringValue() );
			// }
			for (int i = 0; i < stories.length; i++) {
				int minIndex = i;
				Comparable min = stories[i].bottomElevation.getDoubleValue();
				for (int j = i + 1; j < stories.length; j++) {
					if (min.compareTo(stories[j].bottomElevation
							.getDoubleValue()) > 0) // list[j].compareTo(min)
													// <
													// 0
					{
						min = stories[j].bottomElevation.getDoubleValue();
						minIndex = j;
					}
				}
				swap(stories, i, minIndex);
			}

			for (int i = 0; i < stories.length; i++) {
				// // print ==========
				double disToL;
				if (i != 0) {
					disToL = Utils
							.round((stories[i].bottomElevation.getDoubleValue() - stories[i - 1].bottomElevation
									.getDoubleValue()), 2);
				} else {
					disToL = 0.0;
				}

				System.out.print(stories[i].name.getStringValue() + " : ");
				System.out.print("DisToLower: " + disToL + "mm");
				SortedSet sSlab = stories[i].getRelated(SContains.class, true,
						SSlab.class);
				System.out.println("  has " + sSlab.size() + " Slab objects.");

				Iterator itSlab = sSlab.iterator();
				while (itSlab.hasNext()) {
					Object oo = itSlab.next();
					SSlab ss = (SSlab) oo;

					// ****** ADD
					visualizeObject.add(ss);

					System.out.print("- " + ss.getDisplayName() + " : ");
					System.out.println("Bottom Area= "
							+ Utils.round(ss.bottomArea.getDoubleValue(), 2)
							+ " m2 | ");
					// System.out.println("\tThickness= " +
					// Utils.m2f(ss.thickness.getDoubleValue()/1000, 2) + " F");
					System.out.println("\tBottom Height= "
							+ Utils.round(
									ss.bottomElevation.getDoubleValue() / 1000,
									2) + " m");
					System.out
							.println("Area of Openings= "
									+ Utils.round(
											ss.areaOfOpenings.getDoubleValue(),
											2) + " m2");

					SortedSet sOpening = ss.getRelated(SVoids.class, false,
							SOpening.class);
					System.out.println("- " + ss.getDisplayName() + " has "
							+ sOpening.size() + " Opening objects.");

					Iterator itOpening = sOpening.iterator();
					while (itOpening.hasNext()) {
						p++;
						Object oo2 = itOpening.next();
						SOpening so = (SOpening) oo2;
						// if (no.size() == 0) {
						openings.add(so);
						no.add(p);
						System.out.print("\t- " + so.getDisplayName() + " : ");
						name.add(so.getDisplayName());
						System.out.print(so.getContainer().getDisplayName()
								+ " | ");
						level.add(so.getContainer().getDisplayName());
						System.out.print("Area= "
								+ Utils.round(so.area.getDoubleValue(), 2)
								+ " m2 | ");
						area.add(Utils.round(so.area.getDoubleValue(), 2));
						System.out.print("Width= "
								+ Utils.round(so.width.getDoubleValue()/1000, 2)
								+ " m" + " | ");
						width.add(Utils.round(so.width.getDoubleValue()/1000, 2));

						System.out.print("Height= "
								+ Utils.round(so.area.getDoubleValue()/so.width.getDoubleValue()*1000, 2)
								+ " m" + " | ");
						height.add(Utils.round(so.area.getDoubleValue()/so.width.getDoubleValue()*1000, 2));
						disToLower.add(disToL);
						//Rule of opening on the slab
						if (Utils.round(so.width.getDoubleValue()/1000, 2) <= 1.0
								| Utils.round(so.area.getDoubleValue()/so.width.getDoubleValue()*1000, 2) <= 1.0) {
							prevention.add("Cover");
							System.out.println("Prevention= "+ "Cover");
						} else {
							prevention.add("Guardrail System");
							System.out.println("Prevention= "+ "Guardrail System");
						}
						check.add(new Boolean(false));
						// }
					}
				}
			}
		} else {
			model = (SModel) ProductModelHandlingPlugin.getInstance()
					.getCurrentModel();
			stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
			slabs = (SSlab[]) model.findAll(SSlab.class);
			// for (int i = 0; i < stories.length; i++) {
			// System.out.println(stories[i].name.getStringValue() );
			// }
			for (int i = 0; i < stories.length; i++) {
				int minIndex = i;
				Comparable min = stories[i].bottomElevation.getDoubleValue();
				for (int j = i + 1; j < stories.length; j++) {
					if (min.compareTo(stories[j].bottomElevation
							.getDoubleValue()) > 0) // list[j].compareTo(min)
													// <
													// 0
					{
						min = stories[j].bottomElevation.getDoubleValue();
						minIndex = j;
					}
				}
				swap(stories, i, minIndex);
			}

			for (int i = 0; i < stories.length; i++) {
				// // print ==========
				double disToL;
				if (i != 0) {
					disToL = Utils
							.round((stories[i].bottomElevation.getDoubleValue() - stories[i - 1].bottomElevation
									.getDoubleValue())/1000, 2);
				} else {
					disToL = 0.0;
				}

				System.out.print(stories[i].name.getStringValue() + " : ");
				System.out.print("DisToLower: " + disToL + "m");
				SortedSet sSlab = stories[i].getRelated(SContains.class, true,
						SSlab.class);
				System.out.println("  has " + sSlab.size() + " Slab objects.");

				Iterator itSlab = sSlab.iterator();
				while (itSlab.hasNext()) {
					Object oo = itSlab.next();
					SSlab ss = (SSlab) oo;

					// ****** ADD
					visualizeObject.add(ss);
				}
			}

		}
		Visualizer.visualizeNothing(); // clear current model view
		Visualizer.visualizeCol(visualizeObject); // visualize collected objects

	}

	private void swap(Comparable[] list, int a, int b) {
		Comparable temp = stories[a];
		stories[a] = stories[b];
		stories[b] = (SBuildingStorey) temp;
	}

	public void getSlabs() {
		no.clear();
		name.clear();
		level.clear();
		width.clear();
		height.clear();
		area.clear();
		disToLower.clear();
		prevention.clear();
		check.clear();
		System.out.println("--------------------");

		// clear ArrayList first then collect again
		visualizeObject.clear();

		model = (SModel) ProductModelHandlingPlugin.getInstance()
				.getCurrentModel();
		stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
		slabs = (SSlab[]) model.findAll(SSlab.class);
		// for (int i = 0; i < stories.length; i++) {
		// System.out.println(stories[i].name.getStringValue() );
		// }
		for (int i = 0; i < stories.length; i++) {
			int minIndex = i;
			Comparable min = stories[i].bottomElevation.getDoubleValue();
			for (int j = i + 1; j < stories.length; j++) {
				if (min.compareTo(stories[j].bottomElevation.getDoubleValue()) > 0) // list[j].compareTo(min)
																					// <
																					// 0
				{
					min = stories[j].bottomElevation.getDoubleValue();
					minIndex = j;
				}
			}
			swap(stories, i, minIndex);
		}

		for (int i = 0; i < stories.length; i++) {
			// // print ==========
			double disToL;
			if (i != 0) {
				disToL = Utils
						.round((stories[i].bottomElevation.getDoubleValue() - stories[i - 1].bottomElevation
								.getDoubleValue())/1000, 2);
			} else {
				disToL = 0.0;
			}

			System.out.print(stories[i].name.getStringValue() + " : ");
			System.out.print("DisToLower: " + disToL + "m");
			SortedSet sSlab = stories[i].getRelated(SContains.class, true,
					SSlab.class);
			System.out.println("  has " + sSlab.size() + " Slab objects.");

			Iterator itSlab = sSlab.iterator();
			while (itSlab.hasNext()) {
				Object oo = itSlab.next();
				SSlab ss = (SSlab) oo;

				// ****** ADD
				visualizeObject.add(ss);

				System.out.print("- " + ss.getDisplayName() + " : ");
				System.out.print("Bottom Area= "
						+ Utils.round(ss.bottomArea.getDoubleValue(), 2)
						+ " m2 | ");
				// System.out.println("\tThickness= " +
				// Utils.m2f(ss.thickness.getDoubleValue()/1000, 2) + " F");
				// System.out.println("\tBottom Height= " +
				// Utils.m2f(ss.bottomElevation.getDoubleValue()/1000, 2) +
				// " F");
				System.out.println("Area of Openings= "
						+ Utils.round(ss.areaOfOpenings.getDoubleValue(), 2)
						+ " m2");

				SortedSet sOpening = ss.getRelated(SVoids.class, false,
						SOpening.class);
				System.out.println("- " + ss.getDisplayName() + " has "
						+ sOpening.size() + " Opening objects.");

				Iterator itOpening = sOpening.iterator();
				while (itOpening.hasNext()) {
					// p++;
					Object oo2 = itOpening.next();
					SOpening so = (SOpening) oo2;

					// no.add(p);
					System.out.print("\t- " + so.getDisplayName() + " : ");
					name.add(so.getDisplayName());
					System.out
							.print(so.getContainer().getDisplayName() + " | ");
					// level.add(so.getContainer().getDisplayName());
					System.out.print("Area= "
							+ Utils.round(so.area.getDoubleValue(), 2)
							+ " m2 | ");
					// area.add(Utils.sm2sf(so.area.getDoubleValue(), 2));
					System.out.print("Width= "
							+ Utils.round(so.width.getDoubleValue()/1000, 2) + " m"
							+ " | ");
					// width.add(Utils.m2f(so.width.getDoubleValue(), 2));

					System.out.println("Height= "
							+ Utils.round(so.area.getDoubleValue()/so.width.getDoubleValue()*1000, 2)
							+ " m");
					// height.add(Utils.m2f(so.height.getDoubleValue(), 2));
					// disToLower.add(disToL);
					// prevention.add("Guardrail System");
					// check.add(new Boolean(false));
				}

			}

		}

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
