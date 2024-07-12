/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: LocalClusteringCoefficient.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	02/12/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.transformations;

/**
 * 
 * @author Danilo Araujo
 * @since 02/12/2014
 */
public class LocalClusteringCoefficient {
	private static final LocalClusteringCoefficient instance = new LocalClusteringCoefficient();

	private LocalClusteringCoefficient() {
	}

	public static LocalClusteringCoefficient getInstance() {
		return instance;
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
		if (connections == 0 || neighborhoodConnections == 0) {
			return 0;
		}
		return 2.0 * neighborhoodConnections / (connections * (connections - 1));
	}

	public Double[] calculate(final Integer[][] matrix) {
		Double[] m = new Double[matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			m[i] = localClusteringCoefficient(matrix, i);
		}

		return m;
	}

}
