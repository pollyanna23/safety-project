package edu.gatech.construction.safety.utils;

import com.solibri.saf.plugins.unitsettingplugin.AreaUnitType;
import com.solibri.saf.plugins.unitsettingplugin.LengthUnitType;

public class Utils {

	public static double sm2sf(double in) {
		in = AreaUnitType.SQUARE_FEET.getExternalValue(in);
		return in;
	}

	public static double sm2sf(double in, int round) {
		in = round(AreaUnitType.SQUARE_FEET.getExternalValue(in), round);
		return in;
	}

	public static double m2f(double in) {
		in = LengthUnitType.DECIMAL_FEET.getExternalValue(in);
		return in;
	}

	public static double m2f(double in, int round) {
		in = round(LengthUnitType.DECIMAL_FEET.getExternalValue(in), round);
		return in;
	}

	/**
	 * Round double
	 * 
	 * @param num
	 * @param dec
	 * @return double Example String num = NumberUtil.round(1163.512, -2))
	 *         Return : 1200.0
	 */
	public static double round(double num, int dec) {

		double temp = decToDigit(dec);

		num = num * temp;
		num = Math.round(num);
		num = num / temp;
		return num;
	}

	/**
	 * Convert to Integer
	 * 
	 * @param dec
	 * @return int Example String num = NumberUtil.decToDigit(2) Return : 100.0
	 */
	private static double decToDigit(int dec) {
		double temp = 1;
		if (dec >= 1) {
			for (int i = 0; i < dec; i++) {
				temp = temp * 10;
			}
		} else if (dec < 1) {
			for (int i = dec; i < 0; i++) {
				temp = temp / 10;
			}
		}
		return temp;
	}

	/**
	 * Extract chars except comma and return Int (for floor level count)
	 * 
	 * @param str
	 * @return Int Example String num =
	 *         NumberUtil.stringToNumber("####1163.51244***123#####") Return :
	 *         1163
	 */
	public static int stringToInt(String str) {
		StringBuffer sb = new StringBuffer();
		String number = "1234567890";
		int returnInt = 0;

		for (int i = 0; i < str.length(); i++) {
			if (number.indexOf(str.charAt(i)) > -1) {
				sb.append(str.charAt(i));
			}
		}

		if (sb.length() == 0) { // avoid null exception
			returnInt = 0;
		} else {
			returnInt = Integer.parseInt(sb.toString());
		}
		return returnInt;
	}

}
