package br.cns24.transformations;

import br.cns24.Transformation;

public class FloydWarshallShortestPath implements Transformation<Integer> {
	private static final FloydWarshallShortestPath instance = new FloydWarshallShortestPath();
	
	private FloydWarshallShortestPath(){
	}
	
	public static FloydWarshallShortestPath getInstance(){
		return instance;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] path = new Integer[matrix.length][matrix.length];

		for (int i = 0; i < n; i++) {
			path[i][i] = 0;
			for (int j = i + 1; j < n; j++) {
				if (matrix[j][i] == 0) {
					path[i][j] = Integer.MAX_VALUE;
					path[j][i] = Integer.MAX_VALUE;
				} else {
					path[i][j] = matrix[j][i];
					path[j][i] = matrix[j][i];
				}
			}
		}

		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (path[i][k] == Integer.MAX_VALUE
							|| path[k][j] == Integer.MAX_VALUE) {
						path[i][j] = path[i][j];
					} else {
						path[i][j] = Math.min(path[i][j], path[i][k]
								+ path[k][j]);
					}

				}
			}
		}

		return path;
	}

	public static void main(String[] args) {
		FloydWarshallShortestPath a = new FloydWarshallShortestPath();
		Integer[][] m = new Integer[][] { { 0, 1, 0, 1 }, { 1, 0, 1, 0 },
				{ 0, 1, 0, 0 }, { 1, 0, 0, 0 } };

		Integer[][] d = a.transform(m);

		for (int k = 0; k < d.length; k++) {
			for (int i = 0; i < d.length; i++) {
				System.out.print(d[k][i] + " ");
			}
			System.out.println();
		}
	}

}
