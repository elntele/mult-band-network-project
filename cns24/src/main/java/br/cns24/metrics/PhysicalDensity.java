/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PhysicalDensity.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	16/12/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

/**
 * 
 * @author Danilo Araujo
 * @since 16/12/2014
 */
public class PhysicalDensity implements Metric<Double> {
	private static final PhysicalDensity instance = new PhysicalDensity();
	private static Double[][] completeMatrix;

	private PhysicalDensity() {
	}

	public static PhysicalDensity getInstance(Double[][] matrix) {
		completeMatrix = matrix;
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getDistances());
	}

	@Override
	public double calculate(Double[][] matrix) {
		int n = matrix.length;
		double sum = 0;
		double deployedSum = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				sum += completeMatrix[i][j];
				deployedSum += matrix[i][j];
			}
		}

		return deployedSum / sum;
	}

	@Override
	public String name() {
		return TMetric.PHYSICAL_AVERAGE_PATH_LENGTH.toString();
	}

}
