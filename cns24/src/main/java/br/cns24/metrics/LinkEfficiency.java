package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

public class LinkEfficiency implements Metric<Integer> {

	private static final LinkEfficiency instance = new LinkEfficiency();

	private LinkEfficiency() {
	}

	public static LinkEfficiency getInstance() {
		return instance;
	}

	public double calculate(ComplexNetwork cn) {
		double apl = AveragePathLength.getInstance().calculate(cn);
		Integer[][] matrix = cn.getAdjacencyMatrix();
		int n = matrix.length;
		int a = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (matrix[i][j] != 0) {
					a++;
				}
			}
		}

		return (a - apl) / a;
	}

	@Override
	public double calculate(Integer[][] matrix) {
		double apl = AveragePathLength.getInstance().calculate(matrix);
		int n = matrix.length;
		int a = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (matrix[i][j] != 0) {
					a++;
				}
			}
		}

		return (a - apl) / a;
	}

	@Override
	public String name() {
		return TMetric.LINK_EFFICIENCY.toString();
	}

}
