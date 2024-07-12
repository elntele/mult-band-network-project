/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SWDistance.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	01/01/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.HashSet;
import java.util.Set;

import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo
 * @since 01/01/2014
 */
public class SWDistance extends GenerativeProcedure {
	private Double[][] distances;

	private double d;

	private int k;

	private boolean flagDensity = false;

	public SWDistance(Double[][] distances, int k, double d) {
		this.distances = distances;
		this.k = k;
		this.d = d;
		this.flagDensity = true;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		Set<String> initialLinks = new HashSet<>();
		Set<String> newLinks = new HashSet<>();
		int source;
		int dest;
		fill(newMatrix);
		int numLinksNewNodes = (int) Math.ceil((((n - 1) * n) / 2) * d);
		int numLinks = 0;
		// o algoritmo original considera uma rede k-regular
		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= k; j++) {
				if (i + j > n - 1) {
					newMatrix[i][i + j - n] = 1;
					newMatrix[i + j - n][i] = 1;
					initialLinks.add(i + ";" + (i + j - n));
				} else {
					newMatrix[i][i + j] = 1;
					newMatrix[i + j][i] = 1;
					initialLinks.add(i + ";" + (i + j));
				}
				numLinks++;
				if (numLinks >= numLinksNewNodes) {
					break;
				}
			}
		}

		// System.out.println("Criando atalhos");
		for (String strLink : initialLinks) {
			String[] strLinkArray = strLink.split(";");
			int[] link = new int[] { Integer.parseInt(strLinkArray[0]), Integer.parseInt(strLinkArray[1]) };

			int c1 = 0;
			int c2 = 0;
			for (int j = 0; j < matrix.length; j++) {
				if (newMatrix[link[0]][j] == 1) {
					c1++;
				}
				if (newMatrix[link[1]][j] == 1) {
					c2++;
				}
			}
			if (newMatrix[link[0]][link[1]] == 1 && (c1 > 1 && c2 > 1) && Math.random() > distances[link[0]][link[1]]) {
				newMatrix[link[0]][link[1]] = 0;
				newMatrix[link[1]][link[0]] = 0;

				// sortear novo link
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
				while (Math.random() > distances[source][dest] || (source == link[0] && dest == link[1])
						|| (source == link[1] && dest == link[0]) || source == dest || newMatrix[source][dest] == 1
						|| newLinks.contains(source + ";" + dest)) {
					source = RandomUtils.getInstance().nextInt(n - 1);
					dest = RandomUtils.getInstance().nextInt(n - 1);
				}
				newMatrix[source][dest] = 1;
				newMatrix[dest][source] = 1;
				newLinks.add(source + ";" + dest);
				newLinks.add(dest + ";" + source);
			}
		}
		int maxAttempts = n * (n-1)/2;
		int attempts = 0;
		lblExt: while (flagDensity && numLinks < numLinksNewNodes && attempts < maxAttempts) {
			// sortear novo link
			source = RandomUtils.getInstance().nextInt(n - 1);
			dest = RandomUtils.getInstance().nextInt(n - 1);
			while (Math.random() > distances[source][dest]
					|| (source == dest || newMatrix[source][dest] == 1 || newLinks.contains(source + ";" + dest))) {
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
				attempts++;
				if (attempts == maxAttempts) {
					break lblExt;
				}
			}
			newMatrix[source][dest] = 1;
			newMatrix[dest][source] = 1;
			newLinks.add(source + ";" + dest);
			newLinks.add(dest + ";" + source);
			numLinks++;
		}
		return newMatrix;

	}

	public Integer[][] transform1(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		Set<String> initialLinks = new HashSet<>();
		fill(newMatrix);
		int numLinksNewNodes = (int) Math.ceil((((n - 1) * n) / 2) * d);
		int numLinks = 0;

		// o algoritmo original considera uma rede k-regular
		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= k; j++) {
				if (i + j > n - 1) {
					newMatrix[i][i + j - n] = 1;
					newMatrix[i + j - n][i] = 1;
					initialLinks.add(i + ";" + (i + j - n));
				} else {
					newMatrix[i][i + j] = 1;
					newMatrix[i + j][i] = 1;
					initialLinks.add(i + ";" + (i + j));
				}
				numLinks++;
			}
		}
		// System.out.println("Criando atalhos");
		while (numLinks > numLinksNewNodes) {
			for (String strLink : initialLinks) {
				String[] strLinkArray = strLink.split(";");
				int[] link = new int[] { Integer.parseInt(strLinkArray[0]), Integer.parseInt(strLinkArray[1]) };
				if (newMatrix[link[0]][link[1]] == 1 && Math.random() > distances[link[0]][link[1]]) {
					int c1 = 0;
					int c2 = 0;
					for (int j = 0; j < matrix.length; j++) {
						if (newMatrix[link[0]][j] == 1) {
							c1++;
						}
						if (newMatrix[link[1]][j] == 1) {
							c2++;
						}
					}
					if (c1 > 1 && c2 > 1) {
						newMatrix[link[0]][link[1]] = 0;
						newMatrix[link[1]][link[0]] = 0;
						// System.out.println("Removeu " + link[0] + ", " +
						// link[1]);
						numLinks--;
						if (numLinks <= numLinksNewNodes) {
							break;
						}
					}
				}
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
		return TModel.WATTS_STROGATZ_DENSITY.toString();
	}

	/**
	 * @return o valor do atributo d
	 */
	public double getD() {
		return d;
	}

	/**
	 * Altera o valor do atributo d
	 * 
	 * @param d
	 *            O valor para setar em d
	 */
	public void setD(double d) {
		this.d = d;
	}

	/**
	 * @return o valor do atributo k
	 */
	public int getK() {
		return k;
	}

	/**
	 * Altera o valor do atributo k
	 * 
	 * @param k
	 *            O valor para setar em k
	 */
	public void setK(int k) {
		this.k = k;
	}

	public static void main(String[] args) {
		Double[][] traffic = new Double[][] {
				{ 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 0.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 5.000000, 5.000000, 5.000000, 5.000000, 5.000000, 5.000000, 0.000000, 5.000000, 5.000000, 5.000000,
						5.000000, 5.000000, 5.000000, 5.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 0.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 0.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 0.000000,
						1.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						0.000000, 1.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 0.000000, 1.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 0.000000, 1.000000 },
				{ 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 1.000000, 5.000000, 1.000000, 1.000000, 1.000000,
						1.000000, 1.000000, 1.000000, 0.000000 }, };

		double maxDistance = 0;
		for (int i = 0; i < traffic.length; i++) {
			for (int j = i + 1; j < traffic.length; j++) {
				if (maxDistance < traffic[i][j]) {
					maxDistance = traffic[i][j];
				}
			}
		}
		for (int i = 0; i < traffic.length; i++) {
			traffic[i][i] = 0.0;
			for (int j = i + 1; j < traffic.length; j++) {
				// traffic[i][j] = traffic[i][j]/maxDistance;
				traffic[i][j] = Math.random();
				traffic[j][i] = traffic[i][j];
			}
		}

		SWDistance pl = new SWDistance(traffic, 3, 0.3);
		Integer[][] am = pl.transform(new Integer[14][14]);

		for (int i = 0; i < am.length; i++) {
			for (int j = 0; j < am.length; j++) {
				System.out.print(am[i][j] + " ");
			}
			System.out.println("");
		}
	}

}
