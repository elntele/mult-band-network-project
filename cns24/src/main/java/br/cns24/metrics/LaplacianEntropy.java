/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: LaplacianEntropy.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/09/2013		Vers�o inicial
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
 * @author Danilo
 * @since 05/09/2013
 */
public class LaplacianEntropy implements Metric<Integer> {

	private static final LaplacianEntropy instance = new LaplacianEntropy();

	private LaplacianEntropy() {
	}

	public static LaplacianEntropy getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		double[] realEigenvalues = cn.getRealEigenvalues();
		Integer[][] matrix = cn.getAdjacencyMatrix();
		Integer[][] laplacian = cn.getLaplacianMatrix();
		
		if (cn.getLaplacianMatrix() == null) {
			laplacian = Laplacian.getInstance().transform(matrix);
			cn.setLaplacianMatrix(laplacian);
		}
		
		if (cn.getRealEigenvalues() == null) {
			laplacian = Laplacian.getInstance().transform(matrix);
			
			double[][] realValues = new double[matrix.length][matrix.length];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					realValues[i][j] = laplacian[i][j];
				}
			}
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			try {
				EigenDecomposition solver = new EigenDecomposition(rm, 0);
				realEigenvalues = solver.getRealEigenvalues();
				double aux;
				for (int i = 0; i < realEigenvalues.length; i++) {
					for (int j = i; j < realEigenvalues.length; j++) {
						if (realEigenvalues[j] < realEigenvalues[i]) {
							aux = realEigenvalues[i];
							realEigenvalues[i] = realEigenvalues[j];
							realEigenvalues[j] = aux;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Falha ao calcular autovalores da matriz...");
				e.printStackTrace();
				return 0;
			}
			
			cn.setRealEigenvalues(realEigenvalues);
		}
		
		double sum = 0;
		double menor = Double.MAX_VALUE;
		double maior = Double.MIN_VALUE;
		for (double d : realEigenvalues) {
			if (d < menor) {
				menor = d;
			}
			if (d > maior) {
				maior = d;
			}
		}
		for (double fftValue : realEigenvalues) {
			fftValue += menor;
			fftValue /= (maior - menor);
			if (fftValue > 0) {
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));
			}
		}
		return -sum;
	}

	@Override
	public double calculate(Integer[][] matrix) {
		double[] realEigenvalues = null;
		Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
			}
		}
		RealMatrix rm = new Array2DRowRealMatrix(realValues);
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < realEigenvalues.length; i++) {
				for (int j = i; j < realEigenvalues.length; j++) {
					if (realEigenvalues[j] < realEigenvalues[i]) {
						aux = realEigenvalues[i];
						realEigenvalues[i] = realEigenvalues[j];
						realEigenvalues[j] = aux;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
			return 0;
		}

		double sum = 0;
		double menor = Double.MAX_VALUE;
		double maior = Double.MIN_VALUE;
		for (double d : realEigenvalues) {
			if (d < menor) {
				menor = d;
			}
			if (d > maior) {
				maior = d;
			}
		}
		for (double fftValue : realEigenvalues) {
			fftValue += menor;
			fftValue /= (maior - menor);
			if (fftValue > 0) {
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));
			}
		}
		return -sum;
	}

	@Override
	public String name() {
		return TMetric.LAPLACIAN_ENTROPY.toString();
	}

}
