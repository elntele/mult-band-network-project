package br.cns24.models;

public class KRegular extends GenerativeProcedure {
	private int k;

	public KRegular(int k) {
		this.k = k;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		int mk = k;
		if (mk > n - 1) {
			mk = n - 1;
		}
		Integer[][] newMatrix = new Integer[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
			}
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= mk; j++) {
				if (i + j > n - 1) {
					newMatrix[i][i + j - n] = 1;
					newMatrix[i + j - n][i] = 1;
				} else {
					newMatrix[i][i + j] = 1;
					newMatrix[i + j][i] = 1;
				}
			}
		}
		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.K_REGULAR.toString();
	}

}
