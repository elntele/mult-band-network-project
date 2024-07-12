package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.FloydWarshallShortestPath;

public class Resilience implements Metric<Integer> {
	private static final Resilience instance = new Resilience();

	private Resilience() {
	}

	public static Resilience getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	public double calculate(Integer[][] matrix) {
		Integer[][] shortestPath = FloydWarshallShortestPath.getInstance().transform(matrix);
		Integer[][] shortestPathAux = null;
		Integer[][] matrixAux = new Integer[matrix.length][matrix.length];
		double sum = 0;
		int qtde = 0;
		
		double sumT = 0;
		int qtdeT = 0;
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = i + 1; j < matrix.length; j++) {
				if (matrix[i][j] == 1) {
					for (int k = 0; k < matrixAux.length; k++) {
						for (int l = 0; l < matrixAux.length; l++) {
							matrixAux[k][l] = matrix[k][l];
						}
					}
					matrixAux[i][j] = 0;
					shortestPathAux = FloydWarshallShortestPath.getInstance().transform(matrixAux);
					for (int k = 0; k < shortestPathAux.length; k++) {
						for (int l = k + 1; l < shortestPathAux.length; l++) {
							if (shortestPathAux[k][l] == Integer.MAX_VALUE) {
								sum++;
							}
							qtde++;
						}
					}
					sumT += sum/qtde;
					sum = 0;
					qtde = 0;
					qtdeT++;
				}
			}
		}
		if (qtdeT == 0){
			return 0;
		}
		return sumT / qtdeT;
	}

	@Override
	public String name() {
		return TMetric.RESILIENCE.toString();
	}
}
