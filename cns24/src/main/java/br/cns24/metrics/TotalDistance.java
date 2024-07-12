package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

public class TotalDistance implements Metric<Double> {
	private static final TotalDistance instance = new TotalDistance();

	private TotalDistance() {
	}

	public static TotalDistance getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getDistances());
	}
	
	@Override
	public double calculate(Double[][] matrix) {
		int n = matrix.length;
		double sum = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				sum += matrix[i][j];
			}
		}
		return sum;
	}

	@Override
	public String name() {
		return TMetric.PHYSICAL_DIAMETER.toString();
	}

}
