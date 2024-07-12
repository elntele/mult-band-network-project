/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Statistics.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	08/11/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * @author Danilo
 * @since 08/11/2013
 */
public class Statistics {
	private List<List<Double>> randomVariableValues = new Vector<>();

	private Map<Integer, Double> mean = new HashMap<Integer, Double>();

	private Map<Integer, Double> standardDeviation = new HashMap<Integer, Double>();

	private Map<String, Double> covariance = new HashMap<String, Double>();

	public Statistics() {
	}

	public int getSize() {
		return randomVariableValues.size();
	}

	public void addRandomVariableValues(List<Double> list) {
		randomVariableValues.add(list);
	}

	public double getMean(int index) {
		if (mean.get(index) == null) {
			double meanValue = 0;
			for (double value : randomVariableValues.get(index)) {
				meanValue += value;
			}
			meanValue /= randomVariableValues.get(index).size();
			mean.put(index, meanValue);
		}
		return mean.get(index);
	}

	public double getMedian(int index) {
		double mean = getMean(index);
		double median = 0;
		for (double value : randomVariableValues.get(index)) {
			median += (value - mean);
		}
		return median / randomVariableValues.get(index).size();
	}

	public double getMinValue(int index) {
		double min = Double.MAX_VALUE;
		for (double value : randomVariableValues.get(index)) {
			if (value < min) {
				min = value;
			}
		}
		return min;
	}

	public double getMaxValue(int index) {
		double max = Double.MIN_VALUE;
		for (double value : randomVariableValues.get(index)) {
			if (value > max) {
				max = value;
			}
		}
		return max;
	}

	public double getStandardDeviation(int index) {
		if (standardDeviation.get(index) == null) {
			double value = 0;
			double meanValue = getMean(index);
			for (double v : randomVariableValues.get(index)) {
				value += Math.pow(v - meanValue, 2);
			}
			value /= randomVariableValues.get(index).size();
			value = Math.sqrt(value);
			standardDeviation.put(index, value);
		}
		return standardDeviation.get(index);
	}

	public double getCovariance(int index1, int index2) {
		if (covariance.get(index1 + ";" + index2) == null) {
			double value = 0;
			double meanValue1 = getMean(index1);
			double meanValue2 = getMean(index2);
			int max = randomVariableValues.get(index1).size();
			if (randomVariableValues.get(index2).size() < max) {
				max = randomVariableValues.get(index2).size();
			}
			for (int i = 0; i < max; i++) {
				value += (randomVariableValues.get(index1).get(i) - meanValue1)
						* (randomVariableValues.get(index2).get(i) - meanValue2);
			}
			value /= max;
			if (Math.abs(value) > 1e-8) {
				covariance.put(index1 + ";" + index2, value);
			} else {
				covariance.put(index1 + ";" + index2, 0.0);
			}

		}
		return covariance.get(index1 + ";" + index2);
	}

	public double getCovariance(int index1, int index2, int index3) {
		double value = 0;
		double meanValue1 = getMean(index1);
		double meanValue2 = getMean(index2);
		double meanValue3 = getMean(index3);
		int max = randomVariableValues.get(index1).size();
		if (randomVariableValues.get(index2).size() < max) {
			max = randomVariableValues.get(index2).size();
		}
		if (randomVariableValues.get(index3).size() < max) {
			max = randomVariableValues.get(index3).size();
		}
		for (int i = 0; i < max; i++) {
			value += (randomVariableValues.get(index1).get(i) - meanValue1)
					* (randomVariableValues.get(index2).get(i) - meanValue2)
					* (randomVariableValues.get(index3).get(i) - meanValue3);
		}
		value /= max;
		return Math.abs(value) > 1e-8 ? value : 0.0;
	}

	public double getCorrelationCoefficient(int index1, int index2) {
		if (index1 == index2) {
			return 1;
		}
		double cov = getCovariance(index1, index2);
		if (cov == 0 || getStandardDeviation(index1) == 0 || getStandardDeviation(index1) == 0) {
			return 0;
		}
		return cov / (getStandardDeviation(index1) * getStandardDeviation(index2));

	}

	public double getCorrelationCoefficient(int index1, int index2, int index3) {
		if (index1 == index2 && index2 == index3) {
			return 1;
		}
		double rho1 = getCorrelationCoefficient(index1, index2);
		double rho2 = getCorrelationCoefficient(index1, index3);
		double rho3 = getCorrelationCoefficient(index2, index3);
		return Math.sqrt((rho1 * rho1 + rho2 * rho2 - 2 * rho1 * rho2 * rho3) / (1 - rho3 * rho3));

	}

//	public double getCorrelationCoefficient(int index1, int index2, int index3) {
//		if (index1 == index2 && index2 == index3) {
//			return 1;
//		}
//		double cov = getCovariance(index1, index2, index3);
//		if (cov == 0 || getStandardDeviation(index1) == 0 || getStandardDeviation(index2) == 0 || getStandardDeviation(index3) == 0) {
//			return 0;
//		}
//		return cov / (getStandardDeviation(index1) * getStandardDeviation(index2) * getStandardDeviation(index3));
//	}

	public double getVariance(int index) {
		return Math.pow(getStandardDeviation(index), 2);
	}
}
