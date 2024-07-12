package br.cns24.metrics;

import br.cns24.Metric;
import br.cns24.TMetric;
import br.cns24.experiments.ComplexNetwork;
import br.cns24.transformations.DegreeMatrix;

public class Entropy implements Metric<Integer> {

	private static final Entropy instance = new Entropy();

	private Entropy() {
	}

	public static Entropy getInstance() {
		return instance;
	}
	
	public double calculate(ComplexNetwork cn) {
		double sum = 0;
		Integer[][] matrix = cn.getAdjacencyMatrix();
		Integer[][] degreeMatrix = cn.getDegreeMatrix();
		if (cn.getDegreeMatrix() == null) {
			degreeMatrix = DegreeMatrix.getInstance().transform(matrix);
			cn.setDegreeMatrix(degreeMatrix);
		}
		
		double[] sequence = new double[matrix.length];

		for (int i = 0; i < degreeMatrix.length; i++) {
			sequence[degreeMatrix[i][i]]++;
		}
		sum = 0;
		for (int i = 0; i < degreeMatrix.length; i++) {
			sequence[i] /= sequence.length;
			if (sequence[i] > 0) {
				sum += sequence[i] * (Math.log10(sequence[i]) / Math.log10(2));
			}
		}
		return -sum;
	}

	@Override
	public double calculate(Integer[][] matrix) {
		double sum = 0;
		Integer[][] degreeMatrix = DegreeMatrix.getInstance().transform(matrix);
		double[] sequence = new double[matrix.length];

		for (int i = 0; i < degreeMatrix.length; i++) {
			sequence[degreeMatrix[i][i]]++;
		}
		sum = 0;
		for (int i = 0; i < degreeMatrix.length; i++) {
			sequence[i] /= sequence.length;
			if (sequence[i] > 0) {
				sum += sequence[i] * (Math.log10(sequence[i]) / Math.log10(2));
			}
		}
		return -sum;
	}

	@Override
	public String name() {
		return TMetric.ENTROPY.toString();
	}

}
