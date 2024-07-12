package br.cns24.models;

public class CustomProbabilitiesModel extends GenerativeProcedure {
	private double[][] p;

	public CustomProbabilitiesModel(double[][] p) {
		this.p = p;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] newMatrix = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = i + 1; j < matrix.length; j++) {
				if (Math.random() < p[i][j]) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
				}
			}
		}
		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.CUSTOM_PROBABILITY.toString();
	}

}
