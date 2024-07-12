package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.FloydWarshallShortestPath;

public class Diameter implements Metric<Integer> {
	private static final Diameter instance = new Diameter();
	
	private Diameter(){
	}
	
	public static Diameter getInstance(){
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		Integer[][] matrix = cn.getAdjacencyMatrix();
		Integer[][] shortestPath = cn.getShortestPath();
		if (cn.getShortestPath() == null) {
			shortestPath = FloydWarshallShortestPath.getInstance().transform(matrix);
			cn.setShortestPath(shortestPath);
		}
		int n = matrix.length;
		int maior = 0;
		for (int i = 0; i < n; i++){
			for (int j = i+1; j < n; j++){
				if (shortestPath[i][j] > maior){
					maior = shortestPath[i][j];
				}
			}	
		}
		if (maior > matrix.length){
			maior = matrix.length + 1;
		}
		return maior;
	}

	@Override
	public double calculate(Integer[][] matrix) {
		Integer[][] shortestPath = FloydWarshallShortestPath.getInstance().transform(matrix);
		int n = matrix.length;
		int maior = 0;
		for (int i = 0; i < n; i++){
			for (int j = i+1; j < n; j++){
				if (shortestPath[i][j] > maior){
					maior = shortestPath[i][j];
				}
			}	
		}
		if (maior > matrix.length){
			maior = matrix.length + 1;
		}
		return maior;
	}

	@Override
	public String name() {
		return TMetric.DIAMETER.toString();
	}

}
