package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.FloydWarshallShortestPath;

public class SingleFailureImpact implements Metric<Integer> {

	private static final SingleFailureImpact instance = new SingleFailureImpact();

	public static SingleFailureImpact getInstance() {
		return instance;
	}

	private SingleFailureImpact() {
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
		double p = 0;
		Integer[][] auxMatrix = null;
		double pNonConnectedPairs = 0;
		int numLinks = 0;
		auxMatrix = FloydWarshallShortestPath.getInstance().transform(matrix);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (auxMatrix[i][j] > 0){
					numLinks++;
				}
			}
		}

		for (int i = 0; i < matrix.length; i++) {
			copiar(matrix, auxMatrix);
			for (int j = 0; j < matrix.length; j++) {
				auxMatrix[i][j] = 0;
				auxMatrix[j][i] = 0;
			}
			pNonConnectedPairs = pNonConnectedPairs(auxMatrix, numLinks);
			if (pNonConnectedPairs > p) {
				p = pNonConnectedPairs;
			}
		}

		return p;
	}
	
	private void copiar(Integer[][] m1, Integer[][] m2){
		for (int i = 0; i < m1.length; i++){
			for (int j = 0; j < m1[i].length; j++){
				m2[i][j] = m1[i][j];
			}
		}
	}

	/**
	 * Calcula o Número de pares (origem, detino) que n�o est�o conectados.
	 * 
	 * @param matrix
	 *            Matriz de adjac�ncias
	 * @return Número de pares (origem, detino) que n�o est�o conectados.
	 */
	private int nonConnectedPairs(Integer[][] matrix) {
		Integer[][] path = FloydWarshallShortestPath.getInstance().transform(matrix);
		int number = 0;

		for (int i = 0; i < matrix.length; i++) {
			for (int j = i + 1; j < matrix[i].length; j++) {
				if (path[i][j] == 0 || path[i][j] == Integer.MAX_VALUE) {
					number++;
				}
			}
		}
		return 2 * number;
	}

	/**
	 * Calcula o percentual de pares (origem, detino) que n�o est�o conectados.
	 * 
	 * @param matrix
	 *            Matriz de adjac�ncias
	 * @return Percentual de Número de pares (origem, detino) que n�o est�o
	 *         conectados.
	 */
	private double pNonConnectedPairs(Integer[][] matrix, int numMaxLinks) {
		double number = nonConnectedPairs(matrix);
		double pValue = number / numMaxLinks;

		return pValue;
	}

	@Override
	public String name() {
		return TMetric.SINGLE_FAILURE_IMPACT.toString();
	}

}
