package br.cns24.metrics;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.Laplacian;

public class EffectiveConductance implements Metric<Integer> {
	private static final EffectiveConductance instance = new EffectiveConductance();

	private EffectiveConductance() {
	}

	public static EffectiveConductance getInstance() {
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
		
		double rg = 0;
		for (double d : autovalores) {
			if (d > 1e-6) {
				rg += 1/d;
			}
		}
		rg *= matrix.length;
		
		return (matrix.length-1)/rg;
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
			
			double rg = 0;
			for (double d : autovalores) {
				if (d > 1e-6) {
					rg += 1/d;
				}
			}
			rg *= matrix.length;
			
			return (matrix.length-1)/rg;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public String name() {
		return TMetric.ALGEBRAIC_CONNECTIVITY.toString();
	}
}
