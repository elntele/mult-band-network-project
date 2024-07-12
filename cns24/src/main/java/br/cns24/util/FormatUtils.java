package br.cns24.util;

import java.text.NumberFormat;

public class FormatUtils {
	private static final FormatUtils instance = new FormatUtils();

	public static final NumberFormat decimalFormat = NumberFormat.getInstance();
	public static final NumberFormat simpleFormat = NumberFormat.getInstance();

	static {
		decimalFormat.setMinimumFractionDigits(3);
		decimalFormat.setMaximumFractionDigits(3);
		simpleFormat.setMinimumFractionDigits(0);
		simpleFormat.setMaximumFractionDigits(0);
	}

	private FormatUtils() {
	}

	public String toScientific(double num) {
		String result = "";
		if (Math.abs(num) >= 0.01) {
			result = decimalFormat.format(num);
		} else {
			if (num < 0) {
				result = "-" + toScientific(Math.abs(num));
			} else {
				double power = (int) (Math.log(num) / Math.log(10));
				if (power < 0)
					power--;
				double fraction = num / Math.pow(10, power);

				String sign = "";
				if (power > 0)
					sign = "+";
				result += decimalFormat.format(fraction) + "e" + sign
						+ simpleFormat.format(power);
			}
		}
		return result;
	}

	public String getValue(Double value) {
		String formattedValue;
		if (value == -0){
			return "0,00";
		}
		if (value < -1000000000) {
			formattedValue = "-Infinito";
		} else if (Double.isInfinite(value) || Double.isNaN(value)
				|| value > 1000000000) {
			formattedValue = "Infinito";
		} else {
			if (value > 1000000 || value < 0.01) {
				formattedValue = toScientific(value);
			} else {
				formattedValue = decimalFormat.format(value);
			}
		}
		return formattedValue;
	}

	public static FormatUtils getInstance() {
		return instance;
	}

}
