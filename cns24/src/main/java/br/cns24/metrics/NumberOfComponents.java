/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NumberOfComponents.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	10/04/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.Laplacian;

/**
 * 
 * @author Danilo Araujo
 * @since 10/04/2015
 */
public class NumberOfComponents  implements Metric<Integer> {
	/**
	 * 
	 */
	private static final double ZERO = 0.0000001;
	private static final NumberOfComponents instance = new NumberOfComponents();

	private NumberOfComponents() {
	}

	public static NumberOfComponents getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		Integer[][] matrix = cn.getAdjacencyMatrix();
		Integer[][] laplacian = null;
		if (cn.getLaplacianMatrix() == null) {
			laplacian = Laplacian.getInstance().transform(cn.getAdjacencyMatrix());
			cn.setLaplacianMatrix(laplacian);
		} else {
			laplacian = cn.getLaplacianMatrix();
		}
		double[] autovalores = null;
		if (cn.getRealEigenvalues() == null) {
			double[][] realValues = new double[matrix.length][matrix.length];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					realValues[i][j] = laplacian[i][j];
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
				return autovalores[1];
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		} else {
			autovalores = cn.getRealEigenvalues();
		}
		int components = 0;
		for (double av : autovalores) {
			if (av < ZERO) {
				components++;
			}
		}
		return components;
	}

	public double calculate(Integer[][] matrix) {
		Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
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
			int components = 0;
			for (double av : autovalores) {
				if (av < ZERO) {
					components++;
				}
			}
			return components;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public String name() {
		return TMetric.NUMBER_OF_COMPONENTS.toString();
	}
}
