/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DFTLaplacianEntropyPhysical.java
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
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * 
 * @author Danilo
 * @since 08/11/2013
 */
public class PhysicalDFTLaplacianEntropy implements Metric<Double> {

	private static final PhysicalDFTLaplacianEntropy instance = new PhysicalDFTLaplacianEntropy();

	private PhysicalDFTLaplacianEntropy() {
	}

	public static PhysicalDFTLaplacianEntropy getInstance() {
		return instance;
	}

	@Override
	public double calculate(Double[][] matrix) {
		DoubleFFT_1D fft = null;
		double[] realEigenvalues = null;
		double[] fftValues = null;

		int[][] degree = new int[matrix.length][matrix.length];
		double[][] laplacian = new double[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = i + 1; j < matrix[i].length; j++) {
				if (matrix[i][j] != null && matrix[i][j] > 0) {
					degree[i][i]++;
					degree[j][j]++;
				}
			}
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				laplacian[i][j] = degree[i][j] - (matrix[i][j] == null ? 0 : matrix[i][j]);
			}
		}

		RealMatrix rm = new Array2DRowRealMatrix(laplacian);
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
			fft = new DoubleFFT_1D(laplacian.length);
			fftValues = new double[laplacian.length];
			for (int i = 0; i < laplacian.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			fft.realForward(fftValues);

		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
			return 0;
		}

		double sum = 0;
		double menor = Double.MAX_VALUE;
		double maior = Double.MIN_VALUE;
		for (double d : fftValues) {
			if (d < menor) {
				menor = d;
			}
			if (d > maior) {
				maior = d;
			}
		}
		for (double fftValue : fftValues) {
			fftValue += menor;
			fftValue /= (maior - menor);
			if (fftValue > 0) {
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));
			}
		}
		return -sum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.cns.Metric#calculate(br.cns.experiments.ComplexNetwork)
	 */
	@Override
	public double calculate(ComplexNetwork network) {
		return calculate(network.getDistances());
	}

	@Override
	public String name() {
		return TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY.toString();
	}

}
