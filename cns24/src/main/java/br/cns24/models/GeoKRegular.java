/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeoKRegular.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	20/11/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.cns24.Geolocation;

/**
 * 
 * @author Danilo Araujo
 * @since 20/11/2014
 */
public class GeoKRegular extends GeoGenerativeProcedure {
	private int k = 2;

	private double d;
	
	private Geolocation refPoint;

	/**
	 * Construtor da classe.
	 * 
	 * @param geoCoords
	 */
	public GeoKRegular(Geolocation[] geoCoords, int k, double d) {
		super(geoCoords);
		this.k = k;
		this.d = d;
		double maxLong, maxLat, minLong, minLat;
		maxLong = Double.NEGATIVE_INFINITY;
		maxLat = Double.NEGATIVE_INFINITY;
		minLong = Double.MAX_VALUE;
		minLat = Double.MAX_VALUE;
		for (Geolocation g : geoCoords) {
			if (maxLat < g.getLatitude()) {
				maxLat = g.getLatitude();
			}
			if (maxLong < g.getLongitude()) {
				maxLong = g.getLongitude();
			}
			if (minLat > g.getLatitude()) {
				minLat = g.getLatitude();
			}
			if (minLong > g.getLongitude()) {
				minLong = g.getLongitude();
			}
		}
		refPoint = new Geolocation((maxLat + minLat)/2, (maxLong + minLong)/2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.cns.models.GeoGenerativeProcedure#transform(java.lang.Integer[][])
	 */
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int numNodes = matrix.length;
		Integer[][] newMatrix = new Integer[numNodes][numNodes];
		int numTotalLinks = 0;
		int maxLinks = (int) Math.round(0.5 * d * numNodes * (numNodes - 1));
		double[] distRef = new double[numNodes];
		double[] orderedDistances = new double[numNodes];

		// criar array com distancias ordenadas
		double[][] oDistance = new double[distances.length][distances.length];
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			distRef[i] = computeDistance(geoCoords[i], refPoint, true);
			for (int j = 0; j < matrix.length; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
				oDistance[i][j] = distances[i][j];
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			orderedDistances[i] = distRef[i];
			Arrays.sort(oDistance[i]);
		}
		Arrays.sort(orderedDistances);
		
		Map<Integer, Integer> mapDistRef = new HashMap<Integer, Integer>();

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (orderedDistances[i] == distRef[j]) {
					mapDistRef.put(i,j);
				}
			}
		}
		
		while (numTotalLinks < maxLinks) {
			lblExt: for (int o = 0; o < matrix.length; o++) {
				int i = mapDistRef.get(o);
				for (int j = 1; j < matrix.length; j++) {
					for (int l = 0; l < matrix.length; l++) {
						if (oDistance[i][j] == distances[i][l] && newMatrix[i][l] != 1) {
							newMatrix[i][l] = 1;
							newMatrix[l][i] = 1;
							numTotalLinks++;
							if (numTotalLinks >= maxLinks) {
								break lblExt;
							}
							continue lblExt;
						}
					}
				}// ativa��o depender da carga
			}
		}
		
		return newMatrix;
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
}
