package br.cns24.models;

import java.util.HashSet;
import java.util.Set;

import br.cns24.util.RandomUtils;

public class WattsStrogatz extends GenerativeProcedure {
	private double p;
	
	private int k;

	public WattsStrogatz(double p) {
		this.p = p;
		this.k = 2;
	}
	
	public WattsStrogatz(double p, int k) {
		this.p = p;
		this.k = k;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		Set<int[]> initialLinks = new HashSet<>();
		Set<int[]> newLinks = new HashSet<>();
		fill(newMatrix);
		int source;
		int dest;
		// o algoritmo original considera uma rede k-regular
		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= k; j++) {
				if (i + j > n - 1) {
					newMatrix[i][i + j - n] = 1;
					newMatrix[i + j - n][i] = 1;
					initialLinks.add(new int[] { i, i + j - n });
					initialLinks.add(new int[] { i + j - n, i });
				} else {
					newMatrix[i][i + j] = 1;
					newMatrix[i + j][i] = 1;
					initialLinks.add(new int[] { i, i + j });
					initialLinks.add(new int[] { i + j, i });
				}
			}
		}
		for (int[] link : initialLinks) {
			if (Math.random() < p) {
				newMatrix[link[0]][link[1]] = 0;
				newMatrix[link[1]][link[0]] = 0;

				// sortear novo link
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
				while (source == dest || newMatrix[source][dest] == 1
						|| newLinks.contains(new int[] { source, dest })) {
					source = RandomUtils.getInstance().nextInt(n - 1);
					dest = RandomUtils.getInstance().nextInt(n - 1);
				}
				newMatrix[source][dest] = 1;
				newMatrix[dest][source] = 1;
				newLinks.add(new int[] { source, dest });
				newLinks.add(new int[] { dest, source });
			}
		}
		return newMatrix;
	}

	public void fill(Integer[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				m[i][j] = 0;
			}
		}
	}


	@Override
	public String name() {
		return TModel.WATTS_STROGATZ.toString();
	}

}
