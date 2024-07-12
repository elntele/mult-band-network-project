/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PAByDistance.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	01/01/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.transformations.DegreeMatrix;

/**
 * 
 * @author Danilo
 * @since 01/01/2014
 */
public class PAByDistance extends GenerativeProcedure {
	private double density;

	private double g;

	private double a;

	private Double[][] distance;

	public PAByDistance(double density, Double[][] distance) {
		this.density = density;
		this.g = 1;
		this.a = 0;
		this.distance = distance;
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
		double totalDistance = 0;
		double[] cdf = new double[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nMatrix[i][i] = 0;
			for (int j = i + 1; j < numNodes; j++) {
				nMatrix[i][j] = 0;
				nMatrix[j][i] = 0;
				if (j < m0 && i < m0) {
					nMatrix[i][j] = 1;
					nMatrix[j][i] = 1;
					totalDistance += distance[i][j];
				}
			}
		}
		double distanceAux = 0;
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
			distanceAux = 0;
			for (int i = 1; i < n; i++) {
				if (nMatrix[0][i] == 1) {
					distanceAux += distance[0][i];
				}
			}

			cdf[0] = (distanceAux) / ((2.0 * totalDistance));
			for (int i = 1; i < n; i++) {
				distanceAux = 0;
				for (int j = 0; j < n; j++) {
					if (i != j && nMatrix[i][j] == 1) {
						distanceAux += distance[i][j];
					}
				}

				cdf[i] = cdf[i - 1] + (distanceAux) / (2.0 * totalDistance);
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
						totalDistance += distance[n][j];
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
							totalDistance += distance[n][j];
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
		return TModel.POWER_LAW_BY_DISTANCE.toString();
	}

	/**
	 * @return o valor do atributo density
	 */
	public double getDensity() {
		return density;
	}

	/**
	 * Altera o valor do atributo density
	 * @param density O valor para setar em density
	 */
	public void setDensity(double density) {
		this.density = density;
	}

}
