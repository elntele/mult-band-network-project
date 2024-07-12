package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

public class NaturalConnectivity implements Metric<Integer> {
	private static final NaturalConnectivity instance = new NaturalConnectivity();

	private NaturalConnectivity() {
	}

	public static NaturalConnectivity getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		double value = 0;
		double[] autovalores = cn.getRealEigenvalues();
		Integer[][] matrix = cn.getAdjacencyMatrix();

		if (cn.getRealEigenvalues() == null) {
			double[][] realValues = new double[matrix.length][matrix.length];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					realValues[i][j] = matrix[i][j] == null ? 0 : matrix[i][j];
				}
			}
			try {
				RealMatrix rm = new Array2DRowRealMatrix(realValues);
				EigenDecomposition solver = new EigenDecomposition(rm, 0);
				autovalores = solver.getRealEigenvalues();
			} catch (Exception e) {
				e.printStackTrace();
				value = 0;
			}

			cn.setRealEigenvalues(autovalores);
		}

		if (autovalores != null) {
			for (double av : autovalores) {
				value += Math.exp(av);
			}
			value /= autovalores.length;
			value = Math.log(value);
		}

		return value;
	}

	public double calculate(Integer[][] matrix) {
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = matrix[i][j] == null ? 0 : matrix[i][j];
			}
		}
		double value;
		try {
			RealMatrix rm = new Array2DRowRealMatrix(realValues);
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			double[] autovalores = solver.getRealEigenvalues();

			value = 0;
			for (double av : autovalores) {
				value += Math.exp(av);
			}
			value /= autovalores.length;
			value = Math.log(value);
		} catch (Exception e) {
			e.printStackTrace();
			value = 0;
		}

		return value;
	}

	@Override
	public String name() {
		return TMetric.NATURAL_CONNECTIVITY.toString();
	}
}
