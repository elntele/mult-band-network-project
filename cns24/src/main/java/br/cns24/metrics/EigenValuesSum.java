package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.Laplacian;

public class EigenValuesSum implements Metric<Integer> {
	private static final EigenValuesSum instance = new EigenValuesSum();

	private EigenValuesSum() {
	}

	public static EigenValuesSum getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	public double calculate(Integer[][] matrix) {
		Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
		double sum = 0;
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
			for (int i = 0; i < autovalores.length; i++) {
				sum += autovalores[i];
			}
			return sum / (1.0 * matrix.length);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public String name() {
		return TMetric.EIGENVALUES_SUM.toString();
	}
}
