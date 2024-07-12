package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;

public class SimtonBlockingProbability implements Metric<Integer> {

	private static final SimtonBlockingProbability instance = new SimtonBlockingProbability();

	private SimtonBlockingProbability() {
	}

	public static SimtonBlockingProbability getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		return calculate(cn.getAdjacencyMatrix());
	}

	@Override
	public double calculate(Integer[][] matrix) {
//		Simton problem = new Simton(14, 2);
//
//		Integer[] variables = null;
//		Solution<Integer> solution = null;
//		variables = new Integer[problem.getNumberOfVariables()];
//		
//		for (int k = 0; k < problem.getNumberOfVariables() - 2; k++) {
//			variables[k] = 0;
//		}
//		
//		for (int k = 0; k < problem.getNumberOfVariables() - 2; k++) {
//			for (int i = 0; i < matrix.length; i++){
//				for (int j = i+1; j < matrix.length; j++){
//					if (j + 13 * i - i * (i + 1) / 2 == k){
//						variables[k] = matrix[i][j];
//					}
//				}
//			}
//		}
//		variables[problem.getNumberOfVariables() - 2] = 5;
//		variables[problem.getNumberOfVariables() - 1] = 40;
//		solution = new SolutionONTD(problem, variables);
//		problem.evaluate(solution);
//
//		return solution.getObjective(0);
		return 0;
	}

	@Override
	public String name() {
		return TMetric.BLOCKING_PROBABILITY.toString();
	}

}
