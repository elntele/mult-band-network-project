package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.Laplacian;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class ZeroReturnCountFilter implements Metric<Integer> {
	private static final ZeroReturnCountFilter instance = new ZeroReturnCountFilter();

	private static final boolean debug = false;
	
	private static final double filterValue = 8;

	public static ZeroReturnCountFilter getInstance() {
		return instance;
	}

	private ZeroReturnCountFilter() {
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		double[] fftValues = null;
		DoubleFFT_1D fft = null;
		double p = 0;
		try {
			Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
			double[][] realValues = new double[matrix.length][matrix.length];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					realValues[i][j] = laplacian[i][j];
				}
			}
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			double[] realEigenvalues = solver.getRealEigenvalues();
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
			fftValues = new double[2 * realValues.length];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			fft.realForwardFull(fftValues);
		} catch (Exception e) {
			fftValues = new double[matrix.length];
		}
		if (debug) {
			System.out.println("Valores da transformada (compl.):");
			for (int i = 0; i < fftValues.length; i++) {
				System.out.printf("%.2f ", fftValues[i]);
			}
			System.out.println("\nValores da transformada (pares):");
			for (int i = 1; i < fftValues.length; i += 2) {
				System.out.printf("%.2f ", fftValues[i]);
			}

			System.out.println("\nValores da transformada (impares):");
			for (int i = 0; i < fftValues.length; i += 2) {
				System.out.printf("%.2f ", fftValues[i]);
			}
			System.out.println();
		}
		int ant = Math.abs(fftValues[0]) > filterValue ? (int) (fftValues[0] / Math.abs(fftValues[0])) : 0;
		for (int i = 0; i < fftValues.length; i += 2) {
			if (Math.abs(fftValues[i]) > filterValue) {
				if (ant != 0 && ant != (int) (fftValues[i] / Math.abs(fftValues[i]))) {
					p++;
				}
				ant = Math.abs(fftValues[i]) > filterValue ? (int) (fftValues[i] / Math.abs(fftValues[i])) : 0;
			}
		}
		ant = Math.abs(fftValues[1]) > filterValue ? (int) (fftValues[1] / Math.abs(fftValues[1])) : 0;
		for (int i = 1; i < fftValues.length; i += 2) {
			if (Math.abs(fftValues[i]) > filterValue) {
				if (ant != 0 && ant != (int) (fftValues[i] / Math.abs(fftValues[i]))) {
					p++;
				}
				ant = Math.abs(fftValues[i]) > filterValue ? (int) (fftValues[i] / Math.abs(fftValues[i])) : 0;
			}
		}

		return p;
	}

	@Override
	public String name() {
		return TMetric.ZERO_RETURN_COUNT_FILTER.toString();
	}

}
