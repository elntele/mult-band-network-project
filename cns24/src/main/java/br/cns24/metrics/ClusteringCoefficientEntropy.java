/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ClusteringCoefficientEntropy.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	04/12/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

/**
 * 
 * @author Danilo Araujo
 * @since 04/12/2014
 */
public class ClusteringCoefficientEntropy implements Metric<Integer> {
	private static final ClusteringCoefficientEntropy instance = new ClusteringCoefficientEntropy();

	private ClusteringCoefficientEntropy() {
	}

	public static ClusteringCoefficientEntropy getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		int numNodes = matrix.length;
		double[] parcialCoefficient = new double[numNodes];
		double maior = 0;
		for (int i = 0; i < numNodes; i++) {
			parcialCoefficient[i] = localClusteringCoefficient(matrix, i);
			if (parcialCoefficient[i] > maior) {
				maior = parcialCoefficient[i];
			}
		}
		
		double[] sequence = new double[numNodes];
		double increment = maior/(numNodes-1);
		for (int i = 0; i < numNodes; i++) {
			int counter = 0;
			for (double d = 0; d < maior; d += increment) {
				if (parcialCoefficient[i] >= d && parcialCoefficient[i] < d + increment) {
					sequence[counter]++;
					break;
				}
				counter++;
			}
		}
		double sum = 0;
		for (int i = 0; i < numNodes; i++) {
			sequence[i] /= sequence.length;
			if (sequence[i] > 0) {
				sum += sequence[i] * (Math.log10(sequence[i]) / Math.log10(2));
			}
		}
		return -sum;
	}

	public double localClusteringCoefficient(Integer[][] matrix, int vertex) {
		double connections = 0.0;
		double neighborhoodConnections = 0.0;

		for (int i = 0; i < matrix.length; i++) {
			if (i == vertex) {
				continue;
			}
			if (matrix[vertex][i] == 1) {
				connections++;
				for (int j = i + 1; j < matrix.length; j++) {
					if (j == vertex) {
						continue;
					}
					if ((matrix[i][j] == 1) && (matrix[vertex][j]) == 1) {
						neighborhoodConnections++;
					}
				}
			}
		}
		if (connections == 0 || neighborhoodConnections == 0){
			return 0;
		}
		return 2.0 * neighborhoodConnections / (connections * (connections - 1));
	}

	@Override
	public String name() {
		return TMetric.CLUSTERING_COEFFICIENT_ENTROPY.toString();
	}

}
