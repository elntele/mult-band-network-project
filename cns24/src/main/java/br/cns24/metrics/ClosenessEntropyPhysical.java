/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ClosenessEntropyPhysical.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	27/12/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.util.Dijkstra;

/**
 * 
 * @author Danilo
 * @since 27/12/2013
 */
public class ClosenessEntropyPhysical implements Metric<Double> {
	private static final ClosenessEntropyPhysical instance = new ClosenessEntropyPhysical();

	private ClosenessEntropyPhysical() {
	}

	public static ClosenessEntropyPhysical getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getDistances());
	}

	@Override
	public double calculate(Double[][] matrix) {
		int n = matrix.length;
		double sum = 0;

		Map<String, List<Integer>> mapDirectPaths = Dijkstra.calculateAll(matrix);
		List<Integer> path = null;

		double[] individualCloseness = new double[n];

		Arrays.fill(individualCloseness, 0);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				path = mapDirectPaths.get(i + "-" +  j);
				if (path != null && path.size() > 2){
					for (int k = 1; k < path.size()-1; k++) {
						individualCloseness[path.get(k)] += 1;
					}	
				}
			}
		}
		double maior = 0;
		for (double closeness : individualCloseness) {
			if (closeness > maior) {
				maior = closeness;
			}
		}
		for (double closeness : individualCloseness) {
			closeness = closeness / (maior * 1.0);
			if (closeness > 0) {
				sum = sum + closeness * (Math.log10(closeness) / Math.log10(2));	
			}
		}

		return -sum;
	}

	@Override
	public String name() {
		return TMetric.CLOSENESS_ENTROPY_PHYSICAL.toString();
	}

}
