/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: TrafficGuidedModel.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	08/11/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.HashSet;
import java.util.Set;

import br.cns24.Geolocation;
import br.cns24.util.RandomUtils;

/**
 * 
 * @author Danilo Araujo
 * @since 08/11/2014
 */
public class TrafficGuidedModel extends GenerativeProcedure {
	private Double[][] distances;

	private double d;

	private boolean flagDensity = false;

	private Gabriel gabriel;

	private Waxman waxman;

	private ErdosRenyiM random;

	private BarabasiDensity ba;

	private KRegular kRegular;

	private int model;
	
	private double rp;

	public TrafficGuidedModel(Geolocation[] locs, Double[][] distances, double d, double rp) {
		this(locs, distances, d, 0, rp);
	}

	public TrafficGuidedModel(Geolocation[] locs, Double[][] distances, double d, int model, double rp) {
		this.distances = distances;
		this.d = d;
		this.rp = rp;
		this.flagDensity = true;
		gabriel = new Gabriel(locs);
		waxman = new Waxman(locs, 0.5, 0.5);
		random = new ErdosRenyiM(d, locs.length);
		ba = new BarabasiDensity(d);
		kRegular = new KRegular(2);
		this.model = model;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = null;
		if (model == 0) {
			newMatrix = gabriel.transform(matrix);
		} else if (model == 1) {
			waxman.setBeta(Math.random() * 0.5 + 0.2);
			waxman.setAlpha(Math.random() * 0.7 + 0.1);
			newMatrix = waxman.transform(matrix);
		} else if (model == 2)  {
			random = new ErdosRenyiM(d, n);
			newMatrix = random.transform(matrix);
		} else if (model == 3) {
			ba.setDensity(d);
			newMatrix = ba.grow(matrix, n);
		} else if (model == 4) {
			kRegular = new KRegular((int)Math.max(1, Math.floor(0.5 * n * d)));
			newMatrix = kRegular.transform(matrix);
		} 
		Set<String> initialLinks = new HashSet<>();
		Set<String> newLinks = new HashSet<>();
		int source;
		int dest;
		int numLinksNewNodes = (int) Math.ceil((((n - 1) * n) / 2) * d);
		int numLinks = 0;

		for (int i = 0; i < n; i++) {
			for (int j = 1; j < n; j++) {
				if (newMatrix[i][j] == 1) {
					initialLinks.add(i + ";" + j);
					numLinks++;
				}
			}
		}

		// System.out.println("Criando atalhos");
		for (String strLink : initialLinks) {
			if (Math.random() < rp) {
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
		}
		while (flagDensity && numLinks < numLinksNewNodes) {
			// sortear novo link
			source = RandomUtils.getInstance().nextInt(n - 1);
			dest = RandomUtils.getInstance().nextInt(n - 1);
			while (Math.random() > distances[source][dest]
					|| (source == dest || newMatrix[source][dest] == 1 || newLinks.contains(source + ";" + dest))) {
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
			}
			newMatrix[source][dest] = 1;
			newMatrix[dest][source] = 1;
			newLinks.add(source + ";" + dest);
			newLinks.add(dest + ";" + source);
			numLinks++;
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
	 * @return o valor do atributo rp
	 */
	public double getRp() {
		return rp;
	}

	/**
	 * Altera o valor do atributo rp
	 * @param rp O valor para setar em rp
	 */
	public void setRp(double rp) {
		this.rp = rp;
	}
}
