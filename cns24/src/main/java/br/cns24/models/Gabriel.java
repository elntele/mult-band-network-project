/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Gabriel.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	05/11/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

import br.cns24.Geolocation;

/**
 * 
 * @author Danilo Araujo
 * @since 05/11/2014
 */
public class Gabriel extends GeoGenerativeProcedure {
	/**
	 * Construtor da classe.
	 * 
	 * @param geoCoords
	 */
	public Gabriel(Geolocation[] geoCoords) {
		super(geoCoords);
	}

	/**
	 * Construtor da classe.
	 * 
	 * @param geoCoords
	 */
	public Gabriel(Geolocation[] geoCoords, boolean geolocated) {
		super(geoCoords, geolocated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.cns.models.GeoGenerativeProcedure#transform(java.lang.Integer[][])
	 */
	@Override
	public Integer[][] transform(Integer[][] matrix) {
		Integer[][] newMatrix = new Integer[matrix.length][matrix.length];
		double distance = 0;
		double distanceI = 0;
		double distanceJ = 0;
		double epsilon = 1e-6;
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				distance = distances[i][j];
				boolean key = false;
				for (int k = 0; k < matrix.length; k++) {
					if (k != j && k != i) {
						distanceI = distances[i][k];
						distanceJ = distances[j][k];
						// System.out.printf("i = %s; j = %d; k = %d; diametro = %.2f; di = %.2f; dj = %.2f\n",
						// i, j, k, distance, distanceI, distanceJ);
						if (distanceI < distance - epsilon && distanceJ < distance - epsilon) {
							key = true;
							break;
						}
					}
				}
				if (!key) {
					newMatrix[i][j] = 1;
					newMatrix[j][i] = 1;
				} else {
					newMatrix[i][j] = 0;
					newMatrix[j][i] = 0;
				}
			}
		}
		return newMatrix;
	}
}
