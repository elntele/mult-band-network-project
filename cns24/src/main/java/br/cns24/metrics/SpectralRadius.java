/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SpectralRadius.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	08/11/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

/**
 * 
 * @author Danilo
 * @since 08/11/2013
 */
public class SpectralRadius implements Metric<Integer> {
	private static final SpectralRadius instance = new SpectralRadius();

	private SpectralRadius() {
	}

	public static SpectralRadius getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		Integer[][] matrix = cn.getAdjacencyMatrix();

		double[] autovalores = null;
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = matrix[i][j];
			}
		}
		try {
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			autovalores = solver.getRealEigenvalues();
			cn.setRealEigenvalues(autovalores);
			double aux;
			for (int i = 0; i < autovalores.length; i++) {
				for (int j = i; j < autovalores.length; j++) {
					if (autovalores[j] < autovalores[i]) {
						aux = autovalores[i];
						autovalores[i] = autovalores[j];
						autovalores[j] = aux;
					}
				}
			}
			return autovalores[0];
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public double calculate(Integer[][] matrix) {
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = matrix[i][j] == null ? 0 : matrix[i][j];
			}
		}
		try {
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			double[] autovalores = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < autovalores.length; i++) {
				for (int j = i; j < autovalores.length; j++) {
					if (autovalores[j] < autovalores[i]) {
						aux = autovalores[i];
						autovalores[i] = autovalores[j];
						autovalores[j] = aux;
					}
				}
			}
			return autovalores[1];
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public String name() {
		return TMetric.SPECTRAL_RADIUS.toString();
	}
}
