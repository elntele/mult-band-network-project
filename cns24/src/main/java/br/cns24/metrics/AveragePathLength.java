package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.FloydWarshallShortestPath;

public class AveragePathLength implements Metric<Integer> {
	private static final AveragePathLength instance = new AveragePathLength();
	
	private AveragePathLength(){
	}
	
	public static AveragePathLength getInstance(){
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		Integer[][] shortestPath = cn.getShortestPath();
		Integer[][] matrix = cn.getAdjacencyMatrix();
		if (cn.getShortestPath() == null){
			shortestPath = FloydWarshallShortestPath.getInstance().transform(matrix);
			cn.setShortestPath(shortestPath);
		}
		int n = matrix.length;
		int m = 0;
		int sum = 0;
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				if (shortestPath[i][j] != 0){
					sum += shortestPath[i][j];
					m++;
				}
			}	
		}
		
		return sum/(m * 1.0);
	}

	@Override
	public double calculate(Integer[][] matrix) {
		Integer[][] shortestPath = FloydWarshallShortestPath.getInstance().transform(matrix);
		int n = matrix.length;
		int m = 0;
		int sum = 0;
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				if (shortestPath[i][j] != 0){
					sum += shortestPath[i][j];
					m++;
				}
			}	
		}
		
		return sum/(m * 1.0);
	}

	@Override
	public String name() {
		return TMetric.AVERAGE_PATH_LENGTH.toString();
	}

}
