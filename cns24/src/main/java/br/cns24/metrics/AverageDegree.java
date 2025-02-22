package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

public class AverageDegree implements Metric<Integer> {
	private static final AverageDegree instance = new AverageDegree();
	
	private AverageDegree(){
	}
	
	public static AverageDegree getInstance(){
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		int e = 0;
		int n = matrix.length;
		for (int i = 0; i < n; i++){
			for (int j = i+1; j < matrix[i].length; j++){
				if (matrix[i][j] == 1){
					e++;
				}
			}	
		}
		return (2.0 * e)/n;
	}

	@Override
	public String name() {
		return TMetric.AVERAGE_DEGREE.toString();
	}

}
