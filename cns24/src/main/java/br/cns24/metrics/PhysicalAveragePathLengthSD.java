/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PhysicalAveragePathLengthSD.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	09/02/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.PhysicalFloydWarshall;

/**
 * 
 * @author Danilo Araujo
 * @since 09/02/2015
 */
public class PhysicalAveragePathLengthSD implements Metric<Double> {
	private static final PhysicalAveragePathLengthSD instance = new PhysicalAveragePathLengthSD();

	private PhysicalAveragePathLengthSD() {
	}

	public static PhysicalAveragePathLengthSD getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getDistances());
	}

	@Override
	public double calculate(Double[][] matrix) {
		Double[][] shortestPath = PhysicalFloydWarshall.getInstance().transform(matrix);
		int n = matrix.length;
		int m = 0;
		int sum = 0;
		
		double maior = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (shortestPath[i][j] != 0 && shortestPath[i][j] < 1e6) {
					if (shortestPath[i][j]  > maior) {
						maior = shortestPath[i][j];
					}
						
				}
			}
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (shortestPath[i][j] > 1e6) {
					shortestPath[i][j]  = 4 * maior;
				}
			}
		}
			
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (shortestPath[i][j] != 0) {
					sum += shortestPath[i][j];
					m++;
				}
			}
		}
		double mean = sum;
		if (m > 0) {
			mean /= m;
		}
		double sd = 0;
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (shortestPath[i][j] != 0) {
					sd = sd + (shortestPath[i][j] - mean) * (shortestPath[i][j] - mean);
				}
			}
		}
		if (m > 0) {
			sd /= m;
		}
		return Math.sqrt(sd);
	}

	@Override
	public String name() {
		return TMetric.PHYSICAL_AVERAGE_PATH_LENGTH_SD.toString();
	}

}
