/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: kStar.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	03/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.cns24.models;

/**
 * 
 * @author Danilo
 * @since 03/10/2013
 */
public class kStar extends GenerativeProcedure {
	private int k;

	public kStar(int k) {
		this.k = k;
	}

	@Override
	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				newMatrix[i][j] = 0;
				newMatrix[j][i] = 0;
			}
		}

		for (int i = k; i < n; i++) {
			for (int j = 0; j < k; j++) {
				newMatrix[i][j] = 1;
				newMatrix[j][i] = 1;
			}
		}
		return newMatrix;
	}

	@Override
	public String name() {
		return TModel.K_STAR.toString();
	}

}
