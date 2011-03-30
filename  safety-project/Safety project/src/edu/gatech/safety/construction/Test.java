package edu.gatech.safety.construction;

import java.text.MessageFormat;

import javax.swing.JOptionPane;

import com.solibri.sae.greenox.Problem;
import com.solibri.sae.greenox.ProblemCategory;
import com.solibri.sae.redox.Entity;
import com.solibri.sae.redox.IssueSeverity;
import com.solibri.sae.solibri.construction.SBuildingStorey;
import com.solibri.sae.solibri.construction.SColumn;
import com.solibri.sae.solibri.construction.SDoor;
import com.solibri.sae.solibri.construction.SModel;
import com.solibri.sae.solibri.construction.SSlab;
import com.solibri.sae.solibri.construction.SSpace;
import com.solibri.sae.solibri.construction.SWall;
import com.solibri.saf.plugins.checkingplugin.rule.DefaultRule;
import com.solibri.saf.plugins.modelhandling.ProductModelHandlingPlugin;

/**
 * This rule checks "Elevators for Egress" based on a basic program.
 * Each sub-module should be updated in detail later.  
 * @author Jin Kook Lee
 */

public class Test extends DefaultRule {
	private static final long serialVersionUID = 8337527104426098214L;

	private SModel model;
	private SBuildingStorey[] stories;
	private SSpace[] spaces;
	
	private Double buildingHeight = 0.0;
	
	
	public Test() {		
	}
	
	
	@Override
	protected Class<SModel> getCheckedClass() {
		return SModel.class;
	}

	@Override
	public void check(Entity entity) {
		
		model = (SModel)ProductModelHandlingPlugin.getInstance().getCurrentModel();
		stories = (SBuildingStorey[]) model.findAll(SBuildingStorey.class);
		spaces = (SSpace[]) model.findAll(SSpace.class);
		
		Double floorNet = 0.0;
		Double totalNet = 0.0;
		int elevatorCount = getElevatorCount();
		Double result = 0.0;
		Double finalResult = 0.0;
		String msg = "";
		
		// 1. get building height
		this.buildingHeight = calculateBuildingHeight() / 1000;
		System.out.println("buildingHeight = " + buildingHeight);
		if (buildingHeight < 31.0) {
			msg = "This building is lower than 31m. No need to check this rule.";
			System.out.println(msg);
		
		
		} else {
			System.out.println("This building has " + elevatorCount + " elevators.");
			
			for (int i=0; i<stories.length; ++i) {
				floorNet = 0.0;
				for (int j=0; j<spaces.length; j++) {
					SBuildingStorey floor = (SBuildingStorey)spaces[j].getContainer();
					if (compStr(floor.name.getStringValue().trim(), stories[i].name.getStringValue().trim())) {
						floorNet += spaces[j].area.getDoubleValue();  
					}					
				}
				
				totalNet += spaces[i].area.getDoubleValue();
				System.out.println("=========");
				System.out.println(stories[i].name.getStringValue() + "'s floorNet = " + floorNet);				
				
				if (floorNet > 1500) {
					result = ((floorNet - 1500) / 3000) + 1;
					//result = round(result, 3);
					result = Math.ceil(result);
				} else {
					result = 1.0;
				}
				
				finalResult += result;
				System.out.println("This floor has " + result);
				
			}
			System.out.println("totalNet = " + floorNet);
			
			if (elevatorCount >= finalResult) {
				msg = "This building is OK: result number is " + finalResult;
				System.out.println("\n========= Egress Elevator Checking Result:");
				System.out.println(msg);
			} else {
				msg = "Rule violated: result number is " + finalResult;
				System.out.println("\n========= Egress Elevator Checking Result:");
				System.out.println(msg);
			}
			
		}
		
		
		
		JOptionPane.showMessageDialog(null, msg);
		
	}
	
	
	
	
	
	/** TODO Upgrade required.
	 * This method calculates building height: Sum of all floor's height. 
	 * Under-ground floors're not considered. Slopped first floor is not considered.
	 * @return buildingHeight
	 */
	private Double calculateBuildingHeight() {
		Double buildingHeight = 0.0;
		for (int i=0; i<stories.length; ++i) {
			buildingHeight += stories[i].height.getDoubleValue();
		}
		return buildingHeight;
	}
	
	private int getElevatorCount() {
		int num = 0;
		for (int i=0; i<spaces.length; ++i) {
			if (isFirstFloor(spaces[i])) {
				if (isElevatorSpace(spaces[i])) {
					++ num;
				}
			}
		}
		return num;
	}
	
	private boolean isFirstFloor(SSpace space) {
		boolean bool = false;
		SBuildingStorey floor = (SBuildingStorey)space.getContainer();
		if (floor.bottomElevation.getDoubleValue() >= - 100 
				&& floor.bottomElevation.getDoubleValue() <= 100) {
			bool = true;
		}
		return bool;
	}
	
	private boolean isElevatorSpace(SSpace space) {
		boolean bool = false;
		if (contStr(space.name.getStringValue(), "elevator")) {
			bool = true;
		}
		return bool;
	}
	
	
	
	
	
	
	/////////////////////////// Following methods are common utilities. 
	
	public static boolean contStr(String s1, String s2) {
		boolean bool = false;
		// s1 will be a standard name, and s2 is IFC instance name for determining what s2 means.
		// lower cases without dots & double spaces: e.g) U.S.  Attorney -> us attorney, U.S.M.S -> usms
		try {
		s1 = s1.toLowerCase().trim().replace(".", "").replace("  ", " ");
		s2 = s2.toLowerCase().trim().replace(".", "").replace("  ", " ");
		if (s1.indexOf(s2)>=0) {
			bool = true;
		}		
		} catch (Exception e) {}
		
		return bool;
	}
	
	
	public static boolean compStr(String s1, String s2) {
		boolean bool = false;
		if (s1.trim().length()==0 && s2.trim().length()==0) {
			bool = true;
		} else {
			// s1 will be a standard name, and s2 is IFC instance name for determining what s2 means.
			// lower cases without dots & double spaces: e.g) U.S.  Attorney -> us attorney, U.S.M.S -> usms
			try {
			s1 = s1.toLowerCase().trim().replace(".", "").replace("  ", " ");
			s2 = s2.toLowerCase().trim().replace(".", "").replace("  ", " ");
			if (s1.compareToIgnoreCase(s2)==0) {
				bool = true;
			}		
			} catch (Exception e) {}			
		}		
		return bool;
	}
	
	public static double round(double num, int dec){
        
        double temp = decToDigit(dec);
        
        num = num*temp;
        num = Math.round(num);
        num = num/temp;
        return num;
	}

	private static double decToDigit(int dec){
        double temp = 1;
        if(dec>=1){
                for(int i=0; i<dec; i++){
                        temp = temp*10;
                }
        }else if(dec<1){
                for(int i=dec; i<0; i++){
                        temp = temp/10;
                }
        }
        return temp;
	}
}
