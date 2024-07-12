/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NewmanWattsDensity.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	09/09/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.HashSet;
import java.util.Set;

import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo
 * @since 09/09/2013
 */
public class NewmanWattsDensity extends GenerativeProcedure {
	private double p;
	
	private int k;

	private double d;

	public NewmanWattsDensity(double p) {
		this.p = p;
		k = 2;
	}
	
	public NewmanWattsDensity(double p, int k, double d) {
		this.p = p;
		this.k = k;
		this.d = d;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		Set<int[]> initialLinks = new HashSet<>();
		Set<int[]> newLinks = new HashSet<>();
		int source;
		int dest;
		int numLinksNewNodes = (int) Math.ceil((((n - 1) * n) / 2) * d);
		int numLinks = 0;
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				newMatrix[i][j] = 0;
			}
		}

		// o algoritmo original considera uma rede 2-regular
		lblExt: for (int i = 0; i < n; i++) {
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
				numLinks++;
				if (numLinks >= numLinksNewNodes) {
					break lblExt;
				}
			}
		}
		for (int[] link : initialLinks) {
			if (Math.random() < p) {
				if (Math.random() < 0.5) {
					source = link[0];
				} else {
					source = link[1];
				}

				// sortear novo link
				dest = RandomUtils.getInstance().nextInt(n - 1);
				while (source == dest || newMatrix[source][dest] == 1
						|| newLinks.contains(new int[] { source, dest })) {
					if (Math.random() < 0.5) {
						source = link[0];
					} else {
						source = link[1];
					}
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

	@Override
	public String name() {
		return TModel.NEWMAN_WATTS.toString();
	}

}
