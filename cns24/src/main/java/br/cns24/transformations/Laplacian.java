package br.cns24.transformations;

import br.cns24.Transformation;

public class Laplacian implements Transformation<Integer> {
	private static final Laplacian instance = new Laplacian();
	
	private Laplacian(){
	}
	
	public static Laplacian getInstance(){
		return instance;
	}
	
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] degree = DegreeMatrix.getInstance().transform(matrix);
		Integer[][] laplacian = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				laplacian[i][j] = degree[i][j] - (matrix[i][j] == null ? 0 : matrix[i][j]);
			}
		}

		return laplacian;
	}

}
