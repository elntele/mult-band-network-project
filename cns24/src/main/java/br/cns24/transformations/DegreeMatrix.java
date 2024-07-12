package br.cns24.transformations;

import br.cns24.Transformation;

public class DegreeMatrix implements Transformation<Integer> {
	private static final DegreeMatrix instance = new DegreeMatrix();
	
	private DegreeMatrix(){
	}
	
	public static DegreeMatrix getInstance(){
		return instance;
	}
	
	@Override
	public Integer[][] transform(final Integer[][] matrix) {
		Integer[][] m = new Integer[matrix.length][matrix.length];
		int degree = 0;
		for (int i = 0; i < matrix.length; i++) {
			degree = 0;
			for (int j = 0; j < matrix[i].length; j++) {
				if (i != j) {
					m[i][j] = 0;
				}
				degree += matrix[i][j] == null ? 0 : matrix[i][j];
			}
			m[i][i] = degree;
		}

		return m;
	}

}
