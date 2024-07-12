/**
 * 
 */
package br.bm.core;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Danilo
 * 
 */
public class Util {
	public static Logger LOGGER = Logger.getLogger("JOL");

	public static final int GRAV_EXP_STEP = 5;

	static {
		try {
			LOGGER.setLevel(Level.INFO);
			FileHandler handler = new FileHandler("jol.txt");
			handler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the distance between a point and the nearest one in a given front
	 * (the front is given as <code>double [][]</code>)
	 * 
	 * @param point
	 *            The point
	 * @param front
	 *            The front that contains the other points to calculate the
	 *            distances
	 * @return The minimun distance between the point and the front
	 **/
	public static double distanceToClosedPoint(double[] point, double[][] front) {
		double minDistance = distance(point, front[0]);

		for (int i = 1; i < front.length; i++) {
			double aux = distance(point, front[i]);
			if (aux < minDistance) {
				minDistance = aux;
			}
		}

		return minDistance;
	} // distanceToClosedPoint

	/**
	 * This method returns the distance (taken the euclidean distance) between
	 * two points given as <code>double []</code>
	 * 
	 * @param a
	 *            A point
	 * @param b
	 *            A point
	 * @return The euclidean distance between the points
	 **/
	public static double distance(double[] a, double[] b) {
		double distance = 0.0;

		for (int i = 0; i < a.length; i++) {
			distance += Math.pow(a[i] - b[i], 2.0);
		}
		return Math.sqrt(distance);
	} // distance

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

	public static double[][] getNormalizedFront(double[][] front, double[] maximumValue, double[] minimumValue) {

		double[][] normalizedFront = new double[front.length][];

		for (int i = 0; i < front.length; i++) {
			normalizedFront[i] = new double[front[i].length];
			for (int j = 0; j < front[i].length; j++) {
				normalizedFront[i][j] = (front[i][j] - minimumValue[j]) / (maximumValue[j] - minimumValue[j]);
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
