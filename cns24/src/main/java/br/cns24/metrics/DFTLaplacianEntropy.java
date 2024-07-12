package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.Laplacian;

/**
 * 
 * @author Danilo
 * @since 05/09/2013
 */
public class DFTLaplacianEntropy implements Metric<Integer> {

	private static final DFTLaplacianEntropy instance = new DFTLaplacianEntropy();

	private DFTLaplacianEntropy() {
	}

	public static DFTLaplacianEntropy getInstance() {
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
			fft = new DoubleFFT_1D(realValues.length);
			fftValues = new double[realValues.length];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			fft.realForward(fftValues);

		} catch (Exception e) {
			System.out.println("Failed to calculate matrix eigenvalues...");
			e.printStackTrace();
			return 0;
		}

		double sum = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double d : fftValues) {
			if (d < min) {
				min = d;
			}
			if (d > max) {
				max = d;
			}
		}
		for (double fftValue : fftValues) {
			fftValue += min;
			fftValue /= (max - min);
			if (fftValue > 0){
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));	
			}
		}
		return -sum;
	}

	@Override
	public String name() {
		return TMetric.DFT_LAPLACIAN_ENTROPY.toString();
	}

}
