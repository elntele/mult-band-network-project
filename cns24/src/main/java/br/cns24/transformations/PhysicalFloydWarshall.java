/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PhysicalFloydWarshall.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	01/05/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.transformations;

import br.cns24.Transformation;

/**
 * 
 * @author Danilo
 * @since 01/05/2013
 */
public class PhysicalFloydWarshall implements Transformation<Double> {
	private static final PhysicalFloydWarshall instance = new PhysicalFloydWarshall();

	private static final double FLAG_WLINK = Double.MAX_VALUE;

	private PhysicalFloydWarshall() {
	}

	public static PhysicalFloydWarshall getInstance() {
		return instance;
	}

	@Override
	public Double[][] transform(Double[][] matrix) {
		int n = matrix.length;
		Double[][] d = new Double[n][n];

		for (int i = 0; i < n; i++) {
			d[i][i] = 0.0;
			for (int j = i + 1; j < n; j++) {
				if (matrix[i][j].equals(0.0)) {
					d[i][j] = FLAG_WLINK;
					d[j][i] = FLAG_WLINK;
				} else {
					d[i][j] = matrix[j][i];
					d[j][i] = matrix[j][i];
				}
			}
		}

		for (int k = 0; k < d.length; k++) {
			for (int i = 0; i < d.length; i++) {
				for (int j = 0; j < d.length; j++) {
					if (d[i][k].equals(FLAG_WLINK) || d[k][j].equals(FLAG_WLINK)) {
						continue;
					}

					if (d[i][j] > d[i][k] + d[k][j]) {
						d[i][j] = d[i][k] + d[k][j];
					}

				}
			}
		}
		return d;
	}

	public static void main(String[] args) {
		PhysicalFloydWarshall a = new PhysicalFloydWarshall();
		Double[][] m = new Double[][] { { 0.0, 20.0, 0.0, 25.0 }, { 20.0, 0.0, 14.0, 0.0 }, { 0.0, 14.0, 0.0, 0.0 },
				{ 25.0, 0.0, 0.0, 0.0 } };

		Double[][] d = a.transform(m);

		for (int k = 0; k < d.length; k++) {
			for (int i = 0; i < d.length; i++) {
				System.out.print(d[k][i] + " ");
			}
			System.out.println();
		}
	}

}
