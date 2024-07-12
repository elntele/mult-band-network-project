/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PhysicalDiameter.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	01/05/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.PhysicalFloydWarshall;

/**
 * 
 * @author Danilo
 * @since 01/05/2013
 */
public class PhysicalDiameter implements Metric<Double> {
	private static final PhysicalDiameter instance = new PhysicalDiameter();

	private PhysicalDiameter() {
	}

	public static PhysicalDiameter getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getDistances());
	}
	
	@Override
	public double calculate(Double[][] matrix) {
		Double[][] shortestPath = PhysicalFloydWarshall.getInstance().transform(matrix);
		int n = matrix.length;
		double maior = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (shortestPath[i][j] > maior) {
					maior = shortestPath[i][j];
				}
			}
		}
		return maior;
	}

	@Override
	public String name() {
		return TMetric.PHYSICAL_DIAMETER.toString();
	}

}
