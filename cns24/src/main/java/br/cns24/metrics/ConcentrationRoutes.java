/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ConcentrationRoutes.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	17/05/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import java.util.List;
import java.util.Map;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.util.Dijkstra;

/**
 * 
 * @author Danilo Araujo
 * @since 17/05/2015
 */
public class ConcentrationRoutes implements Metric<Integer> {
	private static final ConcentrationRoutes instance = new ConcentrationRoutes();

	private ConcentrationRoutes() {
	}

	public static ConcentrationRoutes getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		int n = matrix.length;

		Map<String, List<Integer>> mapDirectPaths = Dijkstra.calculateAll(matrix);
		List<Integer> path = null;

		Double[][] betweness = new Double[n][n];

		for (int i = 0; i < n; i++) {
			betweness[i][i] = 0.0;
			for (int j = i + 1; j < n; j++) {
				path = mapDirectPaths.get(i + "-" + j);
				if (betweness[i][j] == null) {
					betweness[i][j] = 0.0;
					betweness[j][i] = 0.0;
				}
				if (path != null && path.size() > 2) {
					for (int k = 0; k < path.size() - 1; k++) {
						if (betweness[path.get(k)][path.get(k + 1)] == null) {
							betweness[path.get(k)][path.get(k + 1)] = 0.0;
							betweness[path.get(k + 1)][path.get(k)] = 0.0;
						}
						betweness[path.get(k)][path.get(k + 1)] += 1;
						betweness[path.get(k + 1)][path.get(k)] += 1;
					}
				}
			}
		}

		double maxBetweness = Double.MIN_VALUE;
		double minBetweness = Double.MAX_VALUE;
		int r = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (betweness[i][j] > maxBetweness) {
					maxBetweness = betweness[i][j];
				}
				if (betweness[i][j] < minBetweness) {
					minBetweness = betweness[i][j];
				}
				if (betweness[i][j] != 0) {
					r++;
				}
			}
		}

		return (maxBetweness - minBetweness) / r;
	}

	@Override
	public String name() {
		return TMetric.CONCENTRATION_ROUTES.toString();
	}

}
