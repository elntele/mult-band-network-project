/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Assortativity.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	23/03/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.DegreeMatrix;

/**
 * 
 * @author Danilo
 * @since 23/03/2014
 */
public class Assortativity implements Metric<Integer> {
	private static final Assortativity instance = new Assortativity();

	private Assortativity() {
	}

	public static Assortativity getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		int n = matrix.length;

		double total_degree = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (matrix[i][j] == 1) {
					total_degree++;
				}
			}
		}

		double num1 = 0, num2 = 0, den = 0;
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (matrix[i][j] == 1) {
					num1 += degree[i][i] * degree[j][j];
					num2 += (degree[i][i] + degree[j][j]);
					den += (degree[i][i] * degree[i][i] + degree[j][j] * degree[j][j]);
				}
			}
		}
		num1 /= total_degree;
		num2 = (num2 / (2 * total_degree)) * (num2 / (2 * total_degree));
		den /= (2 * total_degree);
		if (den == num2) {
			return 1;
		}
		return (num1 - num2) / (den - num2);
	}

	@Override
	public String name() {
		return TMetric.ASSORTATIVITY.toString();
	}

}
