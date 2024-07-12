/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: WattsStrogatzDensity.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	06/05/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo
 * @since 06/05/2013
 */
public class WattsStrogatzDensity extends GenerativeProcedure {
	private double p;

	private double d;

	private int k;

	private boolean flagDensity = false;

	public WattsStrogatzDensity(double p, double d) {
		this.p = p;
		this.k = 2;
		this.d = d;
	}

	public WattsStrogatzDensity(double p, int k, double d) {
		this.p = p;
		this.k = k;
		this.d = d;
	}

	public WattsStrogatzDensity(double p, int k, double d, boolean flagDensity) {
		this.p = p;
		this.k = k;
		this.d = d;
		this.flagDensity = flagDensity;
	}

	public static void main(String[] args) {
		int n = 28;
		double d = 0.0873;
		int m = (int) Math.ceil((((n - 1) * n) / 2) * d);
		System.out.println(m);
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
			if (Math.random() < p) {
				newMatrix[link[0]][link[1]] = 0;
				newMatrix[link[1]][link[0]] = 0;
				int attempts = 0;
				// sortear novo link
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
				while ((source == dest || newMatrix[source][dest] == 1 || newLinks.contains(source + ";" + dest)) && attempts < 2 * n) {
					source = RandomUtils.getInstance().nextInt(n - 1);
					dest = RandomUtils.getInstance().nextInt(n - 1);
					attempts++;
				}
				newMatrix[source][dest] = 1;
				newMatrix[dest][source] = 1;
				newLinks.add(source + ";" + dest);
				newLinks.add(dest + ";" + source);
			}
		}
		if (flagDensity) {
			while (numLinks < numLinksNewNodes) {
				// sortear novo link
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
				int attempts = 0;
				while ((source == dest || newMatrix[source][dest] == 1 || newLinks.contains(source + ";" + dest)) && attempts < 2 * n) {
					source = RandomUtils.getInstance().nextInt(n - 1);
					dest = RandomUtils.getInstance().nextInt(n - 1);
					attempts++;
				}
				newMatrix[source][dest] = 1;
				newMatrix[dest][source] = 1;
				newLinks.add(source + ";" + dest);
				newLinks.add(dest + ";" + source);
				numLinks++;
			}
			List<String> possibleIndex = new Vector<>();
			for (int i = 0; i < n; i++) {
				possibleIndex.add(i+"");
			}
			int degree1 = 0;
			int degree2 = 0;
			String key1 = null;
			String key2 = null;
			while (numLinks > numLinksNewNodes) {
				// sortear novo link
				source = RandomUtils.getInstance().nextInt(possibleIndex.size() - 1);
				dest = RandomUtils.getInstance().nextInt(possibleIndex.size() - 1);
				key1 = possibleIndex.get(source);
				key2 = possibleIndex.get(dest);
				degree1 = 0;
				degree2 = 0;
				for (int i = 0; i < n; i++) {
					if (newMatrix[Integer.parseInt(possibleIndex.get(source))][i] == 1) {
						degree1++;
					}
					if (newMatrix[Integer.parseInt(possibleIndex.get(dest))][i] == 1) {
						degree2++;
					}
				}
				int attempts = 0;
				while ((source == dest || newMatrix[Integer.parseInt(possibleIndex.get(source))][Integer.parseInt(possibleIndex.get(dest))] == 0 || degree1 < 2 || degree2 < 2) && attempts < 2 * n) {
					if (degree1 < 2) {
						possibleIndex.remove(key1);
					}
					if (degree2 < 2) {
						possibleIndex.remove(key2);
					}

					if (possibleIndex.isEmpty()){ 
						break;
					}

					source = RandomUtils.getInstance().nextInt(possibleIndex.size() - 1);
					dest = RandomUtils.getInstance().nextInt(possibleIndex.size() - 1);
					key1 = possibleIndex.get(source);
					key2 = possibleIndex.get(dest);
					
					degree1 = 0;
					degree2 = 0;
					for (int i = 0; i < n; i++) {
						if (newMatrix[Integer.parseInt(possibleIndex.get(source))][i] == 1) {
							degree1++;
						}
						if (newMatrix[Integer.parseInt(possibleIndex.get(dest))][i] == 1) {
							degree2++;
						}
					}
					attempts++;
				}

				if (possibleIndex.isEmpty()){ 
					break;
				}
				newMatrix[Integer.parseInt(possibleIndex.get(source))][Integer.parseInt(possibleIndex.get(dest))] = 0;
				newMatrix[Integer.parseInt(possibleIndex.get(dest))][Integer.parseInt(possibleIndex.get(source))] = 0;
				numLinks--;
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
	 * @param d O valor para setar em d
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
	 * @param k O valor para setar em k
	 */
	public void setK(int k) {
		this.k = k;
	}

	/**
	 * @return o valor do atributo p
	 */
	public double getP() {
		return p;
	}

	/**
	 * Altera o valor do atributo p
	 * @param p O valor para setar em p
	 */
	public void setP(double p) {
		this.p = p;
	}

}
