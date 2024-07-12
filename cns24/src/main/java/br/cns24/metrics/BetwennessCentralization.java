/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BetwennessCentralization.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	17/05/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.LinkClosenessSequence;

/**
 * 
 * @author Danilo Araujo
 * @since 17/05/2015
 */
public class BetwennessCentralization implements Metric<Integer> {
	private static final BetwennessCentralization instance = new BetwennessCentralization();

	private BetwennessCentralization() {
	}

	public static BetwennessCentralization getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		int n = matrix.length;

		Double[][] betweness = new Double[n][n];

		for (int i = 0; i < n; i++) {
			betweness[i][i] = 0.0;
			for (int j = i + 1; j < n; j++) {
				betweness[i][j] = matrix[i][j].doubleValue();
				betweness[j][i] = matrix[i][j].doubleValue();
			}
		}

		betweness = LinkClosenessSequence.getInstance().transform(betweness);

		double maxBetweness = Double.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (betweness[i][j] > maxBetweness) {
					maxBetweness = betweness[i][j];
				}
			}
		}

		double sumDiffs = 0;

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				sumDiffs += maxBetweness - betweness[i][j];
			}
		}
		sumDiffs *= 2;

		return sumDiffs/(n * n * n -4 * n * n + 5 * n - 2);
	}

	@Override
	public String name() {
		return TMetric.BETWENNESS_CENTRALIZATION.toString();
	}

}
