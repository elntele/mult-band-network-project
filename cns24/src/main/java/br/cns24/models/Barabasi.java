package br.cns24.models;

import br.cns24.transformations.DegreeMatrix;

public class Barabasi extends GenerativeProcedure {
	private int numLinksNewNodes;

	public Barabasi(int numLinksNewNodes) {
		this.numLinksNewNodes = numLinksNewNodes;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] nMatrix = new Integer[n + 1][n + 1];
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		double r;
		int numOldLinks = 0;
		for (int i = 0; i < n; i++){
			nMatrix[n][i] = 0;
			nMatrix[i][n] = 0;
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				nMatrix[i][j] = matrix[i][j];
				nMatrix[j][i] = matrix[j][i];
				if (nMatrix[i][j] == 1){
					numOldLinks++;
				} 
			}
		}
		numLinksNewNodes -= numOldLinks;
		int numLinks = Math.min(numLinksNewNodes, n) < 1 ? 1 : Math.min(numLinksNewNodes, n);
		double count;
		for (int i = 0; i < numLinks; i++) {
			r = Math.random();
			count = 0;
			for (int j = 0; j < n - 1; j++) {
				count += degree[i][i] / (2.0 * n);
				if (r < count && nMatrix[n][j] != 1) {
					nMatrix[n][j] = 1;
					nMatrix[j][n] = 1;
					break;
				}
			}
		}

		return nMatrix;
	}

	@Override
	public Integer[][] grow(Integer[][] matrix, int time) {
		Integer[][] nMatrix = new Integer[][] { { 0, 1, 1 }, { 1, 0, 1 }, { 1, 1, 0 } };
		for (int i = 0; i < time - 3; i++) {
			nMatrix = transform(nMatrix);
		}
		return nMatrix;
	}

	@Override
	public String name() {
		return TModel.BARABASI.toString();
	}

}
