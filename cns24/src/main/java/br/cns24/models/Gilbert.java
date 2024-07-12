package br.cns24.models;

import br.cns24.util.RandomUtils;

public class Gilbert extends GenerativeProcedure {
	private double p;

	public Gilbert(double p) {
		this.p = p;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		int m = (n * (n - 1)) / 2;
		for (int i = 0; i < m; i++) {
			int node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			int node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			while (node2 == node1 || newMatrix[node1][node2] == 1) {
				node1 = RandomUtils.getInstance().nextInt(matrix.length - 1);
				node2 = RandomUtils.getInstance().nextInt(matrix.length - 1);
			}
			if (Math.random() < p) {
				newMatrix[node1][node2] = 1;
				newMatrix[node2][node1] = 1;
			}
		}
		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.GILBERT.toString();
	}

}
