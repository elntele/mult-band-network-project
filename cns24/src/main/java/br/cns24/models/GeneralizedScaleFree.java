/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeneralizedScaleFree.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.transformations.DegreeMatrix;

/**
 * 
 * @author Danilo
 * @since 05/10/2013
 */
public class GeneralizedScaleFree extends GenerativeProcedure {
	private double density;

	private double g;

	private double a;

	public GeneralizedScaleFree(double density, double g, double a) {
		this.density = density;
		this.g = g;
		this.a = a;
	}

	public GeneralizedScaleFree(double density, double a) {
		this.density = density;
		this.g = 1;
		this.a = a;
	}

	public GeneralizedScaleFree(double density) {
		this.density = density;
		this.g = 1;
		this.a = 0;
	}

	public void fill(Integer[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				if (m[i][j] == null) {
					m[i][j] = 0;
				}
			}
		}
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		return null;
	}

	@Override
	public Integer[][] grow(Integer[][] matrix, int numNodes) {
		Integer[][] nMatrix = new Integer[numNodes][numNodes];
		int m0 = 3;
		double[] cdf = new double[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nMatrix[i][i] = 0;
			for (int j = i + 1; j < numNodes; j++) {
				nMatrix[i][j] = 0;
				nMatrix[j][i] = 0;
				if (j < m0 && i < m0) {
					nMatrix[i][j] = 1;
					nMatrix[j][i] = 1;
				}
			}
		}
		double r;
		cdf = new double[numNodes];
		int numCreatedLinks = (m0 * (m0 - 1)) / 2;
		Integer[][] degree = null;
		int deltaM = 2;
		degree = DegreeMatrix.getInstance().transform(nMatrix);
		lblExt: for (int n = 3; n < numNodes; n++) {
			deltaM = (int) ((((n) * (n + 1)) / 2) * density) - numCreatedLinks;
			if (deltaM < 2) {
				deltaM = 2;
			}
			cdf[0] = (degree[0][0] + a / g) / ((2.0 * numCreatedLinks) + n * a / g);
			for (int i = 1; i < n; i++) {
				cdf[i] = cdf[i - 1] + (degree[i][i] + a / g) / ((2.0 * numCreatedLinks + n * a / g));
			}
			int numLinks = Math.min(deltaM, n - 1);
			boolean linked = false;
			for (int i = 0; i < numLinks; i++) {
				if (n == numNodes && numCreatedLinks / ((numNodes * (numNodes - 1)) / 2.0) >= density) {
					break lblExt;
				}
				linked = false;
				r = Math.random();
				for (int j = 0; j < n; j++) {
					if (((j == 0 && r < cdf[0]) || (r < cdf[j] && r >= cdf[j - 1])) && nMatrix[n][j] != 1) {
						nMatrix[n][j] = 1;
						nMatrix[j][n] = 1;
						degree[n][n]++;
						degree[j][j]++;
						numCreatedLinks++;
						linked = true;
						break;
					}
				}
				if (!linked) {
					for (int j = 0; j < n; j++) {
						if (nMatrix[n][j] != 1) {
							nMatrix[n][j] = 1;
							nMatrix[j][n] = 1;
							degree[n][n]++;
							degree[j][j]++;
							numCreatedLinks++;
							break;
						}
					}
				}
			}

		}
		return nMatrix;
	}

	@Override
	public String name() {
		return TModel.GENERALIZED_SCALE_FREE.toString();
	}

}
