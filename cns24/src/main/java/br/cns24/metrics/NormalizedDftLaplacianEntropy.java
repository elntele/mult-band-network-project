/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NormalizedDftLaplacianEntropy.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	23/04/2014		Vers�o inicial
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
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * 
 * @author Danilo
 * @since 23/04/2014
 */
public class NormalizedDftLaplacianEntropy implements Metric<Integer> {

	private static final NormalizedDftLaplacianEntropy instance = new NormalizedDftLaplacianEntropy();

	private NormalizedDftLaplacianEntropy() {
	}

	public static NormalizedDftLaplacianEntropy getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		DoubleFFT_1D fft = null;
		double[] realEigenvalues = null;
		double[] fftValues = null;
		double max = Double.MIN_VALUE;
		Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
//		Integer[][] laplacian = matrix;
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			realValues[i][i] = laplacian[i][i] / (1.0 * (matrix.length-1));
			for (int j = i + 1; j < matrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
				realValues[j][i] = laplacian[j][i];
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
						if (aux > max) {
							max = aux;
						}
						realEigenvalues[i] = realEigenvalues[j];
						realEigenvalues[j] = aux;
					}
				}
			}
			fft = new DoubleFFT_1D(realValues.length);
			fftValues = new double[realValues.length];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i]/max;
			}
			fft.realForward(fftValues);

		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
			return 0;
		}

		double sum = 0;
		
		for (double d : fftValues) {
			sum += d;
		}
		for (int i = 0; i < fftValues.length; i++) {
			fftValues[i] = fftValues[i]/sum;
		}
		sum = 0;
		
		for (double d : fftValues) {
			sum += d;
		}
		sum = 0;
		for (double fftValue : fftValues) {
			if (fftValue > 0){
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));	
			}
		}
		return -sum;
	}

	@Override
	public String name() {
		return TMetric.NORMALIZED_DFT_LAPLACIAN_ENTROPY.toString();
	}

}
