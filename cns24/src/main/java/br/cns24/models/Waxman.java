/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Waxman.java
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
public class Waxman extends GeoGenerativeProcedure {

	private double alpha;

	private double beta;

	private double maxDistance;

	/**
	 * Construtor da classe.
	 * 
	 * @param geoCoords
	 */
	public Waxman(Geolocation[] geoCoords, double alpha, double beta) {
		super(geoCoords);
		this.alpha = alpha;
		this.beta = beta;
		maxDistance = 0;
		for (int i = 0; i < geoCoords.length; i++) {
			for (int j = i + 1; j < geoCoords.length; j++) {
				if (maxDistance < distances[i][j]) {
					maxDistance = distances[i][j];
				}
			}
		}
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
		double probability = 0;
		for (int i = 0; i < matrix.length; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				probability = beta / Math.exp(distances[i][j] / (maxDistance * alpha));
				if (Math.random() < probability) {
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

	/**
	 * @return o valor do atributo alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * Altera o valor do atributo alpha
	 * @param alpha O valor para setar em alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return o valor do atributo beta
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * Altera o valor do atributo beta
	 * @param beta O valor para setar em beta
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}
}
