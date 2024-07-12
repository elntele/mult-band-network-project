/**
 * 
 */
package br.cns24.som;

import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Danilo
 * 
 */
public class Util {
	public static Logger LOGGER = Logger.getLogger("JOL");

	public static NumberFormat decimalFormat = NumberFormat.getInstance();

	static {
		LOGGER.setLevel(Level.INFO);
		decimalFormat.setMaximumFractionDigits(4);
		decimalFormat.setMinimumFractionDigits(4);
	}

	/**
	 * Gets the maximun values for each objectives in a given pareto front
	 * 
	 * @param front
	 *            The pareto front
	 * @param noObjectives
	 *            Number of objectives in the pareto front
	 * @return double [] An array of noOjectives values whit the maximun values
	 *         for each objective
	 **/
	public static double[] getMaximumValues(double[][] front, int noObjectives) {
		double[] maximumValue = new double[noObjectives];
		for (int i = 0; i < noObjectives; i++)
			maximumValue[i] = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < front.length; i++) {
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] > maximumValue[j])
					maximumValue[j] = front[i][j];
			}
		}

		return maximumValue;
	}

	public static double[][] getNormalizedFront(double[][] front,
			double[] maximumValue, double[] minimumValue) {

		double[][] normalizedFront = new double[front.length][];

		for (int i = 0; i < front.length; i++) {
			normalizedFront[i] = new double[front[i].length];
			for (int j = 0; j < front[i].length; j++) {
				normalizedFront[i][j] = (front[i][j] - minimumValue[j])
						/ (maximumValue[j] - minimumValue[j]);
			}
		}
		return normalizedFront;
	}

	public static double[][] getInvertedFront(double[][] front) {
		double[][] invertedFront = new double[front.length][];

		for (int i = 0; i < front.length; i++) {
			invertedFront[i] = new double[front[i].length];
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] <= 1.0 && front[i][j] >= 0.0) {
					invertedFront[i][j] = 1.0 - front[i][j];
				} else if (front[i][j] > 1.0) {
					invertedFront[i][j] = 0.0;
				} else if (front[i][j] < 0.0) {
					invertedFront[i][j] = 1.0;
				}
			}
		}
		return invertedFront;
	}

	/**
	 * Gets the minimun values for each objectives in a given pareto front
	 * 
	 * @param front
	 *            The pareto front
	 * @param noObjectives
	 *            Number of objectives in the pareto front
	 * @return double [] An array of noOjectives values whit the minimum values
	 *         for each objective
	 **/
	public static double[] getMinimumValues(double[][] front, int noObjectives) {
		double[] minimumValue = new double[noObjectives];
		for (int i = 0; i < noObjectives; i++)
			minimumValue[i] = Double.MAX_VALUE;

		for (int i = 0; i < front.length; i++) {
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] < minimumValue[j])
					minimumValue[j] = front[i][j];
			}
		}
		return minimumValue;
	}
}
